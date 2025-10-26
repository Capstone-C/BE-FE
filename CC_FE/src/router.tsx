import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import RootLayout from '@/pages/RootLayout';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import ProfilePage from '@/pages/ProfilePage';
import ProfileEditPage from '@/pages/ProfileEditPage';
import WithdrawPage from '@/pages/WithdrawPage'; // WithdrawPage import 추가
import ProtectedRoute from './router/ProtectedRoute';
import NotFoundPage from '@/pages/NotFoundPage';
import ChangePasswordPage from '@/pages/ChangePasswordPage'; // import 추가

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },

      // 보호된 라우트 그룹
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'mypage', element: <ProfilePage /> },
          { path: 'mypage/edit', element: <ProfileEditPage /> },
          { path: 'mypage/withdraw', element: <WithdrawPage /> }, // 회원 탈퇴 페이지 라우트 추가
          { path: 'mypage/password', element: <ChangePasswordPage /> }, // 비밀번호 변경 페이지 라우트 추가
        ],
      },

      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
