package com.aiinterviewer.adapter.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 구글 소셜 로그인 설정. application.yml의 {@code oauth.google.*}와 매핑(결정사항 D38).
 * {@code clientId}는 ID 토큰 검증의 대상(audience)으로 쓰인다. 환경변수(GOOGLE_CLIENT_ID)로 주입하며,
 * 비어 있으면 구글 로그인만 비활성이고 앱은 정상 기동한다.
 *
 * @param clientId 구글 OAuth 클라이언트 ID (예: {@code xxxx.apps.googleusercontent.com})
 */
@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProperties(
        String clientId
) {
}
