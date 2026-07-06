import { useEffect, useRef, useState } from 'react';
import { googleClientId, loadGoogleIdentity } from './googleIdentity';

interface Props {
  /** 구글이 발급한 ID 토큰을 받는다(백엔드로 전송용). */
  onCredential: (idToken: string) => void;
  /** 스크립트 로드 실패 등 오류 안내(선택). */
  onError?: (message: string) => void;
}

/**
 * '구글로 로그인' 버튼(결정사항 D38). GIS 버튼을 렌더하고, 로그인 시 ID 토큰을 onCredential으로 넘긴다.
 * 클라이언트 ID 미설정 또는 스크립트 로드 실패 시 아무것도 렌더하지 않는다(그레이스풀 폴백 — 자체
 * 로그인은 그대로 동작).
 */
export function GoogleLoginButton({ onCredential, onError }: Props) {
  const clientId = googleClientId();
  const containerRef = useRef<HTMLDivElement>(null);
  const [failed, setFailed] = useState(false);
  // 최신 콜백을 참조해 effect를 재실행하지 않는다.
  const onCredentialRef = useRef(onCredential);
  onCredentialRef.current = onCredential;
  const onErrorRef = useRef(onError);
  onErrorRef.current = onError;

  useEffect(() => {
    if (!clientId) return;
    let cancelled = false;
    loadGoogleIdentity()
      .then((googleId) => {
        if (cancelled || !containerRef.current) return;
        googleId.initialize({
          client_id: clientId,
          callback: (response) => onCredentialRef.current(response.credential),
        });
        googleId.renderButton(containerRef.current, {
          type: 'standard',
          theme: 'outline',
          size: 'large',
          text: 'continue_with',
          width: 320,
        });
      })
      .catch((e: unknown) => {
        if (cancelled) return;
        setFailed(true);
        onErrorRef.current?.(e instanceof Error ? e.message : '구글 로그인을 사용할 수 없습니다.');
      });
    return () => {
      cancelled = true;
    };
  }, [clientId]);

  if (!clientId || failed) return null;
  return <div ref={containerRef} className="google-login" />;
}
