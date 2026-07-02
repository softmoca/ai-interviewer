package com.aiinterviewer.support;

import com.aiinterviewer.llm.LlmClient;
import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;
import java.util.List;

/**
 * 테스트용 가짜 LLM(테스트 전략: Fake > Mock). 키 없이 결정론적으로 동작해, 세션 흐름 통합
 * 테스트가 실제 Gemini 호출 없이 꼬리질문 생성까지 검증할 수 있게 한다(test-strategy.md).
 */
public class FakeLlmClient implements LlmClient {

    @Override
    public FollowUpResult generateFollowUp(String prompt) {
        return new FollowUpResult(
                List.of("방금 답변을 조금 더 구체적으로 설명해 주시겠어요?"),
                "이해도 확인",
                true);
    }

    @Override
    public EvaluationResult evaluate(String prompt) {
        throw new UnsupportedOperationException("평가는 다음 슬라이스");
    }
}
