package com.aiinterviewer.adapter.web.session;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.LlmNotConfiguredException;
import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 키가 없을 때(LLM 미설정) 동작 검증 — 앱은 정상 기동하고, 답변 제출 시 LLM 호출만 명확한
 * 503으로 실패한다(결정사항 D26). 환경과 무관하도록 '미설정 LLM'을 주입해 결정론적으로 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(LlmNotConfiguredIntegrationTest.NotConfiguredLlmConfig.class)
class LlmNotConfiguredIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @TestConfiguration
    static class NotConfiguredLlmConfig {
        @Bean
        @Primary
        LlmClient notConfiguredLlmClient() {
            return new LlmClient() {
                @Override
                public FollowUpResult generateFollowUp(String prompt) {
                    throw new LlmNotConfiguredException("GEMINI_API_KEY가 설정되지 않았습니다.");
                }

                @Override
                public EvaluationResult evaluate(String prompt) {
                    throw new LlmNotConfiguredException("GEMINI_API_KEY가 설정되지 않았습니다.");
                }
            };
        }
    }

    @Test
    @DisplayName("키 없이 답변 제출 시 503(LLM_NOT_CONFIGURED), 앱은 정상")
    void answerWithoutLlmKeyReturns503() throws Exception {
        String token = authenticate("no-llm@test.com");
        long sessionId = startSession(token);

        mockMvc.perform(post("/api/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"답변입니다."}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("LLM_NOT_CONFIGURED"));
    }

    private long startSession(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categorySlugs":["os"],"randomAll":false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("sessionId").asLong();
    }

    private String authenticate(String email) throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password1","nickname":"tester"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"password1"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
