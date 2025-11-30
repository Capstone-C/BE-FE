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
      <div className="text-center mb-8">
        <h1 className="text-4xl font-bold gradient-text mb-2">✏️ 회원정보 수정</h1>
        <p className="text-gray-600">프로필을 업데이트하세요</p>
      </div>
      <form onSubmit={handleSubmit} className="space-y-6 bg-white shadow-lg rounded-2xl p-8 border-2 border-gray-100">
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-2">📧 이메일 (수정 불가)</label>
          <p className="mt-1 text-gray-500 bg-gradient-to-r from-gray-50 to-gray-100 p-3 rounded-xl border-2 border-gray-200">{initialData?.email}</p>
        </div>
        <div>
          <label htmlFor="nickname" className="block text-sm font-semibold text-gray-700 mb-2">
            👤 닉네임
          </label>
          <input
            id="nickname"
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-semibold text-gray-700 mb-3">📸 프로필 이미지</label>
          <div className="mt-2 flex items-center space-x-6">
            <div className="relative">
              <img
                src={previewUrl || initialData?.profile || 'https://via.placeholder.com/100'}
                alt="프로필 미리보기"
                className="w-28 h-28 rounded-full object-cover bg-gray-200 border-4 border-purple-100 shadow-lg"
              />
              <div className="absolute -bottom-1 -right-1 w-8 h-8 bg-gradient-to-r from-purple-600 to-indigo-600 rounded-full flex items-center justify-center">
                <span className="text-white text-sm">✏️</span>
              </div>
            </div>
            <input
              id="profileImage"
              type="file"
              accept="image/png, image/jpeg, image/gif"
              onChange={handleImageChange}
              className="block w-full text-sm text-gray-600 file:mr-4 file:py-2.5 file:px-5 file:rounded-xl file:border-2 file:border-purple-200 file:text-sm file:font-semibold file:bg-gradient-to-r file:from-purple-50 file:to-indigo-50 file:text-purple-700 hover:file:bg-purple-100 file:transition-all"
            />
          </div>
        </div>
        {errorMessage && <p className="text-sm text-red-600 text-center bg-red-50 p-3 rounded-xl">⚠️ {errorMessage}</p>}
        <div className="flex justify-end space-x-4 pt-4">
          <button 
            type="button" 
            onClick={() => navigate('/mypage')} 
            className="px-6 py-2.5 border-2 border-gray-200 rounded-xl text-gray-600 font-medium hover:bg-gray-50 hover:border-gray-300 transition-all"
          >
            취소
          </button>
          <button 
            type="submit" 
            disabled={isPending} 
            className="px-6 py-2.5 bg-gradient-to-r from-purple-600 to-indigo-600 text-white rounded-xl font-semibold hover:shadow-lg hover:scale-105 disabled:opacity-50 disabled:hover:scale-100 transition-all"
          >
            {isPending ? '⏳ 수정 중...' : '✅ 수정 완료'}
          </button>
        </div>
      </form>
    </div>
  );
}
