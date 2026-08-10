/**
 * 页面卸载、定时 flush 等生命周期钩子。
 */
import { cancelBatch, flushBatchSync, getBatchKind } from '../batchSession'

/**
 * @param {{
 *   intervalMs: number,
 *   isTracking: () => boolean,
 *   hasBufferedEvents: () => boolean,
 *   flush: (reason?: string) => void
 * }} deps
 * @returns {{ dispose: () => void }}
 */
export function bindLifecycleCollector(deps) {
  const { intervalMs, isTracking, hasBufferedEvents, flush } = deps

  const onBeforeUnload = () => {
    flushBatchSync('leave')
  }
  window.addEventListener('beforeunload', onBeforeUnload)

  const timerId = window.setInterval(() => {
    if (!isTracking() || !hasBufferedEvents()) {
      return
    }
    if (getBatchKind() !== 'action') {
      return
    }
    flush('timer')
    cancelBatch()
  }, intervalMs)

  function dispose() {
    window.removeEventListener('beforeunload', onBeforeUnload)
    clearInterval(timerId)
  }

  return { dispose }
}
