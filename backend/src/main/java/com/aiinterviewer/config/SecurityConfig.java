package com.aiinterviewer.config;

import com.aiinterviewer.adapter.security.JwtAuthenticationFilter;
import com.aiinterviewer.application.auth.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 보안 설정(결정사항 D21). JWT 기반 무상태(STATELESS) 인증.
 *
 * <ul>
 *   <li>공개: 인증 API(/api/auth/**), 헬스체크(/api/health), H2 콘솔(dev)</li>
 *   <li>그 외: 인증 필요 → {@link JwtAuthenticationFilter}가 세운 인증이 없으면 401</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                // CORS 활성화(CorsConfigurationSource 빈 사용 — 프론트 직접 호출/배포 대비, D29)
                .cors(Customizer.withDefaults())
                // 무상태 토큰 인증이라 CSRF 불필요(세션 미사용). H2 콘솔 프레임 허용.
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개: 가입/로그인/헬스체크. /api/auth/me 등 그 외는 인증 필요.
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/google", "/api/health").permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "인증이 필요합니다.")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** 인증 필터를 토큰 포트로 구성한다(구현 교체 시 이 배선만 영향). */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(TokenProvider tokenProvider) {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
