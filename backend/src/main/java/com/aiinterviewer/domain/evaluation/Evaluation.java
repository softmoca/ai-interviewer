package com.aiinterviewer.domain.evaluation;

import com.aiinterviewer.domain.session.InterviewSession;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세션 평가 리포트의 '개념 단위' 평가 한 건. 점수는 5점 척도로 단순화(결정사항 D10).
 * 세션 종료 시 LLM이 JSON으로 반환한 결과를 검증 후 저장한다(docs/프롬프트-설계.md §4).
 */
@Getter
@Entity
@Table(name = "evaluation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Evaluation {

    /** 평가 점수는 5점 척도(결정사항 D10). 도메인이 범위를 강제한다. */
    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    /** 개념/주제 단위 (예: 프로세스와 스레드) */
    @Column(nullable = false)
    private String concept;

    /** 정확성 1~5 */
    @Column(nullable = false)
    private int accuracyScore;

    /** 깊이 1~5 */
    @Column(nullable = false)
    private int depthScore;

    /** 답변에서 언급했으면 좋았을 핵심 키워드 */
    @ElementCollection
    @CollectionTable(name = "evaluation_missed_keyword",
            joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "keyword")
    private List<String> missedKeywords = new ArrayList<>();

    /** 해당 개념의 모범답안 */
    @Column(columnDefinition = "TEXT")
    private String modelAnswer;

    /** 세션 전반 총평 (세션당 한 건에만 채워질 수 있음 — docs/아키텍처.md evaluation) */
    @Column(columnDefinition = "TEXT")
    private String overallComment;

    private Evaluation(InterviewSession session, String concept, int accuracyScore, int depthScore,
                       List<String> missedKeywords, String modelAnswer, String overallComment) {
        this.session = session;
        this.concept = concept;
        this.accuracyScore = requireInScoreRange(accuracyScore, "accuracy");
        this.depthScore = requireInScoreRange(depthScore, "depth");
        this.missedKeywords = missedKeywords == null ? new ArrayList<>() : new ArrayList<>(missedKeywords);
        this.modelAnswer = modelAnswer;
        this.overallComment = overallComment;
    }

    /**
     * 검증된 평가 한 건을 생성한다. 점수는 1~5 범위를 강제하므로, 검증되지 않은 LLM 점수가
     * 그대로 저장되는 것을 도메인 경계에서 막는다(규칙 5, AP-9 방지).
     */
    public static Evaluation of(InterviewSession session, String concept, int accuracyScore,
                                int depthScore, List<String> missedKeywords, String modelAnswer,
                                String overallComment) {
        return new Evaluation(session, concept, accuracyScore, depthScore, missedKeywords,
                modelAnswer, overallComment);
    }

    private static int requireInScoreRange(int score, String field) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException(
                    "%s 점수는 %d~%d 범위여야 합니다: %d".formatted(field, MIN_SCORE, MAX_SCORE, score));
        }
        return score;
    }
}
