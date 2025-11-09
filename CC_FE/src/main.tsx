// src/main.tsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import '@/index.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import AppRouter from '@/router';
import { AuthProvider } from '@/contexts/AuthProvider';
import { ToastProvider } from '@/contexts/ToastContext';
import { AutocompleteBlocker } from '@/contexts/AutocompleteBlocker';

const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <AutocompleteBlocker>
          <ToastProvider>
            <AppRouter />
          </ToastProvider>
        </AutocompleteBlocker>
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
);
