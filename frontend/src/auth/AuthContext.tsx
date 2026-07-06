import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { fetchMe, googleLogin as googleLoginRequest, login as loginRequest } from '../api/auth';
import { setAuthTokenProvider } from '../api/client';
import type { MeResponse } from '../api/types';
import { tokenStorage } from './tokenStorage';

interface AuthContextValue {
  user: MeResponse | null;
  token: string | null;
  /** 앱 시작 시 저장된 토큰으로 사용자 복원 중인지 */
  initializing: boolean;
  login: (email: string, password: string) => Promise<void>;
  /** 구글 ID 토큰으로 로그인(백엔드 검증 후 우리 토큰 저장) — D38 */
  loginWithGoogle: (idToken: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => tokenStorage.get());
  const [user, setUser] = useState<MeResponse | null>(null);
  const [initializing, setInitializing] = useState(true);

  // API 클라이언트가 매 요청 시 최신 토큰을 읽도록 연결
  useEffect(() => {
    setAuthTokenProvider(() => tokenStorage.get());
  }, []);

  // 저장된 토큰이 있으면 /me로 사용자 복원(토큰 만료/무효면 정리)
  useEffect(() => {
    if (!token) {
      setInitializing(false);
      return;
    }
    let active = true;
    fetchMe()
      .then((me) => {
        if (active) setUser(me);
      })
      .catch(() => {
        if (active) {
          tokenStorage.clear();
          setToken(null);
          setUser(null);
        }
      })
      .finally(() => {
        if (active) setInitializing(false);
      });
    return () => {
      active = false;
    };
  }, [token]);

  const login = useCallback(async (email: string, password: string) => {
    const result = await loginRequest(email, password);
    tokenStorage.set(result.accessToken);
    setToken(result.accessToken);
    setUser(await fetchMe());
  }, []);

  const loginWithGoogle = useCallback(async (idToken: string) => {
    const result = await googleLoginRequest(idToken);
    tokenStorage.set(result.accessToken);
    setToken(result.accessToken);
    setUser(await fetchMe());
  }, []);

  const logout = useCallback(() => {
    tokenStorage.clear();
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, token, initializing, login, loginWithGoogle, logout }),
    [user, token, initializing, login, loginWithGoogle, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth는 AuthProvider 안에서만 사용할 수 있습니다.');
  }
  return context;
}
