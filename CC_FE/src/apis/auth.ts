// src/apis/auth.ts
import { publicClient } from './client';
import type { SignupRequest } from './types'; // 1. 이제 이 import가 정상적으로 작동합니다.

// 회원가입 API 호출 함수
export const signup = async (data: SignupRequest) => {
  const response = await publicClient.post('/api/v1/members', data);
  return response.data;
};