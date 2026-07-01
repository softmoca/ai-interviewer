package com.aiinterviewer.application.session;

/** 존재하지 않는 세션을 조회/조작하려 한 경우. */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(Long sessionId) {
        super("세션을 찾을 수 없습니다: " + sessionId);
    }
}
