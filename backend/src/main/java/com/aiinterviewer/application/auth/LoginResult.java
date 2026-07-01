package com.aiinterviewer.application.auth;

/**
 * 로그인 성공 결과(애플리케이션 계층 반환값). 웹 응답 DTO와는 분리한다(관심사 분리).
 *
 * @param accessToken 발급된 액세스 토큰
 * @param userId      사용자 식별자
 * @param nickname    사용자 닉네임
 */
public record LoginResult(
        String accessToken,
        Long userId,
        String nickname
) {
}
