import { defineConfig } from 'vite'
import vue2 from '@vitejs/plugin-vue2'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue2()],
  server: {
    historyApiFallback: true, // 支持 history 模式
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
})