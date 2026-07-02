package com.aiinterviewer.application.evaluation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.aiinterviewer.application.auth.AuthService;
import com.aiinterviewer.application.session.SessionService;
import com.aiinterviewer.application.session.StartSessionCommand;
import com.aiinterviewer.application.session.StartSessionResult;
import com.aiinterviewer.domain.evaluation.EvaluationRepository;
import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
 * 평가 동시성 회귀 테스트 — 같은 세션에 대해 평가 요청이 <b>동시에 두 번</b> 들어와도
 * LLM 평가는 한 번만 수행되고 리포트는 한 세트만 저장되는지 검증한다(결정사항 D35).
 *
 * <p>느린 Fake LLM(평가 시 잠깐 대기)으로 두 트랜잭션의 임계 구간을 겹치게 만든다. 비관적 락이
 * 없으면 두 요청 모두 "평가 없음"을 보고 각자 생성해 중복이 생긴다(고쳐진 버그).
 */
@SpringBootTest
@Import(EvaluationConcurrencyIntegrationTest.SlowLlmConfig.class)
class EvaluationConcurrencyIntegrationTest {

    @Autowired
    AuthService authService;

    @Autowired
    SessionService sessionService;

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    EvaluationRepository evaluationRepository;

    @Autowired
    CountingSlowLlmClient fakeLlm;

    @Test
    @DisplayName("동시에 두 번 평가해도 LLM 호출 1회·평가 한 세트만 생성된다")
    void concurrentEvaluateCreatesOnlyOneReport() throws Exception {
        // 완료된 세션 준비 (start/complete는 LLM을 부르지 않는다)
        Long userId = authService.signup("concurrent-eval@test.com", "password1", "동시");
        StartSessionResult started = sessionService.startSession(userId,
                new StartSessionCommand(List.of("os"), false, null, null));
        Long sessionId = started.sessionId();
        sessionService.completeSession(userId, sessionId);

        // 두 요청을 동시에 발사
        int threads = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                gate.await();
                return evaluationService.evaluate(userId, sessionId);
            }));
        }
        gate.countDown();
        for (Future<?> future : futures) {
            future.get(20, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        assertSoftly(softly -> {
            softly.assertThat(fakeLlm.evaluateCalls.get()).as("실제 LLM 평가 호출 횟수").isEqualTo(1);
            softly.assertThat(evaluationRepository.findBySessionId(sessionId))
                    .as("저장된 평가 행 수(개념 1개만)").hasSize(1);
        });
    }

    /** 임계 구간을 겹치게 만드는 느린 Fake — 평가 호출 횟수도 센다. */
    static class CountingSlowLlmClient implements LlmClient {

        final AtomicInteger evaluateCalls = new AtomicInteger();

        @Override
        public FollowUpResult generateFollowUp(String prompt) {
            return new FollowUpResult(List.of("꼬리질문"), "이유", true);
        }

        @Override
        public EvaluationResult evaluate(String prompt) {
            evaluateCalls.incrementAndGet();
            try {
                Thread.sleep(300); // 두 요청의 임계 구간이 겹치도록 잠시 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new EvaluationResult(
                    List.of(new EvaluationResult.ConceptEvaluation("동시성 개념", 4, 3,
                            List.of("락"), "모범답안")),
                    "총평");
        }
    }

    @TestConfiguration
    static class SlowLlmConfig {
        @Bean
        @Primary
        CountingSlowLlmClient countingSlowLlmClient() {
            return new CountingSlowLlmClient();
        }
    }
}
