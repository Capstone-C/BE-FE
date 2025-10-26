// src/apis/auth.ts
import { authClient, publicClient } from './client';
import type { SignupRequest, LoginRequest, LoginResponse } from './types';

// 회원가입 API 호출 함수
export const signup = async (data: SignupRequest) => {
  const response = await publicClient.post('/api/v1/members', data);
  return response.data;
};

// login 함수
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  const response = await publicClient.post<LoginResponse>('/api/v1/auth/login', credentials);
  return response.data;
};

// 내 정보 조회 API (백엔드 응답 형식에 맞춰 Member 타입 정의 필요)
// LoginResponse의 member와 동일하다고 가정합니다.
type Member = LoginResponse['member'];

export const getMe = async (): Promise<Member> => {
  const response = await authClient.get<Member>('/api/v1/members/me'); // 이 엔드포인트는 백엔드와 협의해야 합니다.
  return response.data;
};

// 로그아웃 API 함수 추가
export const logout = async (): Promise<{ message: string }> => {
  const response = await authClient.post<{ message: string }>('/api/v1/auth/logout');
  return response.data;
};
