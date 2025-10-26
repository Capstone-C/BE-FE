// src/main.tsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '@/index.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import AppRouter from '@/router.tsx';
import { AuthProvider } from '@/contexts/AuthContext'; // 추가

const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider> {/* AppRouter를 감싸줍니다. */}
        <AppRouter />
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
);