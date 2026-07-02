package com.aiinterviewer.domain.session;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    /** 사용자별 면접 기록 조회 (최근 순) */
    List<InterviewSession> findByUserIdOrderByStartedAtDesc(Long userId);

    /**
     * 세션 행에 쓰기 잠금(SELECT ... FOR UPDATE)을 걸고 조회한다. 같은 세션을 동시에 평가하는
     * 요청을 직렬화해 평가가 중복 생성되는 것을 막는다(결정사항 D35). 반드시 트랜잭션 안에서 사용.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from InterviewSession s where s.id = :id")
    Optional<InterviewSession> findByIdForUpdate(@Param("id") Long id);
}
