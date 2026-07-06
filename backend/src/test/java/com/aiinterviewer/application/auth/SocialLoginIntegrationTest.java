package com.aiinterviewer.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiinterviewer.domain.user.User;
import com.aiinterviewer.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 소셜 로그인(D38) 흐름 검증 — 실제 구글 토큰 없이, 프로바이더 이름이 다른 가짜 검증기를 추가로
 * 등록해 find-or-create / 이메일 연동 / 미검증 이메일 거부를 확인한다. (가짜는 provider가
 * "test-social"이라 실제 GoogleIdentityVerifier("google")와 충돌하지 않는다.)
 */
@SpringBootTest
@Import(SocialLoginIntegrationTest.FakeSocialConfig.class)
class SocialLoginIntegrationTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FakeSocialVerifier fakeVerifier;

    @Test
    @DisplayName("처음 보는 소셜 사용자는 계정을 생성하고 토큰을 발급한다")
    void createsNewSocialUser() {
        fakeVerifier.next = new SocialIdentity("sub-new", "new@social.com", true, "새유저");

        LoginResult result = authService.socialLogin("test-social", "cred");

        assertThat(result.accessToken()).isNotBlank();
        User saved = userRepository.findByProviderAndProviderId("test-social", "sub-new").orElseThrow();
        assertThat(saved.getEmail()).isEqualTo("new@social.com");
        assertThat(saved.getNickname()).isEqualTo("새유저");
        assertThat(saved.getId()).isEqualTo(result.userId());
    }

    @Test
    @DisplayName("이메일이 같은 기존 계정에는 소셜을 연동한다(계정 중복 생성 없음)")
    void linksToExistingEmailAccount() {
        Long existingId = authService.signup("link@social.com", "password1", "기존닉");
        fakeVerifier.next = new SocialIdentity("sub-link", "link@social.com", true, "구글이름");

        LoginResult result = authService.socialLogin("test-social", "cred");

        // 새 계정을 만들지 않고 기존 계정에 연결 → 같은 userId
        assertThat(result.userId()).isEqualTo(existingId);
        User linked = userRepository.findById(existingId).orElseThrow();
        assertThat(linked.getProvider()).isEqualTo("test-social");
        assertThat(linked.getProviderId()).isEqualTo("sub-link");
    }

    @Test
    @DisplayName("같은 프로바이더 신원으로 다시 로그인하면 같은 계정을 재사용한다")
    void reusesAccountForReturningUser() {
        fakeVerifier.next = new SocialIdentity("sub-repeat", "repeat@social.com", true, "재방문");

        Long first = authService.socialLogin("test-social", "cred").userId();
        Long second = authService.socialLogin("test-social", "cred").userId();

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("이메일 미검증 소셜 계정은 거부한다(계정 가로채기 방지)")
    void rejectsUnverifiedEmail() {
        fakeVerifier.next = new SocialIdentity("sub-unverified", "spoof@social.com", false, "미검증");

        assertThatThrownBy(() -> authService.socialLogin("test-social", "cred"))
                .isInstanceOf(SocialAuthenticationException.class);
    }

    @Test
    @DisplayName("지원하지 않는 프로바이더는 거부한다")
    void rejectsUnsupportedProvider() {
        assertThatThrownBy(() -> authService.socialLogin("myspace", "cred"))
                .isInstanceOf(SocialAuthenticationException.class);
    }

    /** 프로바이더 이름이 "test-social"인 가짜 검증기 — 다음에 돌려줄 신원을 테스트가 주입한다. */
    static class FakeSocialVerifier implements SocialIdentityVerifier {
        SocialIdentity next;

        @Override
        public String provider() {
            return "test-social";
        }

        @Override
        public SocialIdentity verify(String credential) {
            return next;
        }
    }

    @TestConfiguration
    static class FakeSocialConfig {
        @Bean
        FakeSocialVerifier fakeSocialVerifier() {
            return new FakeSocialVerifier();
        }
    }
}
