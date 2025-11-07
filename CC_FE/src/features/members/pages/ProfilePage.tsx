import { useQuery } from '@tanstack/react-query';
import { getMe } from '@/apis/auth';
import { Link } from 'react-router-dom';

export default function ProfilePage() {
  const {
    data: userProfile,
    isPending,
    isError,
  } = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
  });

  if (isPending) {
    return <div className="p-8 text-center">프로필 정보를 불러오는 중입니다...</div>;
  }

  if (isError) {
    return <div className="p-8 text-center text-red-600">프로필 정보를 불러오는 중 오류가 발생했습니다.</div>;
  }

  const profileImageUrl = userProfile.profile || 'https://via.placeholder.com/150';

  return (
    <div className="max-w-4xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">마이페이지</h1>
      <div className="bg-white shadow-md rounded-lg p-6 flex items-center space-x-6">
        <img
          src={profileImageUrl}
          alt={`${userProfile.nickname}의 프로필 이미지`}
          className="w-32 h-32 rounded-full object-cover border-4 border-gray-200"
        />
        <div className="flex-grow">
          <div className="mb-4">
            <p className="text-sm text-gray-500">닉네임</p>
            <p className="text-2xl font-semibold">{userProfile.nickname}</p>
          </div>
          <div className="mb-4">
            <p className="text-sm text-gray-500">이메일</p>
            <p className="text-lg text-gray-800">{userProfile.email}</p>
          </div>
          <div className="text-sm">
            <p className="text-gray-500">가입일: {new Date(userProfile.joinedAt).toLocaleDateString()}</p>
          </div>
        </div>
      </div>
      <div className="mt-6 flex space-x-4">
        <Link to="/mypage/edit" className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
          회원정보 수정
        </Link>
        <Link to="/mypage/password" className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700">
          비밀번호 변경
        </Link>
        <Link to="/mypage/withdraw" className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
          회원 탈퇴
        </Link>
      </div>
    </div>
  );
}
