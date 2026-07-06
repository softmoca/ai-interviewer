package com.aiinterviewer.adapter.web.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 구글 로그인 요청(결정사항 D38). 프론트가 GIS로 받은 ID 토큰을 담아 보낸다.
 *
 * @param idToken 구글 Identity Services가 발급한 ID 토큰(JWT)
 */
public record GoogleLoginRequest(
        @NotBlank(message = "idToken은 필수입니다.") String idToken
) {
}
