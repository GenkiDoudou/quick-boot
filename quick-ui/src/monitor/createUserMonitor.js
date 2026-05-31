/**
 * Vue 全局用户行为监控插件（轻量版，对齐公众号「全局插件 + 本地缓冲 + 错误立即 flush」方案）。
 */
import { postTrackBatch } from './report'
import { resolveBatchTriggerAction, formatTrackLabel } from './trackLabel'
import { shouldRecordPendingTrigger, shouldAutoBeginOperation } from './operationRules'
import { readClickTarget } from './clickTarget'
import {
  beginOperation,
  getOperationId,
  endOperation,
  cancelOperation,
  registerOperationEndHook,
  registerOperationBeginHook,
  setActivePage
} from './operationContext'

/**
 * @typedef {Object} UserMonitorOptions
 * @property {boolean} [enabled=true]
 * @property {string} [reportUrl]
 * @property {number} [maxKeep=40]
 * @property {number} [interval=10000]
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
  let currentRoute = typeof location !== 'undefined' ? location.pathname : ''
  let timerId = null
  /** @type {ReturnType<typeof setTimeout> | null} */
  let actionFlushTimer = null

  /**
   * 当前路由是否处于采集态（排除页整页静默，不采集、不 flush、不开 operation）。
   * @returns {boolean}
   */
  function isTracking() {
    return enabled && canTrackPath(currentRoute, allowPages, excludePages)
  }

  function clearPendingActionFlush() {
    if (actionFlushTimer) {
      clearTimeout(actionFlushTimer)
      actionFlushTimer = null
    }
  }

  function discardBuffer() {
    store.length = 0
  }

  /**
   * 进入排除页时丢弃本地缓冲与 operation，避免维护操作污染业务页统计。
   */
  function resetMonitorOnExcludedPage() {
    clearPendingActionFlush()
    cancelOperation()
    discardBuffer()
  }

  /**
   * 无活跃 operation 时，在操作类点击后延迟 flush，合并紧随其后的 API（如弹窗 getXxx）。
   */
  function scheduleActionFlush() {
    if (!isTracking()) {
      return
    }
    if (actionFlushTimer) {
      clearTimeout(actionFlushTimer)
    }
    actionFlushTimer = setTimeout(() => {
      actionFlushTimer = null
      if (!isTracking()) {
        return
      }
      if (!getOperationId() && store.length > 0) {
        flush('action_click')
      }
    }, 1500)
  }

  /**
   * @param {Record<string, unknown>} row
   */
  function pushEvent(row) {
    if (!isTracking()) {
      return
    }
    const opId = getOperationId()
    const item = {
      page: currentRoute || (typeof location !== 'undefined' ? location.pathname : ''),
      ts: Date.now(),
      ua: typeof navigator !== 'undefined' ? navigator.userAgent : '',
      ...(opId ? { operationId: opId } : {}),
      ...row
    }
    store.push(item)
    if (store.length > maxKeep) {
      store.shift()
    }
    if (row.level === 'error') {
      flush('error')
    }
  }

  /**
   * @param {string} [reason='normal']
   * @param {string} [forcedOperationId] operation_end 时传入，保证批次级 operationId 不丢
   */
  function flush(reason = 'normal', forcedOperationId) {
    if (!enabled || !store.length) {
      return
    }
    const events = store.splice(0, store.length)
    if (isNoiseFlush(events, reason)) {
      return
    }
    const batchOpId = forcedOperationId || resolveBatchOperationId(events)
    const { raw: triggerAction, label: triggerLabel } = resolveBatchTriggerAction(events)
    postTrackBatch(reportUrl, {
      reason,
      operationId: batchOpId || undefined,
      triggerAction: triggerAction || undefined,
      triggerLabel: triggerLabel || undefined,
      events
    })
  }

  return {
    pushEvent,
    flush,
    /**
     * @param {import('vue').App} app
     * @param {{ router?: import('vue-router').Router }} [pluginOptions]
     */
    install(app, pluginOptions = {}) {
      if (!enabled) {
        return
      }
      const router = pluginOptions.router
      app.config.globalProperties.$track = pushEvent

      document.addEventListener(
        'click',
        (e) => {
          if (!isTracking()) {
            return
          }
          const target = /** @type {HTMLElement | null} */ (e.target)
          if (!target) {
            return
          }
          const { label, isAction } = readClickTarget(target)
          if (!isAction) {
            return
          }
          pushEvent({
            type: 'click',
            target: label,
            x: e.clientX,
            y: e.clientY
          })
          if (shouldAutoBeginOperation(label)) {
            beginOperation(label)
          } else if (shouldRecordPendingTrigger(label)) {
            scheduleActionFlush()
          }
        },
        true
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
        flush('leave')
      })

      /**
       * 立即 flush（路由切换 / operation 结束须同步，避免 afterEach 的 route_enter 混入上一批）。
       * @param {string} reason
       * @param {string} [forcedOperationId]
       */
      function flushNow(reason, forcedOperationId) {
        flush(reason, forcedOperationId)
      }

      /**
       * 延后 flush，仅用于非边界场景（如 beforeunload 尽力上报）。
       * @param {string} reason
       * @param {string} [forcedOperationId]
       */
      function scheduleFlush(reason, forcedOperationId) {
        window.setTimeout(() => flush(reason, forcedOperationId), 0)
      }

      if (router) {
        setActivePage(currentRoute)

        router.beforeEach((to, from, next) => {
          const fromTrackable = canTrackPath(from.path, allowPages, excludePages)
          if (fromTrackable) {
            pushEvent({
              type: 'route_leave',
              from: from.fullPath,
              to: to.fullPath
            })
            const hadOperation = getOperationId()
            if (hadOperation) {
              endOperation()
            } else if (store.length > 0) {
              flushNow('route_leave')
            }
          }
          currentRoute = to.path
          next()
        })
        router.afterEach((to) => {
          currentRoute = to.path
          setActivePage(to.path)
          if (!canTrackPath(to.path, allowPages, excludePages)) {
            resetMonitorOnExcludedPage()
            return
          }
          pushEvent({
            type: 'route_enter',
            path: to.fullPath,
            title: typeof document !== 'undefined' ? document.title : ''
          })
        })
      }

      registerOperationEndHook((endingOperationId) => {
        queueMicrotask(() => {
          if (!enabled) {
            return
          }
          // operation 结束须上报：切到 excludePages 时 isTracking 已为 false，不能因此丢批次
          if (!endingOperationId && !isTracking()) {
            discardBuffer()
            return
          }
          if (endingOperationId) {
            for (const ev of store) {
              if (!ev.operationId) {
                ev.operationId = endingOperationId
              }
            }
          }
          flushNow('operation_end', endingOperationId || undefined)
        })
      })

      /**
       * operation 开始时：回填紧邻 click；若无则补写一条触发点击（如 C7JsonTable onAdd 仅回调未走全局 begin）。
       * @param {string} operationId
       * @param {string | null} triggerLabel
       */
      function syncOperationBeginClick(operationId, triggerLabel) {
        if (!triggerLabel || !isTracking()) {
          return
        }
        const displayTarget = formatTrackLabel(triggerLabel) || triggerLabel
        for (let i = store.length - 1; i >= 0; i -= 1) {
          const ev = store[i]
          if (ev.operationId === operationId && ev.type === 'click') {
            return
          }
        }
        for (let i = store.length - 1; i >= 0; i -= 1) {
          const ev = store[i]
          if (ev.type === 'click' && !ev.operationId) {
            ev.operationId = operationId
            if (ev.source !== 'operation_begin') {
              ev.target = displayTarget
            }
            return
          }
        }
        pushEvent({
          type: 'click',
          target: displayTarget,
          source: 'operation_begin'
        })
      }

      registerOperationBeginHook((operationId, triggerLabel) => {
        syncOperationBeginClick(operationId, triggerLabel)
      })

      timerId = window.setInterval(() => {
        if (!isTracking()) {
          return
        }
        if (getOperationId()) {
          return
        }
        flush('timer')
      }, intervalMs)
      app.config.globalProperties.$trackFlush = flush

      if (import.meta.env.DEV) {
        // eslint-disable-next-line no-console
        console.info('[monitor] user behavior monitor installed')
      }

      /** 供测试或手动卸载时清理定时器 */
      app.config.globalProperties.$trackDispose = () => {
        if (timerId != null) {
          clearInterval(timerId)
          timerId = null
        }
        if (actionFlushTimer != null) {
          clearTimeout(actionFlushTimer)
          actionFlushTimer = null
        }
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
 * 丢弃仅含 route_leave 的孤立批次（切页边界噪声）。
 * @param {Record<string, unknown>[]} events
 * @param {string} reason
 * @returns {boolean}
 */
function isNoiseFlush(events, reason) {
  if (reason !== 'route_leave' || events.length !== 1) {
    return false
  }
  return events[0].type === 'route_leave'
}

export default createUserMonitor
