/**
 * 单次 axios 请求的完整观测：在 request.js 请求拦截器创建快照，响应拦截器 finalize 并 emit。
 * trace 双来源兼容：clientTraceId = 请求头 x-trace-id；serverTraceId 优先 R.traceId，缺失时回退 clientTraceId。
 */
import {
  getOperationId,
  getActivePage,
  getLastTrigger,
  getBatchKind
} from './operationContext'
import { nextRequestTraceHeaders, shouldAttachRequestTrace } from './requestTrace'
import { scheduleIdleTask } from './scheduleIdle'

/** @typedef {(row: Record<string, unknown>) => void} TrackFn */

/** @type {TrackFn | null} */
let trackFn = null

/** @type {number} */
let slowApiMs = 3000

/** @type {RegExp | undefined} */
let skipUrlPattern

/**
 * @typedef {Object} RequestObservation
 * @property {string | null} operationId
 * @property {string} page
 * @property {string | null} trigger
 * @property {string} method
 * @property {string} url
 * @property {number} requestStart
 * @property {string | null} clientTraceId
 * @property {string | null} traceparent
 */

/**
 * 注册 API 事件写入函数（setupUserMonitor 启动时调用一次）。
 *
 * @param {TrackFn} fn
 * @param {{ slowApiMs?: number, skipUrlPattern?: RegExp }} [options]
 */
export function registerObservationEmitter(fn, options = {}) {
  trackFn = fn
  slowApiMs = options.slowApiMs ?? 3000
  skipUrlPattern = options.skipUrlPattern
}

/**
 * @param {string | undefined} url
 * @returns {string}
 */
function normalizeUrl(url) {
  if (!url) {
    return ''
  }
  const q = url.indexOf('?')
  return q >= 0 ? url.slice(0, q) : url
}

/**
 * @param {string} url
 * @returns {boolean}
 */
function shouldSkipUrl(url) {
  if (!url) {
    return true
  }
  if (skipUrlPattern && skipUrlPattern.test(url)) {
    return true
  }
  return url.includes('/monitor/clientTrack')
}

/**
 * 请求发出前：快照 operation/page/trigger，注入 trace 与 observation（与 Header 共用同一 trace 对象）。
 *
 * @param {import('axios').InternalAxiosRequestConfig} config
 */
export function beginRequestObservation(config) {
  const url = normalizeUrl(config.url)
  if (shouldSkipUrl(url)) {
    return
  }

  config.headers = config.headers || {}

  if (shouldAttachRequestTrace(config)) {
    const trace = nextRequestTraceHeaders()
    config.headers['traceparent'] = trace.traceparent
    config.headers['x-trace-id'] = trace.traceId
  }

  // 无活跃批次时不建 observation，列表页背景 GET 不再走 finalize（trace 头仍保留供 oper_log）
  if (!trackFn || (!getOperationId() && !getBatchKind())) {
    return
  }

  const operationId = getOperationId()

  /** @type {RequestObservation} */
  const observation = {
    operationId,
    page: getActivePage(),
    trigger: getLastTrigger(),
    method: String(config.method || 'get').toLowerCase(),
    url,
    requestStart: Date.now(),
    clientTraceId: null,
    traceparent: null
  }

  if (operationId) {
    config.headers['X-Client-Operation-Id'] = operationId
  }

  if (config.headers['x-trace-id']) {
    observation.clientTraceId = String(config.headers['x-trace-id'])
    observation.traceparent = config.headers['traceparent'] ? String(config.headers['traceparent']) : null
  }

  config.metadata = { ...(config.metadata || {}), observation }
}

/**
 * @param {unknown} data
 * @returns {Record<string, unknown> | null}
 */
function parseResponseBody(data) {
  if (data == null || typeof data !== 'object' || Array.isArray(data)) {
    return null
  }
  return /** @type {Record<string, unknown>} */ (data)
}

/**
 * 从 R.traceId 读取响应侧 trace（可能与请求头 x-trace-id 不同）。
 *
 * @param {import('axios').AxiosResponse | undefined} res
 * @returns {string | undefined}
 */
function readResponseTraceId(res) {
  const body = parseResponseBody(res && res.data)
  if (!body) {
    return undefined
  }
  const traceId = body.traceId
  if (traceId == null || traceId === '') {
    return undefined
  }
  return String(traceId)
}

/**
 * 解析 trace 双字段：请求发出时已快照 clientTraceId；响应后补 responseTraceId。
 * serverTraceId 优先 R.traceId（oper_log 对齐），无响应 trace 时回退 clientTraceId（与 Network 一致）。
 *
 * @param {RequestObservation} obs
 * @param {import('axios').AxiosResponse | undefined} res
 * @returns {{ clientTraceId: string | undefined, responseTraceId: string | undefined, serverTraceId: string | undefined }}
 */
function resolveTraceIds(obs, res) {
  const clientTraceId = obs.clientTraceId || undefined
  const responseTraceId = readResponseTraceId(res)
  const serverTraceId = responseTraceId || clientTraceId
  return { clientTraceId, responseTraceId, serverTraceId }
}

/**
 * @param {RequestObservation} obs
 * @param {number} cost
 * @param {{ clientTraceId?: string, responseTraceId?: string, serverTraceId?: string }} traceIds
 * @param {{ httpStatus?: number, bizCode?: number, success?: boolean, msg?: string }} [extra]
 * @returns {Record<string, unknown>}
 */
function buildApiEvent(obs, cost, traceIds, extra = {}) {
  const { clientTraceId, responseTraceId, serverTraceId } = traceIds
  return {
    ...(obs.operationId ? { operationId: obs.operationId } : {}),
    page: obs.page,
    ...(obs.trigger ? { trigger: obs.trigger } : {}),
    ...(clientTraceId ? { clientTraceId } : {}),
    ...(responseTraceId ? { responseTraceId } : {}),
    ...(serverTraceId ? { serverTraceId } : {}),
    url: obs.url,
    method: obs.method,
    cost,
    ...extra
  }
}

/**
 * @param {RequestObservation} obs
 * @param {Record<string, unknown>} event
 */
function emitApiEvent(_obs, event) {
  if (!trackFn) {
    return
  }
  scheduleIdleTask(() => {
    if (trackFn) {
      trackFn(event)
    }
  })
}

/**
 * 成功响应：finalize 并写入 api_call / api_slow。
 *
 * @param {import('axios').AxiosResponse} res
 */
export function finalizeRequestObservationSuccess(res) {
  const obs = res.config?.metadata?.observation
  if (!obs || !trackFn) {
    return
  }
  const cost = Date.now() - obs.requestStart
  const traceIds = resolveTraceIds(obs, res)
  const body = parseResponseBody(res.data)
  const bizCode = body && body.code != null ? Number(body.code) : undefined
  const base = buildApiEvent(obs, cost, traceIds, {
    httpStatus: res.status,
    ...(Number.isFinite(bizCode) ? { bizCode } : {}),
    success: bizCode === undefined || bizCode === 200
  })
  const type = cost > slowApiMs ? 'api_slow' : 'api_call'
  if (traceIds.clientTraceId || traceIds.serverTraceId) {
    emitApiEvent(obs, { type, ...base })
  }
}

/**
 * 失败响应：写入 api_error。
 *
 * @param {import('axios').AxiosError} err
 */
export function finalizeRequestObservationError(err) {
  const obs = err.config?.metadata?.observation
  if (!obs || !trackFn) {
    return
  }
  const cost = Date.now() - obs.requestStart
  const traceIds = resolveTraceIds(obs, err.response)
  if (traceIds.clientTraceId || traceIds.serverTraceId) {
    emitApiEvent(obs, {
      type: 'api_error',
      level: 'error',
      status: err.response && err.response.status,
      msg: err.message,
      ...buildApiEvent(obs, cost, traceIds, { success: false })
    })
  }
}

/** @deprecated 使用 registerObservationEmitter */
export function registerApiCallTrack(fn, options) {
  registerObservationEmitter(fn, options)
}

/** @deprecated 使用 finalizeRequestObservationSuccess */
export function recordApiSuccess(res) {
  finalizeRequestObservationSuccess(res)
}

/** @deprecated 使用 finalizeRequestObservationError */
export function recordApiError(err) {
  finalizeRequestObservationError(err)
}

export default {
  registerObservationEmitter,
  beginRequestObservation,
  finalizeRequestObservationSuccess,
  finalizeRequestObservationError
}
