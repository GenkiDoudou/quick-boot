/**
 * Vue 全局用户行为监控插件（轻量版，对齐公众号「全局插件 + 本地缓冲 + 错误立即 flush」方案）。
 */
import { postTrackBatch } from './report'
import { getOperationId, endOperation, registerOperationEndHook, registerOperationBeginHook, setActivePage, setPendingTrigger } from './operationContext'

/**
 * @typedef {Object} UserMonitorOptions
 * @property {boolean} [enabled=true]
 * @property {string} [reportUrl]
 * @property {number} [maxKeep=40]
 * @property {number} [interval=10000]
 * @property {string[]} [allowPages] 路径前缀白名单
 */

/**
 * 判断当前路由是否应采集监控事件。
 *
 * @param {string} path
 * @param {string[]} allowPages
 * @returns {boolean}
 */
export function canTrackPath(path, allowPages) {
  if (!path) {
    return false
  }
  if (!allowPages || allowPages.length === 0) {
    return true
  }
  return allowPages.some((prefix) => path === prefix || path.startsWith(`${prefix}/`))
}

/**
 * @param {HTMLElement | null | undefined} el
 * @returns {{ label: string, trackLabel: string | null }}
 */
function readClickTarget(el) {
  if (!el) {
    return { label: '', trackLabel: null }
  }
  let node = el
  let deep = 0
  while (node && deep < 6) {
    if (node.dataset && node.dataset.track) {
      return { label: node.dataset.track, trackLabel: node.dataset.track }
    }
    if (isOperationTriggerElement(node)) {
      const label =
        (node.dataset && node.dataset.track) ||
        (node.innerText && node.innerText.trim().slice(0, 40)) ||
        node.getAttribute('aria-label') ||
        node.tagName
      return { label: String(label), trackLabel: null }
    }
    node = node.parentElement
    deep += 1
  }
  return { label: el.tagName || 'UNKNOWN', trackLabel: null }
}

/**
 * 关键交互元素：点击时开启新的 operationId（无需手动加 data-track）。
 * @param {HTMLElement} node
 * @returns {boolean}
 */
function isOperationTriggerElement(node) {
  if (!node || !node.tagName) {
    return false
  }
  const tag = node.tagName.toUpperCase()
  if (tag === 'BUTTON') {
    return true
  }
  if (tag === 'INPUT') {
    const type = (node.getAttribute('type') || '').toLowerCase()
    return type === 'submit' || type === 'button'
  }
  if (tag === 'A' && node.getAttribute('role') === 'button') {
    return true
  }
  if (node.classList && node.classList.contains('el-button')) {
    return true
  }
  return false
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
  const intervalMs = options.interval || 10000
  let currentRoute = typeof location !== 'undefined' ? location.pathname : ''
  let timerId = null

  /**
   * @param {Record<string, unknown>} row
   */
  function pushEvent(row) {
    if (!enabled || !canTrackPath(currentRoute, allowPages)) {
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
   */
  function flush(reason = 'normal') {
    if (!enabled || !store.length) {
      return
    }
    const events = store.splice(0, store.length)
    const batchOpId = resolveBatchOperationId(events)
    postTrackBatch(reportUrl, { reason, operationId: batchOpId || undefined, events })
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
          const target = /** @type {HTMLElement | null} */ (e.target)
          if (!target) {
            return
          }
          const { label, trackLabel } = readClickTarget(target)
          if (trackLabel) {
            setPendingTrigger(trackLabel)
          }
          pushEvent({
            type: 'click',
            target: label,
            x: e.clientX,
            y: e.clientY
          })
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

      if (router) {
        setActivePage(currentRoute)
        router.beforeEach((to, from, next) => {
          pushEvent({
            type: 'route_leave',
            from: from.fullPath,
            to: to.fullPath
          })
          if (getOperationId()) {
            endOperation()
          }
          currentRoute = to.path
          next()
        })
        router.afterEach((to) => {
          currentRoute = to.path
          setActivePage(to.path)
          pushEvent({
            type: 'route_enter',
            path: to.fullPath,
            title: typeof document !== 'undefined' ? document.title : ''
          })
        })
      }

      registerOperationEndHook(() => flush('operation_end'))

      registerOperationBeginHook((operationId) => {
        for (let i = store.length - 1; i >= 0; i -= 1) {
          const ev = store[i]
          if (ev.type === 'click' && !ev.operationId) {
            ev.operationId = operationId
            break
          }
        }
      })

      timerId = window.setInterval(() => {
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

export default createUserMonitor
