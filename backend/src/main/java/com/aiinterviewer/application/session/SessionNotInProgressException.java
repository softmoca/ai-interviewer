package com.aiinterviewer.application.session;

/** 진행 중이 아닌 세션에 답변을 제출하려 한 경우. */
public class SessionNotInProgressException extends RuntimeException {

    public SessionNotInProgressException(Long sessionId) {
        super("진행 중인 세션이 아닙니다: " + sessionId);
    }
}
