package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.InterviewSession;
import com.aiinterviewer.domain.session.InterviewSessionRepository;
import org.springframework.stereotype.Component;

/**
 * 세션 조회 + 소유권(인가) 검증을 한곳에 모은다. 여러 유스케이스(세션/평가)에서 재사용해
 * 동일한 인가 규칙이 흩어지지 않게 한다(AP-6 회피). id 비교는 애플리케이션 관심사(도메인 §1.3).
 */
@Component
public class SessionAccessGuard {

    private final InterviewSessionRepository sessionRepository;

    public SessionAccessGuard(InterviewSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /** 세션을 조회하고 요청자가 소유자인지 검증한다. 아니면 404/403. */
    public InterviewSession getOwned(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
        if (!session.getUser().getId().equals(userId)) {
            throw new SessionAccessDeniedException();
        }
        return session;
    }
}
