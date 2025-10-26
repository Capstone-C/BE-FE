// src/router.tsx
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import RootLayout from '@/pages/RootLayout';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/LoginPage';
import NotFoundPage from '@/pages/NotFoundPage';

const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      {
        path: 'signup', // ⬅️ 여기에 추가
        lazy: async () => {
          const { default: Component } = await import('@/pages/SignupPage');
          return { Component };
        },
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />; // ← fallbackElement 제거
}




