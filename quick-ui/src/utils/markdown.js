import { marked } from 'marked'

marked.setOptions({
  gfm: true,
  breaks: true
})

/**
 * 将 Markdown 渲染为 HTML，供聊天气泡展示。
 *
 * @param {string} content 原始 Markdown 或纯文本
 * @returns {string} HTML 字符串
 */
export function renderMarkdownToHtml(content) {
  const text = String(content ?? '').trim()
  if (!text) return ''
  return marked.parse(text)
}

/**
 * 将 Markdown 转为便于阅读的纯文本（去掉常见标记符号）。
 *
 * @param {string} content 原始 Markdown 或纯文本
 * @returns {string} 纯文本
 */
export function markdownToPlainText(content) {
  const text = String(content ?? '')
  if (!text) return ''
  return text
    .replace(/```[\s\S]*?```/g, (block) => block.replace(/```\w*\n?/g, '').trim())
    .replace(/`([^`]+)`/g, '$1')
    .replace(/!\[([^\]]*)\]\([^)]+\)/g, '$1')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .replace(/^#{1,6}\s+/gm, '')
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/\*([^*]+)\*/g, '$1')
    .replace(/__([^_]+)__/g, '$1')
    .replace(/_([^_]+)_/g, '$1')
    .replace(/^\s*[-*+]\s+/gm, '• ')
    .replace(/^\s*\d+\.\s+/gm, '')
    .replace(/^>\s?/gm, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}
