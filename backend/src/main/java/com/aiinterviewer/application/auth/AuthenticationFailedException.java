package com.aiinterviewer.application.auth;

/**
 * 로그인 인증 실패(이메일 없음 또는 비밀번호 불일치).
 * 어느 쪽이 틀렸는지 구분해 노출하지 않는다(계정 존재 여부 노출 방지).
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
