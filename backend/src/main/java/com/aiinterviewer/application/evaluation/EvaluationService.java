package com.aiinterviewer.application.evaluation;

import com.aiinterviewer.application.session.SessionAccessGuard;
import com.aiinterviewer.domain.evaluation.Evaluation;
import com.aiinterviewer.domain.evaluation.EvaluationRepository;
import com.aiinterviewer.domain.session.InterviewSession;
import com.aiinterviewer.domain.session.QaLog;
import com.aiinterviewer.domain.session.QaLogRepository;
import com.aiinterviewer.llm.LlmCallException;
import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.dto.EvaluationResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 세션 평가 유스케이스(애플리케이션 계층). 완료된 세션의 대화 이력을 LLM에 넘겨 개념별 점수·
 * 모범답안·총평을 받아 저장하고 리포트로 반환한다(docs/프롬프트-설계.md §4, 결정사항 D28).
 *
 * <p>점수 1~5 검증은 도메인({@link Evaluation})이 소유한다. 소유권 검증은 공용
 * {@link SessionAccessGuard}로 위임한다. 이미 평가된 세션은 재호출 없이 기존 리포트를 반환한다.
 */
@Service
public class EvaluationService {

    private final SessionAccessGuard sessionAccessGuard;
    private final QaLogRepository qaLogRepository;
    private final EvaluationRepository evaluationRepository;
    private final LlmClient llmClient;
    private final EvaluationPromptFactory promptFactory;

    public EvaluationService(SessionAccessGuard sessionAccessGuard, QaLogRepository qaLogRepository,
                             EvaluationRepository evaluationRepository, LlmClient llmClient,
                             EvaluationPromptFactory promptFactory) {
        this.sessionAccessGuard = sessionAccessGuard;
        this.qaLogRepository = qaLogRepository;
        this.evaluationRepository = evaluationRepository;
        this.llmClient = llmClient;
        this.promptFactory = promptFactory;
    }

    /**
     * 완료된 세션을 평가한다. 이미 평가되어 있으면 기존 리포트를 그대로 반환한다(멱등).
     *
     * <p>세션 행에 쓰기 잠금을 걸어 <b>동시에 들어온 두 요청</b>이 각각 LLM을 호출해 평가를
     * 중복 생성하는 것을 막는다(결정사항 D35). 뒤늦은 요청은 앞 요청이 커밋될 때까지 대기했다가
     * 이미 존재하는 리포트를 반환한다. 잠금은 이 트랜잭션이 끝날 때까지 유지된다.
     */
    @Transactional
    public EvaluationReportResult evaluate(Long userId, Long sessionId) {
        InterviewSession session = sessionAccessGuard.getOwnedForUpdate(userId, sessionId);
        if (!session.isCompleted()) {
            throw new SessionNotCompletedException(sessionId);
        }
        List<Evaluation> existing = evaluationRepository.findBySessionId(sessionId);
        if (!existing.isEmpty()) {
            return toReport(sessionId, existing);
        }

        List<QaLog> transcript = qaLogRepository.findBySessionIdOrderBySeqAsc(sessionId);
        EvaluationResult result = llmClient.evaluate(promptFactory.build(transcript));
        if (result.evaluations() == null || result.evaluations().isEmpty()) {
            throw new LlmCallException("평가 결과가 비어 있습니다.");
        }

        List<Evaluation> saved = new ArrayList<>();
        boolean first = true;
        for (EvaluationResult.ConceptEvaluation c : result.evaluations()) {
            // 총평은 세션당 한 건에만 저장(첫 행) — docs/아키텍처.md evaluation 규약
            String overall = first ? result.overallComment() : null;
            saved.add(evaluationRepository.save(Evaluation.of(session, c.concept(), c.accuracy(),
                    c.depth(), c.missedKeywords(), c.modelAnswer(), overall)));
            first = false;
        }
        return toReport(sessionId, saved);
    }

    /** 저장된 평가 리포트를 조회한다(소유자만). 아직 평가 전이면 404. */
    @Transactional(readOnly = true)
    public EvaluationReportResult getReport(Long userId, Long sessionId) {
        sessionAccessGuard.getOwned(userId, sessionId);
        List<Evaluation> rows = evaluationRepository.findBySessionId(sessionId);
        if (rows.isEmpty()) {
            throw new EvaluationNotFoundException(sessionId);
        }
        return toReport(sessionId, rows);
    }

    private EvaluationReportResult toReport(Long sessionId, List<Evaluation> rows) {
        // 트랜잭션 안에서 missedKeywords(@ElementCollection, lazy)를 복사해 materialize.
        // (open-in-view=false이므로 응답 직렬화 시점에는 세션이 닫혀 lazy 로딩 불가)
        List<EvaluationReportResult.ConceptScore> concepts = rows.stream()
                .map(e -> new EvaluationReportResult.ConceptScore(e.getConcept(), e.getAccuracyScore(),
                        e.getDepthScore(), new ArrayList<>(e.getMissedKeywords()), e.getModelAnswer()))
                .toList();
        String overall = rows.stream()
                .map(Evaluation::getOverallComment)
                .filter(o -> o != null)
                .findFirst()
                .orElse(null);
        return new EvaluationReportResult(sessionId, concepts, overall);
    }
}
