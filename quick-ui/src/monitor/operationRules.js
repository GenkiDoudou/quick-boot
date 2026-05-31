/**
 * 操作类 click 分类规则（供 createUserMonitor 全局 click 使用）。
 */
import { isHumanReadableLabel, formatTrackLabel } from './display/labelFormat'

/**
 * @param {string} label
 * @returns {boolean}
 */
export function shouldRecordPendingTrigger(label) {
  if (!label || label === 'UNKNOWN') {
    return false
  }
  if (/^(BUTTON|A|SPAN|DIV|INPUT)$/i.test(label)) {
    return false
  }
  return isHumanReadableLabel(label) || label.length <= 40
}

/** 仅查询/工具类：记 click，不建批次 */
const QUERY_ONLY =
  /^(搜索|重置|刷新|查询|展开|折叠|展开\/折叠|保存排序|全部删除|清空)$/

/** 确认类：延长当前批次，不替换 trigger */
const PASSIVE_ACTION = /^(确定|取消|关闭)$/

/**
 * 是否为主操作 click（删除/修改/新增/导出等），应 openBatch。
 * @param {string} label
 * @returns {boolean}
 */
export function isPrimaryAction(label) {
  if (!shouldRecordPendingTrigger(label)) {
    return false
  }
  const text = formatTrackLabel(String(label).trim()) || String(label).trim()
  if (QUERY_ONLY.test(text)) {
    return false
  }
  if (PASSIVE_ACTION.test(text)) {
    return false
  }
  return true
}

/**
 * 是否为被动确认类 click（确定/取消/关闭）。
 * @param {string} label
 * @returns {boolean}
 */
export function isPassiveAction(label) {
  if (!shouldRecordPendingTrigger(label)) {
    return false
  }
  const text = formatTrackLabel(String(label).trim()) || String(label).trim()
  return PASSIVE_ACTION.test(text)
}

/**
 * 查询/工具类 click：只记事件，短 idle flush，不建批次。
 * @param {string} label
 * @returns {boolean}
 */
export function isQueryOnlyAction(label) {
  if (!shouldRecordPendingTrigger(label)) {
    return false
  }
  const text = formatTrackLabel(String(label).trim()) || String(label).trim()
  return QUERY_ONLY.test(text)
}

/** @deprecated 使用 isPrimaryAction */
export function shouldAutoBeginOperation(label) {
  return isPrimaryAction(label)
}

export default {
  shouldRecordPendingTrigger,
  isPrimaryAction,
  isPassiveAction,
  isQueryOnlyAction,
  shouldAutoBeginOperation
}
