package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.SessionStatus;

/**
 * 답변 제출 결과. 답변 기록 + **다음에 제시할 꼬리질문 하나**(패턴 B, 순차 큐잉 — 결정사항 D36).
 * LLM이 한 답변에 2개를 주면 첫 번째만 지금 제시하고 두 번째는 세션에 보관했다가 다음 답변 때 낸다.
 *
 * @param answerLogId  기록된 사용자 답변 로그 식별자
 * @param answerSeq    답변의 세션 내 순서
 * @param nextQuestion 다음 꼬리질문(항상 하나). 종료 등으로 없으면 null
 * @param status       세션 상태
 */
public record AnswerResult(
        Long answerLogId,
        int answerSeq,
        FollowUpView nextQuestion,
        SessionStatus status
) {

    /**
     * 꼬리질문 표현.
     *
     * @param qaLogId 꼬리질문 로그 식별자
     * @param seq     세션 내 순서
     * @param content 꼬리질문 본문
     */
    public record FollowUpView(
            Long qaLogId,
            int seq,
            String content
    ) {
    }
}
