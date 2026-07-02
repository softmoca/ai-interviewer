import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

/** 인증되지 않은 접근을 /login으로 보내는 보호 라우트. */
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { token, initializing } = useAuth();

  if (initializing) {
    return <p className="centered">불러오는 중…</p>;
  }
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}
