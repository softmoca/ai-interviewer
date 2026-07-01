package com.aiinterviewer.adapter.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 바디. 웹 계약(DTO)이며 도메인/애플리케이션 타입과 분리한다(AP-3 방지).
 * 형식 검증(빈값·이메일 형식·길이)은 여기서, 업무 규칙(중복 등)은 애플리케이션/도메인에서.
 */
public record SignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(max = 30) String nickname
) {
}
