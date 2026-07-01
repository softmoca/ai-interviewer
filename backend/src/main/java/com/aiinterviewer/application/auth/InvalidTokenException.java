package com.aiinterviewer.application.auth;

/** 유효하지 않은(위조/만료/형식 오류) 인증 토큰. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
