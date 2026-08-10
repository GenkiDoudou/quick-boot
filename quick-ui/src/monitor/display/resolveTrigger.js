/**
 * 解析批次/操作的展示用 trigger 文案。
 */
import { formatTrackLabel, isHumanReadableLabel, isTechnicalSlug } from './labelFormat'

/**
 * 解析批次/操作的展示用 trigger 文案（优先点击按钮可见文案，而非 user-edit:123 类 slug）。
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
