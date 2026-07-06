import { apiRequest } from './client';
import type { MeResponse, SignupResponse, TokenResponse } from './types';

/** 회원가입 */
export function signup(email: string, password: string, nickname: string): Promise<SignupResponse> {
  return apiRequest<SignupResponse>('/auth/signup', {
    method: 'POST',
    body: { email, password, nickname },
  });
}

/** 로그인 → 액세스 토큰 발급 */
export function login(email: string, password: string): Promise<TokenResponse> {
  return apiRequest<TokenResponse>('/auth/login', {
    method: 'POST',
    body: { email, password },
  });
}

/** 구글 소셜 로그인 → 구글 ID 토큰을 백엔드가 검증하고 우리 액세스 토큰 발급(결정사항 D38) */
export function googleLogin(idToken: string): Promise<TokenResponse> {
  return apiRequest<TokenResponse>('/auth/google', {
    method: 'POST',
    body: { idToken },
  });
}

/** 현재 로그인 사용자 조회(보호 자원) */
export function fetchMe(): Promise<MeResponse> {
  return apiRequest<MeResponse>('/auth/me');
}
