/**
 * 前端用户行为监控入口：全局 Vue 插件 + request.js 观测注册。
 *
 * 模块结构：
 * - `plugin/`：Vue 插件安装
 * - `core/`：缓冲、flush、路由白名单
 * - `collectors/`：click / error / route / lifecycle 采集器
 * - `composables/`：组件内 `useUserMonitor()`
 * - `display/`：标签格式化、批次 trigger 展示（管理端优先从此导入）
 */
import { createUserMonitor } from './plugin/userMonitorPlugin'
import { bindRequestMonitor } from './bindRequestMonitor'
import { loadMonitorConfig, isMonitorEnabled } from './config'
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

export { createUserMonitor, bindRequestMonitor, loadMonitorConfig, isMonitorEnabled }
export { useUserMonitor, USER_MONITOR_KEY } from './composables/useUserMonitor'
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
  getOrCreateSessionId,
  resetSessionId,
  clearSessionId,
  onSessionContextChange
} from './sessionContext'
export {
  getOrCreateBrowserVisitId,
  startBrowserVisitHeartbeat,
  stopBrowserVisitHeartbeat
} from './browserVisitContext'
export {
  getOperationId,
  getPageVisitId,
  getBatchKind,
  setActivePage,
  getActivePage,
  getLastTrigger,
  openBatch,
  openPageVisit,
  flushPageVisitIfNeeded,
  touchBatch,
  touchBatchPassive,
  flushBatchSync,
  registerBatchFlushHook,
  configureBatchSession,
  isOverlayBlocking,
  cancelBatch,
  closeBatch
} from './batchSession'
export {
  isPrimaryAction,
  isPassiveAction,
  isQueryOnlyAction,
  shouldRecordPendingTrigger
} from './operationRules'
export { nextRequestTraceHeaders, shouldAttachRequestTrace } from './requestTrace'
export { formatTrackLabel, resolveBatchTriggerAction, resolveDisplayTrigger, isHumanReadableLabel, extractVisibleActionLabel } from './display'
export { readClickTarget } from './clickTarget'
export { canTrackPath } from './core/pathGuard'
export default setupUserMonitor
