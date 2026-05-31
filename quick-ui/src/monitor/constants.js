/** Vue provide/inject 键：全局用户行为监控实例 */
export const USER_MONITOR_KEY = Symbol('userMonitor')

/** 全局 click 采集：仅匹配可操作控件，避免无效 DOM 回溯 */
export const ACTIONABLE_CLICK_SELECTOR =
  'button, .el-button, .c7-button, [data-track], a[role="button"], input[type="submit"], input[type="button"]'
