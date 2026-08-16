/**
 * 单次 axios 请求观测：注入 x-trace-id，并向 Lite RUM 记 api 事件。
 */
import { nextRequestTraceHeaders, shouldAttachRequestTrace } from './requestTrace'
import { getLiteRum } from './liteRum'

const SENSITIVE_KEY = /pass|token|secret|authorization|cookie|pwd|credential/i

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
 * @param {string | undefined} url
 * @returns {string}
 */
function extractQuery(url) {
  if (!url) {
    return ''
  }
  const q = url.indexOf('?')
  return q >= 0 ? url.slice(q + 1).slice(0, 500) : ''
}

/**
 * @param {unknown} value
 * @param {number} max
 * @returns {string}
 */
function safeJsonSlice(value, max) {
  if (value == null || value === '') {
    return ''
  }
  try {
    const s = typeof value === 'string' ? value : JSON.stringify(value)
    return s.length > max ? `${s.slice(0, max)}…` : s
  } catch {
    return String(value).slice(0, max)
  }
}

/**
 * @param {unknown} value
 * @returns {unknown}
 */
function redactValue(value) {
  if (value == null) {
    return value
  }
  if (Array.isArray(value)) {
    return value.slice(0, 20).map(redactValue)
  }
  if (typeof value !== 'object') {
    return value
  }
  /** @type {Record<string, unknown>} */
  const out = {}
  for (const [k, v] of Object.entries(/** @type {Record<string, unknown>} */ (value))) {
    out[k] = SENSITIVE_KEY.test(k) ? '***' : redactValue(v)
  }
  return out
}

/**
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @returns {{ query: string, params: string, body: string, paramsSummary: string }}
 */
function buildRequestFields(config) {
  const query = extractQuery(config.url)
  const params = config.params ? safeJsonSlice(redactValue(config.params), 400) : ''
  const body = config.data != null && typeof config.data !== 'undefined'
    ? safeJsonSlice(redactValue(config.data), 500)
    : ''
  const parts = []
  if (query) parts.push(`query=${query}`)
  if (params) parts.push(`params=${params}`)
  if (body) parts.push(`body=${body}`)
  return {
    query,
    params,
    body,
    paramsSummary: parts.join('\n').slice(0, 800)
  }
}

/**
 * @param {import('axios').AxiosResponse | undefined} res
 * @returns {string}
 */
function buildResponsePreview(res) {
  if (!res) {
    return ''
  }
  const body = parseResponseBody(res.data)
  if (!body) {
    return safeJsonSlice(res.data, 500)
  }
  const preview = {
    code: body.code,
    msg: body.msg,
    data: redactValue(body.data)
  }
  return safeJsonSlice(preview, 500)
}

/**
 * @param {string} url
 * @returns {boolean}
 */
function shouldSkipUrl(url) {
  if (!url) {
    return true
  }
  return url.includes('/monitor/liteTrace/rum/ingest')
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

  const clientTraceId = config.headers['x-trace-id'] ? String(config.headers['x-trace-id']) : null
  const req = buildRequestFields(config)
  config.metadata = {
    ...(config.metadata || {}),
    liteRum: {
      method: String(config.method || 'get').toLowerCase(),
      url,
      query: req.query,
      requestParams: req.params,
      requestBody: req.body,
      paramsSummary: req.paramsSummary,
      requestStart: Date.now(),
      clientTraceId,
      page: typeof location !== 'undefined' ? location.pathname : ''
    }
  }
}

/**
 * @param {Record<string, unknown>} meta
 * @param {number|undefined} status
 * @param {boolean} ok
 * @param {import('axios').AxiosResponse | undefined} res
 */
function emitLiteRumApi(meta, status, ok, res) {
  const rum = getLiteRum()
  if (!rum || !meta) {
    return
  }
  const body = parseResponseBody(res && res.data)
  const responseTraceId = readResponseTraceId(res)
  const active = typeof rum.getActiveOperation === 'function' ? rum.getActiveOperation() : null
  rum.trackApi({
    method: meta.method,
    url: meta.url,
    query: meta.query,
    requestParams: meta.requestParams,
    requestBody: meta.requestBody,
    paramsSummary: meta.paramsSummary,
    responsePreview: buildResponsePreview(res),
    status,
    ok,
    durationMs: Date.now() - (meta.requestStart || Date.now()),
    traceId: responseTraceId || meta.clientTraceId,
    operationId: active ? active.operationId : undefined,
    action: active ? active.action : undefined,
    page: meta.page,
    bizCode: body && body.code != null ? Number(body.code) : undefined,
    bizMsg: body && body.msg != null ? String(body.msg).slice(0, 200) : undefined
  })
}

/**
 * @param {import('axios').AxiosResponse} res
 */
export function finalizeRequestObservationSuccess(res) {
  const liteMeta = res.config?.metadata?.liteRum
  if (!liteMeta) {
    return
  }
  const body = parseResponseBody(res.data)
  const bizCode = body && body.code != null ? Number(body.code) : undefined
  const ok = bizCode === undefined || bizCode === 200
  emitLiteRumApi(liteMeta, res.status, ok, res)
}

/**
 * @param {import('axios').AxiosError} err
 */
export function finalizeRequestObservationError(err) {
  const liteMeta = err.config?.metadata?.liteRum
  if (!liteMeta) {
    return
  }
  emitLiteRumApi(liteMeta, err.response && err.response.status, false, err.response)
}

export default {
  beginRequestObservation,
  finalizeRequestObservationSuccess,
  finalizeRequestObservationError
}
