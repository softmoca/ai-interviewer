package com.aiinterviewer.application.auth;

import com.aiinterviewer.domain.user.PasswordEncryptor;
import com.aiinterviewer.domain.user.User;
import com.aiinterviewer.domain.user.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 흐름 오케스트레이션(애플리케이션 계층). 판단(규칙)은 도메인/포트에 위임하고,
 * 여기서는 조회·트랜잭션·흐름 조립만 담당한다(SRP, 도메인 우선 — code-quality §3).
 *
 * <p>구체 구현(BCrypt, JWT)이 아니라 포트({@link PasswordEncryptor}, {@link TokenProvider})에
 * 의존한다(DIP).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncryptor passwordEncryptor;
    private final TokenProvider tokenProvider;
    /** 프로바이더 이름 → 검증기. 어댑터를 추가하면 자동으로 지원 프로바이더가 늘어난다(D38). */
    private final Map<String, SocialIdentityVerifier> socialVerifiers;

    public AuthService(UserRepository userRepository, PasswordEncryptor passwordEncryptor,
                       TokenProvider tokenProvider, List<SocialIdentityVerifier> socialVerifiers) {
        this.userRepository = userRepository;
        this.passwordEncryptor = passwordEncryptor;
        this.tokenProvider = tokenProvider;
        this.socialVerifiers = socialVerifiers.stream()
                .collect(Collectors.toMap(SocialIdentityVerifier::provider, Function.identity()));
    }

    /**
     * 회원가입. 이메일 중복이면 거부하고, 그 외 필드 검증은 도메인({@link User#register})이 수행한다.
     *
     * @return 생성된 사용자 식별자
     */
    @Transactional
    public Long signup(String email, String rawPassword, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        User user = User.register(email, rawPassword, nickname, passwordEncryptor);
        return userRepository.save(user).getId();
    }

    /** 로그인. 비밀번호 대조 판단은 도메인({@link User#authenticate})이 수행한다. */
    @Transactional(readOnly = true)
    public LoginResult login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(AuthenticationFailedException::new);
        if (!user.authenticate(rawPassword, passwordEncryptor)) {
            throw new AuthenticationFailedException();
        }
        String accessToken = tokenProvider.issue(user.getId());
        return new LoginResult(accessToken, user.getId(), user.getNickname());
    }

    /**
     * 소셜 로그인(결정사항 D38). 프로바이더 토큰을 검증해 신원을 얻고, 사용자를 찾거나(없으면 생성)
     * 이메일이 같은 기존 계정에 연동한 뒤 우리 액세스 토큰을 발급한다. 판단(토큰 검증·연동 규칙)은
     * 검증기 포트와 도메인({@link User})에 위임하고, 여기서는 조회·연동·발급 흐름만 조립한다.
     */
    @Transactional
    public LoginResult socialLogin(String provider, String credential) {
        SocialIdentityVerifier verifier = socialVerifiers.get(provider);
        if (verifier == null) {
            throw new SocialAuthenticationException("지원하지 않는 소셜 로그인입니다: " + provider);
        }
        SocialIdentity identity = verifier.verify(credential);
        if (!identity.emailVerified()) {
            // 이메일 기반 계정 연동의 안전장치 — 미검증 이메일로는 기존 계정을 가로챌 수 없다.
            throw new SocialAuthenticationException("이메일이 검증되지 않은 소셜 계정입니다.");
        }
        User user = findOrCreateSocialUser(provider, identity);
        String accessToken = tokenProvider.issue(user.getId());
        return new LoginResult(accessToken, user.getId(), user.getNickname());
    }

    private User findOrCreateSocialUser(String provider, SocialIdentity identity) {
        // 1) 이미 이 프로바이더로 연결된 사용자면 그대로 로그인.
        return userRepository.findByProviderAndProviderId(provider, identity.providerId())
                .orElseGet(() -> userRepository.findByEmail(identity.email())
                        // 2) 이메일이 같은 기존(예: 자체 로그인) 계정이면 소셜을 연동.
                        .map(existing -> {
                            existing.linkSocial(provider, identity.providerId());
                            return existing;
                        })
                        // 3) 처음 보는 사용자면 소셜 전용 계정 생성.
                        .orElseGet(() -> userRepository.save(User.registerSocial(
                                identity.email(), nicknameFrom(identity), provider, identity.providerId()))));
    }

    /** 표시 이름이 있으면 닉네임으로, 없으면 이메일 로컬 파트를 쓴다. */
    private static String nicknameFrom(SocialIdentity identity) {
        if (identity.name() != null && !identity.name().isBlank()) {
            return identity.name();
        }
        String email = identity.email();
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    /** 현재 로그인 사용자 조회(인증 필터가 인증한 userId 기준). */
    @Transactional(readOnly = true)
    public MeResult getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthenticationFailedException::new);
        return new MeResult(user.getId(), user.getEmail(), user.getNickname());
    }
}
