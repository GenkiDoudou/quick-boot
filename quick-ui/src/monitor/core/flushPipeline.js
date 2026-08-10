/**
 * 批次 flush：组装 payload、过滤噪声、同步/空闲上报。
 */
import { postTrackBatch, postTrackBatchDeferred } from '../report'
import { isUrgentFlushReason } from '../scheduleIdle'
import { resolveBatchTriggerAction } from '../display/batchTrigger'
import { getOperationId, getPageVisitId } from '../batchSession'

/**
 * @param {Record<string, unknown>[]} events
 * @returns {string}
 */
export function resolveBatchOperationId(events) {
  for (const ev of events) {
    const id = ev.operationId
    if (id != null && String(id).trim()) {
      return String(id).trim()
    }
  }
  return ''
}

/**
 * @param {Record<string, unknown>[]} events
 * @returns {string}
 */
export function resolvePageVisitId(events) {
  for (const ev of events) {
    const id = ev.pageVisitId
    if (id != null && String(id).trim()) {
      return String(id).trim()
    }
  }
  return ''
}

/**
 * @param {Record<string, unknown>[]} events
 * @param {string} reason
 * @returns {boolean}
 */
export function isNoiseFlush(events, reason) {
  if (reason !== 'page_leave' || events.length !== 1) {
    return false
  }
  return events[0].type === 'route_leave'
}

/**
 * @param {{ enabled: boolean, reportUrl: string, context: ReturnType<import('./monitorContext.js').createMonitorContext>, buffer: ReturnType<import('./eventBuffer.js').createEventBuffer> }} options
 */
export function createFlushPipeline(options) {
  const { enabled, reportUrl, context, buffer } = options

  /**
   * @param {string} [reason='normal']
   * @param {string} [forcedOperationId]
   * @param {{ urgent?: boolean }} [flushOptions]
   */
  function flush(reason = 'normal', forcedOperationId, flushOptions = {}) {
    if (!enabled || !buffer.length) {
      return
    }
    const pvId = getPageVisitId() || buffer.resolvePageVisitIdFromStore()
    const events = buffer.drain()
    context.attachToEvents(events, forcedOperationId || getOperationId(), pvId)
    if (isNoiseFlush(events, reason)) {
      return
    }
    const batchOpId = forcedOperationId || resolveBatchOperationId(events)
    const batchPvId = resolvePageVisitId(events) || pvId || undefined
    const { raw: triggerAction, label: triggerLabel } = resolveBatchTriggerAction(events)
    const { browserVisitId, sessionId } = context.ensureIds()
    const payload = {
      reason,
      browserVisitId,
      sessionId,
      pageVisitId: batchPvId,
      operationId: batchOpId || undefined,
      triggerAction: triggerAction || undefined,
      triggerLabel: triggerLabel || undefined,
      events
    }
    const urgent = flushOptions.urgent === true || isUrgentFlushReason(reason)
    if (urgent) {
      postTrackBatch(reportUrl, payload)
    } else {
      postTrackBatchDeferred(reportUrl, payload)
    }
  }

  return { flush }
}
