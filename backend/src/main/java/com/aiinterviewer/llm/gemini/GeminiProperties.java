package com.aiinterviewer.llm.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gemini 연동 설정. API 키는 환경변수(GEMINI_API_KEY)로만 주입되며 커밋 금지(결정사항 D18).
 * application.yml의 {@code llm.gemini.*} 와 매핑.
 */
@ConfigurationProperties(prefix = "llm.gemini")
public record GeminiProperties(
        String apiKey,
        String model
) {
}
