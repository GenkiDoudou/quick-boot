/**
 * browserVisitId / sessionId 热路径缓存，避免每次 push 读 localStorage。
 */
import { getOrCreateSessionId, onSessionContextChange } from '../sessionContext'
import { getBrowserVisitId, getOrCreateBrowserVisitId } from '../browserVisitContext'

/**
 * @returns {{ refresh: () => { browserVisitId: string, sessionId: string }, attachToEvents: (events: Record<string, unknown>[], operationId?: string | null, pageVisitId?: string | null) => void, dispose: () => void }}
 */
export function createMonitorContext() {
  let cachedBrowserVisitId = ''
  let cachedSessionId = ''

  function invalidate() {
    cachedBrowserVisitId = ''
    cachedSessionId = ''
  }

  onSessionContextChange(invalidate)

  function refresh() {
    cachedBrowserVisitId = getBrowserVisitId() || getOrCreateBrowserVisitId()
    cachedSessionId = getOrCreateSessionId()
    return { browserVisitId: cachedBrowserVisitId, sessionId: cachedSessionId }
  }

  function ensureIds() {
    if (!cachedBrowserVisitId || !cachedSessionId) {
      refresh()
    }
    return { browserVisitId: cachedBrowserVisitId, sessionId: cachedSessionId }
  }

  /**
   * @param {Record<string, unknown>[]} events
   * @param {string | null | undefined} operationId
   * @param {string | null | undefined} pageVisitId
   */
  function attachToEvents(events, operationId, pageVisitId) {
    ensureIds()
    for (const ev of events) {
      if (!ev.browserVisitId) {
        ev.browserVisitId = cachedBrowserVisitId
      }
      if (!ev.sessionId) {
        ev.sessionId = cachedSessionId
      }
      if (pageVisitId && !ev.pageVisitId) {
        ev.pageVisitId = pageVisitId
      }
      if (operationId && !ev.operationId) {
        ev.operationId = operationId
      }
    }
  }

  return {
    refresh,
    ensureIds,
    attachToEvents,
    getIds: () => ensureIds(),
    dispose: () => {}
  }
}
