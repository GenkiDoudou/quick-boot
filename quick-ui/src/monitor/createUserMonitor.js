/**
 * Vue 全局用户行为监控插件（轻量版，对齐公众号「全局插件 + 本地缓冲 + 错误立即 flush」方案）。
 */
import { postTrackBatch, postTrackBatchDeferred } from './report'
import { isUrgentFlushReason } from './scheduleIdle'
import { resolveBatchTriggerAction } from './trackLabel'
import { isPrimaryAction, isPassiveAction, isQueryOnlyAction } from './operationRules'
import { readClickTarget } from './clickTarget'
import { getOrCreateSessionId, onSessionContextChange } from './sessionContext'
import { getBrowserVisitId, getOrCreateBrowserVisitId, startBrowserVisitHeartbeat } from './browserVisitContext'
import {
  configureBatchSession,
  registerBatchFlushHook,
  openBatch,
  openPageVisit,
  touchBatch,
  touchBatchPassive,
  cancelBatch,
  flushBatchSync,
  getOperationId,
  getPageVisitId,
  getBatchKind,
  setActivePage
} from './batchSession'

/**
 * @typedef {Object} UserMonitorOptions
 * @property {boolean} [enabled=true]
 * @property {string} [reportUrl]
 * @property {number} [maxKeep=40]
 * @property {number} [interval=10000]
 * @property {number} [idleMs=2000]
 * @property {string[]} [allowPages] 路径前缀白名单
 * @property {string[]} [excludePages] 路径前缀黑名单（优先于白名单）
 */

/**
 * 判断当前路由是否应采集监控事件。
 *
 * @param {string} path
 * @param {string[]} allowPages
 * @param {string[]} [excludePages]
 * @returns {boolean}
 */
export function canTrackPath(path, allowPages, excludePages = []) {
  if (!path) {
    return false
  }
  if (excludePages.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))) {
    return false
  }
  if (!allowPages || allowPages.length === 0) {
    return true
  }
  return allowPages.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))
}

/**
 * 创建用户行为监控实例；可作为 Vue 插件 `app.use(monitor, { router })` 安装。
 *
 * @param {UserMonitorOptions} [options]
 * @returns {{ install: Function, pushEvent: Function, flush: Function }}
 */
export function createUserMonitor(options = {}) {
  const enabled = options.enabled !== false
  const store = []
  const maxKeep = options.maxKeep || 40
  const reportUrl = options.reportUrl || '/monitor/clientTrack/report'
  const allowPages = options.allowPages || []
  const excludePages = options.excludePages || []
  const intervalMs = options.interval || 10000
  const idleMs = options.idleMs ?? 2000
  let currentRoute = typeof location !== 'undefined' ? location.pathname : ''
  let timerId = null
  /** @type {ReturnType<typeof setTimeout> | null} */
  let queryFlushTimer = null
  /** 热路径缓存，避免每次 push 读 localStorage */
  let cachedBrowserVisitId = ''
  let cachedSessionId = ''
  /** canTrackPath 结果缓存，避免每次 click 扫描白名单 */
  let trackingCachePath = ''
  let trackingCacheValue = false

  function invalidateContextCache() {
    cachedBrowserVisitId = ''
    cachedSessionId = ''
  }

  configureBatchSession({ idleMs })

  onSessionContextChange(invalidateContextCache)

  function invalidateTrackingCache() {
    trackingCachePath = ''
  }

  function refreshContextIds() {
    cachedBrowserVisitId = getBrowserVisitId() || getOrCreateBrowserVisitId()
    cachedSessionId = getOrCreateSessionId()
    return { browserVisitId: cachedBrowserVisitId, sessionId: cachedSessionId }
  }

  function isTracking() {
    if (!enabled) {
      return false
    }
    if (currentRoute === trackingCachePath) {
      return trackingCacheValue
    }
    trackingCachePath = currentRoute
    trackingCacheValue = canTrackPath(currentRoute, allowPages, excludePages)
    return trackingCacheValue
  }

  function clearQueryFlushTimer() {
    if (queryFlushTimer) {
      clearTimeout(queryFlushTimer)
      queryFlushTimer = null
    }
  }

  function discardBuffer() {
    store.length = 0
  }

  function resetMonitorOnExcludedPage() {
    clearQueryFlushTimer()
    cancelBatch()
    discardBuffer()
  }

  function scheduleQueryFlush() {
    if (!isTracking()) {
      return
    }
    clearQueryFlushTimer()
    queryFlushTimer = setTimeout(() => {
      queryFlushTimer = null
      if (!isTracking() || !store.length) {
        return
      }
      if (!getOperationId()) {
        flush('action_click')
      }
    }, 1500)
  }

  /**
   * @param {Record<string, unknown>[]} events
   * @param {string | null | undefined} operationId
   * @param {string | null | undefined} pageVisitId
   */
  function attachBatchContext(events, operationId, pageVisitId) {
    if (!cachedBrowserVisitId || !cachedSessionId) {
      refreshContextIds()
    }
    const sessionId = cachedSessionId
    const browserVisitId = cachedBrowserVisitId
    for (const ev of events) {
      if (!ev.browserVisitId) {
        ev.browserVisitId = browserVisitId
      }
      if (!ev.sessionId) {
        ev.sessionId = sessionId
      }
      if (pageVisitId && !ev.pageVisitId) {
        ev.pageVisitId = pageVisitId
      }
      if (operationId && !ev.operationId) {
        ev.operationId = operationId
      }
    }
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
    if (!cachedBrowserVisitId || !cachedSessionId) {
      refreshContextIds()
    }
    const item = {
      page: currentRoute || (typeof location !== 'undefined' ? location.pathname : ''),
      ts: Date.now(),
      browserVisitId: cachedBrowserVisitId,
      sessionId: cachedSessionId,
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
    if (row.level === 'error') {
      flush('error', undefined, { urgent: true })
    }
  }

  /**
   * @param {string} [reason='normal']
   * @param {string} [forcedOperationId]
   * @param {{ urgent?: boolean }} [options]
   */
  function flush(reason = 'normal', forcedOperationId, options = {}) {
    if (!enabled || !store.length) {
      return
    }
    const pvId = getPageVisitId() || resolvePageVisitIdFromStore()
    attachBatchContext(store, forcedOperationId || getOperationId(), pvId)
    const events = store.splice(0, store.length)
    if (isNoiseFlush(events, reason)) {
      return
    }
    const batchOpId = forcedOperationId || resolveBatchOperationId(events)
    const batchPvId = resolvePageVisitId(events) || pvId || undefined
    const { raw: triggerAction, label: triggerLabel } = resolveBatchTriggerAction(events)
    if (!cachedBrowserVisitId || !cachedSessionId) {
      refreshContextIds()
    }
    const payload = {
      reason,
      browserVisitId: cachedBrowserVisitId,
      sessionId: cachedSessionId,
      pageVisitId: batchPvId,
      operationId: batchOpId || undefined,
      triggerAction: triggerAction || undefined,
      triggerLabel: triggerLabel || undefined,
      events
    }
    const urgent = options.urgent === true || isUrgentFlushReason(reason)
    if (urgent) {
      postTrackBatch(reportUrl, payload)
    } else {
      postTrackBatchDeferred(reportUrl, payload)
    }
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

  registerBatchFlushHook((reason, operationId) => {
    if (!enabled) {
      return
    }
    flush(reason, operationId || undefined, { urgent: isUrgentFlushReason(reason) })
  })

  return {
    pushEvent,
    flush,
    install(app, pluginOptions = {}) {
      if (!enabled) {
        return
      }
      const router = pluginOptions.router
      refreshContextIds()
      startBrowserVisitHeartbeat()
      app.config.globalProperties.$track = pushEvent

      /** 仅匹配可操作控件，避免每次 click 做无效 DOM 回溯 */
      const ACTIONABLE_SELECTOR =
        'button, .el-button, .c7-button, [data-track], a[role="button"], input[type="submit"], input[type="button"]'

      document.addEventListener(
        'click',
        (e) => {
          if (!isTracking()) {
            return
          }
          const rawTarget = e.target
          if (!(rawTarget instanceof Element)) {
            return
          }
          const actionable = rawTarget.closest(ACTIONABLE_SELECTOR)
          if (!actionable || !(actionable instanceof HTMLElement)) {
            return
          }
          const { label, isAction } = readClickTarget(actionable)
          if (!isAction) {
            return
          }
          if (isPrimaryAction(label)) {
            openBatch(label)
          } else if (isPassiveAction(label)) {
            touchBatchPassive()
          }
          pushEvent({
            type: 'click',
            target: label,
            x: e.clientX,
            y: e.clientY
          })
          if (isQueryOnlyAction(label)) {
            scheduleQueryFlush()
          }
        },
        { passive: true }
      )

      window.addEventListener('error', (e) => {
        pushEvent({
          type: 'js_error',
          level: 'error',
          msg: e.message,
          file: e.filename,
          line: e.lineno,
          col: e.colno
        })
      })

      window.addEventListener('unhandledrejection', (e) => {
        const reason = e.reason
        const msg =
          reason && typeof reason === 'object' && reason.message
            ? String(reason.message)
            : String(reason)
        pushEvent({
          type: 'promise_error',
          level: 'error',
          msg
        })
      })

      window.addEventListener('beforeunload', () => {
        flushBatchSync('leave')
      })

      if (router) {
        setActivePage(currentRoute)

        router.beforeEach((to, from, next) => {
          refreshContextIds()
          const fromTrackable = canTrackPath(from.path, allowPages, excludePages)
          if (fromTrackable && (store.length > 0 || getBatchKind())) {
            flushBatchSync('page_leave', getOperationId() || getPageVisitId() || undefined)
          } else if (fromTrackable) {
            cancelBatch()
          }
          currentRoute = to.path
          invalidateTrackingCache()
          next()
        })
        router.afterEach((to) => {
          currentRoute = to.path
          invalidateTrackingCache()
          setActivePage(to.path)
          if (!canTrackPath(to.path, allowPages, excludePages)) {
            resetMonitorOnExcludedPage()
            return
          }
          const title = typeof document !== 'undefined' ? document.title : ''
          openPageVisit(title || to.meta?.title || to.path, to.path)
          pushEvent({
            type: 'route_enter',
            path: to.fullPath,
            title
          })
        })
      }

      timerId = window.setInterval(() => {
        if (!isTracking() || !store.length) {
          return
        }
        if (getBatchKind() !== 'action') {
          return
        }
        flush('timer')
        cancelBatch()
      }, intervalMs)
      app.config.globalProperties.$trackFlush = flush

      if (import.meta.env.DEV) {
        // eslint-disable-next-line no-console
        console.info('[monitor] user behavior monitor installed')
      }

      app.config.globalProperties.$trackDispose = () => {
        if (timerId != null) {
          clearInterval(timerId)
          timerId = null
        }
        clearQueryFlushTimer()
        cancelBatch()
      }
    }
  }
}

/**
 * @param {Record<string, unknown>[]} events
 * @returns {string}
 */
function resolveBatchOperationId(events) {
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
function resolvePageVisitId(events) {
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
function isNoiseFlush(events, reason) {
  if (reason !== 'page_leave' || events.length !== 1) {
    return false
  }
  return events[0].type === 'route_leave'
}

export default createUserMonitor
