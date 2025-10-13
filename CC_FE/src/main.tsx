import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '@/index.css';
import App from '@/pages/App';

// 1. TanStack Query에서 필요한 것들을 import 합니다.
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// 2. QueryClient 인스턴스를 생성합니다.
// 이 인스턴스가 데이터 캐싱 등 모든 것을 관리하는 주체입니다.
const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {/* 3. 기존 App 컴포넌트를 QueryClientProvider로 감싸줍니다. */}
    {/* client prop으로 위에서 만든 queryClient를 전달해야 합니다. */}
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </StrictMode>,
);
