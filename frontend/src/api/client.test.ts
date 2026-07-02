import { describe, expect, it } from 'vitest';
import { authHeaders } from './client';

// 스모크 테스트 — 이번 슬라이스는 최소 검증(순수 함수). 컴포넌트 테스트는 후속.
describe('authHeaders', () => {
  it('토큰이 없으면 Content-Type만 있고 Authorization은 없다', () => {
    const headers = authHeaders(null);
    expect(headers['Content-Type']).toBe('application/json');
    expect(headers.Authorization).toBeUndefined();
  });

  it('토큰이 있으면 Bearer Authorization 헤더를 붙인다', () => {
    expect(authHeaders('abc.def.ghi').Authorization).toBe('Bearer abc.def.ghi');
  });
});
