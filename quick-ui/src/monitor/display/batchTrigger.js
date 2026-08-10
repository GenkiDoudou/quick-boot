/**
 * 从一批 events 解析主触发操作（flush 上报与管理端展示共用）。
 */
import { formatTrackLabel } from './labelFormat'

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
