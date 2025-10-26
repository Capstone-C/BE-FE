// src/contexts/AuthContext.tsx (최종 수정본)
import { createContext, useState, ReactNode, useEffect } from 'react';
import { LoginResponse } from '@/apis/types';
import { getToken, setToken, removeToken } from '@/utils/token';
import { getMe } from '@/apis/auth';

type User = LoginResponse['member'];

export interface AuthContextType {
  user: User | null;
  login: (userData: User, token: string) => void;
  logout: () => void;
  isInitialized: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const initializeAuth = async () => {
      const token = getToken();
      if (token) {
        try {
          const userData = await getMe();
          setUser(userData);
        } catch (error) {
          console.error('자동 로그인 실패, 토큰이 유효하지 않을 수 있습니다.', error);
          removeToken();
        }
      }
      setIsInitialized(true);
    };
    initializeAuth();
  }, []);

  const login = (userData: User, token: string) => {
    setUser(userData);
    setToken(token);
  };

  const logout = () => {
    setUser(null);
    removeToken();
  };

  const value = { user, login, logout, isInitialized };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}