package com.aiinterviewer.llm.dto;

import java.util.List;

/**
 * 세션 평가 결과(구조화). LLM이 반환한 JSON을 검증 후 매핑한다
 * (docs/프롬프트-설계.md §4). 점수는 5점 척도(결정사항 D10).
 *
 * @param evaluations    개념별 평가 목록
 * @param overallComment 전반적 강점/약점 총평
 */
public record EvaluationResult(
        List<ConceptEvaluation> evaluations,
        String overallComment
) {

    /**
     * 개념 단위 평가.
     *
     * @param concept        개념/주제명
     * @param accuracy       정확성 1~5
     * @param depth          깊이 1~5
     * @param missedKeywords 놓친 핵심 키워드
     * @param modelAnswer    모범답안(간결)
     */
    public record ConceptEvaluation(
            String concept,
            int accuracy,
            int depth,
            List<String> missedKeywords,
            String modelAnswer
    ) {
    }
}
