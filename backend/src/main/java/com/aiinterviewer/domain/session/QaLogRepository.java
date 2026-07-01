package com.aiinterviewer.domain.session;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QaLogRepository extends JpaRepository<QaLog, Long> {

    /** 세션의 전체 대화 이력 조회 (순서대로) — 꼬리질문/평가 시 컨텍스트 구성에 사용 */
    List<QaLog> findBySessionIdOrderBySeqAsc(Long sessionId);

    /** 세션의 로그 개수 — 다음 순서(seq) 계산에 사용 */
    long countBySessionId(Long sessionId);
}
