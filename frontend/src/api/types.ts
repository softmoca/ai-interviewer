// 백엔드 응답 계약 (docs/아키텍처.md 4.5 인증 API와 정합)

export interface SignupResponse {
  id: number;
  email: string;
  nickname: string;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  nickname: string;
}

export interface MeResponse {
  userId: number;
  email: string;
  nickname: string;
}

/** GlobalExceptionHandler의 표준 에러 바디 */
export interface ErrorResponse {
  code: string;
  message: string;
}
