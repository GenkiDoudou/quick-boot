import { formatTime } from '@/utils/formatTime'

/**
 * 统一展示操作时间（兼容 API 返回 ISO 或 yyyy-MM-dd HH:mm:ss）。
 * @param {string|number|Date|null|undefined} value
 * @returns {string}
 */
export function formatOperTime(value) {
  if (value == null || value === '') {
    return '—'
  }
  const formatted = formatTime(value, '{y}-{m}-{d} {h}:{i}:{s}')
  return formatted || String(value)
}
