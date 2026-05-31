/**
 * 行为监控展示层工具：标签格式化、批次 trigger 解析、DOM 文案提取。
 * 管理端页面应优先从此模块导入；采集层内部也可复用。
 */
export {
  formatTrackLabel,
  isHumanReadableLabel,
  isTechnicalSlug,
  normalizeWhitespace
} from './labelFormat'
export { extractVisibleActionLabel } from './domLabel'
export { resolveDisplayTrigger } from './resolveTrigger'
export { resolveBatchTriggerAction } from './batchTrigger'

import { formatTrackLabel, isHumanReadableLabel } from './labelFormat'
import { extractVisibleActionLabel } from './domLabel'
import { resolveDisplayTrigger } from './resolveTrigger'
import { resolveBatchTriggerAction } from './batchTrigger'

export default {
  formatTrackLabel,
  isHumanReadableLabel,
  extractVisibleActionLabel,
  resolveDisplayTrigger,
  resolveBatchTriggerAction
}
