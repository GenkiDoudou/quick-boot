/**
 * C7-Plus 全局配置接口
 */
export interface C7PlusConfig {
  // 全局配置
  locale?: string
  size?: 'large' | 'default' | 'small'
  zIndex?: number
  
  // 组件默认配置
  button?: {
    size?: 'large' | 'default' | 'small'
    debounce?: number
  }
  
  table?: {
    pageSize?: number
    pageSizes?: number[]
  }
}

/**
 * 全局配置存储
 */
let globalConfig: C7PlusConfig = {}

/**
 * 设置全局配置
 * @param config 配置对象
 */
export function setConfig(config: C7PlusConfig): void {
  globalConfig = { ...globalConfig, ...config }
}

/**
 * 获取全局配置
 * @returns 当前配置
 */
export function getConfig(): C7PlusConfig {
  return globalConfig
}

/**
 * 重置配置
 */
export function resetConfig(): void {
  globalConfig = {}
}

