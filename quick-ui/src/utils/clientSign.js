/**
 * Client HMAC 请求签名，与后端 {@code ClientSignService.buildCanonical} 一致。
 * <p>
 * 使用 crypto-js 而非 Web Crypto {@code subtle}，以便在 HTTP 生产环境（非安全上下文）下可用。
 */
import CryptoJS from 'crypto-js'
import { tansParams } from '@/utils/ruoyi'

/**
 * 解析用于签名的 path（不含 query、不含 /dev-api、/prod-api 前缀）。
 *
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @returns {string}
 */
function resolveSignPath(config) {
  let path = config.url || '/'
  const q = path.indexOf('?')
  if (q >= 0) {
    path = path.substring(0, q)
  }
  const apiBase = import.meta.env.VITE_APP_BASE_API || ''
  if (apiBase && path.startsWith(apiBase)) {
    path = path.slice(apiBase.length) || '/'
  }
  if (apiBase.startsWith('http')) {
    try {
      const basePath = new URL(apiBase).pathname.replace(/\/$/, '')
      if (basePath && path.startsWith(basePath)) {
        path = path.slice(basePath.length) || '/'
      }
    } catch {
      // ignore invalid URL
    }
  }
  for (const prefix of ['/dev-api', '/prod-api']) {
    if (path.startsWith(prefix)) {
      path = path.slice(prefix.length) || '/'
    }
  }
  if (!path.startsWith('/')) {
    path = '/' + path
  }
  return path
}

/**
 * @param {Uint8Array} bytes
 * @returns {CryptoJS.lib.WordArray}
 */
function bytesToWordArray(bytes) {
  const words = []
  for (let i = 0; i < bytes.length; i += 1) {
    words[i >>> 2] |= bytes[i] << (24 - (i % 4) * 8)
  }
  return CryptoJS.lib.WordArray.create(words, bytes.length)
}

/**
 * 与 axios 发送一致的 body 字节（拦截器阶段）。
 *
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @returns {Promise<Uint8Array>}
 */
/**
 * 取出与最终 HTTP 请求一致的 body 字节；JSON 对象会先 {@code JSON.stringify} 并写回 {@code config.data}。
 *
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @returns {Promise<Uint8Array>}
 */
async function serializeBodyBytes(config) {
  const data = config.data
  if (data == null || data === '') {
    return new Uint8Array(0)
  }
  if (typeof data === 'string') {
    return new TextEncoder().encode(data)
  }
  if (data instanceof ArrayBuffer) {
    return new Uint8Array(data)
  }
  if (ArrayBuffer.isView(data)) {
    return new Uint8Array(data.buffer, data.byteOffset, data.byteLength)
  }
  if (typeof FormData !== 'undefined' && data instanceof FormData) {
    return new Uint8Array(0)
  }
  if (typeof Blob !== 'undefined' && data instanceof Blob) {
    return new Uint8Array(await data.arrayBuffer())
  }
  const headers = config.headers || {}
  const ct = String(headers['Content-Type'] || headers['content-type'] || '').toLowerCase()
  if (typeof data === 'object') {
    // 与 downloadRequest / 表单 POST 的 transformRequest + tansParams 对齐
    if (ct.includes('application/x-www-form-urlencoded')) {
      const encoded = tansParams(data)
      return new TextEncoder().encode(encoded)
    }
    const asJson = !ct || ct.includes('application/json')
    if (asJson) {
      const json = JSON.stringify(data)
      config.data = json
      return new TextEncoder().encode(json)
    }
  }
  return new Uint8Array(0)
}

/** 空 body 的 SHA256（GET 等无体请求复用，减少主线程重复哈希） */
const EMPTY_BODY_BYTES = new Uint8Array(0)
const EMPTY_BODY_HASH = CryptoJS.SHA256(bytesToWordArray(EMPTY_BODY_BYTES)).toString(CryptoJS.enc.Hex)

/**
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @returns {boolean}
 */
function isEmptyBodyRequest(config) {
  const data = config.data
  if (data == null || data === '') {
    return true
  }
  if (typeof data === 'string' && data.length === 0) {
    return true
  }
  return false
}

/**
 * @param {Uint8Array} bytes
 * @returns {string} 小写 hex
 */
function sha256Hex(bytes) {
  return CryptoJS.SHA256(bytesToWordArray(bytes)).toString(CryptoJS.enc.Hex)
}

/**
 * @param {string} secret
 * @param {string} canonical
 * @returns {string} Base64 HMAC-SHA256
 */
function hmacSha256Base64(secret, canonical) {
  return CryptoJS.HmacSHA256(canonical, secret).toString(CryptoJS.enc.Base64)
}

/**
 * @returns {string} 32 位 hex nonce
 */
function randomNonce() {
  const buf = new Uint8Array(16)
  if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
    crypto.getRandomValues(buf)
  } else {
    for (let i = 0; i < buf.length; i += 1) {
      buf[i] = Math.floor(Math.random() * 256)
    }
  }
  return Array.from(buf)
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}

/**
 * 为 axios 请求附加 Client 签名头；未配置 clientId/signKey 时跳过。
 *
 * @param {import('axios').InternalAxiosRequestConfig} config
 * @returns {Promise<import('axios').InternalAxiosRequestConfig>}
 */
export async function applyClientSignHeaders(config) {
  const clientId = String(import.meta.env.VITE_APP_CLIENT_ID || '').trim()
  const signKey = String(import.meta.env.VITE_APP_CLIENT_SIGN_KEY || '').trim()
  if (!clientId || !signKey) {
    if (import.meta.env.DEV) {
      console.warn('[clientSign] 未配置 VITE_APP_CLIENT_ID / VITE_APP_CLIENT_SIGN_KEY，跳过加签')
    }
    return config
  }

  const method = (config.method || 'get').toUpperCase()
  const path = resolveSignPath(config)
  let bodyBytes = EMPTY_BODY_BYTES
  if (!isEmptyBodyRequest(config)) {
    bodyBytes = await serializeBodyBytes(config)
  }
  const signed = buildSignedHeaders(method, path, bodyBytes, clientId, signKey)

  config.headers = config.headers || {}
  Object.assign(config.headers, signed)
  return config
}

/**
 * 为原生 fetch 构造 Client HMAC 签名头（监控上报等不走 axios 的场景）。
 *
 * @param {string} method HTTP 方法
 * @param {string} path 不含 query 的 API 路径（如 `/monitor/clientTrack/report`）
 * @param {string} [bodyString=''] 请求体字符串
 * @returns {Promise<Record<string, string>>}
 */
export async function buildSignedFetchHeaders(method, path, bodyString = '') {
  const clientId = String(import.meta.env.VITE_APP_CLIENT_ID || '').trim()
  const signKey = String(import.meta.env.VITE_APP_CLIENT_SIGN_KEY || '').trim()
  if (!clientId || !signKey) {
    return {}
  }
  const bodyBytes = new TextEncoder().encode(bodyString || '')
  return buildSignedHeaders(method, path, bodyBytes, clientId, signKey)
}

/**
 * @param {string} method
 * @param {string} path
 * @param {Uint8Array} bodyBytes
 * @param {string} clientId
 * @param {string} signKey
 * @returns {Record<string, string>}
 */
function buildSignedHeaders(method, path, bodyBytes, clientId, signKey) {
  const bodyHash =
    bodyBytes === EMPTY_BODY_BYTES || bodyBytes.length === 0
      ? EMPTY_BODY_HASH
      : sha256Hex(bodyBytes)
  const timestamp = String(Math.floor(Date.now() / 1000))
  const nonce = randomNonce()
  const canonical = `${(method || 'get').toUpperCase()}\n${path}\n${bodyHash}\n${timestamp}\n${nonce}\n${clientId}`
  const signature = hmacSha256Base64(signKey, canonical)
  return {
    'X-Client-Id': clientId,
    'X-Client-Timestamp': timestamp,
    'X-Client-Nonce': nonce,
    'X-Client-Signature': signature
  }
}

/**
 * 与后端对齐的 canonical 串（供单测或调试）。
 *
 * @param {string} method
 * @param {string} path
 * @param {Uint8Array} bodyBytes
 * @param {string} timestamp
 * @param {string} nonce
 * @param {string} clientId
 * @returns {string}
 */
export function buildCanonical(method, path, bodyBytes, timestamp, nonce, clientId) {
  const upperMethod = (method || '').toUpperCase()
  const bodyHash = sha256Hex(bodyBytes || new Uint8Array(0))
  return `${upperMethod}\n${path}\n${bodyHash}\n${timestamp}\n${nonce}\n${clientId}`
}
