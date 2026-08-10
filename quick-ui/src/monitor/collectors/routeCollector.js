/**
 * Vue Router 路由进入/离开采集与批次同步 flush。
 */
import { canTrackPath } from '../core/pathGuard'
import {
  cancelBatch,
  flushBatchSync,
  getBatchKind,
  getOperationId,
  getPageVisitId,
  openPageVisit,
  setActivePage
} from '../batchSession'

/**
 * @param {{
 *   router: import('vue-router').Router,
 *   allowPages: string[],
 *   excludePages: string[],
 *   getCurrentRoute: () => string,
 *   setCurrentRoute: (path: string) => void,
 *   invalidateTracking: () => void,
 *   pushEvent: (row: Record<string, unknown>) => void,
 *   hasBufferedEvents: () => boolean,
 *   resetOnExcludedPage: () => void,
 *   refreshContext: () => void
 * }} deps
 * @returns {() => void} dispose（Router 钩子无原生 off，仅占位）
 */
export function bindRouteCollector(deps) {
  const {
    router,
    allowPages,
    excludePages,
    getCurrentRoute,
    setCurrentRoute,
    invalidateTracking,
    pushEvent,
    hasBufferedEvents,
    resetOnExcludedPage,
    refreshContext
  } = deps

  setActivePage(getCurrentRoute())

  router.beforeEach((to, from, next) => {
    refreshContext()
    const fromTrackable = canTrackPath(from.path, allowPages, excludePages)
    if (fromTrackable && (hasBufferedEvents() || getBatchKind())) {
      flushBatchSync('page_leave', getOperationId() || getPageVisitId() || undefined)
    } else if (fromTrackable) {
      cancelBatch()
    }
    setCurrentRoute(to.path)
    invalidateTracking()
    next()
  })

  router.afterEach((to) => {
    setCurrentRoute(to.path)
    invalidateTracking()
    setActivePage(to.path)
    if (!canTrackPath(to.path, allowPages, excludePages)) {
      resetOnExcludedPage()
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

  return () => {}
}
