/**
 * 路由采集白/黑名单判断与热路径缓存。
 */

/**
 * 判断当前路由是否应采集监控事件。
 *
 * @param {string} path
 * @param {string[]} allowPages 路径前缀白名单；空数组表示不限制
 * @param {string[]} [excludePages] 路径前缀黑名单（优先于白名单）
 * @returns {boolean}
 */
export function canTrackPath(path, allowPages, excludePages = []) {
  if (!path) {
    return false
  }
  if (excludePages.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))) {
    return false
  }
  if (!allowPages || allowPages.length === 0) {
    return true
  }
  return allowPages.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))
}

/**
 * 创建带路径缓存的采集开关（避免每次 click 扫描白名单）。
 *
 * @param {{ enabled: boolean, allowPages: string[], excludePages: string[], getCurrentRoute: () => string }} options
 * @returns {{ isTracking: () => boolean, invalidate: () => void, setRoute: (path: string) => void }}
 */
export function createTrackingGuard(options) {
  const { enabled, allowPages, excludePages, getCurrentRoute } = options
  let cachePath = ''
  let cacheValue = false

  function invalidate() {
    cachePath = ''
  }

  function setRoute(path) {
    invalidate()
    if (typeof path === 'string') {
      cachePath = path
      cacheValue = canTrackPath(path, allowPages, excludePages)
    }
  }

  function isTracking() {
    if (!enabled) {
      return false
    }
    const path = getCurrentRoute()
    if (path === cachePath) {
      return cacheValue
    }
    cachePath = path
    cacheValue = canTrackPath(path, allowPages, excludePages)
    return cacheValue
  }

  return { isTracking, invalidate, setRoute }
}
