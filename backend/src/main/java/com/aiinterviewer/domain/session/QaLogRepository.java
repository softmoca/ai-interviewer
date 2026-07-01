package com.aiinterviewer.domain.session;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QaLogRepository extends JpaRepository<QaLog, Long> {

    /** 세션의 전체 대화 이력 조회 (순서대로) — 꼬리질문/평가 시 컨텍스트 구성에 사용 */
    List<QaLog> findBySessionIdOrderBySeqAsc(Long sessionId);
}
