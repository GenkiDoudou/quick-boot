/**
 * 内存事件缓冲：push、溢出裁剪、从 store 反查 pageVisitId。
 */
import { getOperationId, getPageVisitId, getBatchKind, touchBatch } from '../batchSession'

/**
 * @param {{ maxKeep: number, getCurrentRoute: () => string, context: ReturnType<import('./monitorContext.js').createMonitorContext>, isTracking: () => boolean, onErrorEvent?: () => void }} options
 */
export function createEventBuffer(options) {
  const { maxKeep, getCurrentRoute, context, isTracking, onErrorEvent } = options
  /** @type {Record<string, unknown>[]} */
  const store = []

  function discard() {
    store.length = 0
  }

  function resolvePageVisitIdFromStore() {
    for (let i = store.length - 1; i >= 0; i -= 1) {
      const id = store[i].pageVisitId
      if (id != null && String(id).trim()) {
        return String(id).trim()
      }
    }
    return ''
  }

  /**
   * @param {Record<string, unknown>} row
   */
  function pushEvent(row) {
    if (!isTracking()) {
      return
    }
    const opId = getOperationId()
    const pvId = getPageVisitId()
    context.ensureIds()
    const { browserVisitId, sessionId } = context.getIds()
    const item = {
      page: getCurrentRoute() || (typeof location !== 'undefined' ? location.pathname : ''),
      ts: Date.now(),
      browserVisitId,
      sessionId,
      ...(pvId ? { pageVisitId: pvId } : {}),
      ...(opId ? { operationId: opId } : {}),
      ...row
    }
    store.push(item)
    if (store.length > maxKeep) {
      store.shift()
    }
    if (getBatchKind() === 'action') {
      touchBatch()
    }
    if (row.level === 'error' && onErrorEvent) {
      onErrorEvent()
    }
  }

  return {
    pushEvent,
    discard,
    drain: () => store.splice(0, store.length),
    get length() {
      return store.length
    },
    resolvePageVisitIdFromStore
  }
}
