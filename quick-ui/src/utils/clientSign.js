/**
 * Client HMAC 请求签名，与后端 {@code ClientSignService.buildCanonical} 一致。
 */

/**
 * 解析用于签名的 path（不含 query、不含 /dev-api 前缀）。
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
  if (!path.startsWith('/')) {
    path = '/' + path
  }
  return path
}

/**
 * 与 axios 发送一致的 body 字节（拦截器阶段）。
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
  const ct = headers['Content-Type'] || headers['content-type'] || ''
  if (String(ct).includes('application/json')) {
    return new TextEncoder().encode(JSON.stringify(data))
  }
  return new Uint8Array(0)
}

/**
 * @param {Uint8Array} bytes
 * @returns {Promise<string>} 小写 hex
 */
async function sha256Hex(bytes) {
  const digest = await crypto.subtle.digest('SHA-256', bytes)
  return Array.from(new Uint8Array(digest))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}

/**
 * @param {string} secret
 * @param {string} canonical
 * @returns {Promise<string>} Base64 HMAC-SHA256
 */
async function hmacSha256Base64(secret, canonical) {
  const key = await crypto.subtle.importKey(
    'raw',
    new TextEncoder().encode(secret),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  )
  const sig = await crypto.subtle.sign('HMAC', key, new TextEncoder().encode(canonical))
  const bin = String.fromCharCode(...new Uint8Array(sig))
  return btoa(bin)
}

/**
 * @returns {string} 32 位 hex nonce
 */
function randomNonce() {
  const buf = new Uint8Array(16)
  crypto.getRandomValues(buf)
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
  const clientId = import.meta.env.VITE_APP_CLIENT_ID
  const signKey = import.meta.env.VITE_APP_CLIENT_SIGN_KEY
  if (!clientId || !signKey) {
    if (import.meta.env.DEV) {
      console.warn('[clientSign] 未配置 VITE_APP_CLIENT_ID / VITE_APP_CLIENT_SIGN_KEY，跳过加签')
    }
    return config
  }

  const method = (config.method || 'get').toUpperCase()
  const path = resolveSignPath(config)
  const bodyBytes = await serializeBodyBytes(config)
  const bodyHash = await sha256Hex(bodyBytes)
  const timestamp = String(Math.floor(Date.now() / 1000))
  const nonce = randomNonce()
  const canonical = `${method}\n${path}\n${bodyHash}\n${timestamp}\n${nonce}\n${clientId}`
  const signature = await hmacSha256Base64(signKey, canonical)

  config.headers = config.headers || {}
  config.headers['X-Client-Id'] = clientId
  config.headers['X-Client-Timestamp'] = timestamp
  config.headers['X-Client-Nonce'] = nonce
  config.headers['X-Client-Signature'] = signature
  return config
}
