package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.category.CategoryRepository;
import com.aiinterviewer.domain.question.Question;
import com.aiinterviewer.domain.question.QuestionRepository;
import com.aiinterviewer.domain.session.InterviewSession;
import com.aiinterviewer.domain.session.InterviewSessionRepository;
import com.aiinterviewer.domain.session.QaLog;
import com.aiinterviewer.domain.session.QaLogRepository;
import com.aiinterviewer.domain.user.User;
import com.aiinterviewer.domain.user.UserRepository;
import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.dto.FollowUpResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 면접 세션 유스케이스(애플리케이션 계층). 판단(설정 검증·상태 전이·문답 일관성)은 도메인이
 * 소유하고, 여기서는 조회·트랜잭션·흐름 조립만 담당한다(SRP, 도메인 우선).
 *
 * <p>답변 제출 시 답변을 기록하고 {@link LlmClient}로 꼬리질문 1~2개를 생성해 저장한다(패턴 B).
 * LLM 호출과 저장은 한 트랜잭션이므로, LLM 미설정/실패 시 답변 기록도 함께 롤백되고 명확한
 * 오류가 반환된다(결정사항 D26). 소유권 검증은 인가 관심사라 애플리케이션에서 처리한다(§1.3).
 */
@Service
public class SessionService {

    private static final int QUESTION_POOL_SIZE = 8;

    private final InterviewSessionRepository sessionRepository;
    private final QaLogRepository qaLogRepository;
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LlmClient llmClient;
    private final FollowUpPromptFactory promptFactory;
    private final SessionAccessGuard sessionAccessGuard;

    public SessionService(InterviewSessionRepository sessionRepository, QaLogRepository qaLogRepository,
                          QuestionRepository questionRepository, CategoryRepository categoryRepository,
                          UserRepository userRepository, LlmClient llmClient,
                          FollowUpPromptFactory promptFactory, SessionAccessGuard sessionAccessGuard) {
        this.sessionRepository = sessionRepository;
        this.qaLogRepository = qaLogRepository;
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.llmClient = llmClient;
        this.promptFactory = promptFactory;
        this.sessionAccessGuard = sessionAccessGuard;
    }

    /** 세션을 시작하고 첫 질문(오프닝)을 서빙한다. */
    @Transactional
    public StartSessionResult startSession(Long userId, StartSessionCommand command) {
        User host = userRepository.findById(userId).orElseThrow(SessionAccessDeniedException::new);
        Set<Long> categoryIds = command.randomAll() ? Set.of() : resolveCategoryIds(command.categorySlugs());

        // 설정 검증은 도메인(start)이 먼저 수행 → 잘못된 설정은 첫 질문 조회 전에 400으로 귀결
        InterviewSession session = InterviewSession.start(host, categoryIds, command.randomAll(),
                command.questionCount(), command.difficulty(), LocalDateTime.now());
        Question first = pickFirstQuestion(categoryIds, command);

        sessionRepository.save(session);
        QaLog opening = qaLogRepository.save(QaLog.opening(session, first, 1));
        return new StartSessionResult(session.getId(), session.getStatus(),
                new StartSessionResult.QuestionView(first.getId(), first.getContent(),
                        first.getDifficulty(), opening.getSeq()));
    }

    /** 사용자 답변을 기록하고 꼬리질문 1~2개를 생성해 저장한다(진행 중 세션만, 소유자만). */
    @Transactional
    public AnswerResult submitAnswer(Long userId, Long sessionId, String content) {
        InterviewSession session = sessionAccessGuard.getOwned(userId, sessionId);
        if (!session.isInProgress()) {
            throw new SessionNotInProgressException(sessionId);
        }
        int answerSeq = (int) qaLogRepository.countBySessionId(sessionId) + 1;
        QaLog answer = qaLogRepository.save(QaLog.userAnswer(session, content, answerSeq));

        List<QaLog> transcript = qaLogRepository.findBySessionIdOrderBySeqAsc(sessionId);
        List<Question> pool = questionPool(session, transcript);
        FollowUpResult followUp = llmClient.generateFollowUp(promptFactory.build(pool, transcript));

        List<AnswerResult.FollowUpView> views = new ArrayList<>();
        int seq = answerSeq;
        for (String question : followUp.followUpQuestions()) {
            QaLog saved = qaLogRepository.save(QaLog.followUp(session, question, ++seq));
            views.add(new AnswerResult.FollowUpView(saved.getId(), saved.getSeq(), saved.getContent()));
        }
        return new AnswerResult(answer.getId(), answerSeq, views, session.getStatus());
    }

    /** 세션을 정상 종료한다(소유자만). 진행 중이 아니면 도메인이 거부한다. */
    @Transactional
    public SessionStatusResult completeSession(Long userId, Long sessionId) {
        InterviewSession session = sessionAccessGuard.getOwned(userId, sessionId);
        session.complete(LocalDateTime.now());
        return new SessionStatusResult(session.getId(), session.getStatus(), session.getEndedAt());
    }

    /** 세션 상세(설정·상태 + 대화 이력)를 조회한다(소유자만). */
    @Transactional(readOnly = true)
    public SessionDetailResult getSession(Long userId, Long sessionId) {
        InterviewSession session = sessionAccessGuard.getOwned(userId, sessionId);
        List<SessionDetailResult.QaLogEntry> transcript =
                qaLogRepository.findBySessionIdOrderBySeqAsc(sessionId).stream()
                        .map(l -> new SessionDetailResult.QaLogEntry(l.getSeq(), l.getRole(),
                                l.getContent(), l.isFollowUp()))
                        .toList();
        return new SessionDetailResult(session.getId(), session.getStatus(), session.getStartedAt(),
                session.getEndedAt(), transcript);
    }

    private Set<Long> resolveCategoryIds(List<String> slugs) {
        if (slugs == null) {
            return Set.of();
        }
        return slugs.stream()
                .map(slug -> categoryRepository.findBySlug(slug)
                        .orElseThrow(() -> new CategoryNotFoundException(slug))
                        .getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Question pickFirstQuestion(Set<Long> categoryIds, StartSessionCommand command) {
        List<Question> openings = command.randomAll()
                ? questionRepository.findByOpeningTrue()
                : questionRepository.findByCategoryIdInAndOpeningTrue(categoryIds);
        if (command.difficulty() != null) {
            openings = openings.stream()
                    .filter(q -> q.getDifficulty() == command.difficulty())
                    .toList();
        }
        if (openings.isEmpty()) {
            throw new NoAvailableQuestionException();
        }
        return openings.get(ThreadLocalRandom.current().nextInt(openings.size()));
    }

    /** 꼬리질문 프롬프트에 주입할 참고 질문 풀. 세션 카테고리(없으면 첫 질문의 카테고리) 기준. */
    private List<Question> questionPool(InterviewSession session, List<QaLog> transcript) {
        Set<Long> categoryIds = session.getCategoryIds().isEmpty()
                ? openingCategoryIds(transcript)
                : session.getCategoryIds();
        if (categoryIds.isEmpty()) {
            return List.of();
        }
        return questionRepository.findByCategoryIdIn(categoryIds).stream()
                .limit(QUESTION_POOL_SIZE)
                .toList();
    }

    private Set<Long> openingCategoryIds(List<QaLog> transcript) {
        return transcript.stream()
                .map(QaLog::getQuestion)
                .filter(q -> q != null)
                .findFirst()
                .map(q -> Set.of(q.getCategory().getId()))
                .orElseGet(Set::of);
    }
}
