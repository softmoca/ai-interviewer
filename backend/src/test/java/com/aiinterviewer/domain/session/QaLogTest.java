package com.aiinterviewer.domain.session;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.aiinterviewer.domain.category.Category;
import com.aiinterviewer.domain.category.CategoryPhase;
import com.aiinterviewer.domain.question.Question;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * QaLog 생성 팩터리 단위 테스트 — role/followUp/출처질문 일관성 규칙 검증
 * (domain-design.md §3). 세션은 같은 패키지의 protected 기본 생성자로 만든다.
 */
class QaLogTest {

    private static final Category CATEGORY = Category.of("운영체제", "os", CategoryPhase.MVP, 0);
    private static final Question QUESTION =
            Question.of(CATEGORY, "개요", "운영체제의 역할은?", 1, List.of("자원 관리"), null, null, true);

    @Test
    @DisplayName("opening: 면접관 발화 + 출처 질문 + 질문 본문을 담고, 꼬리질문이 아니다")
    void opening() {
        InterviewSession session = new InterviewSession();

        QaLog log = QaLog.opening(session, QUESTION, 1);

        assertSoftly(softly -> {
            softly.assertThat(log.getRole()).isEqualTo(QaRole.INTERVIEWER);
            softly.assertThat(log.getQuestion()).isSameAs(QUESTION);
            softly.assertThat(log.getContent()).isEqualTo("운영체제의 역할은?");
            softly.assertThat(log.isFollowUp()).isFalse();
            softly.assertThat(log.getSeq()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("userAnswer: 사용자 발화 + 출처 질문 없음 + 꼬리질문 아님")
    void userAnswer() {
        InterviewSession session = new InterviewSession();

        QaLog log = QaLog.userAnswer(session, "제 답변입니다.", 2);

        assertSoftly(softly -> {
            softly.assertThat(log.getRole()).isEqualTo(QaRole.USER);
            softly.assertThat(log.getQuestion()).isNull();
            softly.assertThat(log.getContent()).isEqualTo("제 답변입니다.");
            softly.assertThat(log.isFollowUp()).isFalse();
            softly.assertThat(log.getSeq()).isEqualTo(2);
        });
    }

    @Nested
    @DisplayName("생성 거부")
    class Rejects {

        @Test
        @DisplayName("빈 답변은 거부한다")
        void rejectsBlankAnswer() {
            InterviewSession session = new InterviewSession();
            assertThatThrownBy(() -> QaLog.userAnswer(session, " ", 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("순서(seq)는 1 이상이어야 한다")
        void rejectsNonPositiveSeq() {
            InterviewSession session = new InterviewSession();
            assertThatThrownBy(() -> QaLog.userAnswer(session, "답변", 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
