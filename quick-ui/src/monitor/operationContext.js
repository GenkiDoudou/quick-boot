/**
 * 兼容层：对外保留 operationContext API，内部委托 batchSession 隐式批次。
 * @deprecated 业务代码请勿再调用 beginOperation/endOperation；监控已全局自动关联。
 */
import {
  openBatch,
  openPageVisit,
  touchBatch,
  touchBatchPassive,
  closeBatch,
  cancelBatch,
  flushBatchSync,
  flushPageVisitIfNeeded,
  setActivePage,
  getActivePage,
  getOperationId,
  getPageVisitId,
  getBatchKind,
  getLastTrigger,
  registerBatchFlushHook,
  resetBatchSessionForTest,
  configureBatchSession,
  isOverlayBlocking
} from './batchSession'

/**
 * @deprecated 请依赖全局 click 监控；DEV 下输出提示。
 * @param {string} [reason]
 * @returns {string}
 */
export function beginOperation(reason) {
  if (import.meta.env.DEV) {
    // eslint-disable-next-line no-console
    console.warn('[monitor] beginOperation 已废弃，请移除业务侧调用，监控将自动关联批次')
  }
  return openBatch(reason || '')
}

/**
 * @deprecated
 * @param {string} [reason]
 * @returns {string}
 */
export function ensureOperation(reason) {
  if (getOperationId()) {
    return getOperationId()
  }
  return beginOperation(reason)
}

/**
 * @deprecated
 */
export function endOperation() {
  if (import.meta.env.DEV) {
    // eslint-disable-next-line no-console
    console.warn('[monitor] endOperation 已废弃，请移除业务侧调用')
  }
  flushBatchSync('operation_end')
}

/**
 * @deprecated 无操作，保留 API 兼容。
 */
export function suppressEndOperation() {}

/**
 * @deprecated 无操作，保留 API 兼容。
 */
export function resumeEndOperation() {}

/**
 * @deprecated 使用 registerBatchFlushHook
 * @param {(endingOperationId: string | null) => void} fn
 */
export function registerOperationEndHook(fn) {
  registerBatchFlushHook((reason, opId) => fn(opId))
}

/**
 * @deprecated 无操作，全局 click 已记录触发按钮。
 */
export function registerOperationBeginHook() {}

/**
 * @deprecated
 * @param {string} label
 */
export function setPendingTrigger() {}

export function cancelOperation() {
  cancelBatch()
}

/**
 * @param {() => T | Promise<T>} fn
 * @param {string} [reason]
 * @template T
 * @returns {Promise<T>}
 */
export async function runInOperation(fn, reason) {
  openBatch(reason || '')
  try {
    return await fn()
  } finally {
    flushBatchSync('operation_end')
  }
}

export {
  setActivePage,
  getActivePage,
  getOperationId,
  getPageVisitId,
  getBatchKind,
  getLastTrigger,
  openBatch,
  openPageVisit,
  flushPageVisitIfNeeded,
  touchBatch,
  touchBatchPassive,
  closeBatch,
  cancelBatch,
  flushBatchSync,
  registerBatchFlushHook,
  resetBatchSessionForTest,
  configureBatchSession,
  isOverlayBlocking
}

export default {
  beginOperation,
  ensureOperation,
  getOperationId,
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
  touchBatch,
  touchBatchPassive,
  flushBatchSync
}
