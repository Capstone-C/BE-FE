import { useContext } from 'react';
import { AuthContext, AuthContextType } from '@/contexts/AuthContext'; // AuthContext와 타입을 import

export function useAuth() {
  const context = useContext<AuthContextType | undefined>(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth는 AuthProvider 내부에서 사용해야 합니다.');
  }
  return context;
}