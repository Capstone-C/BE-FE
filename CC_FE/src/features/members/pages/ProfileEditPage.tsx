import { useState, useEffect, ChangeEvent, FormEvent, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { getMe, updateProfile } from '@/apis/auth';
import { useAuth } from '@/hooks/useAuth';
import { MemberProfileResponse } from '@/apis/types';

const useProfileImagePreview = (initialImageUrl: string | null) => {
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(initialImageUrl);

  useEffect(() => {
    setPreviewUrl(initialImageUrl);
  }, [initialImageUrl]);

  const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setImageFile(file);
    const newPreviewUrl = URL.createObjectURL(file);
    setPreviewUrl(newPreviewUrl);
  };

  useEffect(() => {
    return () => {
      if (previewUrl && previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  return { imageFile, previewUrl, handleImageChange };
};

export default function ProfileEditPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { updateUser } = useAuth();

  const [nickname, setNickname] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const { data: initialData, isPending: isLoadingProfile } = useQuery<MemberProfileResponse>({
    queryKey: ['me'],
    queryFn: getMe,
  });

  const initialImageUrl = useMemo(() => initialData?.profile || null, [initialData]);
  const { imageFile, previewUrl, handleImageChange } = useProfileImagePreview(initialImageUrl);

  useEffect(() => {
    if (initialData) {
      setNickname(initialData.nickname);
    }
  }, [initialData]);

  const { mutate, isPending } = useMutation({
    mutationFn: updateProfile,
    onSuccess: (updatedProfileData) => {
      queryClient.setQueryData(['me'], updatedProfileData);
      updateUser(updatedProfileData);
      alert('회원정보가 성공적으로 수정되었습니다.');
      navigate('/mypage');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      setErrorMessage(error.response?.data?.message || '알 수 없는 오류가 발생했습니다.');
    },
  });

  const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setErrorMessage('');

    const isNicknameChanged = initialData?.nickname !== nickname;
    const isImageChanged = imageFile !== null;

    if (!isNicknameChanged && !isImageChanged) {
      alert('변경된 정보가 없습니다.');
      return;
    }

    mutate({
      nickname: isNicknameChanged ? nickname : undefined,
      profileImage: isImageChanged ? imageFile : undefined,
    });
  };

  if (isLoadingProfile) {
    return <div className="p-8 text-center">프로필 정보를 불러오는 중입니다...</div>;
  }

  return (
    <div className="max-w-2xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">회원정보 수정</h1>
      <form onSubmit={handleSubmit} className="space-y-6 bg-white shadow-md rounded-lg p-8">
        <div>
          <label className="block text-sm font-medium text-gray-700">이메일 (수정 불가)</label>
          <p className="mt-1 text-gray-500 bg-gray-100 p-2 rounded-md">{initialData?.email}</p>
        </div>
        <div>
          <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">
            닉네임
          </label>
          <input
            id="nickname"
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            className="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">프로필 이미지</label>
          <div className="mt-2 flex items-center space-x-4">
            <img
              src={previewUrl || initialData?.profile || 'https://via.placeholder.com/100'}
              alt="프로필 미리보기"
              className="w-24 h-24 rounded-full object-cover bg-gray-200"
            />
            <input
              id="profileImage"
              type="file"
              accept="image/png, image/jpeg, image/gif"
              onChange={handleImageChange}
              className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
            />
          </div>
        </div>
        {errorMessage && <p className="text-sm text-red-600 text-center">{errorMessage}</p>}
        <div className="flex justify-end space-x-4 pt-4">
          <button type="button" onClick={() => navigate('/mypage')} className="px-4 py-2 bg-gray-200 rounded-md">
            취소
          </button>
          <button type="submit" disabled={isPending} className="px-4 py-2 bg-blue-600 text-white rounded-md">
            {isPending ? '수정 중...' : '수정 완료'}
          </button>
        </div>
      </form>
    </div>
  );
}
