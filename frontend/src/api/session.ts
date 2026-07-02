import { apiRequest } from './client';
import type {
  AnswerResponse,
  SessionDetail,
  SessionStatusResponse,
  StartSessionResponse,
} from './types';

export interface StartSessionParams {
  categorySlugs?: string[];
  randomAll: boolean;
  questionCount?: number | null;
  difficulty?: number | null;
}

/** 세션 시작 + 첫 질문 서빙 */
export function startSession(params: StartSessionParams): Promise<StartSessionResponse> {
  return apiRequest<StartSessionResponse>('/sessions', { method: 'POST', body: params });
}

/** 답변 제출 → 꼬리질문 1~2개 */
export function submitAnswer(sessionId: number, content: string): Promise<AnswerResponse> {
  return apiRequest<AnswerResponse>(`/sessions/${sessionId}/answers`, {
    method: 'POST',
    body: { content },
  });
}

/** 세션 종료 */
export function completeSession(sessionId: number): Promise<SessionStatusResponse> {
  return apiRequest<SessionStatusResponse>(`/sessions/${sessionId}/complete`, { method: 'POST' });
}

/** 세션 상세(설정·상태 + 대화 이력) */
export function getSession(sessionId: number): Promise<SessionDetail> {
  return apiRequest<SessionDetail>(`/sessions/${sessionId}`);
}
