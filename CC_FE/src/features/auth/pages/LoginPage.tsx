import { useState, FormEvent } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { login } from '@/apis/auth';
import { LoginRequest } from '@/apis/types';
import { useAuth } from '@/hooks/useAuth';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';

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
      alert('로그인 되었습니다.');
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
    <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-purple-50 via-white to-indigo-50 px-4">
      <div className="w-full max-w-xl py-8">
        <Card className="p-10 space-y-8 shadow-xl">
          <div className="text-center space-y-3">
            <h1 className="text-4xl font-bold gradient-text">🍽️ 로그인</h1>
            <p className="text-lg text-gray-600">Capstone에 오신 것을 환영합니다</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5" autoComplete="off">
            <Input
              id="email"
              name="email"
              type="email"
              label="이메일"
              autoComplete="off"
              required
              value={formData.email}
              onChange={handleChange}
              placeholder="example@email.com"
            />

            <Input
              id="password"
              name="password"
              type="password"
              label="비밀번호"
              autoComplete="new-password"
              required
              value={formData.password}
              onChange={handleChange}
              placeholder="비밀번호를 입력하세요"
            />

            {errorMessage && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-base text-red-600 text-center">{errorMessage}</p>
              </div>
            )}

            <Button
              type="submit"
              disabled={isPending}
              variant="primary"
              className="w-full py-3 text-base"
            >
              {isPending ? '로그인 중...' : '로그인'}
            </Button>
          </form>

          <div className="flex items-center gap-4 pt-6">
            <div className="flex-1 border-t border-gray-200"></div>
            <span className="text-base text-gray-500">또는</span>
            <div className="flex-1 border-t border-gray-200"></div>
          </div>

          <div className="space-y-3 text-center text-base">
            <p className="text-gray-600">
              계정이 없으신가요?{' '}
              <Link to="/signup" className="text-purple-600 hover:text-purple-700 font-medium hover:underline">
                회원가입
              </Link>
            </p>
            <Link to="/find-password" className="block text-gray-500 hover:text-purple-600 transition-colors">
              비밀번호 찾기
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
}
