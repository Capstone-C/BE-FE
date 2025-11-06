import { useState, FormEvent, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { confirmPasswordReset } from '@/apis/auth';
import { BackendErrorResponse } from '@/apis/types';

export default function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [formData, setFormData] = useState({ newPassword: '', newPasswordConfirm: '' });
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      alert('유효하지 않은 접근입니다. 비밀번호 찾기를 다시 시작해주세요.');
      navigate('/find-password');
    }
  }, [token, navigate]);

  const { mutate, isPending } = useMutation({
    mutationFn: confirmPasswordReset,
    onSuccess: () => {
      alert('비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요.');
      navigate('/login');
    },
    onError: (err: AxiosError<BackendErrorResponse>) => {
      const message = err.response?.data?.message || '알 수 없는 오류가 발생했습니다.';
      setError(message);
      if (message.includes('만료') || message.includes('유효하지')) {
        setTimeout(() => navigate('/find-password'), 3000);
      }
    },
  });

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (formData.newPassword !== formData.newPasswordConfirm) {
      setError('새 비밀번호가 일치하지 않습니다.');
      return;
    }
    if (!token) {
      setError('인증 토큰이 없습니다. 다시 시도해주세요.');
      return;
    }
    mutate({ ...formData, token });
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">
      <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold text-center">새 비밀번호 설정</h1>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="newPassword">새 비밀번호</label>
            <input
              id="newPassword"
              name="newPassword"
              type="password"
              required
              onChange={(e) => setFormData((p) => ({ ...p, newPassword: e.target.value }))}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm"
            />
          </div>
          <div>
            <label htmlFor="newPasswordConfirm">새 비밀번호 확인</label>
            <input
              id="newPasswordConfirm"
              name="newPasswordConfirm"
              type="password"
              required
              onChange={(e) => setFormData((p) => ({ ...p, newPasswordConfirm: e.target.value }))}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm"
            />
          </div>

          {error && <p className="text-sm text-red-600 text-center">{error}</p>}

          <div>
            <button type="submit" disabled={isPending || !token} className="w-full btn-primary">
              {isPending ? '변경 중...' : '비밀번호 변경'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
