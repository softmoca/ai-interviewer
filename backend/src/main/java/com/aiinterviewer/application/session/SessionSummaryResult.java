package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.SessionStatus;
import java.time.LocalDateTime;

/**
 * 세션 목록 항목(사용자별 면접 기록). 목록/다시보기 진입용 요약이며, 상세(대화 이력)는
 * {@code GET /api/sessions/{id}}로 조회한다.
 *
 * @param sessionId 세션 식별자
 * @param status    상태(IN_PROGRESS/COMPLETED/ABANDONED)
 * @param startedAt 시작 시각
 * @param endedAt   종료 시각(미종료면 null)
 */
public record SessionSummaryResult(
        Long sessionId,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
}
