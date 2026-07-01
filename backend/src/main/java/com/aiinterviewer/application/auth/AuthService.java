package com.aiinterviewer.application.auth;

import com.aiinterviewer.domain.user.PasswordEncryptor;
import com.aiinterviewer.domain.user.User;
import com.aiinterviewer.domain.user.UserRepository;
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

    public AuthService(UserRepository userRepository, PasswordEncryptor passwordEncryptor,
                       TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncryptor = passwordEncryptor;
        this.tokenProvider = tokenProvider;
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

    /** 현재 로그인 사용자 조회(인증 필터가 인증한 userId 기준). */
    @Transactional(readOnly = true)
    public MeResult getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthenticationFailedException::new);
        return new MeResult(user.getId(), user.getEmail(), user.getNickname());
    }
}
