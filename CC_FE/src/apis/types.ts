// src/apis/types.ts
export interface SignupRequest {
  email: string;
  password: string;
  passwordConfirm: string;
  nickname: string;
}

// 로그인 요청 타입
export interface LoginRequest {
  email: string;
  password: string;
}

// 로그인 성공 응답 타입 (백엔드 명세 기준)
export interface LoginResponse {
  accessToken: string;
  member: {
    id: number;
    email: string;
    nickname: string;
    role: 'USER' | 'ADMIN'; // 백엔드 응답에 따라 Enum 확장 가능
  };
}

// 프로필 이미지 객체 타입
export interface Profile {
  id: number;
  url: string;
}

// 회원 정보 수정 요청 타입
export interface UpdateProfileRequest {
  nickname?: string;
  profileImage?: File | null;
}

// 확장된 회원 프로필 조회 응답 타입 (기존 정의를 이것으로 교체)
export interface MemberProfileResponse {
  id: number;
  email: string;
  nickname: string;
  role: 'USER' | 'ADMIN';
  profile: string | null; // profile 객체 추가 (null일 수 있음)
  exportScore: number;
  representativeBadgeId: number | null;
  joinedAt: string;
  lastLoginAt: string;
}

export interface UpdateProfileRequest {
  nickname?: string;
  profileImage?: File | null;
}

export interface WithdrawRequest {
  password: string;
}

export interface ChangePasswordRequest {
  oldPassword: string; // [수정] currentPassword -> oldPassword
  newPassword: string;
  newPasswordConfirm: string;
}

// 백엔드 에러 응답 타입 (다중 오류 메시지 처리를 위함)
export interface BackendErrorResponse {
  code: string;
  message: string;
  errors?: {
    field: string;
    message: string; // [수정] reason -> message
  }[];
}
