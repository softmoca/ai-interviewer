package com.aiinterviewer.adapter.web.auth;

import jakarta.validation.constraints.NotBlank;

/** 로그인 요청 바디. */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}
