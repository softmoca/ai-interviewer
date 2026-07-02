import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../api/auth';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export function SignupPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await signup(email, password, nickname);
      await login(email, password); // 가입 후 바로 로그인
      navigate('/', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '회원가입에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="card">
      <h1>회원가입</h1>
      <form onSubmit={handleSubmit}>
        <label>
          이메일
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
          />
        </label>
        <label>
          비밀번호 (8자 이상)
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            autoComplete="new-password"
          />
        </label>
        <label>
          닉네임
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            required
            maxLength={30}
          />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? '가입 중…' : '회원가입'}
        </button>
      </form>
      <p>
        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
      </p>
    </div>
  );
}
