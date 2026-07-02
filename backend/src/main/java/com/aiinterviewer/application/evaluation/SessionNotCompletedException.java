package com.aiinterviewer.application.evaluation;

/** 아직 종료되지 않은 세션을 평가하려 한 경우(평가는 완료된 세션에만 허용). */
public class SessionNotCompletedException extends RuntimeException {

    public SessionNotCompletedException(Long sessionId) {
        super("완료된 세션만 평가할 수 있습니다. 먼저 세션을 종료하세요: " + sessionId);
    }
}
