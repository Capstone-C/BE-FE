import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { RootLayout } from '@/features/common/pages/RootLayout';
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
import MyCommentsPage from '@/features/members/pages/MyCommentsPage';

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

// Refrigerator
import RefrigeratorPage from '@/features/refrigerator/pages/RefrigeratorPage';
import ReceiptScanPage from '../features/refrigerator/pages/ReceiptScanPage';

// Recipes
import RecipeCreatePage from '@/features/recipes/pages/RecipeCreatePage';
import RecipeEditPage from '@/features/recipes/pages/RecipeEditPage';

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
          { path: 'mypage/comments', element: <MyCommentsPage /> },

          // Auth-required boards
          { path: 'boards/new', element: <BoardNewPage /> },
          { path: 'boards/:postId/edit', element: <BoardEditPage /> },

          // 냉장고 페이지
          { path: 'refrigerator', element: <RefrigeratorPage /> },
          { path: 'refrigerator/receipt-scan', element: <ReceiptScanPage /> },

          // 레시피
          { path: 'recipes/new', element: <RecipeCreatePage /> },
          { path: 'recipes/:postId/edit', element: <RecipeEditPage /> },
        ],
      },

      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
