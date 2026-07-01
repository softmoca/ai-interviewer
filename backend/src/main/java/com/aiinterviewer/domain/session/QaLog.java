package com.aiinterviewer.domain.session;

import com.aiinterviewer.domain.question.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 세션 내 문답 로그 한 줄(질문 또는 답변).
 * 꼬리질문은 LLM 생성이라 {@code question}이 null일 수 있다(docs/아키텍처.md qa_log).
 */
@Getter
@Entity
@Table(name = "qa_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    /** 출처 질문(DB 오프닝/참고 질문). 꼬리질문·사용자 답변은 null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QaRole role;

    /** 질문 또는 답변 텍스트 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 꼬리질문 여부 */
    @Column(nullable = false)
    private boolean followUp;

    /** 세션 내 순서 */
    @Column(nullable = false)
    private int seq;
}
