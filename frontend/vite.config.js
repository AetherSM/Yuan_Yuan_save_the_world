import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/common': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/categories': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/products': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/addresses': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/orders': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/role-applications': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/refunds': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/wallet': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/complaints': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/coupons': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/errands': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/notices': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/reviews': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/shopping-records': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/users': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/wanted-posts': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
