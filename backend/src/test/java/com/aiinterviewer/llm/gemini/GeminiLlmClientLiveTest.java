package com.aiinterviewer.llm.gemini;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * 실제 Gemini 호출 검증 — <b>OS 환경변수 GEMINI_API_KEY가 있을 때만</b> 실행된다(없으면 skip).
 * 실제 네트워크 호출/토큰을 사용하므로 기본 CI에서는 돌지 않는다(test-strategy.md).
 *
 * <p>로컬에서 돌리려면 셸에 키를 export 한 뒤 실행:
 * {@code GEMINI_API_KEY=... ./gradlew test --tests '*GeminiLlmClientLiveTest'}
 */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiLlmClientLiveTest {

    @Test
    @DisplayName("실제 Gemini가 구조화된 꼬리질문을 반환한다")
    void generatesFollowUpFromRealGemini() {
        GeminiProperties properties = new GeminiProperties(
                System.getenv("GEMINI_API_KEY"),
                "gemini-2.5-flash",
                "https://generativelanguage.googleapis.com/v1beta");
        GeminiLlmClient client = new GeminiLlmClient(properties, new ObjectMapper());

        String prompt = """
                너는 CS 기술 면접관이다. 지원자가 "프로세스와 스레드의 차이는 메모리 공유 여부입니다"라고
                답했다. 이해도를 확인하는 꼬리질문을 1~2개 만들어라.
                반드시 아래 JSON 형식으로만 답하라:
                {"follow_up_questions": ["..."], "reason": "...", "within_pool": true}
                """;

        FollowUpResult result = client.generateFollowUp(prompt);

        assertThat(result.followUpQuestions()).isNotEmpty();
        assertThat(result.followUpQuestions().get(0)).isNotBlank();
    }

    @Test
    @DisplayName("실제 Gemini가 구조화된 평가 리포트를 반환한다")
    void evaluatesFromRealGemini() {
        GeminiProperties properties = new GeminiProperties(
                System.getenv("GEMINI_API_KEY"),
                "gemini-2.5-flash",
                "https://generativelanguage.googleapis.com/v1beta");
        GeminiLlmClient client = new GeminiLlmClient(properties, new ObjectMapper());

        String prompt = """
                너는 CS 기술 면접 평가자다. 대화: 면접관 "프로세스와 스레드의 차이는?" /
                지원자 "프로세스는 독립 메모리, 스레드는 공유 메모리입니다." 를 평가하라.
                반드시 아래 JSON 형식으로만 답하라(점수 1~5 정수):
                {"evaluations":[{"concept":"...","accuracy":4,"depth":3,"missed_keywords":["..."],"model_answer":"..."}],"overall_comment":"..."}
                """;

        EvaluationResult result = client.evaluate(prompt);

        assertThat(result.evaluations()).isNotEmpty();
        assertThat(result.evaluations().get(0).concept()).isNotBlank();
    }
}
