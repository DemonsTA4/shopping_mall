// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue' // 或其他框架插件
import { fileURLToPath } from 'url'
import path from 'path'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@import "@/assets/styles/variables.scss";`
      }
    }
  },
  server: {
    port: 3100,
    open: true,
    cors: true,
    strictPort: false,
    historyApiFallback: true,
    proxy: {
      // 代理 API 请求
      '/api': {
        target: 'http://localhost:8080', // 您的后端API服务器
        changeOrigin: true,
        secure: false,
        // 如果后端API路径本身不带/api前缀，但前端请求时带了/api，则需要rewrite
        // 例如，前端请求 /api/products -> 后端实际是 /products
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
      // ★★★ 添加代理规则来处理静态图片资源 ★★★
      '/static/images': { // 匹配所有以 /static/images 开头的请求
        target: 'http://localhost:8080', // 您的后端静态资源服务器（与API服务器相同）
        changeOrigin: true,
        secure: false
        // 通常静态资源路径不需要 rewrite，除非后端配置的 URL 路径段与前端请求的不完全一样
      }
      // 或者，如果您所有静态资源都通过 /static 访问，可以更通用一些：
      // '/static': {
      //   target: 'http://localhost:8080',
      //   changeOrigin: true,
      //   secure: false
      // }
    }
  },
  build: {
    // ... 您的构建配置 ...
  }
})