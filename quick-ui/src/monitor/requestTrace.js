/**
 * 单次 HTTP 请求的 W3C trace 头（策略 B：每请求独立 traceId，与 operationId 分离）。
 */

/**
 * @param {number} byteLength
 * @returns {string}
 */
function randomHex(byteLength) {
  const arr = new Uint8Array(byteLength)
  if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
    crypto.getRandomValues(arr)
  } else {
    for (let i = 0; i < byteLength; i += 1) {
      arr[i] = Math.floor(Math.random() * 256)
    }
  }
  return Array.from(arr, (b) => b.toString(16).padStart(2, '0')).join('')
}

/**
 * 生成当次请求的 traceparent / x-trace-id（每调用一次为新 trace）。
 * @returns {{ traceparent: string, traceId: string, 'x-trace-id': string }}
 */
export function nextRequestTraceHeaders() {
  const traceId = randomHex(16)
  const spanId = randomHex(8)
  const traceparent = `00-${traceId}-${spanId}-01`
  return {
    traceparent,
    traceId,
    'x-trace-id': traceId
  }
}

/**
 * 是否为本请求注入 trace 传播头（监控上报等路径跳过，避免噪声）。
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @returns {boolean}
 */
export function shouldAttachRequestTrace(config) {
  const url = String(config.url || '')
  if (url.includes('/monitor/liteTrace/rum/ingest')) {
    return false
  }
  if (config.headers && config.headers['X-Skip-Request-Trace'] === '1') {
    return false
  }
  return true
}

export default {
  nextRequestTraceHeaders,
  shouldAttachRequestTrace
}
