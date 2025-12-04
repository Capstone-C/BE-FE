import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth.ts';

const Header: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogoutClick = (e: React.MouseEvent) => {
    e.preventDefault();
    logout();
    navigate('/');
  };

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-18">
          <div className="flex items-center space-x-8">
            <div className="flex-shrink-0">
              <Link to="/" className="flex flex-col items-start leading-tight">
                <span className="text-xs font-semibold text-gray-500 pl-px">깃밥</span>
                <span className="text-3xl font-bold text-[#4E652F]">GitBap</span>
              </Link>
            </div>
          </div>

          {/* [수정] 검색창 제거 후, 우측 정렬을 유지하기 위한 빈 공간(spacer) 추가 */}
          <div className="flex-1"></div>

          <div className="flex items-center space-x-4">
            {user ? (
              <div className="flex items-center space-x-4">
                <div className="w-10 h-10 rounded-full bg-gray-300 flex-shrink-0 flex items-center justify-center text-gray-600 font-bold" title="Profile">
                  {user.nickname.charAt(0)}
                </div>
                <span className="text-sm font-medium text-gray-700 whitespace-nowrap hidden md:inline">
                  환영합니다, <span className="font-bold text-[#4E652F]">{user.nickname}</span>님.
                </span>
                <Link to="/mypage" className="inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#71853A]">
                  마이페이지
                </Link>
                <a href="#" onClick={handleLogoutClick} className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-[#4E652F] hover:bg-[#425528] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#71853A]">
                  로그아웃
                </a>
              </div>
            ) : (
              <>
                <Link to="/login" className="text-sm font-medium text-gray-500 hover:text-gray-900">로그인</Link>
                <Link to="/signup" className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-[#4E652F] hover:bg-[#425528] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#71853A]">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;