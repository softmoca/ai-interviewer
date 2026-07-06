package com.aiinterviewer.application.auth;

/**
 * 소셜 프로바이더가 인증한 사용자 신원(결정사항 D38). 프로바이더 토큰을 검증해 얻은 결과로,
 * 어느 프로바이더든 공통으로 다루는 표현이다(구글/카카오 등 교체 가능하도록 추상화 — CLAUDE.md 규칙 7).
 *
 * @param providerId    프로바이더 내 사용자 식별자(구글 {@code sub} 등)
 * @param email         프로바이더가 알려준 이메일
 * @param emailVerified 프로바이더가 이메일 소유를 검증했는지(이메일 기반 계정 연동의 안전장치)
 * @param name          표시 이름(닉네임 후보, 없을 수 있음)
 */
public record SocialIdentity(
        String providerId,
        String email,
        boolean emailVerified,
        String name
) {
}
