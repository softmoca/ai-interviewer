package com.aiinterviewer.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** 소셜 프로바이더 신원으로 사용자를 찾는다(소셜 로그인 — 결정사항 D38). */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
