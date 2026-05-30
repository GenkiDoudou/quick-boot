/**
 * 管理「当前用户操作」的 operationId，与后端 traceId 解耦。
 * 一次显式 begin 对应一个 operationId；同 operation 触发的多个 API 共用该 ID。
 */

/** @type {string | null} */
let activeOperationId = null

/** @type {string | null} */
let lastTrigger = null

/** @type {string | null} */
let pendingTrigger = null

/** @type {string} */
let activePage =
  typeof location !== 'undefined' && location.pathname ? location.pathname : ''

/** @type {(() => void) | null} */
let onEndOperationHook = null

/** @type {((operationId: string) => void) | null} */
let onBeginOperationHook = null

/**
 * 注册 operation 结束回调（由 createUserMonitor 用于一次性 flush 整段操作事件）。
 * @param {() => void} fn
 */
export function registerOperationEndHook(fn) {
  onEndOperationHook = fn
}

/**
 * 注册 operation 开始回调（用于回填紧邻的 click 事件 operationId）。
 * @param {(operationId: string) => void} fn
 */
export function registerOperationBeginHook(fn) {
  onBeginOperationHook = fn
}

/**
 * 生成 UUID 风格 operationId。
 * @returns {string}
 */
function newOperationId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

/**
 * 同步当前路由 path（由 monitor 插件 router 钩子调用）。
 * @param {string} path
 */
export function setActivePage(path) {
  if (path) {
    activePage = path
  }
}

/**
 * @returns {string}
 */
export function getActivePage() {
  return activePage
}

/**
 * 记录最近一次 data-track 点击，供后续 beginOperation 关联触发源。
 * @param {string} label
 */
export function setPendingTrigger(label) {
  pendingTrigger = label || null
}

/**
 * @returns {string | null}
 */
export function getLastTrigger() {
  return lastTrigger
}

/**
 * 开始一次新的用户操作上下文（页面显式调用，click 层不再重复 begin）。
 * @param {string} [reason] 操作原因，如 user-edit:123
 * @returns {string} 新建的 operationId
 */
export function beginOperation(reason) {
  activeOperationId = newOperationId()
  lastTrigger = reason || pendingTrigger || null
  pendingTrigger = null
  if (onBeginOperationHook) {
    onBeginOperationHook(activeOperationId)
  }
  if (import.meta.env.DEV && reason) {
    // eslint-disable-next-line no-console
    console.debug('[operationContext] begin', activeOperationId, reason, 'page=', activePage)
  }
  return activeOperationId
}

/**
 * @returns {string | null} 当前活跃 operationId
 */
export function getOperationId() {
  return activeOperationId
}

/**
 * 清除当前活跃 operation（弹窗关闭 / 路由离开时调用）。
 */
export function endOperation() {
  const hadActive = activeOperationId != null
  activeOperationId = null
  lastTrigger = null
  if (hadActive && onEndOperationHook) {
    onEndOperationHook()
  }
}

/**
 * 在独立 operation 上下文中执行 fn。
 * @template T
 * @param {() => T | Promise<T>} fn
 * @param {string} [reason]
 * @returns {Promise<T>}
 */
export async function runInOperation(fn, reason) {
  beginOperation(reason)
  try {
    return await fn()
  } finally {
    endOperation()
  }
}

export default {
  beginOperation,
  getOperationId,
  endOperation,
  runInOperation,
  registerOperationEndHook,
  registerOperationBeginHook,
  setActivePage,
  getActivePage,
  setPendingTrigger,
  getLastTrigger
}
