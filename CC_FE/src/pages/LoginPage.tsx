// src/pages/LoginPage.tsx
import { useState, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { login } from '@/apis/auth';
import { LoginRequest } from '@/apis/types';
import { useAuth } from '@/hooks/useAuth';

export default function LoginPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<LoginRequest>({
    email: '',
    password: '',
  });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // 3단계에서 구현할 useAuth 훅을 임시로 주석 처리합니다.
  const { login: authLogin } = useAuth();

  const { mutate, isPending } = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      // 성공 시:
      // 1. 전역 상태 업데이트 (3단계에서 구현)
      authLogin(data.member, data.accessToken);

      // 2. 메인 페이지로 이동
      alert('로그인 되었습니다.'); // 임시 알림
      navigate('/');
    },
    onError: (error: AxiosError<{ code?: string; message: string }>) => {
      // 실패 시: 요구사항 명세서에 따른 분기 처리
      const responseData = error.response?.data;

      // 비밀번호 필드 초기화
      setFormData(prev => ({ ...prev, password: '' }));

      if (error.response?.status === 401 && responseData?.code === 'AUTH_INVALID_CREDENTIALS') {
        setErrorMessage('이메일 또는 비밀번호가 일치하지 않습니다.');
      } else if (error.response?.status === 403 && responseData?.code === 'AUTH_WITHDRAWN_MEMBER') {
        setErrorMessage('탈퇴한 회원이거나 이용이 정지된 계정입니다.');
      } else {
        // Case 5: 그 외 서버 오류
        setErrorMessage('일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      console.error('로그인 실패:', error);
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    // Case 3: 프론트엔드 유효성 검사 (입력값 누락)
    if (!formData.email || !formData.password) {
      setErrorMessage('이메일과 비밀번호를 모두 입력해주세요.');
      return;
    }
    setErrorMessage(null); // 이전 에러 메시지 초기화
    mutate(formData);
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">
      <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold text-center">로그인</h1>
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 이메일 입력 필드 */}
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">이메일</label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              required
              value={formData.email}
              onChange={handleChange}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* 비밀번호 입력 필드 */}
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">비밀번호</label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={formData.password}
              onChange={handleChange}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* 서버 에러 메시지 표시 */}
          {errorMessage && (
            <p className="text-sm text-red-600 text-center">{errorMessage}</p>
          )}

          {/* 로그인 버튼 */}
          <div>
            <button
              type="submit"
              disabled={isPending}
              className="w-full px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400"
            >
              {isPending ? '로그인 중...' : '로그인'}
            </button>
          </div>
        </form>
        <div className="mt-4 text-center">
          계정이 없으신가요? <Link to="/signup" className="text-blue-600 hover:underline">회원가입</Link>
        </div>
      </div>
    </div>
  );
}