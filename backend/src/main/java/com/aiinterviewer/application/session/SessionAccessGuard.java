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
        return verifyOwner(userId, sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId)));
    }

    /**
     * {@link #getOwned}와 동일하되 세션 행에 <b>쓰기 잠금</b>을 걸어 조회한다. 동일 세션에 대한
     * 동시 요청을 직렬화해야 하는 유스케이스(평가 생성 등)에서 사용한다(결정사항 D35).
     * 트랜잭션 안에서만 호출해야 잠금이 유지된다.
     */
    public InterviewSession getOwnedForUpdate(Long userId, Long sessionId) {
        return verifyOwner(userId, sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId)));
    }

    private InterviewSession verifyOwner(Long userId, InterviewSession session) {
        if (!session.getUser().getId().equals(userId)) {
            throw new SessionAccessDeniedException();
        }
        return session;
    }
}
