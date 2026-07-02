import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

// 개발 프록시: 프론트(5173)의 /api 요청을 백엔드(8080)로 전달 → dev에서 CORS 불필요.
// (직접 호출이 필요하면 VITE_API_BASE_URL로 백엔드 주소를 지정 — 백엔드 CORS가 허용)
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'node',
    globals: true,
  },
});
