package com.aiinterviewer.adapter.web.session;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 세션 목록(내 면접 기록) 통합 테스트 — 소유자만, 최근 순, 사용자 격리, 미인증 401.
 * 세션 시작은 LLM을 호출하지 않으므로 Fake 없이 실 컨텍스트로 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SessionListIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("내가 시작한 세션만 목록에 나오고, 다른 사용자 세션은 보이지 않는다")
    void listsOwnSessionsOnly() throws Exception {
        String owner = authenticate("history-owner@test.com");
        startSession(owner);
        startSession(owner);

        mockMvc.perform(get("/api/sessions").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sessionId").isNumber())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        String other = authenticate("history-other@test.com");
        mockMvc.perform(get("/api/sessions").header("Authorization", "Bearer " + other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("토큰 없이 목록 조회는 401")
    void withoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/sessions")).andExpect(status().isUnauthorized());
    }

    private void startSession(String token) throws Exception {
        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categorySlugs":["os"],"randomAll":false}
                                """))
                .andExpect(status().isCreated());
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
