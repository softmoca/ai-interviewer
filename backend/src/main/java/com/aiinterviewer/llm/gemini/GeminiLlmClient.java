package com.aiinterviewer.llm.gemini;

import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;
import org.springframework.stereotype.Component;

/**
 * {@link LlmClient}의 Google Gemini 구현(결정사항 D18 — 개발용 gemini-2.5-flash).
 *
 * <p>현재는 뼈대 단계라 실제 HTTP 호출/JSON 파싱은 비워둔다. M2에서:
 * <ol>
 *   <li>Gemini generateContent API 호출(HTTP)</li>
 *   <li>응답 JSON을 {@link FollowUpResult}/{@link EvaluationResult}로 검증·매핑</li>
 * </ol>
 * 을 채운다. 프로바이더 교체 시 이 클래스만 대체하면 된다.
 */
@Component
public class GeminiLlmClient implements LlmClient {

    private final GeminiProperties properties;

    public GeminiLlmClient(GeminiProperties properties) {
        this.properties = properties;
    }

    @Override
    public FollowUpResult generateFollowUp(String prompt) {
        // TODO(M2): Gemini API 호출 + 구조화 응답 파싱
        throw new UnsupportedOperationException("Gemini 꼬리질문 생성 미구현 (M2)");
    }

    @Override
    public EvaluationResult evaluate(String prompt) {
        // TODO(M2): Gemini API 호출 + 구조화 응답 파싱
        throw new UnsupportedOperationException("Gemini 평가 생성 미구현 (M2)");
    }
}
