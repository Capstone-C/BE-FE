import { useState, FormEvent } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { login } from '@/apis/auth';
import { LoginRequest } from '@/apis/types';
import { useAuth } from '@/hooks/useAuth';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation() as any;
  const [formData, setFormData] = useState<LoginRequest>({
    email: '',
    password: '',
  });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const { login: authLogin } = useAuth();

  const { mutate, isPending } = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      authLogin(data.member, data.accessToken);
      // alert('로그인 되었습니다.');

      // 로그인 전 페이지로 리다이렉트 또는 홈으로 이동
      const from = location?.state?.from;
      if (typeof from === 'string') {
        navigate(from, { replace: true });
      } else if (from && typeof from === 'object') {
        const pathname = from.pathname ?? '/';
        const search = from.search ?? '';
        const hash = from.hash ?? '';
        const state = from.state ?? undefined;
        navigate(pathname + search + hash, { state, replace: true });
      } else {
        navigate('/', { replace: true });
      }
    },
    onError: (error: AxiosError<{ code?: string; message: string }>) => {
      const responseData = error.response?.data;
      setFormData((prev) => ({ ...prev, password: '' }));

      if (error.response?.status === 401 && responseData?.code === 'AUTH_INVALID_CREDENTIALS') {
        setErrorMessage('이메일 또는 비밀번호가 일치하지 않습니다.');
      } else if (error.response?.status === 403 && responseData?.code === 'AUTH_WITHDRAWN_MEMBER') {
        setErrorMessage('탈퇴 처리되었거나 이용이 정지된 계정입니다.');
      } else {
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
    if (!formData.email || !formData.password) {
      setErrorMessage('이메일과 비밀번호를 모두 입력해주세요.');
      return;
    }
    setErrorMessage(null);
    mutate(formData);
  };

  return (
    <div className="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-[#F0F5E5] min-h-[calc(100vh-5rem)]">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            로그인
          </h2>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-6" autoComplete="off">
          <div className="rounded-md shadow-sm -space-y-px">
            <div>
              <label htmlFor="email" className="sr-only">
                이메일
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={formData.email}
                onChange={handleChange}
                placeholder="이메일"
                className="appearance-none rounded-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] focus:z-10 sm:text-sm"
              />
            </div>

            <div>
              <label htmlFor="password" className="sr-only">
                비밀번호
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
                value={formData.password}
                onChange={handleChange}
                placeholder="비밀번호"
                className="appearance-none rounded-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] focus:z-10 sm:text-sm"
              />
            </div>
          </div>

          {errorMessage && <p className="text-sm text-red-600 text-center">{errorMessage}</p>}

          <div className="flex items-center justify-between text-sm">
            <div className="flex items-center space-x-2 text-gray-600">
              <Link to="/find-password" className="font-medium text-[#4E652F] hover:text-[#425528]">
                아이디/비밀번호 찾기
              </Link>
              <span className="text-gray-300">|</span>
              <Link to="/signup" className="font-medium text-[#4E652F] hover:text-[#425528]">
                회원가입
              </Link>
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isPending}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-lg font-medium rounded-md text-white bg-[#4E652F] hover:bg-[#425528] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#71853A] transition-colors disabled:bg-gray-400"
            >
              {isPending ? '로그인 중...' : '로그인'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}