package com.aiinterviewer.domain.user;

/**
 * 비밀번호 암호화 포트(도메인 소유 추상화).
 *
 * <p>도메인은 "비밀번호가 암호화되어 저장되고 원문과 대조된다"는 규칙만 알고, 구체적인
 * 암호화 방식(BCrypt 등)은 모른다. 실제 구현은 adapter 계층에 둔다(DIP, code-quality §1·§2).
 * 이렇게 하면 도메인이 Spring Security 타입에 오염되지 않는다(도메인 침범 방지 §1.1/§1.2).
 */
public interface PasswordEncryptor {

    /** 원문 비밀번호를 암호화한다. */
    String encrypt(String rawPassword);

    /** 원문 비밀번호가 암호화된 값과 일치하는지 확인한다. */
    boolean matches(String rawPassword, String encryptedPassword);
}
