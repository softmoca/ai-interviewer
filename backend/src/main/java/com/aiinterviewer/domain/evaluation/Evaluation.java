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
}
