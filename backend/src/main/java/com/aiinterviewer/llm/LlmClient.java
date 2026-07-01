package com.aiinterviewer.llm;

import com.aiinterviewer.llm.dto.EvaluationResult;
import com.aiinterviewer.llm.dto.FollowUpResult;

/**
 * LLM 연동 추상화(결정사항 D18).
 *
 * <p>도메인 서비스는 구체적인 프로바이더(Gemini/Claude/GPT ...)가 아니라 이 인터페이스에만
 * 의존한다. 프로바이더 교체 시 구현체만 갈아끼우면 되도록 특정 SDK에 코드가 묶이지 않게 한다.
 *
 * <p>모든 응답은 구조화(JSON)로 받아 검증한다(CLAUDE.md 규칙 5). 프롬프트 규칙은
 * docs/프롬프트-설계.md 참고. 실제 구현은 M2에서 채운다.
 */
public interface LlmClient {

    /**
     * 사용자 답변 + 카테고리 질문 풀(참고자료)을 바탕으로 꼬리질문을 생성한다(패턴 B).
     *
     * @param prompt 대화 이력·참고 질문 풀을 포함해 조립된 최종 프롬프트
     * @return 구조화된 꼬리질문 결과
     */
    FollowUpResult generateFollowUp(String prompt);

    /**
     * 세션 종료 후 전체 대화 이력을 바탕으로 평가 리포트를 생성한다.
     *
     * @param prompt 전체 대화 이력을 포함해 조립된 최종 프롬프트
     * @return 구조화된 평가 결과
     */
    EvaluationResult evaluate(String prompt);
}
