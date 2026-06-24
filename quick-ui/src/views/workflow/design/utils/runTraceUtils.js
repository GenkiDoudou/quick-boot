/**
 * 从步骤 inputs 中提取 Trace 元数据（含 token 用量）。
 * @param {object|null|undefined} step
 * @returns {Record<string, unknown>}
 */
export function extractStepMeta(step) {
  return step?.inputs?._meta || {}
}

/**
 * @param {object|null|undefined} step
 * @returns {Record<string, number>|null}
 */
export function extractTokenUsage(step) {
  const usage = extractStepMeta(step)?.tokenUsage
  return usage && typeof usage === 'object' ? usage : null
}

/**
 * @param {Record<string, number>|null|undefined} usage
 * @returns {number}
 */
export function resolveTotalTokens(usage) {
  if (!usage) return 0
  if (usage.totalTokens != null) return Number(usage.totalTokens) || 0
  return (Number(usage.promptTokens) || 0) + (Number(usage.completionTokens) || 0)
}

/**
 * @param {Record<string, number>|null|undefined} usage
 * @returns {string}
 */
export function formatTokenUsage(usage) {
  const total = resolveTotalTokens(usage)
  if (!total) return ''
  return `${total.toLocaleString()} tokens`
}

/**
 * @param {Record<string, number>|null|undefined} usage
 * @returns {string}
 */
export function formatTokenUsageDetail(usage) {
  if (!usage) return '—'
  const prompt = usage.promptTokens
  const completion = usage.completionTokens
  const total = resolveTotalTokens(usage)
  const parts = []
  if (prompt != null) parts.push(`输入 ${prompt}`)
  if (completion != null) parts.push(`输出 ${completion}`)
  if (total) parts.push(`合计 ${total}`)
  return parts.length ? parts.join(' · ') : '—'
}

/**
 * 格式化为秒（节点运行标记用）。
 * @param {number|null|undefined} ms
 * @returns {string}
 */
export function formatDurationSec(ms) {
  if (ms == null || ms === '') return ''
  const n = Number(ms)
  if (Number.isNaN(n)) return ''
  if (n < 1000) return `${(n / 1000).toFixed(2)} s`
  return `${(n / 1000).toFixed(2)} s`
}

/**
 * @param {number|null|undefined} ms
 * @returns {string}
 */
export function formatDurationMs(ms) {
  if (ms == null || ms === '') return '—'
  const n = Number(ms)
  if (Number.isNaN(n)) return '—'
  if (n < 1000) return `${n} ms`
  if (n < 60_000) return `${(n / 1000).toFixed(2)} s`
  const min = Math.floor(n / 60_000)
  const sec = ((n % 60_000) / 1000).toFixed(1)
  return `${min}m ${sec}s`
}

/**
 * 汇总运行统计（Dify/Coze 风格顶栏）。
 * @param {Array} steps
 * @param {{ durationMs?: number, status?: string }} [runInfo]
 */
export function computeRunStats(steps, runInfo = {}) {
  const list = steps || []
  let totalDurationMs = 0
  let totalTokens = 0
  let success = 0
  let failed = 0
  let running = 0
  for (const step of list) {
    if (step.durationMs != null) totalDurationMs += Number(step.durationMs) || 0
    totalTokens += resolveTotalTokens(extractTokenUsage(step))
    if (step.status === 'FAILED') failed += 1
    else if (step.status === 'SUCCESS') success += 1
    else if (step.status === 'RUNNING') running += 1
  }
  let finalStatus = runInfo.status || null
  if (!finalStatus) {
    if (running > 0) finalStatus = 'RUNNING'
    else if (failed > 0) finalStatus = 'FAILED'
    else if (list.length > 0) finalStatus = 'SUCCESS'
  }
  const durationMs = runInfo.durationMs != null ? runInfo.durationMs : totalDurationMs
  return {
    stepCount: list.length,
    success,
    failed,
    running,
    totalDurationMs: durationMs,
    totalTokens,
    finalStatus
  }
}

/** MCP 相关输出字段（在「输出」Tab 中剥离，改由「MCP」Tab 展示）。 */
const MCP_OUTPUT_KEYS = ['mcpToolsUsed', 'mcpToolResults']

/**
 * 去除 Trace 内部字段后展示。
 * @param {Record<string, unknown>|null|undefined} data
 */
export function stripInternalTraceFields(data) {
  if (data == null || typeof data !== 'object') return data
  const out = { ...data }
  delete out._meta
  delete out._loop
  delete out._batch
  return out
}

/**
 * 去除 MCP 专用输出字段（避免与 MCP Tab 重复展示）。
 * @param {Record<string, unknown>|null|undefined} data
 */
export function stripMcpOutputFields(data) {
  if (data == null || typeof data !== 'object') return data
  const out = { ...data }
  for (const key of MCP_OUTPUT_KEYS) {
    delete out[key]
  }
  return out
}

/**
 * 从步骤 outputs 或 inputs._meta 提取 MCP 工具调用记录。
 * @param {object|null|undefined} step
 * @returns {Array<{ toolName: string, input: unknown, output: unknown }>}
 */
export function extractMcpToolResults(step) {
  const raw = step?.outputs?.mcpToolResults
  if (Array.isArray(raw) && raw.length) {
    return raw
      .filter((item) => item && typeof item === 'object')
      .map((item) => ({
        toolName: String(item.toolName ?? item.name ?? '未知工具'),
        input: item.input,
        output: item.output
      }))
  }
  return []
}

/**
 * Trace 元数据中已挂载的 MCP 工具名列表。
 * @param {object|null|undefined} step
 * @returns {string[]}
 */
export function extractMcpAvailableTools(step) {
  const tools = extractStepMeta(step)?.mcpAvailableTools
  return Array.isArray(tools) ? tools.map(String) : []
}

/**
 * 实际调用的 MCP 工具名（优先 outputs，其次 _meta）。
 * @param {object|null|undefined} step
 * @returns {string[]}
 */
export function extractMcpToolsUsedNames(step) {
  const fromOutputs = step?.outputs?.mcpToolsUsed
  if (Array.isArray(fromOutputs) && fromOutputs.length) {
    return fromOutputs.map(String)
  }
  const fromMeta = extractStepMeta(step)?.mcpToolsUsed
  if (Array.isArray(fromMeta)) return fromMeta.map(String)
  return extractMcpToolResults(step).map((c) => c.toolName)
}

/**
 * 步骤是否涉及 MCP（已启用或存在调用记录）。
 * @param {object|null|undefined} step
 */
export function hasMcpTrace(step) {
  if (extractStepMeta(step)?.mcpEnabled) return true
  if (extractMcpAvailableTools(step).length) return true
  const used = step?.outputs?.mcpToolsUsed
  if (Array.isArray(used) && used.length) return true
  return extractMcpToolResults(step).length > 0
}

/**
 * MCP 调用次数摘要标签。
 * @param {object|null|undefined} step
 * @returns {string}
 */
export function formatMcpCallBadge(step) {
  const calls = extractMcpToolResults(step)
  if (calls.length) return `MCP×${calls.length}`
  if (extractStepMeta(step)?.mcpEnabled) return 'MCP'
  return ''
}

/**
 * 格式化 MCP 工具入参/返回值为展示文本。
 * @param {unknown} value
 * @returns {string}
 */
export function formatMcpPayload(value) {
  if (value == null || value === '') return '—'
  if (typeof value === 'string') return value
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}

/**
 * 格式化单步 Trace 的 outputs 为展示文本（剥离 MCP 专用字段）。
 * @param {Record<string, unknown>|null|undefined} data
 * @returns {string}
 */
export function formatStepOutputs(data) {
  return formatStepIo(stripMcpOutputFields(stripInternalTraceFields(data)))
}

/**
 * 格式化单步 Trace 的 inputs/outputs 为展示文本。
 * @param {Record<string, unknown>|null|undefined} data
 * @returns {string}
 */
export function formatStepIo(data) {
  const cleaned = stripInternalTraceFields(data)
  if (cleaned == null) return '—'
  if (typeof cleaned !== 'object') return String(cleaned)
  const keys = Object.keys(cleaned).filter(
    (k) => cleaned[k] !== undefined && cleaned[k] !== null && cleaned[k] !== ''
  )
  if (!keys.length) return '—'
  if (keys.length === 1 && (keys[0] === 'text' || keys[0] === 'output')) {
    return String(cleaned[keys[0]] ?? '')
  }
  try {
    return JSON.stringify(cleaned, null, 2)
  } catch {
    return String(cleaned)
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

/**
 * 运行状态中文标签。
 * @param {string|null|undefined} status
 */
export function formatRunStatusLabel(status) {
  if (status === 'FAILED') return '失败'
  if (status === 'SUCCESS') return '成功'
  if (status === 'RUNNING') return '运行中'
  if (status === 'QUEUED') return '排队中'
  return status || '—'
}

/**
 * 运行状态 tag 类型。
 * @param {string|null|undefined} status
 */
export function runStatusTagType(status) {
  if (status === 'FAILED') return 'danger'
  if (status === 'SUCCESS') return 'success'
  if (status === 'RUNNING') return 'warning'
  return 'info'
}
