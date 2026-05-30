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
 * @returns {UserMonitorOptions}
 */
export function loadMonitorConfig() {
  const baseApi = import.meta.env.VITE_APP_BASE_API || ''
  return {
    enabled: envBool(import.meta.env.VITE_APP_MONITOR_ENABLED, true),
    reportUrl: `${baseApi}/monitor/clientTrack/report`,
    maxKeep: Number(import.meta.env.VITE_APP_MONITOR_MAX_KEEP || 40),
    interval: Number(import.meta.env.VITE_APP_MONITOR_INTERVAL || 10000),
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
    ]
  }
}

export default loadMonitorConfig
