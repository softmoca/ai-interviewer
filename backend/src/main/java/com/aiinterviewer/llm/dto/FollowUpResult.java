package com.aiinterviewer.llm.dto;

/**
 * 꼬리질문 생성 결과(구조화). LLM이 반환한 JSON을 검증 후 매핑한다
 * (docs/프롬프트-설계.md §3).
 *
 * @param followUpQuestion 생성된 꼬리질문
 * @param reason           그 질문을 던진 의도(검증하려는 개념)
 * @param withinPool       참고 질문 풀 범위 안인지 여부(범위 이탈 모니터링용)
 */
public record FollowUpResult(
        String followUpQuestion,
        String reason,
        boolean withinPool
) {
}
