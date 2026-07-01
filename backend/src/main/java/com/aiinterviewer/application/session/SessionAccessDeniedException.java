package com.aiinterviewer.application.session;

/** 다른 사용자의 세션에 접근하려 한 경우(소유자 아님). */
public class SessionAccessDeniedException extends RuntimeException {

    public SessionAccessDeniedException() {
        super("해당 세션에 접근할 권한이 없습니다.");
    }
}
