package com.aiinterviewer.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * User 도메인 단위 테스트 — Spring/DB 없이 ms 단위로 실행(도메인 우선, 테스트 용이성).
 * 암호화는 실제 BCrypt 대신 단순 가짜 포트로 대체해 도메인 규칙만 검증한다.
 */
class UserTest {

    /** 테스트용 가짜 암호화기: "enc:" 접두사만 붙인다(도메인은 구현을 모른다는 점을 이용). */
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

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("원문 비밀번호는 암호화되어 저장되고 필수값이 채워진다")
        void registersWithEncryptedPassword() {
            User user = User.register("me@test.com", "password1", "닉네임", FAKE_ENCRYPTOR);

            assertThat(user.getEmail()).isEqualTo("me@test.com");
            assertThat(user.getNickname()).isEqualTo("닉네임");
            // 원문으로는 인증되지만(=암호화 저장), 해시 자체는 getter로 노출되지 않는다.
            assertThat(user.authenticate("password1", FAKE_ENCRYPTOR)).isTrue();
        }

        @Test
        @DisplayName("이메일/비밀번호/닉네임이 비면 생성을 거부한다")
        void rejectsBlankRequiredFields() {
            assertThatThrownBy(() -> User.register(" ", "password1", "닉", FAKE_ENCRYPTOR))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> User.register("me@test.com", " ", "닉", FAKE_ENCRYPTOR))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> User.register("me@test.com", "password1", " ", FAKE_ENCRYPTOR))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("올바른 비밀번호면 true, 틀리면 false")
        void matchesPassword() {
            User user = User.register("me@test.com", "password1", "닉", FAKE_ENCRYPTOR);

            assertThat(user.authenticate("password1", FAKE_ENCRYPTOR)).isTrue();
            assertThat(user.authenticate("wrong-pass", FAKE_ENCRYPTOR)).isFalse();
        }
    }

    @Nested
    @DisplayName("소셜 로그인 (D38)")
    class Social {

        @Test
        @DisplayName("registerSocial: 비밀번호 없이 프로바이더 신원으로 가입 — 자체 로그인은 불가")
        void registersSocialUser() {
            User user = User.registerSocial("me@gmail.com", "구글닉", "google", "google-sub-123");

            assertThat(user.getEmail()).isEqualTo("me@gmail.com");
            assertThat(user.getNickname()).isEqualTo("구글닉");
            assertThat(user.getProvider()).isEqualTo("google");
            assertThat(user.getProviderId()).isEqualTo("google-sub-123");
            // 비밀번호가 없으므로 어떤 원문으로도 자체 로그인되지 않는다.
            assertThat(user.authenticate("anything", FAKE_ENCRYPTOR)).isFalse();
        }

        @Test
        @DisplayName("linkSocial: 기존 계정에 프로바이더 신원을 연결한다")
        void linksSocialToExistingUser() {
            User user = User.register("me@test.com", "password1", "닉", FAKE_ENCRYPTOR);

            user.linkSocial("google", "google-sub-123");

            assertThat(user.getProvider()).isEqualTo("google");
            assertThat(user.getProviderId()).isEqualTo("google-sub-123");
            // 연동 후에도 기존 비밀번호 로그인은 그대로 가능하다.
            assertThat(user.authenticate("password1", FAKE_ENCRYPTOR)).isTrue();
        }

        @Test
        @DisplayName("linkSocial: 이미 다른 프로바이더에 연결됐으면 거부한다")
        void rejectsRelinkToDifferentProvider() {
            User user = User.registerSocial("me@gmail.com", "닉", "google", "google-sub-123");

            assertThatThrownBy(() -> user.linkSocial("kakao", "kakao-999"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("linkSocial: 필수값(provider/providerId)이 비면 거부한다")
        void rejectsBlankLinkArgs() {
            User user = User.register("me@test.com", "password1", "닉", FAKE_ENCRYPTOR);

            assertThatThrownBy(() -> user.linkSocial(" ", "id")).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> user.linkSocial("google", " ")).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
