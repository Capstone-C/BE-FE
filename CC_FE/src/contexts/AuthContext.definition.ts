import { createContext } from 'react';
import { LoginResponse } from '@/apis/types';

type User = LoginResponse['member'];

export interface AuthContextType {
  user: User | null;
  login: (userData: User, token: string) => void;
  logout: () => Promise<void>;
  isInitialized: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
