package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.session.SessionStatus;

/**
 * 세션 시작 결과. 생성된 세션과 서빙된 첫 질문을 담는다.
 *
 * @param sessionId     생성된 세션 식별자
 * @param status        세션 상태(시작 직후 IN_PROGRESS)
 * @param firstQuestion 서빙된 첫 질문
 */
public record StartSessionResult(
        Long sessionId,
        SessionStatus status,
        QuestionView firstQuestion
) {

    /**
     * 질문 표현.
     *
     * @param questionId 질문 식별자(DB 질문)
     * @param content    질문 본문
     * @param difficulty 난이도
     * @param seq        세션 내 순서
     */
    public record QuestionView(
            Long questionId,
            String content,
            int difficulty,
            int seq
    ) {
    }
}
