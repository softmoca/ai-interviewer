package com.aiinterviewer.application.auth;

/**
 * 인증 토큰 발급/검증 포트(애플리케이션 소유 추상화).
 *
 * <p>토큰은 도메인 개념이 아니라 인증 메커니즘(인프라)이다. 애플리케이션은 이 인터페이스에만
 * 의존하고, JWT(jjwt) 등 구체 구현은 adapter 계층에 둔다(DIP — LlmClient/D18과 동일한 패턴).
 */
public interface TokenProvider {

    /** 사용자 식별자로 액세스 토큰을 발급한다. */
    String issue(Long userId);

    /**
     * 토큰을 검증하고 사용자 식별자를 꺼낸다.
     *
     * @throws com.aiinterviewer.application.auth.InvalidTokenException 유효하지 않은 토큰
     */
    Long getUserId(String token);
}
