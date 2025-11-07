import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import RootLayout from '@/features/common/pages/RootLayout';
import HomePage from '@/features/common/pages/HomePage';
import NotFoundPage from '@/features/common/pages/NotFoundPage';
import ProtectedRoute from './ProtectedRoute';

// Auth
import LoginPage from '@/features/auth/pages/LoginPage';
import SignupPage from '@/features/auth/pages/SignupPage';
import ChangePasswordPage from '@/features/auth/pages/ChangePasswordPage';
import FindPasswordPage from '@/features/auth/pages/FindPasswordPage';
import ResetPasswordPage from '@/features/auth/pages/ResetPasswordPage';

// Members
import ProfilePage from '@/features/members/pages/ProfilePage';
import ProfileEditPage from '@/features/members/pages/ProfileEditPage';
import WithdrawPage from '@/features/members/pages/WithdrawPage';
import MyPostsPage from '@/features/members/pages/MyPostsPage';

// Boards
import BoardsListPage from '@/features/boards/pages/BoardsListPage';
import BoardNewPage from '@/features/boards/pages/BoardNewPage';
import BoardDetailPage from '@/features/boards/pages/BoardDetailPage';
import BoardEditPage from '@/features/boards/pages/BoardEditPage';

// Diary
import DiaryCalendarPage from '@/features/diary/pages/DiaryCalendarPage';
import DiaryCreatePage from '@/features/diary/pages/DiaryCreatePage';
import DiaryDayPage from '@/features/diary/pages/DiaryDayPage';
import DiaryEditPage from '@/features/diary/pages/DiaryEditPage';

// Community
import CommunityPage from '@/features/community/pages/CommunityPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },

      // Public auth routes
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'find-password', element: <FindPasswordPage /> },
      { path: 'reset-password', element: <ResetPasswordPage /> },

      // Public boards
      { path: 'boards', element: <BoardsListPage /> },
      { path: 'boards/:postId', element: <BoardDetailPage /> },
      { path: 'community', element: <CommunityPage /> },

      // Diary
      { path: 'diary', element: <DiaryCalendarPage /> },
      { path: 'diary/:date', element: <DiaryDayPage /> },
      { path: 'diary/:date/new', element: <DiaryCreatePage /> },
      { path: 'diary/:date/edit/:id', element: <DiaryEditPage /> },

      // Protected routes
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'mypage', element: <ProfilePage /> },
          { path: 'mypage/edit', element: <ProfileEditPage /> },
          { path: 'mypage/withdraw', element: <WithdrawPage /> },
          { path: 'mypage/password', element: <ChangePasswordPage /> },
          { path: 'mypage/posts', element: <MyPostsPage /> },

          // Auth-required boards
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
