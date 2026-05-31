/**
 * 隐式操作批次：页面访问批 + 按钮操作批，配合 sessionId/pageVisitId 串联链路。
 */
import { formatTrackLabel } from './trackLabel'

/** @type {'page_visit' | 'action' | null} */
let batchKind = null

/** @type {string | null} */
let batchOperationId = null

/** @type {string | null} 当前页面访问 ID，同页内多批操作共用 */
let pageVisitId = null

/** @type {string | null} */
let lastTrigger = null

/** @type {string} */
let activePage =
  typeof location !== 'undefined' && location.pathname ? location.pathname : ''

/** @type {number} */
let idleMs = 2000

/** @type {ReturnType<typeof setTimeout> | null} */
let idleTimer = null

/** @type {ReturnType<typeof setTimeout> | null} */
let overlayPollTimer = null

/** @type {((reason: string, operationId: string | null) => void) | null} */
let onFlushHook = null

const OVERLAY_SELECTORS = [
  '.el-overlay',
  '.el-message-box__wrapper',
  '.el-dialog__wrapper',
  '.el-drawer__wrapper'
]

/** @type {(() => boolean) | null} */
let overlayBlockingOverride = null

/** overlay 检测结果短缓存，避免 idle 轮询频繁 querySelector + getComputedStyle */
let overlayCacheValue = false
let overlayCacheAt = 0
const OVERLAY_CACHE_MS = 200

/**
 * @param {{ idleMs?: number }} [options]
 */
export function configureBatchSession(options = {}) {
  if (options.idleMs != null && Number.isFinite(options.idleMs)) {
    idleMs = Math.max(500, Number(options.idleMs))
  }
}

/**
 * @param {(reason: string, operationId: string | null) => void} fn
 */
export function registerBatchFlushHook(fn) {
  onFlushHook = fn
}

function newOperationId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`
}

function clearIdleTimer() {
  if (idleTimer != null) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
}

function clearOverlayPoll() {
  if (overlayPollTimer != null) {
    clearTimeout(overlayPollTimer)
    overlayPollTimer = null
  }
}

export function setOverlayBlockingOverrideForTest(fn) {
  overlayBlockingOverride = fn
}

export function isOverlayBlocking() {
  if (overlayBlockingOverride) {
    return overlayBlockingOverride()
  }
  const now = Date.now()
  if (now - overlayCacheAt < OVERLAY_CACHE_MS) {
    return overlayCacheValue
  }
  overlayCacheAt = now
  if (typeof document === 'undefined') {
    overlayCacheValue = false
    return false
  }
  for (const sel of OVERLAY_SELECTORS) {
    const nodes = document.querySelectorAll(sel)
    for (const node of nodes) {
      if (!(node instanceof HTMLElement)) {
        continue
      }
      if (node.offsetParent === null && !node.classList.contains('el-overlay')) {
        continue
      }
      const style = window.getComputedStyle(node)
      if (style.display === 'none' || style.visibility === 'hidden') {
        continue
      }
      if (node.classList.contains('el-overlay') && style.opacity === '0') {
        continue
      }
      overlayCacheValue = true
      return true
    }
  }
  overlayCacheValue = false
  return false
}

function scheduleIdleFlush() {
  if (batchKind !== 'action') {
    return
  }
  clearIdleTimer()
  clearOverlayPoll()
  idleTimer = setTimeout(tryIdleFlush, idleMs)
}

function tryIdleFlush() {
  idleTimer = null
  if (batchKind !== 'action') {
    return
  }
  if (isOverlayBlocking()) {
    overlayCacheAt = 0
    overlayPollTimer = setTimeout(tryIdleFlush, 400)
    return
  }
  const opId = batchOperationId
  if (!onFlushHook) {
    closeBatch()
    return
  }
  onFlushHook('idle', opId)
  closeBatch()
}

/**
 * 若当前为页面访问批且缓冲有内容，先 flush（按钮操作开始前）。
 */
export function flushPageVisitIfNeeded() {
  if (batchKind !== 'page_visit' || !batchOperationId) {
    return
  }
  flushBatchSync('page_action')
}

/**
 * 进入新页面：结束上一页批次，开启页面访问批。
 * @param {string} pageTitle
 * @param {string} [path]
 * @returns {string} pageVisitId
 */
export function openPageVisit(pageTitle, path) {
  if (batchKind === 'action' && batchOperationId) {
    flushBatchSync('page_leave')
  } else if (batchKind === 'page_visit' && batchOperationId) {
    flushBatchSync('page_leave')
  }
  pageVisitId = newOperationId()
  batchOperationId = pageVisitId
  batchKind = 'page_visit'
  const title = String(pageTitle || path || '页面').trim()
  lastTrigger = title.startsWith('访问:') ? title : `访问:${title}`
  clearIdleTimer()
  clearOverlayPoll()
  if (import.meta.env.DEV) {
    // eslint-disable-next-line no-console
    console.debug('[batchSession] pageVisit', pageVisitId, lastTrigger, 'page=', path || activePage)
  }
  return pageVisitId
}

/**
 * 主操作 click：先结束页面访问批，再开按钮操作批。
 * @param {string} triggerLabel
 * @returns {string}
 */
export function openBatch(triggerLabel) {
  if (batchKind === 'action' && batchOperationId) {
    flushBatchSync('idle')
  }
  flushPageVisitIfNeeded()
  batchOperationId = newOperationId()
  batchKind = 'action'
  const text = String(triggerLabel || '').trim()
  lastTrigger = formatTrackLabel(text) || text || null
  scheduleIdleFlush()
  if (import.meta.env.DEV && lastTrigger) {
    // eslint-disable-next-line no-console
    console.debug('[batchSession] action', batchOperationId, 'pageVisit=', pageVisitId, lastTrigger)
  }
  return batchOperationId
}

export function touchBatch() {
  if (batchKind === 'action') {
    scheduleIdleFlush()
  }
}

export function touchBatchPassive() {
  touchBatch()
}

export function closeBatch() {
  batchOperationId = null
  batchKind = null
  lastTrigger = null
  clearIdleTimer()
  clearOverlayPoll()
}

export function cancelBatch() {
  closeBatch()
}

/**
 * @param {string} reason
 * @param {string} [forcedOperationId]
 */
export function flushBatchSync(reason, forcedOperationId) {
  const opId = forcedOperationId || batchOperationId
  if (onFlushHook && (opId || batchKind)) {
    onFlushHook(reason, opId || null)
  }
  closeBatch()
}

export function setActivePage(path) {
  if (path) {
    activePage = path
  }
}

export function getActivePage() {
  return activePage
}

export function getOperationId() {
  return batchOperationId
}

export function getPageVisitId() {
  return pageVisitId
}

/**
 * @returns {'page_visit' | 'action' | null}
 */
export function getBatchKind() {
  return batchKind
}

export function getLastTrigger() {
  return lastTrigger
}

export function resetBatchSessionForTest() {
  closeBatch()
  pageVisitId = null
  onFlushHook = null
  idleMs = 2000
  overlayBlockingOverride = null
  activePage = typeof location !== 'undefined' && location.pathname ? location.pathname : ''
}

export default {
  configureBatchSession,
  registerBatchFlushHook,
  isOverlayBlocking,
  openPageVisit,
  flushPageVisitIfNeeded,
  openBatch,
  touchBatch,
  touchBatchPassive,
  closeBatch,
  cancelBatch,
  flushBatchSync,
  setActivePage,
  getActivePage,
  getOperationId,
  getPageVisitId,
  getBatchKind,
  getLastTrigger,
  resetBatchSessionForTest,
  setOverlayBlockingOverrideForTest
}
