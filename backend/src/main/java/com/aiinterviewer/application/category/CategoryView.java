package com.aiinterviewer.application.category;

import com.aiinterviewer.domain.category.CategoryPhase;

/**
 * 카테고리 조회 결과(세션 설정 UI 선택지). 슬러그로 세션을 시작한다.
 *
 * @param slug  식별 슬러그(예: os)
 * @param name  표시 이름(예: 운영체제)
 * @param phase MVP(A안) / EXPANSION(B안)
 */
public record CategoryView(
        String slug,
        String name,
        CategoryPhase phase
) {
}
