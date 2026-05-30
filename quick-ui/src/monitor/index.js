/**
 * 前端用户行为监控入口：全局 Vue 插件 + request.js 观测注册。
 */
import { createUserMonitor } from './createUserMonitor'
import { bindRequestMonitor } from './bindRequestMonitor'
import { loadMonitorConfig } from './config'
import request from '@/utils/request'

/**
 * 按环境配置创建并绑定监控（在 `main.js` 中调用一次即可）。
 *
 * @returns {ReturnType<typeof createUserMonitor> | null}
 */
export function setupUserMonitor() {
  const config = loadMonitorConfig()
  if (!config.enabled) {
    return null
  }
  const monitor = createUserMonitor(config)
  bindRequestMonitor(request, (row) => monitor.pushEvent(row), {
    slowApiMs: config.slowApiMs
  })
  return monitor
}

export { createUserMonitor, bindRequestMonitor, loadMonitorConfig }
export {
  registerObservationEmitter,
  beginRequestObservation,
  finalizeRequestObservationSuccess,
  finalizeRequestObservationError
} from './requestObservation'
export {
  registerApiCallTrack,
  recordApiSuccess,
  recordApiError
} from './requestObservation'
export {
  beginOperation,
  getOperationId,
  endOperation,
  runInOperation,
  registerOperationEndHook,
  registerOperationBeginHook,
  setActivePage,
  getActivePage,
  setPendingTrigger,
  getLastTrigger
} from './operationContext'
export { nextRequestTraceHeaders, shouldAttachRequestTrace } from './requestTrace'
export default setupUserMonitor
