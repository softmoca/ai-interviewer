import { apiRequest } from './client';
import type { EvaluationReport } from './types';

/**
 * 평가 리포트 생성(멱등 — 이미 평가됐으면 기존 반환). 완료된 세션에만 가능(아니면 409).
 * 최초 호출 시 LLM을 호출하므로 다소 시간이 걸릴 수 있다.
 */
export function generateEvaluation(sessionId: number): Promise<EvaluationReport> {
  return apiRequest<EvaluationReport>(`/sessions/${sessionId}/evaluation`, { method: 'POST' });
}

/** 저장된 평가 리포트 조회(없으면 404). */
export function getEvaluation(sessionId: number): Promise<EvaluationReport> {
  return apiRequest<EvaluationReport>(`/sessions/${sessionId}/evaluation`);
}
