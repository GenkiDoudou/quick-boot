/**
 * 格式化单步 Trace 的 inputs/outputs 为展示文本。
 * @param {Record<string, unknown>|null|undefined} data
 * @returns {string}
 */
export function formatStepIo(data) {
  if (data == null) return '—'
  if (typeof data !== 'object') return String(data)
  const keys = Object.keys(data).filter(
    (k) => data[k] !== undefined && data[k] !== null && data[k] !== ''
  )
  if (!keys.length) return '—'
  if (keys.length === 1 && (keys[0] === 'text' || keys[0] === 'output')) {
    return String(data[keys[0]] ?? '')
  }
  try {
    return JSON.stringify(data, null, 2)
  } catch {
    return String(data)
  }
}

/**
 * 生成 Trace 步骤唯一 key。
 * @param {object} step
 * @param {number} idx
 * @returns {string}
 */
export function traceStepKey(step, idx) {
  const loopIt = step?.inputs?._loop?.iteration
  const suffix = loopIt != null ? `_L${loopIt}` : ''
  return `${step?.nodeId || 'step'}_${step?.orderNo ?? idx}${suffix}_${idx}`
}

/**
 * 循环步骤轮次标签（从 inputs._loop 读取）。
 * @param {object|null|undefined} step
 * @returns {string}
 */
export function formatLoopIterationBadge(step) {
  const it = step?.inputs?._loop?.iteration
  if (it == null || it === '') return ''
  return `第 ${Number(it) + 1} 轮`
}

/**
 * 按节点 ID 与轮次筛选 Trace 步骤。
 * @param {Array} steps
 * @param {string} nodeId
 * @param {number|null} iteration
 * @returns {Array}
 */
export function filterStepsByLoopIteration(steps, nodeId, iteration) {
  return (steps || []).filter((s) => {
    if (s.nodeId !== nodeId) return false
    if (iteration == null) return true
    return Number(s.inputs?._loop?.iteration) === iteration
  })
}
