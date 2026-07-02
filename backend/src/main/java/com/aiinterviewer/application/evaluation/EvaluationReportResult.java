package com.aiinterviewer.application.evaluation;

import java.util.List;

/**
 * 세션 평가 리포트(애플리케이션 결과). 개념별 점수 목록 + 전반 총평.
 *
 * @param sessionId      세션 식별자
 * @param concepts       개념별 평가
 * @param overallComment 전반 총평
 */
public record EvaluationReportResult(
        Long sessionId,
        List<ConceptScore> concepts,
        String overallComment
) {

    /**
     * 개념 단위 점수.
     *
     * @param concept        개념/주제명
     * @param accuracy       정확성 1~5
     * @param depth          깊이 1~5
     * @param missedKeywords 놓친 키워드
     * @param modelAnswer    모범답안
     */
    public record ConceptScore(
            String concept,
            int accuracy,
            int depth,
            List<String> missedKeywords,
            String modelAnswer
    ) {
    }
}
