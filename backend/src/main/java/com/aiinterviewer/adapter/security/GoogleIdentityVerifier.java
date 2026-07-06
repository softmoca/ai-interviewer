package com.aiinterviewer.adapter.security;

import com.aiinterviewer.application.auth.SocialAuthenticationException;
import com.aiinterviewer.application.auth.SocialIdentity;
import com.aiinterviewer.application.auth.SocialIdentityVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import org.springframework.stereotype.Component;

/**
 * 구글 ID 토큰 검증 어댑터(결정사항 D38). {@link GoogleIdTokenVerifier}로 구글이 서명한 ID 토큰의
 * 서명·발급자·만료·대상(client-id)을 검증하고, 신뢰할 수 있는 신원({@link SocialIdentity})으로 옮긴다.
 *
 * <p>프론트가 GIS로 받은 ID 토큰을 백엔드가 여기서 검증하므로, 클라이언트 시크릿은 필요 없다.
 */
@Component
public class GoogleIdentityVerifier implements SocialIdentityVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityVerifier(GoogleOAuthProperties properties) {
        // 대상(audience)을 우리 client-id로 고정 → 다른 앱을 위해 발급된 토큰은 거부된다.
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(properties.clientId()))
                .build();
    }

    @Override
    public String provider() {
        return "google";
    }

    @Override
    public SocialIdentity verify(String credential) {
        GoogleIdToken idToken = parse(credential);
        if (idToken == null) {
            // 서명 불일치·만료·대상 불일치 등은 모두 여기로 온다(원인은 노출하지 않는다).
            throw new SocialAuthenticationException("유효하지 않은 구글 토큰입니다.");
        }
        Payload payload = idToken.getPayload();
        return new SocialIdentity(
                payload.getSubject(),
                payload.getEmail(),
                Boolean.TRUE.equals(payload.getEmailVerified()),
                (String) payload.get("name"));
    }

    private GoogleIdToken parse(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new SocialAuthenticationException("구글 토큰이 비어 있습니다.");
        }
        try {
            return verifier.verify(credential);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new SocialAuthenticationException("구글 토큰 검증에 실패했습니다.");
        }
    }
}
