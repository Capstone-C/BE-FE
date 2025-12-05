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
    <header className="glass-effect sticky top-0 z-50 border-b border-white/20 shadow-sm">
      <Container>
        <nav className="h-16 flex justify-between items-center">
          <div className="flex items-center gap-8">
            <Link to="/" className="text-xl font-bold gradient-text hover:scale-105 transition-transform">
              🍽️ Capstone
            </Link>
            <div className="hidden md:flex items-center gap-6">
              <Link to="/community" className="text-sm font-medium text-gray-700 hover:text-purple-600 transition-colors relative group">
                커뮤니티
                <span className="absolute inset-x-0 -bottom-1 h-0.5 bg-purple-600 transform scale-x-0 group-hover:scale-x-100 transition-transform"></span>
              </Link>
              <Link to="/shopping" className="text-sm font-medium text-gray-700 hover:text-purple-600 transition-colors relative group">
                쇼핑
                <span className="absolute inset-x-0 -bottom-1 h-0.5 bg-purple-600 transform scale-x-0 group-hover:scale-x-100 transition-transform"></span>
              </Link>
              {user && (
                <>
                  <Link to="/diary" className="text-sm font-medium text-gray-700 hover:text-purple-600 transition-colors relative group">
                    다이어리
                    <span className="absolute inset-x-0 -bottom-1 h-0.5 bg-purple-600 transform scale-x-0 group-hover:scale-x-100 transition-transform"></span>
                  </Link>
                  <Link to="/refrigerator" className="text-sm font-medium text-gray-700 hover:text-purple-600 transition-colors relative group">
                    내 냉장고
                    <span className="absolute inset-x-0 -bottom-1 h-0.5 bg-purple-600 transform scale-x-0 group-hover:scale-x-100 transition-transform"></span>
                  </Link>
                </>
              )}
            </div>
          </div>
          <div className="flex items-center gap-4">
            {user ? (
              <>
                <Link to="/mypage" className="text-sm font-medium text-gray-700 hover:text-purple-600 transition-colors">
                  마이페이지
                </Link>
                <span className="hidden sm:inline text-sm font-medium text-gray-600 px-3 py-1 bg-purple-50 rounded-full">{user.nickname}님</span>
                <Button variant="outline" size="sm" onClick={handleLogout}>
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">로그인</Button>
                </Link>
                <Link to="/signup">
                  <Button size="sm">회원가입</Button>
                </Link>
              </>
            )}
          </div>
        </nav>
      </Container>
    </header>
  );
}
