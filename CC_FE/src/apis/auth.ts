import { publicClient, authClient } from './client';
import type {
  SignupRequest,
  LoginRequest,
  LoginResponse,
  MemberProfileResponse, // MemberProfileResponse 타입을 types.ts에서 가져옵니다.
} from './types';

// 회원가입
export const signup = async (userData: SignupRequest) => {
  const response = await publicClient.post('/api/v1/members/signup', userData);
  return response.data;
};

// 로그인
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  const response = await publicClient.post<LoginResponse>('/api/v1/auth/login', credentials);
  return response.data;
};

// 로그아웃
export const logout = async (): Promise<{ message: string }> => {
  const response = await authClient.post<{ message: string }>('/api/v1/auth/logout');
  return response.data;
};

// 내 정보 조회
export const getMe = async (): Promise<MemberProfileResponse> => {
  const response = await authClient.get<MemberProfileResponse>('/api/v1/members/me');
  return response.data;
};
