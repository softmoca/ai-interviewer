package com.aiinterviewer.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiinterviewer.application.auth.InvalidTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** JwtTokenProvider 단위 테스트 — 발급→검증 라운드트립과 위조/오류 토큰 거부. */
class JwtTokenProviderTest {

    private static final String SECRET = "test-only-jwt-secret-at-least-32-bytes-long-000";
    private final JwtTokenProvider tokenProvider =
            new JwtTokenProvider(new JwtProperties(SECRET, 3600));

    @Test
    @DisplayName("발급한 토큰에서 원래 userId를 복원한다")
    void issueAndParseRoundTrip() {
        String token = tokenProvider.issue(42L);

        assertThat(tokenProvider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("변조되거나 형식이 잘못된 토큰은 InvalidTokenException")
    void rejectsInvalidToken() {
        assertThatThrownBy(() -> tokenProvider.getUserId("not-a-jwt"))
                .isInstanceOf(InvalidTokenException.class);

        String token = tokenProvider.issue(1L);
        assertThatThrownBy(() -> tokenProvider.getUserId(token + "tampered"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("다른 서명 키로 만든 토큰은 거부한다")
    void rejectsTokenSignedWithDifferentKey() {
        JwtTokenProvider other =
                new JwtTokenProvider(new JwtProperties("another-secret-at-least-32-bytes-long-111", 3600));
        String foreignToken = other.issue(7L);

        assertThatThrownBy(() -> tokenProvider.getUserId(foreignToken))
                .isInstanceOf(InvalidTokenException.class);
    }
}
