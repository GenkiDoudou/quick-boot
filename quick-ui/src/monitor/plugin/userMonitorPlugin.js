/**
 * 用户行为监控 Vue 插件工厂：组装 core + collectors，对外暴露统一实例。
 */
import {
  configureBatchSession,
  registerBatchFlushHook,
  cancelBatch,
  getOperationId
} from '../batchSession'
import { startBrowserVisitHeartbeat } from '../browserVisitContext'
import { isUrgentFlushReason } from '../scheduleIdle'
import { USER_MONITOR_KEY } from '../constants'
import { canTrackPath, createTrackingGuard } from '../core/pathGuard'
import { createMonitorContext } from '../core/monitorContext'
import { createEventBuffer } from '../core/eventBuffer'
import { createFlushPipeline } from '../core/flushPipeline'
import { bindClickCollector } from '../collectors/clickCollector'
import { bindErrorCollector } from '../collectors/errorCollector'
import { bindRouteCollector } from '../collectors/routeCollector'
import { bindLifecycleCollector } from '../collectors/lifecycleCollector'

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
 * 创建用户行为监控实例；可作为 Vue 插件 `app.use(monitor, { router })` 安装。
 *
 * @param {UserMonitorOptions} [options]
 * @returns {{ install: Function, pushEvent: Function, flush: Function, enabled: boolean }}
 */
export function createUserMonitor(options = {}) {
  const enabled = options.enabled !== false
  const maxKeep = options.maxKeep || 40
  const reportUrl = options.reportUrl || '/monitor/clientTrack/report'
  const allowPages = options.allowPages || []
  const excludePages = options.excludePages || []
  const intervalMs = options.interval || 10000
  const idleMs = options.idleMs ?? 2000

  let currentRoute = typeof location !== 'undefined' ? location.pathname : ''
  /** @type {ReturnType<typeof setTimeout> | null} */
  let queryFlushTimer = null
  /** @type {(() => void) | null} */
  let disposeCollectors = null

  configureBatchSession({ idleMs })

  const context = createMonitorContext()
  const tracking = createTrackingGuard({
    enabled,
    allowPages,
    excludePages,
    getCurrentRoute: () => currentRoute
  })

  const buffer = createEventBuffer({
    maxKeep,
    getCurrentRoute: () => currentRoute,
    context,
    isTracking: () => tracking.isTracking(),
    onErrorEvent: () => flush('error', undefined, { urgent: true })
  })

  const { flush } = createFlushPipeline({ enabled, reportUrl, context, buffer })

  registerBatchFlushHook((reason, operationId) => {
    if (!enabled) {
      return
    }
    flush(reason, operationId || undefined, { urgent: isUrgentFlushReason(reason) })
  })

  function clearQueryFlushTimer() {
    if (queryFlushTimer) {
      clearTimeout(queryFlushTimer)
      queryFlushTimer = null
    }
  }

  function resetMonitorOnExcludedPage() {
    clearQueryFlushTimer()
    cancelBatch()
    buffer.discard()
  }

  function scheduleQueryFlush() {
    if (!tracking.isTracking()) {
      return
    }
    clearQueryFlushTimer()
    queryFlushTimer = setTimeout(() => {
      queryFlushTimer = null
      if (!tracking.isTracking() || !buffer.length) {
        return
      }
      if (!getOperationId()) {
        flush('action_click')
      }
    }, 1500)
  }

  return {
    enabled,
    pushEvent: buffer.pushEvent,
    flush,
    install(app, pluginOptions = {}) {
      if (!enabled) {
        return
      }
      const router = pluginOptions.router
      context.refresh()
      startBrowserVisitHeartbeat()

      const monitorApi = { pushEvent: buffer.pushEvent, flush, enabled: true }
      app.provide(USER_MONITOR_KEY, monitorApi)
      app.config.globalProperties.$track = buffer.pushEvent
      app.config.globalProperties.$trackFlush = flush

      const disposers = []
      disposers.push(
        bindClickCollector({
          isTracking: () => tracking.isTracking(),
          pushEvent: buffer.pushEvent,
          scheduleQueryFlush
        })
      )
      disposers.push(bindErrorCollector({ pushEvent: buffer.pushEvent }))

      if (router) {
        disposers.push(
          bindRouteCollector({
            router,
            allowPages,
            excludePages,
            getCurrentRoute: () => currentRoute,
            setCurrentRoute: (path) => {
              currentRoute = path
              tracking.setRoute(path)
            },
            invalidateTracking: () => tracking.invalidate(),
            pushEvent: buffer.pushEvent,
            hasBufferedEvents: () => buffer.length > 0,
            resetOnExcludedPage: resetMonitorOnExcludedPage,
            refreshContext: () => context.refresh()
          })
        )
      }

      const lifecycle = bindLifecycleCollector({
        intervalMs,
        isTracking: () => tracking.isTracking(),
        hasBufferedEvents: () => buffer.length > 0,
        flush
      })
      disposers.push(lifecycle.dispose)

      disposeCollectors = () => {
        for (const off of disposers) {
          off()
        }
        clearQueryFlushTimer()
        cancelBatch()
      }

      if (import.meta.env.DEV) {
        // eslint-disable-next-line no-console
        console.info('[monitor] user behavior monitor installed')
      }

      app.config.globalProperties.$trackDispose = disposeCollectors
    }
  }
}

export { canTrackPath }
export default createUserMonitor
