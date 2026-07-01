package com.aiinterviewer.domain.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.aiinterviewer.domain.user.PasswordEncryptor;
import com.aiinterviewer.domain.user.User;
import java.time.LocalDateTime;
import java.util.Set;
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

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 7, 1, 10, 0);
    private static final LocalDateTime ENDED_AT = LocalDateTime.of(2026, 7, 1, 10, 30);

    private static final PasswordEncryptor FAKE_ENCRYPTOR = new PasswordEncryptor() {
        @Override
        public String encrypt(String rawPassword) {
            return "enc:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encryptedPassword) {
            return encryptedPassword.equals("enc:" + rawPassword);
        }
    };

    private static User host() {
        return User.register("host@test.com", "password1", "host", FAKE_ENCRYPTOR);
    }

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
    @DisplayName("start: 세션 시작 설정 검증")
    class Start {

        @Test
        @DisplayName("유효한 설정으로 진행 중 세션을 시작한다")
        void startsSession() {
            InterviewSession session =
                    InterviewSession.start(host(), Set.of(1L, 2L), false, 5, 2, STARTED_AT);

            assertSoftly(softly -> {
                softly.assertThat(session.isInProgress()).isTrue();
                softly.assertThat(session.getCategoryIds()).containsExactlyInAnyOrder(1L, 2L);
                softly.assertThat(session.isRandomAll()).isFalse();
                softly.assertThat(session.getQuestionCount()).isEqualTo(5);
                softly.assertThat(session.getDifficulty()).isEqualTo(2);
                softly.assertThat(session.getStartedAt()).isEqualTo(STARTED_AT);
            });
        }

        @Test
        @DisplayName("전체 랜덤이면 카테고리 없이도 시작할 수 있다")
        void randomAllAllowsNoCategory() {
            assertThatCode(() -> InterviewSession.start(host(), Set.of(), true, null, null, STARTED_AT))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("전체 랜덤이 아닌데 카테고리가 없으면 거부한다")
        void rejectsNoCategoryWhenNotRandom() {
            assertThatThrownBy(() -> InterviewSession.start(host(), Set.of(), false, null, null, STARTED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("난이도(1~3)·질문 수(≥1)·사용자 필수를 위반하면 거부한다")
        void rejectsInvalidSettings() {
            assertThatThrownBy(() -> InterviewSession.start(host(), Set.of(1L), false, null, 4, STARTED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> InterviewSession.start(host(), Set.of(1L), false, 0, null, STARTED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> InterviewSession.start(null, Set.of(1L), false, null, null, STARTED_AT))
                    .isInstanceOf(IllegalArgumentException.class);
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
