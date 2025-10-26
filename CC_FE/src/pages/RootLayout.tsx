// src/pages/RootLayout.tsx
import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export default function RootLayout() {
  const { user, logout } = useAuth();

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
                <button onClick={logout} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'blue', textDecoration: 'underline' }}>
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