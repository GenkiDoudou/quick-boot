/**
 * uni-app 应用入口：挂载 uView Pro（绿色主题）、Pinia，导出 createApp 供框架调用。
 */
import { createSSRApp } from 'vue'
import uViewPro from 'uview-pro'
import themes from '@/common/uview-pro.theme'
import store from '@/stores'
import App from './App.vue'

/** 创建 Vue 应用实例并注册 UI 库与全局 store */
export function createApp() {
  const app = createSSRApp(App)
  app.use(uViewPro, {
    theme: {
      themes,
      defaultTheme: 'green',
      defaultDarkMode: 'light',
    },
    locale: 'zh-CN',
  })
  app.use(store)
  return { app }
}
