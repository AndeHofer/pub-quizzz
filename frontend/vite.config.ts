import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/admin': 'http://localhost:8080',
      '/api': 'http://localhost:8080',
      '/user': 'http://localhost:8080',
      '/results': 'http://localhost:8080',
      '/leaderboard': 'http://localhost:8080',
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    minify: false, // Disables minification for "pretty" mode
  }
})
