package com.aiinterviewer.adapter.web.evaluation;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aiinterviewer.support.TestLlmConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 세션 평가 API 통합 테스트 — 실 컨텍스트(보안 + JWT + seed H2)로 생성/조회/오류 케이스 검증.
 * LLM은 {@link TestLlmConfig}의 Fake(결정론적 평가)로 대체해 키 없이 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestLlmConfig.class)
class EvaluationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("완료된 세션을 평가하면 개념별 점수 리포트를 반환하고, 조회도 동일하다")
    void evaluateThenGet() throws Exception {
        String token = authenticate("eval-flow@test.com");
        long sessionId = completedSession(token);

        // 평가 생성
        mockMvc.perform(post("/api/sessions/" + sessionId + "/evaluation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value((int) sessionId))
                .andExpect(jsonPath("$.concepts.length()").value(1))
                .andExpect(jsonPath("$.concepts[0].concept").value("프로세스와 스레드"))
                .andExpect(jsonPath("$.concepts[0].accuracy").value(4))
                .andExpect(jsonPath("$.concepts[0].depth").value(3))
                .andExpect(jsonPath("$.overallComment", notNullValue()));

        // 조회 — 동일 리포트
        mockMvc.perform(get("/api/sessions/" + sessionId + "/evaluation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepts.length()").value(1))
                .andExpect(jsonPath("$.overallComment", notNullValue()));
    }

    @Test
    @DisplayName("진행 중(미완료) 세션 평가는 409")
    void evaluateInProgressIsConflict() throws Exception {
        String token = authenticate("eval-inprogress@test.com");
        long sessionId = startSession(token); // 종료하지 않음

        mockMvc.perform(post("/api/sessions/" + sessionId + "/evaluation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("평가 전 리포트 조회는 404")
    void getBeforeEvaluateIsNotFound() throws Exception {
        String token = authenticate("eval-none@test.com");
        long sessionId = completedSession(token);

        mockMvc.perform(get("/api/sessions/" + sessionId + "/evaluation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 사용자의 세션 평가/조회는 403")
    void othersSessionIsForbidden() throws Exception {
        String owner = authenticate("eval-owner@test.com");
        long sessionId = completedSession(owner);

        String intruder = authenticate("eval-intruder@test.com");
        mockMvc.perform(post("/api/sessions/" + sessionId + "/evaluation")
                        .header("Authorization", "Bearer " + intruder))
                .andExpect(status().isForbidden());
    }

    /** 세션 시작 → 답변 → 종료까지 진행하고 sessionId 반환. */
    private long completedSession(String token) throws Exception {
        long sessionId = startSession(token);
        mockMvc.perform(post("/api/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"프로세스는 독립 메모리, 스레드는 공유 메모리입니다."}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        return sessionId;
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
