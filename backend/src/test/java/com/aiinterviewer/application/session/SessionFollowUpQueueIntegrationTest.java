package com.aiinterviewer.application.session;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.aiinterviewer.application.auth.AuthService;
import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * 꼬리질문 순차 큐잉(D36) 검증 — LLM이 한 답변에 2개를 주면, 첫 번째만 제시하고 두 번째는
 * 보관했다가 다음 답변 때 낸다. 보관분이 소진되기 전에는 LLM을 다시 호출하지 않는다.
 * (즉 답변 2번당 LLM 생성 1회)
 */
@SpringBootTest
@Import(SessionFollowUpQueueIntegrationTest.TwoFollowUpConfig.class)
class SessionFollowUpQueueIntegrationTest {

    @Autowired
    AuthService authService;

    @Autowired
    SessionService sessionService;

    @Autowired
    TwoFollowUpLlmClient fakeLlm;

    @Test
    @DisplayName("두 번째 꼬리질문은 보관분에서 제시되고, 소진 전엔 LLM을 재호출하지 않는다")
    void secondFollowUpIsServedFromQueue() {
        Long userId = authService.signup("followup-queue@test.com", "password1", "큐");
        Long sessionId = sessionService.startSession(userId,
                new StartSessionCommand(List.of("os"), false, null, null)).sessionId();

        // 1) 오프닝 답변 → LLM 1회 호출, A 제시 / B 보관
        AnswerResult r1 = sessionService.submitAnswer(userId, sessionId, "답변1");
        // 2) A 답변 → LLM 재호출 없이 보관된 B 제시
        AnswerResult r2 = sessionService.submitAnswer(userId, sessionId, "답변2");
        // 3) B 답변 → 보관 소진 → LLM 재호출, 새 배치 A 제시
        AnswerResult r3 = sessionService.submitAnswer(userId, sessionId, "답변3");

        assertSoftly(softly -> {
            softly.assertThat(r1.nextQuestion().content()).isEqualTo("꼬리질문 A");
            softly.assertThat(r2.nextQuestion().content()).isEqualTo("꼬리질문 B");
            softly.assertThat(r3.nextQuestion().content()).isEqualTo("꼬리질문 A");
            softly.assertThat(fakeLlm.generateCalls.get())
                    .as("LLM 생성 호출 횟수 (답변 3번 동안 2회)").isEqualTo(2);
        });
    }

    /** 항상 꼬리질문 2개를 주는 Fake — 생성 호출 횟수를 센다. */
    static class TwoFollowUpLlmClient implements LlmClient {

        final AtomicInteger generateCalls = new AtomicInteger();

        @Override
        public FollowUpResult generateFollowUp(String prompt) {
            generateCalls.incrementAndGet();
            return new FollowUpResult(List.of("꼬리질문 A", "꼬리질문 B"), "이유", true);
        }

        @Override
        public EvaluationResult evaluate(String prompt) {
            throw new UnsupportedOperationException("이 테스트에서는 평가 미사용");
        }
    }

    @TestConfiguration
    static class TwoFollowUpConfig {
        @Bean
        @Primary
        TwoFollowUpLlmClient twoFollowUpLlmClient() {
            return new TwoFollowUpLlmClient();
        }
    }
}
