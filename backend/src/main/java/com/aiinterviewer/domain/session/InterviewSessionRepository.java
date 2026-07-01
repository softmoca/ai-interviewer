package com.aiinterviewer.domain.session;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    /** 사용자별 면접 기록 조회 (최근 순) */
    List<InterviewSession> findByUserIdOrderByStartedAtDesc(Long userId);
}
