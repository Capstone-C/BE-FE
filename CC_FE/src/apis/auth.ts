import { publicClient, authClient } from './client';
import type {
  SignupRequest,
  LoginRequest,
  LoginResponse,
  UpdateProfileRequest,
  MemberProfileResponse,
  WithdrawRequest,
  ChangePasswordRequest,
} from './types';

// [수정] 회원가입: publicClient 사용이 필수입니다.
export const signup = async (userData: SignupRequest) => {
  // 경로에서 '/signup'을 제거하여 백엔드의 공개 엔드포인트와 일치시킵니다.
  const response = await publicClient.post('/api/v1/members', userData);
  return response.data;
};

// 로그인: publicClient 사용
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  const response = await publicClient.post<LoginResponse>('/api/v1/auth/login', credentials);
  return response.data;
};

// 로그아웃: authClient 사용
export const logout = async (): Promise<{ message: string }> => {
  const response = await authClient.post<{ message: string }>('/api/v1/auth/logout');
  return response.data;
};

// 내 정보 조회: authClient 사용
export const getMe = async (): Promise<MemberProfileResponse> => {
  const response = await authClient.get<MemberProfileResponse>('/api/v1/members/me');
  return response.data;
};

// 회원 정보 수정: authClient 사용
export const updateProfile = async (updateData: UpdateProfileRequest): Promise<MemberProfileResponse> => {
  const formData = new FormData();
  if (updateData.nickname) {
    formData.append('nickname', updateData.nickname);
  }
  if (updateData.profileImage) {
    formData.append('profileImage', updateData.profileImage);
  }
  const response = await authClient.patch<MemberProfileResponse>('/api/v1/members/me', formData);
  return response.data;
};

export const withdrawAccount = async (data: WithdrawRequest): Promise<{ message: string }> => {
  // DELETE 메소드는 보통 body를 보내지 않지만, axios는 data 옵션을 통해 보낼 수 있습니다.
  const response = await authClient.delete<{ message: string }>('/api/v1/members/me', { data });
  return response.data;
};

// 비밀번호 변경: authClient 사용, PATCH 메소드
export const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
  // 성공 시 204 No Content 이므로, 반환값이 없습니다 (Promise<void>).
  await authClient.patch('/api/v1/members/password', data);
};
