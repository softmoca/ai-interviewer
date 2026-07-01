package com.aiinterviewer.application.auth;

/**
 * 현재 로그인 사용자 정보(애플리케이션 계층 반환값).
 *
 * @param userId   사용자 식별자
 * @param email    이메일
 * @param nickname 닉네임
 */
public record MeResult(
        Long userId,
        String email,
        String nickname
) {
}
