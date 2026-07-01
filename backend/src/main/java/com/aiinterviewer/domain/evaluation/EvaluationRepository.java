package com.aiinterviewer.domain.evaluation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    /** 세션의 평가 리포트 조회 */
    List<Evaluation> findBySessionId(Long sessionId);
}
