package com.aiinterviewer.llm.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gemini 연동 설정. API 키는 환경변수/`.env`의 GEMINI_API_KEY로만 주입되며 커밋 금지(D18/D26).
 * application.yml의 {@code llm.gemini.*} 와 매핑. 키가 비어 있으면 LLM 호출만 비활성된다.
 *
 * @param apiKey  Gemini API 키(비어 있을 수 있음)
 * @param model   모델명(예: gemini-2.5-flash)
 * @param baseUrl Gemini REST 베이스 URL
 */
@ConfigurationProperties(prefix = "llm.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl
) {

    /** 키가 설정되어 있는지(공백 아님) 여부. */
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
