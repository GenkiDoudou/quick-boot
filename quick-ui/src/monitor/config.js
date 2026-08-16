/**
 * Lite RUM / 请求观测开关（原 clientTrack 行为监控已移除）。
 */

/**
 * @param {string | undefined} raw
 * @param {boolean} defaultValue
 */
function envBool(raw, defaultValue) {
  if (raw === undefined || raw === '') {
    return defaultValue
  }
  return raw === 'true' || raw === '1'
}

/**
 * 是否启用请求观测头注入（与 Lite RUM 对齐；关闭则不写 x-trace-id 观测元数据）。
 * @returns {boolean}
 */
export function isMonitorEnabled() {
  return envBool(import.meta.env.VITE_APP_LITE_RUM_ENABLED, true)
}

export default {
  isMonitorEnabled
}
