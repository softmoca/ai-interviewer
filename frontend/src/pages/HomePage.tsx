import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/** 보호 화면 — 로그인 후 사용자 정보 + 면접 시작 진입점 + 로그아웃. */
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
      <Link to="/sessions/new" className="button-link">
        새 면접 시작
      </Link>
      <button type="button" className="secondary" onClick={logout}>
        로그아웃
      </button>
    </div>
  );
}
