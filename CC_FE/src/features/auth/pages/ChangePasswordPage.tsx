import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { changePassword } from '@/apis/auth';
import { ChangePasswordRequest, BackendErrorResponse } from '@/apis/types';
import { useAuth } from '@/hooks/useAuth';

type ErrorMessages = {
  oldPassword?: string;
  newPassword?: string[];
  newPasswordConfirm?: string;
  general?: string;
};

const parseApiError = (error: AxiosError<BackendErrorResponse>): ErrorMessages => {
  const responseData = error.response?.data;
  const newErrors: ErrorMessages = {};

  if (responseData?.errors) {
    responseData.errors.forEach((err) => {
      if (err.field === 'oldPassword') {
        newErrors.oldPassword = err.message;
      }
      if (err.field === 'newPassword' || err.field === 'newPasswordConfirm') {
        if (!newErrors.newPassword) newErrors.newPassword = [];
        newErrors.newPassword.push(err.message);
      }
    });
  } else if (responseData) {
    const { message } = responseData;
    if (message.includes('기존 비밀번호') || message.includes('현재 비밀번호')) {
      newErrors.oldPassword = message;
    } else if (message.includes('동일한 비밀번호') || message.includes('재사용')) {
      newErrors.newPassword = [message];
    } else {
      newErrors.general = message;
    }
  } else {
    newErrors.general = '알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
  }
  return newErrors;
};

export default function ChangePasswordPage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [formData, setFormData] = useState<ChangePasswordRequest>({
    oldPassword: '',
    newPassword: '',
    newPasswordConfirm: '',
  });
  const [errors, setErrors] = useState<ErrorMessages>({});

  const { mutate, isPending } = useMutation({
    mutationFn: changePassword,
    onSuccess: async () => {
      alert('비밀번호가 성공적으로 변경되었습니다. 보안을 위해 로그아웃 처리됩니다. 다시 로그인해주세요.');
      await logout();
      navigate('/login', { replace: true });
    },
    onError: (error: AxiosError<BackendErrorResponse>) => {
      setErrors(parseApiError(error));
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setErrors({});
    if (formData.newPassword !== formData.newPasswordConfirm) {
      setErrors({ newPasswordConfirm: '새 비밀번호가 일치하지 않습니다.' });
      return;
    }
    mutate(formData);
  };

  const inputClasses =
    'w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500';
  const primaryButtonClasses =
    'px-4 py-2 font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400';
  const secondaryButtonClasses = 'px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300';
  const errorTextClasses = 'text-sm text-red-600 mt-1';

  return (
    <div className="max-w-md mx-auto p-8 mt-10">
      <h1 className="text-3xl font-bold mb-6 text-center">비밀번호 변경</h1>
      <div className="bg-white shadow-md rounded-lg p-8">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="oldPassword" className="block text-sm font-medium text-gray-700">
              현재 비밀번호
            </label>
            <input
              id="oldPassword"
              name="oldPassword"
              type="password"
              required
              onChange={handleChange}
              className={inputClasses}
            />
            {errors.oldPassword && <p className={errorTextClasses}>{errors.oldPassword}</p>}
          </div>

          <div>
            <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">
              새 비밀번호
            </label>
            <input
              id="newPassword"
              name="newPassword"
              type="password"
              required
              onChange={handleChange}
              className={inputClasses}
            />
            {errors.newPassword &&
              errors.newPassword.map((msg, i) => (
                <p key={i} className={errorTextClasses}>
                  {msg}
                </p>
              ))}
          </div>

          <div>
            <label htmlFor="newPasswordConfirm" className="block text-sm font-medium text-gray-700">
              새 비밀번호 확인
            </label>
            <input
              id="newPasswordConfirm"
              name="newPasswordConfirm"
              type="password"
              required
              onChange={handleChange}
              className={inputClasses}
            />
            {errors.newPasswordConfirm && <p className={errorTextClasses}>{errors.newPasswordConfirm}</p>}
          </div>

          {errors.general && <p className={`${errorTextClasses} text-center`}>{errors.general}</p>}

          <div className="flex justify-end space-x-4 pt-4">
            <button type="button" onClick={() => navigate('/mypage')} className={secondaryButtonClasses}>
              취소
            </button>
            <button type="submit" disabled={isPending} className={primaryButtonClasses}>
              {isPending ? '변경 중...' : '변경 완료'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
