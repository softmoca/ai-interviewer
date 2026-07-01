package com.aiinterviewer.adapter.web.session;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
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
 * 세션 API 통합 테스트 — 실 컨텍스트(보안 + JWT + seed 적재된 H2)로 전 흐름과 오류 케이스 검증.
 * seed 로더가 기동 시 "os" 카테고리를 적재하므로 첫 질문 서빙이 실제로 동작한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SessionIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("세션 시작 → 답변 기록 → 조회 → 종료 전 흐름이 동작한다")
    void fullSessionFlow() throws Exception {
        String token = authenticate("session-flow@test.com");

        // 세션 시작 + 첫 질문 서빙
        MvcResult started = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categorySlugs":["os"],"randomAll":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.firstQuestion.questionId", notNullValue()))
                .andExpect(jsonPath("$.firstQuestion.seq").value(1))
                .andReturn();

        long sessionId = objectMapper.readTree(started.getResponse().getContentAsString())
                .get("sessionId").asLong();

        // 답변 기록 (seq 2)
        mockMvc.perform(post("/api/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"운영체제는 자원 관리자입니다."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seq").value(2));

        // 조회 — 대화 이력 2건(오프닝 + 답변)
        mockMvc.perform(get("/api/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.transcript.length()").value(2))
                .andExpect(jsonPath("$.transcript[0].role").value("INTERVIEWER"))
                .andExpect(jsonPath("$.transcript[1].role").value("USER"));

        // 종료
        mockMvc.perform(post("/api/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.endedAt", notNullValue()));

        // 종료된 세션에 답변 제출 → 409
        mockMvc.perform(post("/api/sessions/" + sessionId + "/answers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"늦은 답변"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("전체 랜덤으로도 세션을 시작할 수 있다")
    void startWithRandomAll() throws Exception {
        String token = authenticate("session-random@test.com");

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"randomAll":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstQuestion.questionId", notNullValue()));
    }

    @Test
    @DisplayName("카테고리 미선택(전체 랜덤 아님)은 400")
    void startWithoutCategoryIsBadRequest() throws Exception {
        String token = authenticate("session-nocat@test.com");

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"randomAll":false}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("다른 사용자의 세션 조회는 403")
    void accessingOthersSessionIsForbidden() throws Exception {
        String owner = authenticate("session-owner@test.com");
        long sessionId = startSession(owner);

        String intruder = authenticate("session-intruder@test.com");
        mockMvc.perform(get("/api/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + intruder))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("토큰 없이 세션 API 접근은 401")
    void withoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/sessions/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 세션 조회는 404")
    void unknownSessionIsNotFound() throws Exception {
        String token = authenticate("session-404@test.com");

        mockMvc.perform(get("/api/sessions/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
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

    /** 회원가입 후 로그인해 액세스 토큰을 얻는다. */
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

        JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
