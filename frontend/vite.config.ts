import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite 設定：開発サーバーで /api リクエストをバックエンド Spring Boot にプロキシし、ブラウザの CORS を回避する
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
})
