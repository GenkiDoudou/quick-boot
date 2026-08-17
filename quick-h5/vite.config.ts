import { fileURLToPath, URL } from 'node:url'

import Uni from '@uni-helper/plugin-uni'
import Components from '@uni-helper/vite-plugin-uni-components'
import { uViewProResolver } from '@uni-helper/vite-plugin-uni-components/resolvers'
import UniRoot from '@uni-ku/root'
import { defineConfig } from 'vite'

export default defineConfig({
  // 生产 H5 挂在 Nginx /h5/；开发与其它端保持根路径
  base:
    process.env.UNI_PLATFORM === 'h5' && process.env.NODE_ENV === 'production' ? '/h5/' : '/',
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@quickboot/lite-rum': fileURLToPath(new URL('../packages/lite-rum/src/index.js', import.meta.url)),
    },
  },
  plugins: [
    UniRoot(),
    Components({
      dts: true,
      resolvers: [uViewProResolver()],
    }),
    Uni(),
  ],
  server: {
    port: 5173,
    host: true,
    proxy: {
      // 对齐 quick-ui：H5 开发走同源代理，避免浏览器 CORS
      '/dev-api': {
        target: 'http://127.0.0.1:9993',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/dev-api/, ''),
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: '@import "uview-pro/theme.scss";',
      },
    },
  },
  optimizeDeps: {
    exclude: process.env.UNI_PLATFORM === 'h5' && process.env.NODE_ENV === 'development' ? ['uview-pro'] : [],
  },
})
