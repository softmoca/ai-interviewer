package com.aiinterviewer.domain.session;

import com.aiinterviewer.common.DomainGuard;
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

    private QaLog(InterviewSession session, Question question, QaRole role, String content,
                  boolean followUp, int seq) {
        this.session = DomainGuard.requireNotNull(session, "session");
        this.question = question;
        this.role = role;
        this.content = DomainGuard.requireNotBlank(content, "content");
        this.followUp = followUp;
        this.seq = DomainGuard.requireInRange(seq, 1, Integer.MAX_VALUE, "seq");
    }

    /** DB에서 꺼낸 오프닝(첫 질문) 로그. 면접관 발화이며 꼬리질문이 아니다. */
    public static QaLog opening(InterviewSession session, Question question, int seq) {
        DomainGuard.requireNotNull(question, "question");
        return new QaLog(session, question, QaRole.INTERVIEWER, question.getContent(), false, seq);
    }

    /** 사용자 답변 로그. 출처 질문이 없고 꼬리질문이 아니다. */
    public static QaLog userAnswer(InterviewSession session, String content, int seq) {
        return new QaLog(session, null, QaRole.USER, content, false, seq);
    }
}
