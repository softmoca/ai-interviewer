package com.aiinterviewer.application.session;

import java.util.List;

/**
 * 세션 시작 명령(애플리케이션 입력). 웹 요청 DTO와 분리한다.
 *
 * @param categorySlugs 선택 카테고리 슬러그들(전체 랜덤이면 무시)
 * @param randomAll     전체 카테고리 랜덤 여부
 * @param questionCount 목표 질문 수(nullable)
 * @param difficulty    난이도 1~3(nullable = 혼합)
 */
public record StartSessionCommand(
        List<String> categorySlugs,
        boolean randomAll,
        Integer questionCount,
        Integer difficulty
) {
}
