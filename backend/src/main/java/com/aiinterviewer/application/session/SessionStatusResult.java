package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.SessionStatus;
import java.time.LocalDateTime;

/**
 * 세션 상태 결과(종료 등 상태 전이 후 반환).
 *
 * @param sessionId 세션 식별자
 * @param status    현재 상태
 * @param endedAt   종료 시각(미종료면 null)
 */
public record SessionStatusResult(
        Long sessionId,
        SessionStatus status,
        LocalDateTime endedAt
) {
}
