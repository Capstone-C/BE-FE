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