import { useAuth } from '../auth/AuthContext';

/** 보호 화면 — 로그인 후 /me 정보를 표시하고 로그아웃 제공. (면접 기능은 다음 슬라이스) */
export function HomePage() {
  const { user, logout } = useAuth();

  return (
    <div className="card">
      <h1>AI 면접관</h1>
      <p>
        <strong>{user?.nickname}</strong>님, 환영합니다.
      </p>
      <ul>
        <li>이메일: {user?.email}</li>
        <li>사용자 ID: {user?.userId}</li>
      </ul>
      <p className="muted">면접 시작 화면은 다음 슬라이스에서 연결됩니다.</p>
      <button type="button" onClick={logout}>
        로그아웃
      </button>
    </div>
  );
}
