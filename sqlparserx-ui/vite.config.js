import { defineConfig, loadEnv } from 'vite'
import vue2 from '@vitejs/plugin-vue2'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    // UI 直接部署到后端服务的 conf/static，资源从同源站点根路径加载。
    base: '/',
    plugins: [vue2()], // 放插件用的
    resolve: {
      alias: {
        '@': '/src' // 配置@/提示符
      }
    },
    server: {
      proxy: {
        '/api': {
          target: env.VITE_API_PROXY_TARGET || 'http://localhost:16758',
          changeOrigin: true
        }
      }
    }
  }
})
