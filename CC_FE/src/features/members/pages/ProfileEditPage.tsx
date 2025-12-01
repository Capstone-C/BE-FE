import { useState, useEffect, ChangeEvent, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMe, updateProfile } from '@/apis/auth';
import { useAuth } from '@/hooks/useAuth';
import { MemberProfileResponse } from '@/apis/types';

export default function ProfileEditPage() {
  const navigate = useNavigate();
  const { updateUser } = useAuth();
  const queryClient = useQueryClient();

  const { data: me } = useQuery<MemberProfileResponse>({ queryKey: ['me'], queryFn: getMe });

  const [nickname, setNickname] = useState('');
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  useEffect(() => {
    if (me) {
      setNickname(me.nickname);
      setPreviewUrl(me.profile || null);
    }
  }, [me]);

  const handleImageChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setImageFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const updateMutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: (data) => {
      updateUser(data);
      queryClient.setQueryData(['me'], data);
      alert('회원정보가 수정되었습니다.');
      navigate('/mypage');
    },
    onError: () => {
      alert('회원정보 수정에 실패했습니다.');
    }
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    updateMutation.mutate({
      nickname,
      profileImage: imageFile
    });
  };

  if (!me) return <div className="p-8 text-center">로딩 중...</div>;

  return (
    <div className="max-w-4xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold leading-tight text-gray-900">
          회원정보 수정
        </h1>
      </div>

      <div className="bg-white rounded-lg shadow-lg p-8">
        <form onSubmit={handleSubmit} className="space-y-8">

          <div>
            <label className="block text-sm font-medium text-gray-700">
              이메일 (수정 불가)
            </label>
            <div className="mt-1">
              <input
                type="email"
                disabled
                className="shadow-sm bg-gray-100 block w-full sm:text-sm border-gray-300 rounded-md cursor-not-allowed p-2"
                value={me.email}
              />
            </div>
          </div>

          <div>
            <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">
              닉네임
            </label>
            <div className="mt-1">
              <input
                type="text"
                id="nickname"
                className="shadow-sm focus:ring-[#71853A] focus:border-[#71853A] block w-full sm:text-sm border border-gray-300 rounded-md p-2"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">
              프로필 이미지
            </label>
            <div className="mt-2 flex items-center space-x-6">
              <div className="w-24 h-24 rounded-full bg-gray-200 overflow-hidden flex items-center justify-center">
                {previewUrl ? (
                  <img src={previewUrl} alt="Preview" className="w-full h-full object-cover" />
                ) : (
                  <span className="text-3xl text-gray-500">{nickname.charAt(0)}</span>
                )}
              </div>
              <label className="cursor-pointer px-4 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50">
                파일 선택
                <input type="file" className="hidden" accept="image/*" onChange={handleImageChange} />
              </label>
            </div>
          </div>

          <div className="pt-5 border-t border-gray-200">
            <div className="flex justify-end space-x-4">
              <button
                type="button"
                onClick={() => navigate('/mypage')}
                className="px-6 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50"
              >
                취소
              </button>
              <button
                type="submit"
                disabled={updateMutation.isPending}
                className="px-6 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-[#4E652F] hover:bg-[#425528]"
              >
                {updateMutation.isPending ? '저장 중...' : '수정 완료'}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}