package com.aiinterviewer.llm.gemini;

import com.aiinterviewer.llm.LlmCallException;
import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.LlmNotConfiguredException;
import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * {@link LlmClient}의 Google Gemini 구현(결정사항 D18/D26 — gemini-2.5-flash, generateContent).
 *
 * <p>프롬프트(면접관 지시 + 질문 풀 + 대화 이력)를 받아 Gemini에 보내고, 구조화(JSON) 응답을
 * {@link FollowUpResult}로 파싱한다. jjwt처럼 프로바이더 종속 코드는 이 클래스에만 존재한다.
 * 키가 없으면 기동은 정상이되 호출 시 {@link LlmNotConfiguredException}으로 명확히 알린다.
 */
@Component
public class GeminiLlmClient implements LlmClient {

    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiLlmClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(properties.baseUrl()).build();
    }

    @Override
    public FollowUpResult generateFollowUp(String prompt) {
        GeminiFollowUpPayload payload = parse(requestJson(prompt), GeminiFollowUpPayload.class);
        List<String> questions = payload.followUpQuestions() == null ? List.of()
                : payload.followUpQuestions().stream()
                        .filter(q -> q != null && !q.isBlank())
                        .limit(2)
                        .toList();
        if (questions.isEmpty()) {
            throw new LlmCallException("LLM이 꼬리질문을 반환하지 않았습니다.");
        }
        boolean withinPool = payload.withinPool() == null || payload.withinPool();
        return new FollowUpResult(questions, payload.reason(), withinPool);
    }

    @Override
    public EvaluationResult evaluate(String prompt) {
        GeminiEvaluationPayload payload = parse(requestJson(prompt), GeminiEvaluationPayload.class);
        List<EvaluationResult.ConceptEvaluation> evaluations = payload.evaluations() == null ? List.of()
                : payload.evaluations().stream()
                        .filter(e -> e != null && e.concept() != null && !e.concept().isBlank())
                        .map(e -> new EvaluationResult.ConceptEvaluation(e.concept(), e.accuracy(),
                                e.depth(), e.missedKeywords() == null ? List.of() : e.missedKeywords(),
                                e.modelAnswer()))
                        .toList();
        if (evaluations.isEmpty()) {
            throw new LlmCallException("LLM이 평가 결과를 반환하지 않았습니다.");
        }
        return new EvaluationResult(evaluations, payload.overallComment());
    }

    /** Gemini generateContent 호출 → 응답 텍스트(JSON 문자열) 추출. */
    private String requestJson(String prompt) {
        if (!properties.hasApiKey()) {
            throw new LlmNotConfiguredException(
                    "GEMINI_API_KEY가 설정되지 않았습니다. backend/.env에 키를 넣어주세요.");
        }
        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))),
                new GeminiRequest.GenerationConfig("application/json"));
        try {
            GeminiResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", properties.model())
                    .header("x-goog-api-key", properties.apiKey())
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);
            return extractText(response);
        } catch (RestClientException e) {
            throw new LlmCallException("Gemini 호출에 실패했습니다.", e);
        }
    }

    private String extractText(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new LlmCallException("Gemini 응답이 비어 있습니다.");
        }
        GeminiResponse.Candidate candidate = response.candidates().get(0);
        if (candidate.content() == null || candidate.content().parts() == null
                || candidate.content().parts().isEmpty()) {
            throw new LlmCallException("Gemini 응답에 내용이 없습니다.");
        }
        return candidate.content().parts().get(0).text();
    }

    private <T> T parse(String json, Class<T> type) {
        try {
            return objectMapper.readValue(stripCodeFence(json), type);
        } catch (Exception e) {
            throw new LlmCallException("Gemini 응답 JSON 파싱에 실패했습니다.", e);
        }
    }

    /** 모델이 ```json ... ``` 코드펜스로 감싼 경우를 방어적으로 제거한다. */
    private String stripCodeFence(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.strip();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "");
        }
        return trimmed.strip();
    }

    // --- Gemini REST 요청/응답 DTO (이 어댑터 내부 전용) ---

    private record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {
        record Content(List<Part> parts) {
        }

        record Part(String text) {
        }

        record GenerationConfig(String responseMimeType) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<Candidate> candidates) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Candidate(Content content) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Content(List<Part> parts) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        record Part(String text) {
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiFollowUpPayload(
            @JsonProperty("follow_up_questions") List<String> followUpQuestions,
            String reason,
            @JsonProperty("within_pool") Boolean withinPool
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiEvaluationPayload(
            List<ConceptPayload> evaluations,
            @JsonProperty("overall_comment") String overallComment
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        record ConceptPayload(
                String concept,
                int accuracy,
                int depth,
                @JsonProperty("missed_keywords") List<String> missedKeywords,
                @JsonProperty("model_answer") String modelAnswer
        ) {
        }
    }
}
