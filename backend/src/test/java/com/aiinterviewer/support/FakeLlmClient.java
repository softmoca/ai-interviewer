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
        return new EvaluationResult(
                List.of(new EvaluationResult.ConceptEvaluation(
                        "프로세스와 스레드", 4, 3, List.of("PCB", "context switching"),
                        "프로세스는 독립 메모리, 스레드는 공유 메모리를 가진다.")),
                "개념 이해는 양호하나 세부 메커니즘 설명의 깊이가 부족합니다.");
    }
}
