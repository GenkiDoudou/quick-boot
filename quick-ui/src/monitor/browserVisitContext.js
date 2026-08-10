/**
 * 浏览器访问会话 ID：用户打开浏览器访问本系统时生成，跨 tab 共用；
 * 登录/登出不换 ID；关浏览器一段时间后再次访问会生成新 ID（心跳超时判定）。
 */
const VISIT_KEY = 'qb_client_track_browser_visit'
const HEARTBEAT_KEY = 'qb_client_track_browser_visit_hb'
const TAB_BIND_KEY = 'qb_client_track_browser_visit_tab'
/** 无心跳超过该时长视为浏览器已关闭，下次访问换新 ID */
const STALE_MS = 60_000

/** Node/单测无 localStorage 时的内存回退 */
let memoryBrowserVisitId = null
let memoryHeartbeatTs = 0

/** @type {ReturnType<typeof setInterval> | null} */
let heartbeatTimer = null

/**
 * @returns {string}
 */
function newBrowserVisitId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

function touchBrowserVisitHeartbeat() {
  const now = Date.now()
  memoryHeartbeatTs = now
  if (typeof localStorage === 'undefined') {
    return
  }
  try {
    localStorage.setItem(HEARTBEAT_KEY, String(now))
  } catch {
    /* ignore */
  }
}

function readHeartbeatTs() {
  if (typeof localStorage === 'undefined') {
    return memoryHeartbeatTs
  }
  try {
    const v = localStorage.getItem(HEARTBEAT_KEY)
    return v ? Number(v) : memoryHeartbeatTs
  } catch {
    return memoryHeartbeatTs
  }
}

function writeVisitId(id) {
  memoryBrowserVisitId = id
  if (typeof localStorage !== 'undefined') {
    try {
      localStorage.setItem(VISIT_KEY, id)
    } catch {
      /* ignore */
    }
  }
  if (typeof sessionStorage !== 'undefined') {
    try {
      sessionStorage.setItem(TAB_BIND_KEY, id)
    } catch {
      /* ignore */
    }
  }
}

function readTabBoundVisitId() {
  if (typeof sessionStorage === 'undefined') {
    return memoryBrowserVisitId
  }
  try {
    const v = sessionStorage.getItem(TAB_BIND_KEY)
    return v || memoryBrowserVisitId
  } catch {
    return memoryBrowserVisitId
  }
}

function readStoredVisitId() {
  if (typeof localStorage === 'undefined') {
    return memoryBrowserVisitId
  }
  try {
    return localStorage.getItem(VISIT_KEY) || memoryBrowserVisitId
  } catch {
    return memoryBrowserVisitId
  }
}

function isVisitStale(now = Date.now()) {
  const lastHb = readHeartbeatTs()
  return !lastHb || now - lastHb > STALE_MS
}

/**
 * 读取或创建当前浏览器访问会话 ID（跨 tab；登出不清除）。
 * @returns {string}
 */
/**
 * 读取当前 browserVisitId（热路径用，不触发心跳写入）。
 * @returns {string}
 */
export function getBrowserVisitId() {
  const tabBound = readTabBoundVisitId()
  if (tabBound) {
    return tabBound
  }
  const id = readStoredVisitId()
  return id || memoryBrowserVisitId || ''
}

export function getOrCreateBrowserVisitId() {
  const now = Date.now()
  const tabBound = readTabBoundVisitId()
  if (tabBound) {
    return tabBound
  }

  let id = readStoredVisitId()
  if (!id || isVisitStale(now)) {
    id = newBrowserVisitId()
    writeVisitId(id)
  } else if (!memoryBrowserVisitId) {
    memoryBrowserVisitId = id
  }
  return id
}

/**
 * 启动心跳，维持「浏览器仍打开」判定；监控 install 时调用一次即可。
 * @param {number} [intervalMs=20000]
 * @returns {() => void} 停止心跳
 */
export function startBrowserVisitHeartbeat(intervalMs = 20_000) {
  if (heartbeatTimer != null) {
    clearInterval(heartbeatTimer)
  }
  touchBrowserVisitHeartbeat()
  getOrCreateBrowserVisitId()
  heartbeatTimer = setInterval(() => {
    touchBrowserVisitHeartbeat()
  }, intervalMs)
  return stopBrowserVisitHeartbeat
}

/** 停止心跳（测试或 dispose 用） */
export function stopBrowserVisitHeartbeat() {
  if (heartbeatTimer != null) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

/** 供单测注入浏览器访问态 */
export function seedBrowserVisitForTest(id, heartbeatTs = Date.now()) {
  memoryBrowserVisitId = id
  memoryHeartbeatTs = heartbeatTs
  if (typeof localStorage !== 'undefined') {
    try {
      localStorage.setItem(VISIT_KEY, id)
      localStorage.setItem(HEARTBEAT_KEY, String(heartbeatTs))
    } catch {
      /* ignore */
    }
  }
}

/** 供单测重置 */
export function resetBrowserVisitContextForTest() {
  stopBrowserVisitHeartbeat()
  memoryBrowserVisitId = null
  memoryHeartbeatTs = 0
  if (typeof localStorage !== 'undefined') {
    try {
      localStorage.removeItem(VISIT_KEY)
      localStorage.removeItem(HEARTBEAT_KEY)
    } catch {
      /* ignore */
    }
  }
  if (typeof sessionStorage !== 'undefined') {
    try {
      sessionStorage.removeItem(TAB_BIND_KEY)
    } catch {
      /* ignore */
    }
  }
}

export default {
  getBrowserVisitId,
  getOrCreateBrowserVisitId,
  startBrowserVisitHeartbeat,
  stopBrowserVisitHeartbeat,
  resetBrowserVisitContextForTest
}
