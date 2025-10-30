import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import RootLayout from '@/pages/RootLayout';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/LoginPage';
import SignupPage from '@/pages/SignupPage';
import ProfilePage from '@/pages/ProfilePage';
import ProfileEditPage from '@/pages/ProfileEditPage';
import WithdrawPage from '@/pages/WithdrawPage';
import ProtectedRoute from './router/ProtectedRoute';
import NotFoundPage from '@/pages/NotFoundPage';
import ChangePasswordPage from '@/pages/ChangePasswordPage';
import FindPasswordPage from '@/pages/FindPasswordPage';
import ResetPasswordPage from '@/pages/ResetPasswordPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'find-password', element: <FindPasswordPage /> }, // 1단계 페이지 라우트 추가
      { path: 'reset-password', element: <ResetPasswordPage /> }, // 2단계 페이지 라우트 추가

      // 보호된 라우트 그룹
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'mypage', element: <ProfilePage /> },
          { path: 'mypage/edit', element: <ProfileEditPage /> },
          { path: 'mypage/withdraw', element: <WithdrawPage /> },
          { path: 'mypage/password', element: <ChangePasswordPage /> },
        ],
      },

      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
