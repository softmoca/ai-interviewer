package com.aiinterviewer.adapter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정. application.yml의 {@code jwt.*}와 매핑(결정사항 D21).
 * 서명 키는 환경변수(JWT_SECRET)로 주입되며 운영 값은 커밋하지 않는다.
 *
 * @param secret                      HS256 서명 키(최소 32바이트)
 * @param accessTokenValiditySeconds  액세스 토큰 유효기간(초)
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds
) {
}
