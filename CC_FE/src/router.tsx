import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import RootLayout from '@/pages/RootLayout';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/LoginPage';
import ProfilePage from '@/pages/ProfilePage';
import ProtectedRoute from './router/ProtectedRoute';
import NotFoundPage from '@/pages/NotFoundPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      // --- Public Routes ---
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'signup',
        lazy: async () => {
          const { default: Component } = await import('@/pages/SignupPage');
          return { Component };
        },
      },

      // --- Protected Routes ---
      {
        element: <ProtectedRoute />,
        children: [
          {
            path: 'mypage',
            element: <ProfilePage />,
          },
          // TODO: 향후 '회원정보 수정' 등 다른 보호된 페이지를 이곳에 추가합니다.
          // { path: 'mypage/edit', element: <ProfileEditPage /> },
        ],
      },

      // --- Not Found Route ---
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
