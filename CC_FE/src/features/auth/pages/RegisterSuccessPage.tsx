import { useLocation, useNavigate } from 'react-router-dom';

export default function RegisterSuccessPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { nickname, email } = location.state || { nickname: '회원', email: '' };

  return (
    <div className="flex-grow flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-[#F0F5E5] min-h-[calc(100vh-5rem)]">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg text-center">
        <div className="flex justify-center text-6xl mb-4">
          🎉
        </div>
        <div>
          <h2 className="mt-6 text-2xl font-bold text-gray-900">
            회원이 되신 것을 환영합니다!
          </h2>
          <p className="mt-4 text-lg text-gray-600">
            <span className="font-semibold text-[#4E652F]">{nickname}</span>
            {email && <span className="text-gray-500">({email})</span>} 님,
          </p>
          <p className="mt-1 text-lg text-gray-600">
            깃밥의 가족이 되셨습니다.
          </p>
        </div>
        <div className="mt-8">
          <button
            onClick={() => navigate('/login')}
            className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-lg font-medium rounded-md text-white bg-[#4E652F] hover:bg-[#425528] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#71853A] transition-colors"
          >
            로그인 하러 가기
          </button>
        </div>
      </div>
    </div>
  );
}