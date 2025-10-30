import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { signup } from '@/apis/auth';
import { SignupRequest } from '@/apis/types';
const SignupPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<SignupRequest>({
    email: '',
    password: '',
    passwordConfirm: '',
    nickname: '',
  });

  // TanStack Query의 useMutation 훅으로 회원가입 API 연동
  const { mutate, isPending, error } = useMutation({
    mutationFn: signup,
    onSuccess: () => {
      // 성공 시
      alert('회원가입이 완료되었습니다.');
      navigate('/login'); // 로그인 페이지로 이동
    },
    onError: (err) => {
      // onError는 여기에서 직접 처리하거나, error 상태를 아래에서 렌더링할 수 있습니다.
      console.error('회원가입 실패:', err);
    },
  });

  // 입력 필드 변경 시 상태 업데이트 핸들러
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  // 폼 제출 핸들러
  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    // TODO: 여기에 프론트엔드 유효성 검사 로직 추가 (비밀번호 일치 여부 등)
    if (formData.password !== formData.passwordConfirm) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }
    mutate(formData);
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">
      <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold text-center">회원가입</h1>
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 이메일 입력 필드 */}
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">이메일</label>
            <input
              id="email"
              name="email"
              type="email"
              required
              value={formData.email}
              onChange={handleChange}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* 닉네임 입력 필드 */}
          <div>
            <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">닉네임</label>
            <input
              id="nickname"
              name="nickname"
              type="text"
              required
              minLength={2}
              maxLength={10}
              value={formData.nickname}
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
              required
              minLength={8}
              maxLength={20}
              value={formData.password}
              onChange={handleChange}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* 비밀번호 확인 입력 필드 */}
          <div>
            <label htmlFor="passwordConfirm" className="block text-sm font-medium text-gray-700">비밀번호 확인</label>
            <input
              id="passwordConfirm"
              name="passwordConfirm"
              type="password"
              required
              value={formData.passwordConfirm}
              onChange={handleChange}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          {/* 서버 에러 메시지 표시 */}
          {error && (
            <p className="text-sm text-red-600">
              {/* @ts-ignore */}
              회원가입 중 오류가 발생했습니다: {error.response?.data?.message || error.message}
            </p>
          )}

          {/* 가입하기 버튼 */}
          <div>
            <button
              type="submit"
              disabled={isPending}
              className="w-full px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400"
            >
              {isPending ? '가입 처리 중...' : '가입하기'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SignupPage;