package com.aiinterviewer.domain.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByPhaseOrderBySortOrder(CategoryPhase phase);

    /** 노출 순서대로 전체 카테고리 조회 — 세션 설정 UI의 선택지 */
    List<Category> findAllByOrderBySortOrderAsc();
}
