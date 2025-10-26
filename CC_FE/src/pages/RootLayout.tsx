// src/pages/RootLayout.tsx
import { Outlet, Link, useNavigate } from 'react-router-dom'; // useNavigate 추가
import { useAuth } from '@/hooks/useAuth';

export default function RootLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    // TODO: "로그아웃 되었습니다" 같은 Toast 메시지를 띄우면 사용자 경험이 향상됩니다.
    alert('안전하게 로그아웃 되었습니다.'); // 임시 알림
    navigate('/'); // 메인 페이지로 이동
  };

  return (
    <div>
      <header style={{ padding: 12, borderBottom: '1px solid #eee' }}>
        <nav style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', gap: 12 }}>
            <Link to="/">홈</Link>
          </div>
          <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
            {user ? (
              <>
                <span>{user.nickname}님, 환영합니다!</span>
                <button
                  onClick={handleLogout}
                  style={{
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    color: 'blue',
                    textDecoration: 'underline',
                  }}
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login">로그인</Link>
                <Link to="/signup">회원가입</Link>
              </>
            )}
          </div>
        </nav>
      </header>
      <main className="p-4">
        <Outlet />
      </main>
    </div>
  );
}
