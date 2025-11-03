import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
import tailwindcss from '@tailwindcss/vite';

// npm i -D babel-plugin-react-compiler (또는 pnpm add -D ...)
export default defineConfig({
  server: {
    proxy: {
      // '/api'로 시작하는 요청은 전부 target으로 프록시됩니다.
      '/api': {
        target: 'http://localhost:8080', // 백엔드 서버 주소
        changeOrigin: true, // cross-origin 요청을 허용합니다.
      },
      '/static': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  plugins: [
    react({
      // Compiler는 Babel 플러그인 체인의 "맨 앞"에 배치
      babel: { plugins: ['babel-plugin-react-compiler'] },
    }),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
});
