/**
 * uni.request 封装：统一 baseUrl、鉴权、R<T> 解包、401 跳转与 Lite RUM 埋点。
 */
import { buildObfuscatedBasicAuthorization } from '@/utils/oauthClientBasic'
import { getLiteRum } from '@/monitor/liteRum'

/** 后端统一响应体 R<T> */
export type ApiResult<T> = { code: number; msg?: string; data: T; traceId?: string }

type RequestOptions = {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: unknown
  /** false 时不带 Bearer，改用 Client Basic（登录等） */
  auth?: boolean
}

const TOKEN_KEY = 'ACCESS_TOKEN'

/** 从 uni 本地存储读取 accessToken，异常或缺失时返回空串 */
export function getStoredToken(): string {
  try {
    return String(uni.getStorageSync(TOKEN_KEY) || '')
  }
  catch {
    return ''
  }
}

/** 持久化 accessToken，供后续 Bearer 鉴权 */
export function setStoredToken(token: string) {
  uni.setStorageSync(TOKEN_KEY, token)
}

/** 清除本地 token（登出或 401 时） */
export function clearStoredToken() {
  uni.removeStorageSync(TOKEN_KEY)
}

/** RUM ingest 自身不上报，避免递归 */
function shouldSkipRumUrl(url: string) {
  return url.includes('/monitor/liteTrace/rum/ingest')
}

/** 生成客户端 traceId，优先 crypto.randomUUID */
function newClientTraceId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID().replace(/-/g, '')
  }
  return `${Date.now().toString(16)}${Math.random().toString(16).slice(2, 10)}`
}

/**
 * 发起 API 请求并返回解包后的 data。
 * - H5 与原生端 baseUrl 环境变量不同
 * - auth 为 false 或无 token 时使用混淆 Basic（登录等匿名接口）
 * - HTTP/biz 非 200 或 401 时 reject；401 会清 token 并 reLaunch 登录页
 */
export function request<T>(options: RequestOptions): Promise<T> {
  const isH5 = process.env.UNI_PLATFORM === 'h5'
  const rawBase = isH5
    ? (import.meta.env.VITE_APP_BASE_API || '')
    : (import.meta.env.VITE_APP_BASE_API_NATIVE || import.meta.env.VITE_APP_BASE_API || '')
  const base = String(rawBase).replace(/\/$/, '')
  const auth = options.auth !== false
  const header: Record<string, string> = {
    'Content-Type': 'application/json;charset=utf-8',
  }
  const token = getStoredToken()
  // 已登录：Bearer；否则（含 auth:false）尝试 Client Basic
  if (auth && token) {
    header.Authorization = `Bearer ${token}`
  }
  else {
    const basic = buildObfuscatedBasicAuthorization()
    if (basic) {
      header.Authorization = basic
    }
  }

  const pathOnly = String(options.url || '').split('?')[0]
  const skipRum = shouldSkipRumUrl(pathOnly)
  const rum = !skipRum ? getLiteRum() : null
  const clientTraceId = rum ? newClientTraceId() : ''
  if (clientTraceId) {
    header['x-trace-id'] = clientTraceId
  }
  const started = Date.now()

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${base}${options.url}`,
      method: options.method || 'GET',
      data: options.data as UniApp.RequestOptions['data'],
      header,
      success: (res) => {
        const status = res.statusCode || 0
        const body = res.data as ApiResult<T>
        if (rum) {
          const bizCode = body && typeof body.code === 'number' ? body.code : undefined
          const ok = status >= 200 && status < 300 && (bizCode === undefined || bizCode === 200)
          const active = typeof rum.getActiveOperation === 'function' ? rum.getActiveOperation() : null
          rum.trackApi({
            method: String(options.method || 'GET').toLowerCase(),
            url: pathOnly,
            status,
            ok,
            durationMs: Date.now() - started,
            traceId: (body && body.traceId) || clientTraceId,
            operationId: active ? active.operationId : undefined,
            action: active ? active.action : undefined,
            bizCode,
            bizMsg: body && body.msg != null ? String(body.msg).slice(0, 200) : undefined,
          })
        }
        if (status === 401 || body?.code === 401) {
          // 登录失效：清 token 并强制回登录页
          clearStoredToken()
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error(body?.msg || '未登录或登录已过期'))
          return
        }
        if (status < 200 || status >= 300) {
          reject(new Error(`HTTP ${status}`))
          return
        }
        // 业务码非 200 视为失败（与 quickboot R<T> 约定一致）
        if (body && typeof body.code === 'number' && body.code !== 200) {
          reject(new Error(body.msg || `业务错误 ${body.code}`))
          return
        }
        resolve(body.data)
      },
      fail: (err) => {
        if (rum) {
          const active = typeof rum.getActiveOperation === 'function' ? rum.getActiveOperation() : null
          rum.trackApi({
            method: String(options.method || 'GET').toLowerCase(),
            url: pathOnly,
            status: 0,
            ok: false,
            durationMs: Date.now() - started,
            traceId: clientTraceId || undefined,
            operationId: active ? active.operationId : undefined,
            action: active ? active.action : undefined,
          })
        }
        reject(new Error(err.errMsg || '网络错误'))
      },
    })
  })
}
