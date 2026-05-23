/**
 * 统一日志管理
 */
const isDev = import.meta.env.DEV

export const logger = {
  /**
   * 普通日志（仅开发环境）
   */
  log: (...args: any[]) => {
    if (isDev) {
      console.log('[C7-Plus]', ...args)
    }
  },
  
  /**
   * 警告日志（仅开发环境）
   */
  warn: (...args: any[]) => {
    if (isDev) {
      console.warn('[C7-Plus]', ...args)
    }
  },
  
  /**
   * 错误日志（所有环境）
   */
  error: (...args: any[]) => {
    console.error('[C7-Plus]', ...args)
    // 生产环境可以上报错误到监控系统
    if (!isDev) {
      // TODO: 集成错误监控系统
      // reportError(...args)
    }
  },
  
  /**
   * 信息日志（仅开发环境）
   */
  info: (...args: any[]) => {
    if (isDev) {
      console.info('[C7-Plus]', ...args)
    }
  }
}

