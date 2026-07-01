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
}
