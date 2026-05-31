/**
 * 前端用户行为监控入口：全局 Vue 插件 + request.js 观测注册。
 */
import { createUserMonitor } from './createUserMonitor'
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
  clearSessionId
} from './sessionContext'
export {
  getOrCreateBrowserVisitId,
  startBrowserVisitHeartbeat,
  stopBrowserVisitHeartbeat
} from './browserVisitContext'
export {
  beginOperation,
  ensureOperation,
  getOperationId,
  getPageVisitId,
  getBatchKind,
  endOperation,
  cancelOperation,
  runInOperation,
  registerOperationEndHook,
  registerOperationBeginHook,
  setActivePage,
  getActivePage,
  setPendingTrigger,
  getLastTrigger,
  suppressEndOperation,
  resumeEndOperation,
  openBatch,
  openPageVisit,
  flushPageVisitIfNeeded,
  touchBatch,
  touchBatchPassive,
  flushBatchSync,
  registerBatchFlushHook,
  configureBatchSession,
  isOverlayBlocking
} from './operationContext'
export {
  isPrimaryAction,
  isPassiveAction,
  isQueryOnlyAction,
  shouldRecordPendingTrigger
} from './operationRules'
export { nextRequestTraceHeaders, shouldAttachRequestTrace } from './requestTrace'
export { formatTrackLabel, resolveBatchTriggerAction, resolveDisplayTrigger, isHumanReadableLabel, extractVisibleActionLabel } from './trackLabel'
export { readClickTarget } from './clickTarget'
export default setupUserMonitor
