/**
 * 管理端常用按钮 action 采集（非全站任意 click）。
 */

const ZONE_SEL =
  '.app-main, .login-container, .el-dialog, .el-drawer, .el-message-box, .el-overlay-dialog'
const EXCLUDE_SEL =
  '.sidebar-container, .navbar, .tags-view-container, .fixed-header, .el-pagination, .el-pager, .el-dialog__headerbtn, .el-drawer__close-btn, .el-message-box__headerbtn'
const CONTROL_SEL = '[data-rum-action], .el-button, button.el-button, .el-link, a.el-link'

const IGNORE_LABELS = new Set([
  '',
  '×',
  'x',
  'X',
  '关闭',
  '取消',
  '展开/折叠',
  '隐藏搜索',
  '显示搜索'
])

/** @type {((action: string) => void) | null} */
let onAction = null
/** @type {((ev: MouseEvent) => void) | null} */
let handler = null
let lastKey = ''
let lastAt = 0

function closestControl(el) {
  if (!el || !(el instanceof Element)) return null
  return el.closest(CONTROL_SEL)
}

function inZone(el) {
  if (el.closest(EXCLUDE_SEL)) return false
  return !!el.closest(ZONE_SEL)
}

function readLabel(el) {
  const attr = el.getAttribute('data-rum-action')
  if (attr != null && String(attr).trim()) return String(attr).trim().slice(0, 64)
  const aria = el.getAttribute('aria-label')
  if (aria && String(aria).trim() && !/close|关闭/i.test(aria)) return String(aria).trim().slice(0, 64)
  const title = el.getAttribute('title')
  if (title && String(title).trim()) return String(title).trim().slice(0, 64)
  return (el.textContent || '').replace(/\s+/g, ' ').trim().slice(0, 64)
}

function isDisabled(el) {
  if (el.hasAttribute('disabled') || el.getAttribute('aria-disabled') === 'true') return true
  return el.classList.contains('is-disabled')
}

function onClick(ev) {
  if (!onAction || ev.button !== 0) return
  const control = closestControl(/** @type {Element} */ (ev.target))
  if (!control || !inZone(control) || isDisabled(control)) return
  const label = readLabel(control)
  if (!label || IGNORE_LABELS.has(label)) return
  if (!control.getAttribute('data-rum-action') && label.length <= 1 && !/[\u4e00-\u9fffA-Za-z0-9]/.test(label)) {
    return
  }
  const now = Date.now()
  const key = `${label}@${typeof location !== 'undefined' ? location.pathname : ''}`
  if (key === lastKey && now - lastAt < 400) return
  lastKey = key
  lastAt = now
  onAction(label)
}

/**
 * 在 document 捕获阶段监听 click，从业务区内按钮/链接提取 action 文案并回调 track。
 * 会先 unbind 旧监听；返回 unbindActionCapture 供 start/destroy 清理。
 * @param {(action: string) => void} track
 */
export function bindActionCapture(track) {
  unbindActionCapture()
  onAction = track
  handler = onClick
  if (typeof document !== 'undefined') {
    document.addEventListener('click', handler, true)
  }
  return unbindActionCapture
}

/** 移除 bindActionCapture 注册的 click 监听并重置内部状态 */
export function unbindActionCapture() {
  if (handler && typeof document !== 'undefined') {
    document.removeEventListener('click', handler, true)
  }
  handler = null
  onAction = null
}
