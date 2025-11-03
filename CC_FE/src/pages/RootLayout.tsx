import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export default function RootLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    alert('안전하게 로그아웃 되었습니다.');
    navigate('/');
  };

  return (
    <div className="flex flex-col min-h-screen">
      <header className="bg-white shadow-sm sticky top-0 z-10">
        <nav className="container mx-auto px-4 h-16 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-lg font-bold text-gray-800 hover:text-blue-600">
              홈
            </Link>
          </div>
          <div className="flex items-center gap-4">
            {user ? (
              <>
                <Link to="/mypage" className="text-sm font-medium text-gray-600 hover:text-blue-600">
                  마이페이지
                </Link>
                <span className="text-sm">
                  <span className="font-semibold">{user.nickname}</span>님
                </span>
                <button onClick={handleLogout} className="text-sm font-medium text-gray-600 hover:text-blue-600">
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm font-medium text-gray-600 hover:text-blue-600">
                  로그인
                </Link>
                <Link to="/signup" className="text-sm font-medium text-gray-600 hover:text-blue-600">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </nav>
      </header>
      <main className="flex-grow bg-gray-50">
        <Outlet />
      </main>
    </div>
  );
}
