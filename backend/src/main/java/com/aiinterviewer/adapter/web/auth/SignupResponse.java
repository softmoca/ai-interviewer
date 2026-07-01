package com.aiinterviewer.adapter.web.auth;

/** 회원가입 응답 바디. */
public record SignupResponse(
        Long id,
        String email,
        String nickname
) {
}
