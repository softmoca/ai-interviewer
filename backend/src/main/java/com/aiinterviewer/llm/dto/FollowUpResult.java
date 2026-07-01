package com.aiinterviewer.llm.dto;

import java.util.List;

/**
 * 꼬리질문 생성 결과(구조화). LLM이 반환한 JSON을 검증 후 매핑한다(docs/프롬프트-설계.md §3).
 * 프로바이더에 독립적인 계약이라 직렬화 어노테이션을 두지 않는다.
 *
 * @param followUpQuestions 생성된 꼬리질문 1~2개
 * @param reason            질문 의도(검증하려는 개념)
 * @param withinPool        참고 질문 풀 범위 안인지(범위 이탈 모니터링용)
 */
public record FollowUpResult(
        List<String> followUpQuestions,
        String reason,
        boolean withinPool
) {
}
