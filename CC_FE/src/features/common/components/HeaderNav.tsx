import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import Container from '@/components/ui/Container';
import Button from '@/components/ui/Button';

export function HeaderNav() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <header className="bg-white sticky top-0 z-10 border-b">
      <Container>
        <nav className="h-16 flex justify-between items-center">
          <div className="flex items-center gap-6">
            <Link to="/" className="text-lg font-bold text-gray-900">
              Capstone
            </Link>
            <Link to="/community" className="text-sm text-gray-700 hover:text-blue-600">
              커뮤니티
            </Link>
            {user && (
              <>
                <Link to="/diary" className="text-sm text-gray-700 hover:text-blue-600">
                  다이어리
                </Link>
                <Link to="/refrigerator" className="text-sm text-gray-700 hover:text-blue-600">
                  내 냉장고
                </Link>
              </>
            )}
          </div>
          <div className="flex items-center gap-3">
            {user ? (
              <>
                <Link to="/mypage" className="text-sm text-gray-700 hover:text-blue-600">
                  마이페이지
                </Link>
                <span className="text-sm text-gray-600">{user.nickname}님</span>
                <Button variant="ghost" onClick={handleLogout}>
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm text-gray-700 hover:text-blue-600">
                  로그인
                </Link>
                <Link to="/signup" className="text-sm text-gray-700 hover:text-blue-600">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </nav>
      </Container>
    </header>
  );
}
