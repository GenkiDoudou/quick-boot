/**
 * 触发操作可读名称：优先使用 DOM 按钮文案，仅对技术 slug 做启发式转换。
 * 采集层（批次 trigger）与管理端展示层共用。
 */

/**
 * @param {string} text
 * @returns {string}
 */
function normalizeWhitespace(text) {
  return String(text || '').replace(/\s+/g, ' ').trim()
}

/**
 * 是否为技术 slug（user-edit:1、c7-json-table-delete）。
 * @param {string} text
 * @returns {boolean}
 */
export function isTechnicalSlug(text) {
  return /^[a-z0-9]+([:_-][a-z0-9]+)+$/i.test(text) || text.startsWith('c7-json-table-')
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

export { normalizeWhitespace }
