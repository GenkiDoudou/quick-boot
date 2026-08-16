/**
 * 会话 ID：可注入 storage（浏览器 localStorage / uni.setStorageSync）。
 */
const STORAGE_KEY = 'qb_client_track_session'

/** @type {{ getItem: (k: string) => string | null, setItem: (k: string, v: string) => void, removeItem: (k: string) => void } | null} */
let storageAdapter = null

let memorySessionId = null
/** @type {Set<() => void>} */
const changeListeners = new Set()

/**
 * 注入持久化适配器（如 uni.setStorageSync）；未配置时回退 localStorage。
 * @param {{ getItem: (k: string) => string | null, setItem: (k: string, v: string) => void, removeItem: (k: string) => void } | null} adapter
 */
export function configureSessionStorage(adapter) {
  storageAdapter = adapter
}

function storeGet(key) {
  if (storageAdapter) {
    try {
      const v = storageAdapter.getItem(key)
      return v == null || v === '' ? null : String(v)
    } catch {
      return null
    }
  }
  if (typeof localStorage === 'undefined') return null
  try {
    return localStorage.getItem(key)
  } catch {
    return null
  }
}

function storeSet(key, value) {
  if (storageAdapter) {
    try {
      storageAdapter.setItem(key, value)
    } catch {
      /* ignore */
    }
    return
  }
  if (typeof localStorage === 'undefined') return
  try {
    localStorage.setItem(key, value)
  } catch {
    /* ignore */
  }
}

function storeRemove(key) {
  if (storageAdapter) {
    try {
      storageAdapter.removeItem(key)
    } catch {
      /* ignore */
    }
    return
  }
  if (typeof localStorage === 'undefined') return
  try {
    localStorage.removeItem(key)
  } catch {
    /* ignore */
  }
}

/** 订阅 sessionId 变更（reset/clear 后触发）；返回取消订阅函数 */
export function onSessionContextChange(listener) {
  changeListeners.add(listener)
  return () => changeListeners.delete(listener)
}

function notify() {
  changeListeners.forEach((fn) => {
    try {
      fn()
    } catch {
      /* ignore */
    }
  })
}

function newSessionId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

function readStored() {
  let id = storeGet(STORAGE_KEY)
  if (!id && !storageAdapter && typeof sessionStorage !== 'undefined') {
    try {
      const legacy = sessionStorage.getItem(STORAGE_KEY)
      if (legacy) {
        storeSet(STORAGE_KEY, legacy)
        sessionStorage.removeItem(STORAGE_KEY)
        id = legacy
      }
    } catch {
      /* ignore */
    }
  }
  return id || memorySessionId
}

function writeStored(id) {
  memorySessionId = id
  storeSet(STORAGE_KEY, id)
  if (!storageAdapter && typeof sessionStorage !== 'undefined') {
    try {
      sessionStorage.removeItem(STORAGE_KEY)
    } catch {
      /* ignore */
    }
  }
}

function removeStored() {
  memorySessionId = null
  storeRemove(STORAGE_KEY)
  if (!storageAdapter && typeof sessionStorage !== 'undefined') {
    try {
      sessionStorage.removeItem(STORAGE_KEY)
    } catch {
      /* ignore */
    }
  }
}

/** 读取已有 sessionId，不存在则生成并持久化 */
export function getOrCreateSessionId() {
  let id = readStored()
  if (!id) {
    id = newSessionId()
    writeStored(id)
  }
  return id
}

/** 强制轮换 sessionId 并通知监听方（如用户登出后新会话） */
export function resetSessionId() {
  const id = newSessionId()
  writeStored(id)
  notify()
  return id
}

/** 清除持久化 sessionId 并通知监听方 */
export function clearSessionId() {
  removeStored()
  notify()
}

/** 单测专用：清空内存与存储中的 session，不触发 change 监听 */
export function resetSessionContextForTest() {
  removeStored()
}
