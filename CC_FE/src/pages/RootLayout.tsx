// src/pages/RootLayout.tsx
import { Outlet, Link } from 'react-router-dom';

export default function RootLayout() {
  return (
    <div>
      <header style={{ padding: 12, borderBottom: '1px solid #eee' }}>
        <nav style={{ display: 'flex', gap: 12 }}>
          <Link to="/">홈</Link>
          <Link to="/login">로그인</Link>
        </nav>
      </header>
      <Outlet />
    </div>
  );
}
