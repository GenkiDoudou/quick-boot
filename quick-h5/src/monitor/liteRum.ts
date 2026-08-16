/**
 * quick-h5 Lite RUM：uni storage + uni.request transport；PV 走页面栈 / 路由拦截。
 */
import { LiteRum, clearSessionId, getOrCreateSessionId } from '@quickboot/lite-rum'
import { buildObfuscatedBasicAuthorization } from '@/utils/oauthClientBasic'

type RumInstance = InstanceType<typeof LiteRum>

let singleton: RumInstance | null = null
let routeBound = false

/** 解析 VITE 布尔开关，缺省用 defaultValue */
function envBool(raw: string | undefined, defaultValue: boolean) {
  if (raw === undefined || raw === '') return defaultValue
  return raw === 'true' || raw === '1'
}

/** 是否启用 Lite RUM（VITE_APP_LITE_RUM_ENABLED，默认 true） */
export function isLiteRumEnabled() {
  return envBool(import.meta.env.VITE_APP_LITE_RUM_ENABLED as string | undefined, true)
}

const uniStorage = {
  getItem(key: string) {
    try {
      const v = uni.getStorageSync(key)
      return v == null || v === '' ? null : String(v)
    }
    catch {
      return null
    }
  },
  setItem(key: string, value: string) {
    uni.setStorageSync(key, value)
  },
  removeItem(key: string) {
    uni.removeStorageSync(key)
  },
}

function readAccessToken(): string {
  try {
    return String(uni.getStorageSync('ACCESS_TOKEN') || '')
  }
  catch {
    return ''
  }
}

function buildRumAuthHeaders(): Record<string, string> {
  const token = readAccessToken()
  if (token) {
    return { Authorization: `Bearer ${token}` }
  }
  const basic = buildObfuscatedBasicAuthorization()
  return basic ? { Authorization: basic } : {}
}

function resolveBaseApi() {
  const isH5 = process.env.UNI_PLATFORM === 'h5'
  const raw = isH5
    ? (import.meta.env.VITE_APP_BASE_API || '')
    : (import.meta.env.VITE_APP_BASE_API_NATIVE || import.meta.env.VITE_APP_BASE_API || '')
  return String(raw).replace(/\/$/, '')
}

function uniTransport(ctx: {
  url: string
  headers: Record<string, string>
  payload: Record<string, unknown>
}): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: ctx.url,
      method: 'POST',
      header: ctx.headers,
      data: ctx.payload,
      success: (res) => {
        const status = res.statusCode || 0
        if (status >= 200 && status < 300) {
          resolve()
          return
        }
        reject(new Error(`ingest HTTP ${status}`))
      },
      fail: (err) => {
        reject(new Error(err.errMsg || 'ingest fail'))
      },
    })
  })
}

function createRumInstance() {
  return new LiteRum({
    id: (import.meta.env.VITE_APP_LITE_RUM_APP_ID as string) || 'quick-h5',
    hostUrl: `${resolveBaseApi()}/monitor/liteTrace/rum/ingest`,
    spa: false,
    reportApiSpeed: false,
    actionCapture: false,
    pageLoadAction: true,
    autoStart: false,
    getAuthHeaders: buildRumAuthHeaders,
    transport: uniTransport,
    storage: uniStorage,
  })
}

/** 已初始化的 RUM 单例，未 setup 时为 null */
export function getLiteRum() {
  return singleton
}

/** 初始化并启动 RUM，绑定 uni 路由 PV；已启用且已创建则返回现有实例 */
export function setupLiteRum() {
  if (!isLiteRumEnabled()) return null
  if (singleton) return singleton
  const rum = createRumInstance()
  rum.start({
    spa: false,
    reportApiSpeed: false,
    actionCapture: false,
  })
  singleton = rum
  bindUniRoutePv(rum)
  return rum
}

/** 当前页面路径（含 query） */
export function currentUniPagePath() {
  try {
    const pages = getCurrentPages()
    const cur = pages[pages.length - 1] as { route?: string, options?: Record<string, string> } | undefined
    if (!cur || !cur.route) return '/'
    const path = cur.route.startsWith('/') ? cur.route : `/${cur.route}`
    const opts = cur.options || {}
    const qs = Object.keys(opts)
      .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(String(opts[k]))}`)
      .join('&')
    return qs ? `${path}?${qs}` : path
  }
  catch {
    return '/'
  }
}

function trackCurrentPage(rum: RumInstance) {
  rum.trackPv(currentUniPagePath())
}

function bindUniRoutePv(rum: RumInstance) {
  if (routeBound) return
  routeBound = true
  const wrap = (api: 'navigateTo' | 'redirectTo' | 'reLaunch' | 'switchTab' | 'navigateBack') => {
    uni.addInterceptor(api, {
      success() {
        setTimeout(() => trackCurrentPage(rum), 0)
      },
    })
  }
  wrap('navigateTo')
  wrap('redirectTo')
  wrap('reLaunch')
  wrap('switchTab')
  wrap('navigateBack')
}

/** App onShow / 登录后补一次 PV */
export function trackPageShow() {
  const rum = getLiteRum()
  if (!rum) return
  trackCurrentPage(rum)
}

/** 设置 RUM 用户标识（通常为 username） */
export function setRumUin(uin: string) {
  const rum = getLiteRum()
  if (rum && typeof rum.setUin === 'function') {
    rum.setUin(uin)
  }
}

/** 再导出会话 id 工具，供 user store 登出时使用 */
export { clearSessionId, getOrCreateSessionId }
