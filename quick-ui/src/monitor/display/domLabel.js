/**
 * 从 DOM 节点提取可见操作名（采集 click 与管理端展示共用）。
 */
import { normalizeWhitespace } from './labelFormat'

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
