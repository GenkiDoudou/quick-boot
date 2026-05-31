/**
 * 全局 click 采集：主/被动操作批次 + 查询类短延迟 flush。
 */
import { readClickTarget } from '../clickTarget'
import { isPrimaryAction, isPassiveAction, isQueryOnlyAction } from '../operationRules'
import { openBatch, touchBatchPassive } from '../batchSession'
import { ACTIONABLE_CLICK_SELECTOR } from '../constants'

/**
 * @param {{ isTracking: () => boolean, pushEvent: (row: Record<string, unknown>) => void, scheduleQueryFlush: () => void }} deps
 * @returns {() => void} dispose
 */
export function bindClickCollector(deps) {
  const { isTracking, pushEvent, scheduleQueryFlush } = deps

  const handler = (e) => {
    if (!isTracking()) {
      return
    }
    const rawTarget = e.target
    if (!(rawTarget instanceof Element)) {
      return
    }
    const actionable = rawTarget.closest(ACTIONABLE_CLICK_SELECTOR)
    if (!actionable || !(actionable instanceof HTMLElement)) {
      return
    }
    const { label, isAction } = readClickTarget(actionable)
    if (!isAction) {
      return
    }
    if (isPrimaryAction(label)) {
      openBatch(label)
    } else if (isPassiveAction(label)) {
      touchBatchPassive()
    }
    pushEvent({
      type: 'click',
      target: label,
      x: e.clientX,
      y: e.clientY
    })
    if (isQueryOnlyAction(label)) {
      scheduleQueryFlush()
    }
  }

  document.addEventListener('click', handler, { passive: true })
  return () => document.removeEventListener('click', handler)
}
