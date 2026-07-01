package com.aiinterviewer.adapter.web.session;

import java.util.List;

/**
 * 세션 시작 요청 바디. 상세 검증(카테고리 필수/난이도 범위 등)은 도메인이 수행한다.
 *
 * @param categorySlugs 선택 카테고리 슬러그들(예: ["os"]). 전체 랜덤이면 생략 가능
 * @param randomAll     전체 카테고리 랜덤 여부
 * @param questionCount 목표 질문 수(선택)
 * @param difficulty    난이도 1~3(선택)
 */
public record StartSessionRequest(
        List<String> categorySlugs,
        boolean randomAll,
        Integer questionCount,
        Integer difficulty
) {
}
