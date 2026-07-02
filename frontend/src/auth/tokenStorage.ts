// JWT 저장소 추상화(결정사항 D30). 현재 기본값은 localStorage.
// 한 곳에 격리해 두어, 이후 httpOnly 쿠키 등으로 교체할 때 여기만 바꾸면 되도록 한다.

const TOKEN_KEY = 'ai-interviewer.accessToken';

export const tokenStorage = {
  get(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },
  set(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  },
  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
  },
};
