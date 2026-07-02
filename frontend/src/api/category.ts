import { apiRequest } from './client';
import type { Category } from './types';

/** 세션 설정 선택지 — 카테고리 목록 조회 */
export function listCategories(): Promise<Category[]> {
  return apiRequest<Category[]>('/categories');
}
