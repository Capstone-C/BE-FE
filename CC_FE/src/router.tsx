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

import BoardsListPage from "@/features/boards/pages/BoardsListPage";
import BoardNewPage from "@/features/boards/pages/BoardNewPage";
import BoardDetailPage from "@/features/boards/pages/BoardDetailPage";
import BoardEditPage from "@/features/boards/pages/BoardEditPage";

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

      // 게시판 공개 경로
      { path: 'boards', element: <BoardsListPage /> },
      { path: 'boards/:postId', element: <BoardDetailPage /> },

      // 보호된 라우트 그룹
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'mypage', element: <ProfilePage /> },
          { path: 'mypage/edit', element: <ProfileEditPage /> },
          { path: 'mypage/withdraw', element: <WithdrawPage /> },
          { path: 'mypage/password', element: <ChangePasswordPage /> },

          // 인증 필요한 게시판 경로
          { path: 'boards/new', element: <BoardNewPage /> },
          { path: 'boards/:postId/edit', element: <BoardEditPage /> },
        ],
      },

      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
