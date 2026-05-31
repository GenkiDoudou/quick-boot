/**
 * 前端用户行为监控默认配置（可通过环境变量覆盖）。
 * 页面白名单采用路径前缀匹配：仅对白名单内路由采集事件，避免低频页污染日志。
 */

/** @typedef {import('./createUserMonitor.js').UserMonitorOptions} UserMonitorOptions */

/**
 * 读取布尔型环境变量。
 *
 * @param {string | undefined} raw
 * @param {boolean} defaultValue
 * @returns {boolean}
 */
function envBool(raw, defaultValue) {
  if (raw === undefined || raw === '') {
    return defaultValue
  }
  return raw === 'true' || raw === '1'
}

/**
 * 是否启用前端行为监控（与 VITE_APP_MONITOR_ENABLED 一致，供 request 拦截器等热路径快速判断）。
 * @returns {boolean}
 */
export function isMonitorEnabled() {
  return envBool(import.meta.env.VITE_APP_MONITOR_ENABLED, true)
}

/**
 * @returns {UserMonitorOptions}
 */
export function loadMonitorConfig() {
  const baseApi = import.meta.env.VITE_APP_BASE_API || ''
  return {
    enabled: envBool(import.meta.env.VITE_APP_MONITOR_ENABLED, true),
    reportUrl: `${baseApi}/monitor/clientTrack/report`,
    maxKeep: Number(import.meta.env.VITE_APP_MONITOR_MAX_KEEP || 40),
    interval: Number(import.meta.env.VITE_APP_MONITOR_INTERVAL || 10000),
    /** 主操作批次 idle flush 间隔（ms），最后一次 click/api 后无新事件则上报 */
    idleMs: Number(import.meta.env.VITE_APP_MONITOR_IDLE_MS || 2000),
    slowApiMs: Number(import.meta.env.VITE_APP_MONITOR_SLOW_API_MS || 3000),
    allowPages: [
      '/login',
      '/index',
      '/system',
      '/monitor',
      '/tool',
      '/user',
      '/report',
      '/oauth'
    ],
    /** 不采集行为事件的路由前缀（监控管理页自身，避免查看日志时产生噪声） */
    excludePages: ['/system/clientTrack', '/system/clientTrackEvents', '/system/clientTrackTimeline', '/monitor/clientTrack']
  }
}

export default loadMonitorConfig
