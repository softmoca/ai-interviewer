package com.aiinterviewer.adapter.web.auth;

import com.aiinterviewer.application.auth.LoginResult;

/** 로그인 응답 바디(액세스 토큰 + 최소 사용자 정보). */
public record TokenResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String nickname
) {
    public static TokenResponse from(LoginResult result) {
        return new TokenResponse(result.accessToken(), "Bearer", result.userId(), result.nickname());
    }
}
