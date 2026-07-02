// 백엔드 REST 호출을 담당하는 단일 진입점. 컴포넌트는 fetch를 직접 쓰지 않고 이 계층을 쓴다.
// base URL은 환경변수(VITE_API_BASE_URL). 미설정 시 '/api'(Vite dev 프록시).

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

/** HTTP 오류를 상태코드·백엔드 에러코드와 함께 전달하는 예외 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string | null,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type TokenProvider = () => string | null;
let currentTokenProvider: TokenProvider = () => null;

/** 인증 토큰을 어디서 읽을지 주입한다(AuthProvider가 연결). */
export function setAuthTokenProvider(provider: TokenProvider): void {
  currentTokenProvider = provider;
}

/** 요청 헤더 구성(순수 함수 — 토큰 유무에 따라 Authorization 부착). */
export function authHeaders(token: string | null): Record<string, string> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

interface RequestOptions {
  method?: string;
  body?: unknown;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: options.method ?? 'GET',
    headers: authHeaders(currentTokenProvider()),
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    throw await toApiError(response);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

async function toApiError(response: Response): Promise<ApiError> {
  let code: string | null = null;
  let message = `요청이 실패했습니다 (${response.status})`;
  try {
    const body = (await response.json()) as Partial<{ code: string; message: string }>;
    if (body.message) message = body.message;
    if (body.code) code = body.code;
  } catch {
    // 응답 바디가 없거나 JSON이 아님 — 기본 메시지 유지
  }
  return new ApiError(response.status, code, message);
}
