package com.aiinterviewer.application.auth;

/** 이미 가입된 이메일로 다시 가입을 시도한 경우. */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("이미 사용 중인 이메일입니다: " + email);
    }
}
