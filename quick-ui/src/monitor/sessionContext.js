/**
 * 前端监控登录会话 ID：同一次登录、同浏览器跨 tab 共用 sessionId，供后台串联查询。
 * 存储于 localStorage；登出清除，重新登录轮换。
 */
const STORAGE_KEY = 'qb_client_track_session'

/** Node/单测无 localStorage 时的内存回退 */
let memorySessionId = null

/**
 * @returns {string}
 */
function newSessionId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

function readStoredSessionId() {
  if (typeof localStorage === 'undefined') {
    return memorySessionId
  }
  try {
    let id = localStorage.getItem(STORAGE_KEY)
    // 从旧版 sessionStorage 一次性迁移，避免升级后同登录多 tab 各走各的 ID
    if (!id && typeof sessionStorage !== 'undefined') {
      const legacy = sessionStorage.getItem(STORAGE_KEY)
      if (legacy) {
        localStorage.setItem(STORAGE_KEY, legacy)
        sessionStorage.removeItem(STORAGE_KEY)
        id = legacy
      }
    }
    return id || memorySessionId
  } catch {
    return memorySessionId
  }
}

function writeStoredSessionId(id) {
  memorySessionId = id
  if (typeof localStorage === 'undefined') {
    return
  }
  try {
    localStorage.setItem(STORAGE_KEY, id)
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem(STORAGE_KEY)
    }
  } catch {
    /* ignore */
  }
}

function removeStoredSessionId() {
  memorySessionId = null
  if (typeof localStorage === 'undefined') {
    return
  }
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    /* ignore */
  }
  if (typeof sessionStorage !== 'undefined') {
    try {
      sessionStorage.removeItem(STORAGE_KEY)
    } catch {
      /* ignore */
    }
  }
}

/**
 * 读取或创建当前登录会话的 sessionId（跨 tab 共用）。
 * @returns {string}
 */
export function getOrCreateSessionId() {
  let id = readStoredSessionId()
  if (!id) {
    id = newSessionId()
    writeStoredSessionId(id)
  }
  return id
}

/**
 * 登录成功后轮换 sessionId（新登录 = 新会话链路）。
 * @returns {string}
 */
export function resetSessionId() {
  const id = newSessionId()
  writeStoredSessionId(id)
  return id
}

/**
 * 登出时清除 sessionId。
 */
export function clearSessionId() {
  removeStoredSessionId()
}

/** 供单测重置模块状态 */
export function resetSessionContextForTest() {
  removeStoredSessionId()
}

export default {
  getOrCreateSessionId,
  resetSessionId,
  clearSessionId,
  resetSessionContextForTest
}
