/**
 * quick-ui 接入 @quickboot/lite-rum（PC：vue-router + axios 观测 + 按钮采集）。
 */
import {
  LiteRum,
  createLiteRum,
  normalizeConfig,
  SDK_VERSION,
  clearSessionId,
  getOrCreateSessionId,
  resetSessionId,
  onSessionContextChange
} from '@quickboot/lite-rum'
import { getToken } from '@/utils/auth'
import { buildObfuscatedBasicAuthorization } from '@/utils/oauthClientBasic'

/** @type {import('@quickboot/lite-rum').LiteRum | ReturnType<typeof createLiteRum> | null} */
let singleton = null

function envBool(raw, defaultValue) {
  if (raw === undefined || raw === '') return defaultValue
  return raw === 'true' || raw === '1'
}

export function isLiteRumEnabled() {
  return envBool(import.meta.env.VITE_APP_LITE_RUM_ENABLED, true)
}

/** 与 axios 拦截器一致：Bearer 优先，否则 Client Basic */
function buildRumAuthHeaders() {
  const token = getToken()
  if (token) {
    return { Authorization: 'Bearer ' + token }
  }
  const basic = buildObfuscatedBasicAuthorization()
  return basic ? { Authorization: basic } : {}
}

export function loadLiteRumConfig() {
  const base = String(import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '')
  return {
    id: import.meta.env.VITE_APP_LITE_RUM_APP_ID || 'web-admin',
    hostUrl: `${base}/monitor/liteTrace/rum/ingest`,
    spa: false,
    reportApiSpeed: false,
    reportAssetSpeed: false,
    actionCapture: true,
    pageLoadAction: true,
    flushIntervalMs: Number(import.meta.env.VITE_APP_LITE_RUM_FLUSH_MS || 4000),
    maxBatch: Number(import.meta.env.VITE_APP_LITE_RUM_MAX_BATCH || 40),
    autoStart: false,
    getAuthHeaders: buildRumAuthHeaders
  }
}

/**
 * @returns {ReturnType<typeof createLiteRum> | null}
 */
export function setupLiteRum() {
  if (!isLiteRumEnabled()) return null
  if (singleton) return singleton
  const rum = new LiteRum(loadLiteRumConfig())
  rum.start({
    spa: false,
    reportApiSpeed: false,
    actionCapture: true
  })
  singleton = rum
  return singleton
}

export function getLiteRum() {
  return singleton
}

export {
  LiteRum,
  createLiteRum,
  normalizeConfig,
  SDK_VERSION,
  clearSessionId,
  getOrCreateSessionId,
  resetSessionId,
  onSessionContextChange
}

export default setupLiteRum
