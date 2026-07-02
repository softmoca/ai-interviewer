package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.SessionStatus;
import java.util.List;

/**
 * 답변 제출 결과. 답변 기록 + LLM이 생성한 꼬리질문 1~2개(패턴 B).
 *
 * @param answerLogId 기록된 사용자 답변 로그 식별자
 * @param answerSeq   답변의 세션 내 순서
 * @param followUps   생성된 꼬리질문들
 * @param status      세션 상태
 */
public record AnswerResult(
        Long answerLogId,
        int answerSeq,
        List<FollowUpView> followUps,
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
