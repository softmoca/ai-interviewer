package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.QaRole;
import com.aiinterviewer.domain.session.SessionStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 세션 상세(설정·상태 + 대화 이력) 조회 결과.
 *
 * @param sessionId  세션 식별자
 * @param status     세션 상태
 * @param startedAt  시작 시각
 * @param endedAt    종료 시각(미종료면 null)
 * @param transcript 대화 이력(순서대로)
 */
public record SessionDetailResult(
        Long sessionId,
        SessionStatus status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<QaLogEntry> transcript
) {

    /**
     * 대화 이력 한 줄.
     *
     * @param seq      순서
     * @param role     발화 주체(INTERVIEWER/USER)
     * @param content  내용
     * @param followUp 꼬리질문 여부
     */
    public record QaLogEntry(
            int seq,
            QaRole role,
            String content,
            boolean followUp
    ) {
    }
}
