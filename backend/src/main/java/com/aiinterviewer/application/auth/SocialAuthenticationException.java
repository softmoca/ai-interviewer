package com.aiinterviewer.application.auth;

/**
 * 소셜 로그인 실패(결정사항 D38) — 토큰이 유효하지 않거나, 이메일이 미검증이거나, 지원하지 않는
 * 프로바이더인 경우. 자체 로그인 실패와 마찬가지로 401로 매핑한다(원인 상세는 노출하지 않는다).
 */
public class SocialAuthenticationException extends RuntimeException {

    public SocialAuthenticationException(String message) {
        super(message);
    }
}
