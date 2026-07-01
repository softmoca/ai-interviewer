package com.aiinterviewer.adapter.security;

import com.aiinterviewer.domain.user.PasswordEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 도메인 포트 {@link PasswordEncryptor}의 구현(adapter). Spring Security의
 * {@link PasswordEncoder}(BCrypt)에 위임한다. Spring Security 타입은 이 어댑터에만 존재한다.
 */
@Component
public class BCryptPasswordEncryptor implements PasswordEncryptor {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordEncryptor(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encrypt(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encryptedPassword) {
        return passwordEncoder.matches(rawPassword, encryptedPassword);
    }
}
