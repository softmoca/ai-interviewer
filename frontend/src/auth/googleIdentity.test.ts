import { afterEach, describe, expect, it, vi } from 'vitest';
import { googleClientId } from './googleIdentity';

// 스모크 — 클라이언트 ID 설정/미설정 감지. 미설정이면 null → 버튼 숨김 폴백(D38).
afterEach(() => {
  vi.unstubAllEnvs();
});

describe('googleClientId', () => {
  it('설정되면 앞뒤 공백을 제거한 값을 반환한다', () => {
    vi.stubEnv('VITE_GOOGLE_CLIENT_ID', '  abc.apps.googleusercontent.com  ');
    expect(googleClientId()).toBe('abc.apps.googleusercontent.com');
  });

  it('미설정(빈 값)이면 null을 반환한다(구글 로그인 버튼 숨김 폴백)', () => {
    vi.stubEnv('VITE_GOOGLE_CLIENT_ID', '');
    expect(googleClientId()).toBeNull();
  });
});
