package com.aiinterviewer.adapter.web.auth;

import com.aiinterviewer.application.auth.AuthService;
import com.aiinterviewer.application.auth.LoginResult;
import com.aiinterviewer.application.auth.MeResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 API(웹 어댑터). HTTP ↔ 애플리케이션 번역만 담당하고 업무 판단은 하지 않는다(AP-1 방지).
 *
 * <ul>
 *   <li>POST /api/auth/signup — 회원가입</li>
 *   <li>POST /api/auth/login — 로그인(토큰 발급)</li>
 *   <li>POST /api/auth/google — 구글 소셜 로그인(ID 토큰 검증 후 토큰 발급, D38)</li>
 *   <li>GET  /api/auth/me — 현재 로그인 사용자(보호됨)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        Long id = authService.signup(request.email(), request.password(), request.nickname());
        return new SignupResponse(id, request.email(), request.nickname());
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.email(), request.password());
        return TokenResponse.from(result);
    }

    @PostMapping("/google")
    public TokenResponse googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResult result = authService.socialLogin("google", request.idToken());
        return TokenResponse.from(result);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Long userId) {
        MeResult result = authService.getMe(userId);
        return MeResponse.from(result);
    }
}
