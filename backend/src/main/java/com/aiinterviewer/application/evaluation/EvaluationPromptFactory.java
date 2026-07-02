package com.aiinterviewer.application.evaluation;

import com.aiinterviewer.domain.session.QaLog;
import com.aiinterviewer.domain.session.QaRole;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 세션 평가 프롬프트 조립(애플리케이션 관심사). 전체 대화 이력을 평가자 지시와 함께 구성해
 * 구조화 JSON(개념별 점수/모범답안 + 총평)으로 받도록 지시한다(docs/프롬프트-설계.md §4).
 */
@Component
public class EvaluationPromptFactory {

    public String build(List<QaLog> transcript) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                너는 CS 기술 면접의 평가자다. 아래 면접 대화 전체를 읽고 지원자를 평가하라.

                [평가 기준]
                - 답변을 개념/주제 단위로 나눈다.
                - 각 개념에 대해:
                  - accuracy(정확성) 1~5점
                  - depth(깊이) 1~5점
                  - missed_keywords: 답변에서 언급했으면 좋았을 핵심 키워드
                  - model_answer: 해당 개념의 모범답안(간결하게)
                - overall_comment: 전반적인 강점/약점 한 문단.

                [대화 이력]
                """);
        for (QaLog log : transcript) {
            String speaker = log.getRole() == QaRole.INTERVIEWER ? "면접관" : "지원자";
            sb.append(speaker).append(": ").append(log.getContent()).append('\n');
        }
        sb.append("""

                반드시 아래 JSON 형식으로만 답하라(다른 텍스트 금지). 점수는 1~5 정수:
                {"evaluations":[{"concept":"...","accuracy":4,"depth":3,"missed_keywords":["..."],"model_answer":"..."}],"overall_comment":"..."}
                """);
        return sb.toString();
    }
}
