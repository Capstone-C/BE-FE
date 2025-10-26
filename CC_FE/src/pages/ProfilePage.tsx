import { useQuery } from '@tanstack/react-query';
import { getMe } from '@/apis/auth';
import { Link } from 'react-router-dom';

export default function ProfilePage() {
  const {
    data: userProfile,
    isPending,
    isError,
    error,
  } = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
  });

  if (isPending) {
    return <div className="p-8 text-center">프로필 정보를 불러오는 중입니다...</div>;
  }

  // 401 등의 에러는 ProtectedRoute에서 처리되지만,
  // 404, 500 등 다른 서버 에러를 대비한 방어 코드입니다.
  if (isError) {
    console.error('프로필 조회 실패:', error);
    return <div className="p-8 text-center text-red-600">프로필 정보를 불러오는 중 오류가 발생했습니다.</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">마이페이지</h1>
      <div className="bg-white shadow-md rounded-lg p-6">
        <div className="mb-4">
          <p className="text-sm text-gray-500">닉네임</p>
          <p className="text-xl font-semibold">{userProfile.nickname}</p>
        </div>
        <div className="mb-4">
          <p className="text-sm text-gray-500">이메일</p>
          <p className="text-lg text-gray-800">{userProfile.email}</p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-gray-500">가입일</p>
            <p className="text-gray-800">{new Date(userProfile.joinedAt).toLocaleString()}</p>
          </div>
          <div>
            <p className="text-gray-500">마지막 로그인</p>
            <p className="text-gray-800">{new Date(userProfile.lastLoginAt).toLocaleString()}</p>
          </div>
        </div>
        <hr className="my-6" />
        <div className="flex space-x-4">
          <Link to="/mypage/edit" className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
            회원정보 수정
          </Link>
          <Link to="/mypage/change-password" className="px-4 py-2 bg-gray-600 text-white rounded hover:bg-gray-700">
            비밀번호 변경
          </Link>
        </div>
      </div>
    </div>
  );
}
