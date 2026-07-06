// 구글 Identity Services(GIS) 로더 + 설정 — 결정사항 D38.
// 프론트가 GIS로 사용자를 인증해 '구글 ID 토큰(JWT)'을 받고, 이를 백엔드로 보내 검증·로그인한다.
// 클라이언트 ID는 공개값이라 프론트 번들에 노출돼도 안전하다(비밀이 아님).

const GIS_SRC = 'https://accounts.google.com/gsi/client';

/** 설정된 구글 클라이언트 ID(.env의 VITE_GOOGLE_CLIENT_ID). 없으면 null → 버튼을 숨긴다(폴백). */
export function googleClientId(): string | null {
  const id = import.meta.env.VITE_GOOGLE_CLIENT_ID;
  return id && id.trim() ? id.trim() : null;
}

/** GIS 콜백이 돌려주는 자격증명. credential이 백엔드로 보낼 ID 토큰이다. */
export interface GoogleCredentialResponse {
  credential: string;
}

interface GoogleAccountsId {
  initialize(config: {
    client_id: string;
    callback: (response: GoogleCredentialResponse) => void;
  }): void;
  renderButton(parent: HTMLElement, options: Record<string, unknown>): void;
}

declare global {
  interface Window {
    google?: { accounts?: { id?: GoogleAccountsId } };
  }
}

let loadPromise: Promise<GoogleAccountsId> | null = null;

/** GIS 스크립트를 한 번만 로드하고 google.accounts.id를 반환한다(중복 로드 방지). */
export function loadGoogleIdentity(): Promise<GoogleAccountsId> {
  if (loadPromise) return loadPromise;
  loadPromise = new Promise((resolve, reject) => {
    const existing = window.google?.accounts?.id;
    if (existing) {
      resolve(existing);
      return;
    }
    const script = document.createElement('script');
    script.src = GIS_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => {
      const id = window.google?.accounts?.id;
      if (id) resolve(id);
      else reject(new Error('구글 로그인 초기화에 실패했습니다.'));
    };
    script.onerror = () => reject(new Error('구글 로그인 스크립트를 불러오지 못했습니다.'));
    document.head.appendChild(script);
  });
  return loadPromise;
}
