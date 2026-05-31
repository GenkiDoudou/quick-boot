/**
 * 触发操作可读名称：优先使用 DOM 按钮文案（查看/修改/删除），仅对技术 slug 做启发式转换。
 */

/** @type {Record<string, string>} C7Button btn-type 等无文案时的兜底 */
const BTN_TYPE_FALLBACK = {
  add: '新增',
  edit: '修改',
  delete: '删除',
  query: '查询',
  refresh: '重置',
  upload: '上传',
  download: '下载',
  submit: '提交',
  cancel: '取消'
}

/**
 * @param {string} text
 * @returns {string}
 */
function normalizeWhitespace(text) {
  return String(text || '').replace(/\s+/g, ' ').trim()
}

/**
 * 是否已是用户可见的中文/自然语言标签（无需再映射）。
 * @param {string | null | undefined} text
 * @returns {boolean}
 */
export function isHumanReadableLabel(text) {
  if (text == null || String(text).trim() === '') {
    return false
  }
  const s = String(text).trim()
  if (isTechnicalSlug(s)) {
    return false
  }
  if (/[\u4e00-\u9fff]/.test(s)) {
    return true
  }
  if (s.length <= 16 && !/^[a-z0-9][a-z0-9_-]*$/i.test(s)) {
    return true
  }
  return false
}

/**
 * 是否为技术 slug（user-edit:1、c7-json-table-delete）。
 * @param {string} text
 * @returns {boolean}
 */
function isTechnicalSlug(text) {
  return /^[a-z0-9]+([:_-][a-z0-9]+)+$/i.test(text) || text.startsWith('c7-json-table-')
}

/**
 * 从按钮/链接 DOM 提取可见操作名（innerText / aria-label / title）。
 * @param {HTMLElement} node
 * @returns {string}
 */
export function extractVisibleActionLabel(node) {
  if (!node) {
    return ''
  }
  const text = normalizeWhitespace(node.textContent || node.innerText || '')
  if (text && text.length <= 40) {
    return text
  }
  const aria = node.getAttribute('aria-label')
  if (aria) {
    return normalizeWhitespace(aria).slice(0, 40)
  }
  const title = node.getAttribute('title')
  if (title) {
    return normalizeWhitespace(title).slice(0, 40)
  }
  const btnType = node.getAttribute('btn-type') || node.getAttribute('btnType')
  if (btnType && BTN_TYPE_FALLBACK[btnType]) {
    return BTN_TYPE_FALLBACK[btnType]
  }
  return ''
}

/**
 * 将 trigger / target 转为列表展示文案；已是中文则原样返回。
 * @param {string | null | undefined} raw
 * @returns {string}
 */
export function formatTrackLabel(raw) {
  if (raw == null || String(raw).trim() === '') {
    return ''
  }
  const key = String(raw).trim()
  if (isHumanReadableLabel(key)) {
    return key
  }
  if (key.includes('delete') || key.includes('remove')) {
    return '删除'
  }
  if (key.includes('edit') || key.includes('update')) {
    return '修改'
  }
  if (key.includes('add') || key.includes('create')) {
    return '新增'
  }
  if (key.includes('export')) {
    return '导出'
  }
  if (key.includes('import')) {
    return '导入'
  }
  if (key.includes('view') || key.includes('query')) {
    return '查看'
  }
  return key
}

/**
 * beginOperation 时优先采用点击按钮文案，而非 user-edit:123 类 slug。
 * @param {string | null | undefined} pendingClickLabel
 * @param {string | null | undefined} reason
 * @returns {string | null}
 */
export function resolveDisplayTrigger(pendingClickLabel, reason) {
  const pending = pendingClickLabel != null ? String(pendingClickLabel).trim() : ''
  const r = reason != null ? String(reason).trim() : ''
  if (pending && isHumanReadableLabel(pending)) {
    return pending
  }
  if (r && isHumanReadableLabel(r)) {
    return r
  }
  if (pending) {
    return formatTrackLabel(pending)
  }
  if (r) {
    return isTechnicalSlug(r) ? formatTrackLabel(r) : r
  }
  return null
}

/**
 * @param {string} raw
 * @returns {boolean}
 */
function isMeaningfulClickTarget(raw) {
  if (!raw || raw === 'UNKNOWN') {
    return false
  }
  if (/^(BUTTON|A|SPAN|DIV|INPUT|I|SVG)$/i.test(raw)) {
    return false
  }
  return true
}

/** 批次主触发操作应忽略的确认类按钮（保留在链路中，但不作为主 trigger） */
const BATCH_TRIGGER_SKIP = /^(确定|取消|关闭)$/

/**
 * 从一批 events 解析主触发操作：优先首个有意义且非确认类的 click，其次 api trigger。
 *
 * @param {Record<string, unknown>[]} events
 * @returns {{ raw: string, label: string }}
 */
export function resolveBatchTriggerAction(events) {
  if (!Array.isArray(events) || events.length === 0) {
    return { raw: '', label: '' }
  }
  /** @type {{ raw: string, label: string } | null} */
  let fallbackClick = null
  for (const ev of events) {
    if (ev.type !== 'click') {
      continue
    }
    const target = ev.target
    if (target == null || !isMeaningfulClickTarget(String(target).trim())) {
      continue
    }
    const raw = String(target).trim()
    const label = formatTrackLabel(raw)
    if (!BATCH_TRIGGER_SKIP.test(label)) {
      return { raw, label }
    }
    if (!fallbackClick) {
      fallbackClick = { raw, label }
    }
  }
  if (fallbackClick) {
    return fallbackClick
  }
  for (const ev of events) {
    if (ev.type === 'route_enter') {
      const title = ev.title != null ? String(ev.title).trim() : ''
      const path = ev.path != null ? String(ev.path).trim() : ''
      const raw = title || path
      if (raw) {
        const label = title ? `访问:${title}` : `访问:${path}`
        return { raw: label, label: formatTrackLabel(label) || label }
      }
    }
  }
  for (const ev of events) {
    const trigger = ev.trigger
    if (trigger != null && String(trigger).trim()) {
      const raw = String(trigger).trim()
      return { raw, label: formatTrackLabel(raw) }
    }
  }
  return { raw: '', label: '' }
}

export default {
  isHumanReadableLabel,
  extractVisibleActionLabel,
  formatTrackLabel,
  resolveDisplayTrigger,
  resolveBatchTriggerAction
}
