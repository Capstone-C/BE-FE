import { useState, FormEvent, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { signup } from '@/apis/auth';
import { SignupRequest } from '@/apis/types';
import { AxiosError } from 'axios';

export default function SignupPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [passwordMessage, setPasswordMessage] = useState('');
  const [doPasswordsMatch, setDoPasswordsMatch] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (confirmPassword.length > 0) {
      if (password === confirmPassword) {
        setPasswordMessage('비밀번호가 일치합니다.');
        setDoPasswordsMatch(true);
      } else {
        setPasswordMessage('비밀번호가 일치하지 않습니다.');
        setDoPasswordsMatch(false);
      }
    } else {
      setPasswordMessage('');
      setDoPasswordsMatch(false);
    }
  }, [password, confirmPassword]);

  const { mutate, isPending } = useMutation({
    mutationFn: (data: SignupRequest) => signup(data),
    onSuccess: () => {
      // 회원가입 성공 페이지로 이동하며 데이터 전달
      navigate('/register-success', { state: { nickname, email } });
    },
    onError: (err: AxiosError<{ message?: string, code?: string }>) => {
      const msg = err.response?.data?.message || '회원가입 중 오류가 발생했습니다.';
      setError(msg);
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!doPasswordsMatch) return;
    setError(null);
    mutate({ email, nickname, password, passwordConfirm: confirmPassword });
  };

  const isFormValid = email && nickname && password && doPasswordsMatch;

  return (
    <div className="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-[#F0F5E5] min-h-[calc(100vh-5rem)]">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            회원가입
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label htmlFor="email-address" className="sr-only">이메일</label>
              <input
                id="email-address"
                name="email"
                type="email"
                autoComplete="email"
                required
                className="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm"
                placeholder="이메일"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="nickname" className="sr-only">닉네임</label>
              <input
                id="nickname"
                name="nickname"
                type="text"
                required
                className="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm"
                placeholder="닉네임 (2~10자)"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="password" className="sr-only">비밀번호</label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="new-password"
                required
                className="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm"
                placeholder="비밀번호 (영문/숫자/특수문자 포함 8자 이상)"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="confirm-password" className="sr-only">비밀번호 확인</label>
              <input
                id="confirm-password"
                name="confirm-password"
                type="password"
                autoComplete="new-password"
                required
                className="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-[#71853A] focus:border-[#71853A] sm:text-sm"
                placeholder="비밀번호 확인"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              {passwordMessage && (
                <p className={`mt-2 text-sm ${doPasswordsMatch ? 'text-green-600' : 'text-red-600'}`}>
                  {passwordMessage}
                </p>
              )}
            </div>
          </div>

          {error && <p className="text-sm text-red-600 text-center">{error}</p>}

          <div>
            <button
              type="submit"
              disabled={!isFormValid || isPending}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-lg font-medium rounded-md text-white bg-[#4E652F] hover:bg-[#425528] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#71853A] transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {isPending ? '가입 중...' : '가입'}
            </button>
          </div>

          <div className="text-center text-sm">
            <span className="text-gray-600">이미 계정이 있으신가요? </span>
            <Link to="/login" className="font-medium text-[#4E652F] hover:text-[#425528]">
              로그인
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}