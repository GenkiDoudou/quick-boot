/**
 * 组件内访问全局行为监控（需 app.use(createUserMonitor) 后可用）。
 */
import { inject } from 'vue'
import { USER_MONITOR_KEY } from '../constants'

/** @typedef {{ pushEvent: (row: Record<string, unknown>) => void, flush: (reason?: string) => void, enabled: boolean }} UserMonitorApi */

const noop = () => {}

/**
 * 获取当前应用的用户行为监控 API；未安装插件时返回空实现，避免业务侧判空。
 *
 * @returns {{ track: UserMonitorApi['pushEvent'], flush: UserMonitorApi['flush'], enabled: boolean }}
 */
export function useUserMonitor() {
  /** @type {UserMonitorApi | null} */
  const monitor = inject(USER_MONITOR_KEY, null)
  if (!monitor) {
    return { track: noop, flush: noop, enabled: false }
  }
  return {
    track: monitor.pushEvent,
    flush: monitor.flush,
    enabled: monitor.enabled !== false
  }
}

export { USER_MONITOR_KEY }
export default useUserMonitor
