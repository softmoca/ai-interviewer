package com.aiinterviewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 보안 설정 뼈대 (M2에서 JWT 인증으로 확장 예정 — docs/아키텍처.md, 결정사항 D9).
 *
 * <p>현재는 뼈대 단계라 전 경로를 열어두고 H2 콘솔 접근만 허용한다.
 * 실제 인증/인가 규칙은 인증 기능 구현 시 채운다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 개발 편의: H2 콘솔 사용을 위해 CSRF/프레임 옵션 완화 (운영 전환 시 재검토)
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        AntPathRequestMatcher.antMatcher("/h2-console/**")))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // TODO(M2): 인증 필요한 경로/공개 경로를 구분한다.
                        .anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
