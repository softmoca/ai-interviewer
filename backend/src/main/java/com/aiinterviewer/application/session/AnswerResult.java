package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.SessionStatus;

/**
 * 답변 제출 결과. (이번 슬라이스는 LLM 꼬리질문 없이 기록만 — 다음 슬라이스에서 확장)
 *
 * @param qaLogId 기록된 답변 로그 식별자
 * @param seq     세션 내 순서
 * @param status  세션 상태
 */
public record AnswerResult(
        Long qaLogId,
        int seq,
        SessionStatus status
) {
}
