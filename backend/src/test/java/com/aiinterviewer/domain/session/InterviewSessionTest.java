package com.aiinterviewer.domain.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * InterviewSession 상태 전이 규칙 단위 테스트 — Spring/DB 없이 실행.
 * 도메인 규칙(진행 중일 때만 종료 가능)만 검증한다(test-strategy.md §1).
 *
 * <p>세션은 아직 생성 팩터리가 없어 같은 패키지에서 protected 기본 생성자로 만든다
 * (JPA용 생성자 재사용). 상태 머신 검증에는 status/endedAt만 관여한다.
 */
class InterviewSessionTest {

    private static final LocalDateTime ENDED_AT = LocalDateTime.of(2026, 7, 1, 10, 30);

    @Test
    @DisplayName("새 세션은 진행 중 상태다")
    void newSessionIsInProgress() {
        InterviewSession session = new InterviewSession();

        assertSoftly(softly -> {
            softly.assertThat(session.isInProgress()).isTrue();
            softly.assertThat(session.getStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
            softly.assertThat(session.getEndedAt()).isNull();
        });
    }

    @Nested
    @DisplayName("정상 종료 / 중단")
    class Finish {

        @Test
        @DisplayName("complete()는 완료 상태로 전이하고 종료 시각을 기록한다")
        void complete() {
            InterviewSession session = new InterviewSession();

            session.complete(ENDED_AT);

            assertSoftly(softly -> {
                softly.assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
                softly.assertThat(session.getEndedAt()).isEqualTo(ENDED_AT);
                softly.assertThat(session.isInProgress()).isFalse();
            });
        }

        @Test
        @DisplayName("abandon()은 중단 상태로 전이하고 종료 시각을 기록한다")
        void abandon() {
            InterviewSession session = new InterviewSession();

            session.abandon(ENDED_AT);

            assertSoftly(softly -> {
                softly.assertThat(session.getStatus()).isEqualTo(SessionStatus.ABANDONED);
                softly.assertThat(session.getEndedAt()).isEqualTo(ENDED_AT);
                softly.assertThat(session.isInProgress()).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("전이 불변식: 진행 중일 때만 종료 가능")
    class TransitionInvariant {

        @Test
        @DisplayName("이미 완료된 세션은 다시 종료할 수 없다")
        void cannotFinishCompletedSession() {
            InterviewSession session = new InterviewSession();
            session.complete(ENDED_AT);

            assertThatThrownBy(() -> session.complete(ENDED_AT))
                    .isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> session.abandon(ENDED_AT))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("이미 중단된 세션은 다시 종료할 수 없다")
        void cannotFinishAbandonedSession() {
            InterviewSession session = new InterviewSession();
            session.abandon(ENDED_AT);

            assertThatThrownBy(() -> session.complete(ENDED_AT))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
