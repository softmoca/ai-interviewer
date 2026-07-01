package com.aiinterviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * AI 면접관 백엔드 진입점.
 *
 * <p>{@link EnableJpaAuditing}는 {@code BaseTimeEntity}의 생성/수정 시각 자동 기록에,
 * {@link ConfigurationPropertiesScan}은 {@code GeminiProperties} 등 설정 바인딩에 사용된다.
 */
@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
public class AiInterviewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiInterviewerApplication.class, args);
    }
}
