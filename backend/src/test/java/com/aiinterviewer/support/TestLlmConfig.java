package com.aiinterviewer.support;

import com.aiinterviewer.llm.LlmClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 통합 테스트에서 실제 Gemini 대신 {@link FakeLlmClient}를 주입한다(@Primary로 우선).
 * {@code @Import(TestLlmConfig.class)}로 사용.
 */
@TestConfiguration
public class TestLlmConfig {

    @Bean
    @Primary
    public LlmClient fakeLlmClient() {
        return new FakeLlmClient();
    }
}
