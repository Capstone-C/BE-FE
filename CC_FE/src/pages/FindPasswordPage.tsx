import { useState, FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { requestPasswordReset } from '@/apis/auth';

export default function FindPasswordPage() {
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false); // 메일 발송 요청 완료 여부

  const { mutate, isPending } = useMutation({
    mutationFn: requestPasswordReset,
    onSuccess: () => {
      // 보안 정책에 따라 성공/실패 여부와 관계없이 항상 성공 화면을 보여줍니다.
      setIsSubmitted(true);
    },
    onError: () => {
      // User Enumeration 공격 방지를 위해 에러가 발생해도 성공한 것처럼 처리합니다.
      setIsSubmitted(true);
    },
  });

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!email) return;
    mutate({ email });
  };

  if (isSubmitted) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-100">
        <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md text-center">
          <h1 className="text-2xl font-bold">메일 발송 완료</h1>
          <p className="text-gray-600">
            입력하신 이메일로 비밀번호 재설정 안내 메일을 발송했습니다.
            <br />
            메일함을 확인해주세요. (메일이 오지 않는다면 스팸함도 확인해주세요.)
          </p>
          <Link to="/login" className="text-blue-600 hover:underline">
            로그인 페이지로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-100">
      <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold text-center">비밀번호 찾기</h1>
        <p className="text-center text-sm text-gray-600">
          가입 시 사용한 이메일 주소를 입력하시면, <br />
          비밀번호를 재설정할 수 있는 링크를 보내드립니다.
        </p>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              이메일
            </label>
            <input
              id="email"
              name="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm"
            />
          </div>
          <div>
            <button type="submit" disabled={isPending} className="w-full btn-primary">
              {isPending ? '발송 중...' : '인증 메일 발송'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
