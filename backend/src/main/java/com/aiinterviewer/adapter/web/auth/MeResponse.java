package com.aiinterviewer.adapter.web.auth;

import com.aiinterviewer.application.auth.MeResult;

/** 현재 로그인 사용자 응답 바디. */
public record MeResponse(
        Long userId,
        String email,
        String nickname
) {
    public static MeResponse from(MeResult result) {
        return new MeResponse(result.userId(), result.email(), result.nickname());
    }
}
