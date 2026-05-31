/**
 * 从点击 DOM 解析操作按钮与可见文案（供全局 click 监控使用）。
 */
import { extractVisibleActionLabel } from './trackLabel'

/**
 * @param {HTMLElement} node
 * @returns {boolean}
 */
export function isOperationTriggerElement(node) {
  if (!node || !node.tagName) {
    return false
  }
  const tag = node.tagName.toUpperCase()
  if (tag === 'BUTTON') {
    return true
  }
  if (tag === 'INPUT') {
    const type = (node.getAttribute('type') || '').toLowerCase()
    return type === 'submit' || type === 'button'
  }
  if (tag === 'A' && node.getAttribute('role') === 'button') {
    return true
  }
  if (node.classList && node.classList.contains('el-button')) {
    return true
  }
  if (node.classList && node.classList.contains('c7-button')) {
    return true
  }
  return false
}

/**
 * 从按钮子树提取纯文本（跳过 svg / el-icon，避免点到图标时 innerText 为空）。
 * @param {HTMLElement} node
 * @returns {string}
 */
function extractButtonTextContent(node) {
  const parts = []
  const walk = (el) => {
    if (!el || !el.childNodes) {
      return
    }
    for (const child of el.childNodes) {
      if (child.nodeType === 3) {
        const t = String(child.textContent || '').replace(/\s+/g, ' ').trim()
        if (t) {
          parts.push(t)
        }
      } else if (child.nodeType === 1) {
        const tag = child.tagName.toUpperCase()
        if (tag === 'SVG' || tag === 'I') {
          continue
        }
        if (child.classList && child.classList.contains('el-icon')) {
          continue
        }
        walk(child)
      }
    }
  }
  walk(node)
  return parts.join(' ').trim().slice(0, 40)
}

/**
 * @param {HTMLElement | null | undefined} el
 * @returns {{ label: string, isAction: boolean }}
 */
export function readClickTarget(el) {
  if (!el) {
    return { label: '', isAction: false }
  }
  let node = el
  let deep = 0
  while (node && deep < 8) {
    const track =
      node.dataset && node.dataset.track ? String(node.dataset.track).trim() : ''
    if (track) {
      return { label: track, isAction: true }
    }
    if (isOperationTriggerElement(node)) {
      const visible = extractVisibleActionLabel(node) || extractButtonTextContent(node)
      const label = visible || node.tagName
      return { label: String(label), isAction: true }
    }
    node = node.parentElement
    deep += 1
  }
  return { label: el.tagName || 'UNKNOWN', isAction: false }
}

export default {
  readClickTarget,
  isOperationTriggerElement
}
