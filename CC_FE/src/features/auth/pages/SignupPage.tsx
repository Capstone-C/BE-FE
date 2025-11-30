import { useState, FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { signup } from '@/apis/auth';
import { SignupRequest } from '@/apis/types';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';

export default function SignupPage() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<SignupRequest>({
    email: '',
    password: '',
    passwordConfirm: '',
    nickname: '',
  });

  const { mutate, isPending, error } = useMutation({
    mutationFn: signup,
    onSuccess: () => {
      alert('회원가입이 완료되었습니다.');
      navigate('/login');
    },
    onError: (err) => {
      console.error('회원가입 실패:', err);
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (formData.password !== formData.passwordConfirm) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }
    mutate(formData);
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-purple-50 via-white to-indigo-50 px-4">
      <div className="w-full max-w-xl py-8">
        <Card className="p-10 space-y-8 shadow-xl">
          <div className="text-center space-y-3">
            <h1 className="text-4xl font-bold gradient-text">✨ 회원가입</h1>
            <p className="text-lg text-gray-600">새로운 계정을 만들어보세요</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              id="email"
              name="email"
              type="email"
              label="이메일"
              required
              value={formData.email}
              onChange={handleChange}
              placeholder="example@email.com"
            />

            <Input
              id="nickname"
              name="nickname"
              type="text"
              label="닉네임"
              required
              minLength={2}
              maxLength={10}
              value={formData.nickname}
              onChange={handleChange}
              placeholder="2-10자 이내"
              helperText="다른 사용자에게 보여질 이름입니다"
            />

            <Input
              id="password"
              name="password"
              type="password"
              label="비밀번호"
              required
              minLength={8}
              maxLength={20}
              value={formData.password}
              onChange={handleChange}
              placeholder="8-20자 이내"
              helperText="영문, 숫자, 특수문자 조합 권장"
            />

            <Input
              id="passwordConfirm"
              name="passwordConfirm"
              type="password"
              label="비밀번호 확인"
              required
              value={formData.passwordConfirm}
              onChange={handleChange}
              placeholder="비밀번호를 다시 입력하세요"
              error={
                formData.passwordConfirm && formData.password !== formData.passwordConfirm
                  ? '비밀번호가 일치하지 않습니다'
                  : undefined
              }
            />

            {error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-base text-red-600">
                  {(() => {
                    const anyErr = error as unknown as { response?: { data?: { message?: string } }; message?: string };
                    return anyErr?.response?.data?.message ?? anyErr?.message ?? '회원가입 중 오류가 발생했습니다.';
                  })()}
                </p>
              </div>
            )}

            <Button
              type="submit"
              disabled={isPending}
              variant="primary"
              className="w-full py-3 text-base mt-8"
            >
              {isPending ? '가입 처리 중...' : '가입하기'}
            </Button>
          </form>

          <div className="flex items-center gap-4 pt-6">
            <div className="flex-1 border-t border-gray-200"></div>
            <span className="text-base text-gray-500">또는</span>
            <div className="flex-1 border-t border-gray-200"></div>
          </div>

          <div className="text-center text-base">
            <p className="text-gray-600">
              이미 계정이 있으신가요?{' '}
              <Link to="/login" className="text-purple-600 hover:text-purple-700 font-medium hover:underline">
                로그인
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}
