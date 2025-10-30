import { useState, ReactNode, useEffect, useCallback } from 'react';
import { LoginResponse } from '@/apis/types';
import { getToken, setToken, removeToken } from '@/utils/token';
import { getMe, logout as logoutApi } from '@/apis/auth';
import { AuthContext, AuthContextType } from './AuthContext.definition';

type User = LoginResponse['member'];

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

    // 'void' 연산자를 사용하여 floating promise 경고를 해결합니다.
    void initializeAuth();
  }, []);

  const login = (userData: User, token: string) => {
    setUser(userData);
    setToken(token);
  };

  const logout = useCallback(async () => {
    try {
      await logoutApi();
    } catch (error) {
      console.error('로그아웃 API 호출에 실패했습니다:', error);
    } finally {
      setUser(null);
      removeToken();
    }
  }, []);

  const updateUser = useCallback((newUserData: User) => {
    setUser(newUserData);
  }, []);

  const value: AuthContextType = { user, updateUser, login, logout, isInitialized };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
