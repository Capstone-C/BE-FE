import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath, URL } from 'node:url'
import tailwindcss from '@tailwindcss/vite'

// npm i -D babel-plugin-react-compiler (또는 pnpm add -D ...)
export default defineConfig({
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
})
