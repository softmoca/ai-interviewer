package com.aiinterviewer.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 프론트엔드(다른 오리진)에서 백엔드를 직접 호출할 때를 위한 CORS 설정(결정사항 D29).
 *
 * <p>개발 기본은 Vite 프록시라 CORS가 불필요하지만, 프론트가 백엔드를 직접 부르거나(예:
 * 배포) 다른 포트에서 접근할 때를 대비해 허용 오리진을 열어둔다. 허용 오리진은
 * {@code app.cors.allowed-origins}(기본 http://localhost:5173). Bearer 토큰만 쓰므로
 * 쿠키 자격증명은 허용하지 않는다(allowCredentials=false).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
