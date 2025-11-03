import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { withdrawAccount } from '@/apis/auth';
import { useAuth } from '@/hooks/useAuth';

export default function WithdrawPage() {
  const navigate = useNavigate();
  const { logout } = useAuth(); // 로그아웃 처리를 위해 useAuth 훅을 사용합니다.
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const { mutate, isPending } = useMutation({
    mutationFn: withdrawAccount,
    onSuccess: async () => {
      // 1. 탈퇴 성공 메시지 표시
      alert('회원 탈퇴가 완료되었습니다. 이용해주셔서 감사합니다.');

      // 2. AuthContext의 logout 함수를 호출하여 전역 상태 및 토큰을 정리합니다.
      // logout 함수는 내부적으로 로그아웃 API 호출 후 메인 페이지로 이동시킵니다.
      await logout();
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      // 비밀번호 필드를 초기화합니다.
      setPassword('');

      // Case 1: 비밀번호 불일치 (백엔드에서 401 Unauthorized로 응답한다고 가정)
      if (error.response?.status === 401) {
        setErrorMessage('비밀번호가 일치하지 않습니다. 다시 확인해주세요.');
      } else {
        // 그 외 서버 오류
        setErrorMessage(error.response?.data?.message || '알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
      console.error('회원 탈퇴 실패:', error);
    },
  });

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!password) {
      setErrorMessage('비밀번호를 입력해주세요.');
      return;
    }

    // 경고 문구를 다시 한번 확인시킵니다.
    const isConfirmed = window.confirm(
      '정말로 탈퇴하시겠습니까?\n탈퇴 시 계정 정보는 복구할 수 없으며, 작성하신 게시글과 댓글은 익명으로 유지됩니다.',
    );

    if (isConfirmed) {
      setErrorMessage(null); // 이전 에러 메시지 초기화
      mutate({ password });
    }
  };

  return (
    <div className="max-w-md mx-auto p-8 mt-10">
      <h1 className="text-3xl font-bold mb-6 text-center">회원 탈퇴</h1>
      <div className="bg-white shadow-md rounded-lg p-8">
        <p className="text-center text-gray-600 mb-6">회원 탈퇴를 위해 계정의 비밀번호를 다시 한번 입력해주세요.</p>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">
              비밀번호 확인
            </label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-red-500 focus:border-red-500"
            />
          </div>

          {errorMessage && <p className="text-sm text-red-600 text-center">{errorMessage}</p>}

          <div className="flex justify-end space-x-4 pt-4">
            <button
              type="button"
              onClick={() => navigate('/mypage')}
              className="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:bg-gray-400"
            >
              {isPending ? '처리 중...' : '회원 탈퇴'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
