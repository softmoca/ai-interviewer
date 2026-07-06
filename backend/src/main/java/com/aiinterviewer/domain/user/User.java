package com.aiinterviewer.domain.user;

import com.aiinterviewer.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 서비스 사용자. MVP는 자체 로그인(Spring Security + JWT), 2차에 소셜 로그인 확장
 * (결정사항 D9). {@code provider}/{@code providerId}는 소셜 로그인용 예약 필드.
 *
 * <p>가입/인증 규칙은 이 도메인 객체가 소유한다(domain-design.md §3). 비밀번호 암호화는
 * {@link PasswordEncryptor} 포트에 위임하여 도메인이 암호화 구현을 모르게 한다.
 */
@Getter
@Entity
@Table(name = "users") // 'user'는 일부 DB의 예약어라 회피
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt 등으로 암호화된 비밀번호. 외부에 노출하지 않는다(getter 미생성). 소셜 로그인 사용자는 null 가능. */
    @Getter(AccessLevel.NONE)
    @Column
    private String passwordHash;

    @Column(nullable = false)
    private String nickname;

    /** (2차) 소셜 로그인 프로바이더 (예: google, kakao) */
    private String provider;

    /** (2차) 프로바이더 내 사용자 식별자 */
    private String providerId;

    private User(String email, String passwordHash, String nickname) {
        this.email = requireNotBlank(email, "email");
        this.passwordHash = passwordHash;
        this.nickname = requireNotBlank(nickname, "nickname");
    }

    /**
     * 자체 로그인 사용자를 가입시킨다. 원문 비밀번호는 즉시 암호화되어 저장되며, 원문은
     * 도메인에 남지 않는다. 필수값(email/password/nickname)이 비면 생성을 거부한다.
     */
    public static User register(String email, String rawPassword, String nickname,
                                PasswordEncryptor passwordEncryptor) {
        requireNotBlank(rawPassword, "password");
        return new User(email, passwordEncryptor.encrypt(rawPassword), nickname);
    }

    /**
     * 소셜 로그인 사용자를 가입시킨다(결정사항 D38). 비밀번호가 없으므로 자체 로그인은 불가하고
     * (해당 프로바이더로만 로그인), 프로바이더 신원({@code provider}/{@code providerId})을 함께 저장한다.
     */
    public static User registerSocial(String email, String nickname, String provider, String providerId) {
        User user = new User(email, null, nickname);
        user.linkSocial(provider, providerId);
        return user;
    }

    /**
     * 기존 사용자에 소셜 프로바이더 신원을 연결한다(예: 이메일/비번 계정에 구글 연동). 이미 다른
     * 프로바이더에 연결돼 있으면 거부해 계정이 조용히 가로채이지 않게 한다(Tell, Don't Ask).
     */
    public void linkSocial(String provider, String providerId) {
        requireNotBlank(provider, "provider");
        requireNotBlank(providerId, "providerId");
        if (this.provider != null && !this.provider.equals(provider)) {
            throw new IllegalStateException("이미 다른 소셜 계정에 연결된 사용자입니다.");
        }
        this.provider = provider;
        this.providerId = providerId;
    }

    /**
     * 원문 비밀번호가 이 사용자의 저장된 비밀번호와 일치하는지 확인한다(Tell, Don't Ask —
     * 해시를 밖으로 꺼내지 않고 대조 판단을 사용자 객체가 수행).
     */
    public boolean authenticate(String rawPassword, PasswordEncryptor passwordEncryptor) {
        if (passwordHash == null) {
            return false; // 비밀번호가 없는(예: 소셜 전용) 사용자는 자체 로그인 불가
        }
        return passwordEncryptor.matches(rawPassword, passwordHash);
    }

    private static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "은(는) 필수입니다.");
        }
        return value;
    }
}
