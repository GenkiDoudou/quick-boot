/**
 * lite-rum 核心：配置归一化、实例工厂与 LiteRum 类。
 * 负责 PV / action / api / error 事件入队、定时 flush、SPA / 全局错误 / API 测速等能力装配。
 */
import { getOrCreateSessionId, configureSessionStorage } from './session.js'
import { bindActionCapture, unbindActionCapture } from './actionCapture.js'
import { bindSpaNavigation } from './spa.js'
import { bindXhrFetchHook } from './xhrFetch.js'

/** SDK  semver，随 ingest payload 上报 */
export const SDK_VERSION = '0.1.0'
const OPERATION_TTL_MS = 30_000

function newId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID().replace(/-/g, '')
  }
  return `${Date.now().toString(16)}${Math.random().toString(16).slice(2, 10)}`
}

/** 判定是否为 ingest 自身 URL，避免上报循环 */
function isIngestUrl(url, ingestUrl) {
  if (!url) return true
  const u = String(url)
  return u.includes('/monitor/liteTrace/rum/ingest') || (ingestUrl && u.includes(ingestUrl))
}

/**
 * 将用户传入的 id/appId、hostUrl/ingestUrl 等别名与默认值合并为统一运行时配置。
 * @param {Record<string, unknown>} raw
 */
export function normalizeConfig(raw = {}) {
  const id = raw.id != null ? String(raw.id) : raw.appId != null ? String(raw.appId) : 'web-admin'
  const hostUrl =
    raw.hostUrl != null
      ? String(raw.hostUrl)
      : raw.ingestUrl != null
        ? String(raw.ingestUrl)
        : '/monitor/liteTrace/rum/ingest'
  return {
    appId: id,
    ingestUrl: hostUrl,
    uin: raw.uin != null ? String(raw.uin) : '',
    reportApiSpeed: raw.reportApiSpeed === true,
    reportAssetSpeed: raw.reportAssetSpeed === true,
    spa: raw.spa === true,
    actionCapture: raw.actionCapture === true,
    pageLoadAction: raw.pageLoadAction !== false,
    flushIntervalMs: Number(raw.flushIntervalMs || 4000),
    maxBatch: Number(raw.maxBatch || 40),
    getAuthHeaders: typeof raw.getAuthHeaders === 'function' ? raw.getAuthHeaders : null,
    /** @type {null | ((ctx: { url: string, headers: Record<string, string>, payload: Record<string, unknown> }) => Promise<void>)} */
    transport: typeof raw.transport === 'function' ? raw.transport : null,
    storage: raw.storage && typeof raw.storage === 'object' ? raw.storage : null
  }
}

/**
 * 创建 lite-rum 实例（函数式 API）：维护事件队列、页面上下文与可选 hooks，返回 track/flush/start 等方法集合。
 * @param {ReturnType<typeof normalizeConfig>} config
 */
export function createLiteRum(config) {
  if (config.storage) {
    configureSessionStorage(config.storage)
  }
  /** @type {Record<string, unknown>[]} */
  const queue = []
  /** @type {ReturnType<typeof setInterval> | null} */
  let timer = null
  /** @type {Map<string, number>} */
  const errorFpWindow = new Map()
  let fromPage = ''
  let currentPage =
    typeof location !== 'undefined' ? location.pathname + location.search + location.hash : ''
  let pageVisitId = ''
  let flushing = false
  let retries = 0
  let currentOperationId = ''
  let currentActionName = ''
  let operationUntil = 0
  let started = false
  /** @type {Array<() => void>} */
  const cleanups = []
  const cfg = { ...config }

  function env() {
    return { ua: typeof navigator !== 'undefined' ? navigator.userAgent : '' }
  }

  function clearOperation() {
    currentOperationId = ''
    currentActionName = ''
    operationUntil = 0
  }

  function setActiveOperation(operationId, action) {
    currentOperationId = operationId || ''
    currentActionName = action || ''
    operationUntil = currentOperationId ? Date.now() + OPERATION_TTL_MS : 0
  }

  function getActiveOperation() {
    if (!currentOperationId || Date.now() > operationUntil) return null
    return { operationId: currentOperationId, action: currentActionName }
  }

  function enqueue(event) {
    queue.push({
      ...event,
      ts: Date.now(),
      sessionId: getOrCreateSessionId(),
      uin: cfg.uin || undefined,
      pageVisitId: event.pageVisitId != null ? event.pageVisitId : pageVisitId || undefined,
      page: event.page != null ? event.page : currentPage,
      fromPage: event.fromPage != null ? event.fromPage : fromPage
    })
    if (queue.length >= cfg.maxBatch) flush()
  }

  /** 记录页面访问：轮换 pageVisitId，可选附带「页面加载」action */
  function trackPv(page) {
    fromPage = currentPage
    currentPage = page || currentPage
    pageVisitId = newId()
    clearOperation()
    enqueue({
      type: 'pv',
      page: currentPage,
      fromPage,
      pageVisitId,
      fullPath: typeof location !== 'undefined' ? location.href : currentPage,
      title: typeof document !== 'undefined' ? document.title : ''
    })
    if (cfg.pageLoadAction) trackAction('页面加载')
  }

  /** 记录用户操作并设为当前 operation，供后续 api/error 关联 */
  function trackAction(action, extra = {}) {
    const name = String(action || '').trim()
    if (!name) return
    const operationId = String(extra.operationId || newId())
    setActiveOperation(operationId, name)
    enqueue({
      type: 'action',
      action: name,
      operationId,
      page: currentPage,
      pageVisitId
    })
  }

  function trackApi(api) {
    const url = String(api.url || '')
    if (isIngestUrl(url, cfg.ingestUrl)) return
    const active = getActiveOperation()
    enqueue({
      type: 'api',
      method: api.method,
      url,
      query: api.query,
      requestParams: api.requestParams,
      requestBody: api.requestBody,
      paramsSummary: api.paramsSummary,
      responsePreview: api.responsePreview,
      status: api.status,
      ok: api.ok,
      durationMs: api.durationMs,
      traceId: api.traceId,
      operationId: api.operationId || (active && active.operationId) || undefined,
      action: api.action || (active && active.action) || undefined,
      page: api.page || currentPage,
      pageVisitId,
      bizCode: api.bizCode,
      bizMsg: api.bizMsg
    })
  }

  /** 上报 JS 错误；10s 内相同 message+stack 前缀去重，避免刷屏 */
  function trackError(message, extra = {}) {
    const fp = `${message}|${(extra.stack || '').slice(0, 80)}`
    const now = Date.now()
    const last = errorFpWindow.get(fp) || 0
    if (now - last < 10_000) return
    errorFpWindow.set(fp, now)
    const active = getActiveOperation()
    enqueue({
      type: 'error',
      message: String(message || 'error').slice(0, 500),
      stack: extra.stack ? String(extra.stack).slice(0, 2000) : undefined,
      traceId: extra.traceId,
      operationId: extra.operationId || (active && active.operationId) || undefined,
      page: currentPage,
      pageVisitId
    })
  }

  /** 批量 POST ingest；失败时将 batch 插回队首，最多重试 3 次 */
  async function flush() {
    if (flushing || queue.length === 0) return
    flushing = true
    const batch = queue.splice(0, cfg.maxBatch)
    try {
      /** @type {Record<string, string>} */
      const headers = {
        'Content-Type': 'application/json',
        'X-Skip-Request-Trace': '1'
      }
      if (typeof cfg.getAuthHeaders === 'function') {
        try {
          const extra = cfg.getAuthHeaders() || {}
          Object.assign(headers, extra)
        } catch {
          /* ignore auth header errors */
        }
      }
      const payload = {
        appId: cfg.appId,
        sdkVersion: SDK_VERSION,
        clientTime: Date.now(),
        env: env(),
        events: batch
      }
      if (typeof cfg.transport === 'function') {
        await cfg.transport({ url: cfg.ingestUrl, headers, payload })
      } else {
        const res = await fetch(cfg.ingestUrl, {
          method: 'POST',
          headers,
          body: JSON.stringify(payload),
          credentials: 'include',
          keepalive: true
        })
        if (!res.ok) throw new Error(`ingest ${res.status}`)
      }
      retries = 0
    } catch {
      retries += 1
      if (retries < 3) queue.unshift(...batch)
    } finally {
      flushing = false
    }
  }

  function onError(ev) {
    trackError((ev && ev.message) || 'window.error', {
      stack: ev && ev.error && ev.error.stack
    })
  }

  function onRejection(ev) {
    const reason = ev && ev.reason
    trackError(reason && reason.message ? reason.message : String(reason || 'unhandledrejection'), {
      stack: reason && reason.stack
    })
  }

  function setUin(uin) {
    cfg.uin = uin != null ? String(uin) : ''
  }

  /** 合并 partial 配置；未显式传入时保留已有 getAuthHeaders / transport */
  function setConfig(partial = {}) {
    const prevGetAuth = cfg.getAuthHeaders
    const prevTransport = cfg.transport
    const next = normalizeConfig({
      ...cfg,
      id: cfg.appId,
      hostUrl: cfg.ingestUrl,
      getAuthHeaders: cfg.getAuthHeaders,
      transport: cfg.transport,
      storage: cfg.storage,
      ...partial
    })
    Object.assign(cfg, next)
    if (typeof partial.getAuthHeaders === 'function') {
      cfg.getAuthHeaders = partial.getAuthHeaders
    } else if (typeof next.getAuthHeaders !== 'function' && typeof prevGetAuth === 'function') {
      cfg.getAuthHeaders = prevGetAuth
    }
    if (typeof partial.transport === 'function') {
      cfg.transport = partial.transport
    } else if (typeof next.transport !== 'function' && typeof prevTransport === 'function') {
      cfg.transport = prevTransport
    }
    if (partial.storage) {
      configureSessionStorage(partial.storage)
      cfg.storage = partial.storage
    }
  }

  function bindVueRouter(router) {
    if (!router || !router.afterEach) return
    const remove = router.afterEach((to) => {
      trackPv(to.fullPath || to.path)
    })
    if (typeof remove === 'function') cleanups.push(remove)
  }

  /** 注册全局 error、SPA、API 测速、action 采集与定时 flush，并发送首屏 PV */
  function start(extra = {}) {
    if (started) return api
    started = true
    const actionCapture = extra.actionCapture != null ? !!extra.actionCapture : cfg.actionCapture
    const spa = extra.spa != null ? !!extra.spa : cfg.spa
    const reportApiSpeed =
      extra.reportApiSpeed != null ? !!extra.reportApiSpeed : cfg.reportApiSpeed

    if (typeof window !== 'undefined') {
      window.addEventListener('error', onError)
      window.addEventListener('unhandledrejection', onRejection)
      window.addEventListener('beforeunload', flush)
      cleanups.push(() => {
        window.removeEventListener('error', onError)
        window.removeEventListener('unhandledrejection', onRejection)
        window.removeEventListener('beforeunload', flush)
      })
    }

    if (actionCapture) {
      bindActionCapture((action) => trackAction(action))
      cleanups.push(() => unbindActionCapture())
    }

    if (spa) {
      cleanups.push(bindSpaNavigation((page) => trackPv(page)))
    }

    if (reportApiSpeed) {
      cleanups.push(
        bindXhrFetchHook({
          trackApi,
          shouldSkipUrl: (url) => isIngestUrl(url, cfg.ingestUrl),
          getPage: () => currentPage
        })
      )
    }

    timer = setInterval(() => flush(), cfg.flushIntervalMs)
    trackPv(currentPage)
    return api
  }

  /** 停止定时器、执行 cleanups 并解除 action 监听 */
  function destroy() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    while (cleanups.length) {
      try {
        const fn = cleanups.pop()
        fn && fn()
      } catch {
        /* ignore */
      }
    }
    unbindActionCapture()
    started = false
  }

  const api = {
    get config() {
      return cfg
    },
    SDK_VERSION,
    start,
    destroy,
    stop: destroy,
    setUin,
    setConfig,
    trackPv,
    trackAction,
    trackApi,
    trackError,
    flush,
    bindVueRouter,
    bindRouter: bindVueRouter,
    getActiveOperation,
    getOrCreateSessionId
  }

  return api
}

/**
 * 面向 `new LiteRum(options)` 的类封装；默认 autoStart，内部委托 createLiteRum 实例。
 * @example
 * const rum = new LiteRum({ id: 'web-admin', spa: true, reportApiSpeed: true, hostUrl: '/...' })
 */
export class LiteRum {
  /**
   * @param {Record<string, unknown>} options
   */
  constructor(options = {}) {
    const cfg = normalizeConfig(options)
    const inst = createLiteRum(cfg)
    this._inst = inst
    const methods = [
      'start',
      'destroy',
      'stop',
      'setUin',
      'setConfig',
      'trackPv',
      'trackAction',
      'trackApi',
      'trackError',
      'flush',
      'bindVueRouter',
      'bindRouter',
      'getActiveOperation',
      'getOrCreateSessionId'
    ]
    for (const m of methods) {
      this[m] = (...args) => inst[m](...args)
    }
    Object.defineProperty(this, 'config', {
      get: () => inst.config
    })
    this.SDK_VERSION = SDK_VERSION
    if (options.autoStart !== false) {
      inst.start({
        spa: cfg.spa,
        reportApiSpeed: cfg.reportApiSpeed,
        actionCapture: cfg.actionCapture
      })
    }
  }
}

export default LiteRum
