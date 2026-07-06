package com.aiinterviewer.application.auth;

/**
 * 소셜 프로바이더 자격증명(구글 ID 토큰 등)을 검증해 사용자 신원을 확인하는 포트(결정사항 D38).
 * 구체 프로바이더(구글)는 어댑터로 구현하고, 애플리케이션은 이 인터페이스에만 의존한다(DIP).
 * 카카오/네이버 등은 같은 포트의 어댑터를 추가해 확장한다(CLAUDE.md 규칙 7 · D18과 같은 원칙).
 */
public interface SocialIdentityVerifier {

    /** 이 검증기가 담당하는 프로바이더 이름(예: {@code "google"}). */
    String provider();

    /**
     * 자격증명을 검증하고 신원을 돌려준다.
     *
     * @param credential 프로바이더가 발급한 토큰(구글 ID 토큰)
     * @return 검증된 신원
     * @throws SocialAuthenticationException 토큰이 유효하지 않을 때
     */
    SocialIdentity verify(String credential);
}
