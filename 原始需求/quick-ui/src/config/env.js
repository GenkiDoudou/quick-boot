/**
 * 环境变量配置
 * 直接从 .env 文件中读取配置
 */

export const appConfig = {
  // 应用标题
  title: import.meta.env.VITE_APP_TITLE,
  
  // 登录页面标题
  loginTitle: import.meta.env.VITE_APP_LOGIN_TITLE,
  
  // 版权信息
  copyright: import.meta.env.VITE_APP_COPYRIGHT,
  
  // 加载提示文字
  loadingText: import.meta.env.VITE_APP_LOADING_TEXT,
  
  // 应用版本
  version: import.meta.env.VITE_APP_VERSION,
  
  // 应用描述
  description: import.meta.env.VITE_APP_DESCRIPTION,
  
  // API 基础路径
  baseApi: import.meta.env.VITE_APP_BASE_API,
  
  // 环境
  env: import.meta.env.VITE_APP_ENV
}

export default appConfig
