/** 意图识别兜底出口 handle */
export const INTENT_FALLBACK_HANDLE = '0'

/** 单模式意图数量上限 */
export const INTENT_MAX_INTENTS = 50

export const INTENT_OUTPUT_VARIABLES = [
  { key: 'index', type: 'number', description: '命中意图序号（1..N），未命中为 0' },
  { key: 'reason', type: 'string', description: '分类依据说明' }
]

/** 默认输入参数名 */
export const INTENT_DEFAULT_INPUT_KEY = 'query'

/**
 * 将 description 按行拆为示例列表。
 * @param {string|undefined} text
 * @returns {string[]}
 */
function splitExamples(text) {
  if (!text || !String(text).trim()) return []
  const lines = String(text).split(/\r?\n/).map((l) => l.trim()).filter(Boolean)
  return lines.length ? lines : [String(text).trim()]
}

/**
 * 示例数组转 textarea 文本（一行一例）。
 * @param {string[]|undefined} examples
 * @returns {string}
 */
export function examplesToText(examples) {
  if (!Array.isArray(examples)) return ''
  return examples.map((e) => String(e).trim()).filter(Boolean).join('\n')
}

/**
 * textarea 文本转示例数组。
 * @param {string|undefined} text
 * @returns {string[]}
 */
export function textToExamples(text) {
  return splitExamples(text)
}

/**
 * 旧 classes 迁移为 intents。
 * @param {Array|undefined} classes
 * @returns {Array<{ name: string, examples: string[] }>}
 */
export function migrateClassesToIntents(classes) {
  if (!Array.isArray(classes)) return []
  return classes.map((cls) => ({
    name: (cls?.name || '').trim(),
    examples: splitExamples(cls?.description)
  }))
}

/**
 * 归一化输出变量定义（兼容旧 classificationId）。
 * @param {Array|undefined} outputVariables
 * @returns {Array}
 */
function normalizeOutputVariables(outputVariables) {
  if (!Array.isArray(outputVariables) || !outputVariables.length) {
    return INTENT_OUTPUT_VARIABLES.map((item) => ({ ...item }))
  }
  return outputVariables.map((item) => {
    const key = item?.key === 'classificationId' ? 'index' : (item?.key || '')
    const fallback = INTENT_OUTPUT_VARIABLES.find((v) => v.key === key)
    return {
      key,
      type: item?.type || fallback?.type || 'string',
      description: item?.description || fallback?.description || ''
    }
  })
}

/**
 * 归一化意图识别节点 data（classes → intents）。
 * @param {object|undefined} data
 * @returns {object}
 */
export function normalizeIntentNodeData(data) {
  const next = { ...(data || {}) }
  if (Array.isArray(next.intents) && next.intents.length) {
    next.intents = next.intents.map((item) => ({
      name: (item?.name || '').trim(),
      examples: Array.isArray(item?.examples)
        ? item.examples.map((e) => String(e).trim()).filter(Boolean)
        : []
    }))
  } else if (Array.isArray(next.classes)) {
    next.intents = migrateClassesToIntents(next.classes)
    delete next.classes
  } else if (!Array.isArray(next.intents)) {
    next.intents = []
  }
  if (next.systemPrompt == null) next.systemPrompt = ''
  if (next.chatModelId === undefined) {
    next.chatModelId = next.modelId ?? null
  }
  delete next.mode
  delete next.modelId
  next.outputVariables = normalizeOutputVariables(next.outputVariables)
  if (!Array.isArray(next.inputVariables) || !next.inputVariables.length) {
    const legacyQuery = (next.query || '').trim()
    next.inputVariables = [{
      key: INTENT_DEFAULT_INPUT_KEY,
      value: legacyQuery || '{{start_1.question}}'
    }]
  }
  const primaryKey = (next.inputVariables[0]?.key || INTENT_DEFAULT_INPUT_KEY).trim()
  if (primaryKey) {
    next.query = `{{${primaryKey}}}`
  }
  return next
}

/**
 * 解析画布意图行（含兜底「其他」）。
 * @param {object|undefined} data 节点 data
 * @returns {Array<{ id: string, label: string, isFallback?: boolean }>}
 */
export function resolveIntentCanvasRows(data) {
  const normalized = normalizeIntentNodeData(data)
  const intents = normalized.intents?.length
    ? normalized.intents
    : [{ name: '意图1', examples: [] }]
  const rows = intents.map((intent, idx) => ({
    id: String(idx + 1),
    label: intent.name || `意图${idx + 1}`
  }))
  rows.push({ id: INTENT_FALLBACK_HANDLE, label: '其他', isFallback: true })
  return rows
}

/**
 * 获取意图列表（归一化后）。
 * @param {object|undefined} data
 * @returns {Array<{ name: string, examples: string[] }>}
 */
export function resolveIntentList(data) {
  return normalizeIntentNodeData(data).intents || []
}

/**
 * 旧字符串 handle 映射为数字 handle。
 * @param {string|undefined} handle
 * @param {Array|undefined} legacyClasses
 * @returns {string|null}
 */
export function mapLegacyIntentHandle(handle, legacyClasses) {
  if (!handle) return null
  if (handle === INTENT_FALLBACK_HANDLE || handle === 'default') return INTENT_FALLBACK_HANDLE
  if (/^\d+$/.test(handle)) return handle
  if (Array.isArray(legacyClasses)) {
    const idx = legacyClasses.findIndex((c) => c?.id === handle)
    if (idx >= 0) return String(idx + 1)
  }
  return null
}

/**
 * 迁移图中意图识别节点的边 handle。
 * @param {Array} nodes graph nodes
 * @param {Array} edges graph edges
 * @returns {{ nodes: Array, edges: Array }}
 */
export function migrateIntentRecognitionGraph(nodes, edges) {
  const nodeMap = new Map((nodes || []).map((n) => [n.id, n]))
  const migratedEdges = (edges || []).map((edge) => {
    const source = nodeMap.get(edge.source)
    if (!source || source.type !== 'question-classifier') return edge
    const raw = source.data || {}
    const handle = edge.sourceHandle
    if (!handle || /^\d+$/.test(handle)) return edge
    const mapped = mapLegacyIntentHandle(handle, raw.classes)
    if (!mapped || mapped === handle) return edge
    return { ...edge, sourceHandle: mapped }
  })
  const migratedNodes = (nodes || []).map((node) => {
    if (node.type !== 'question-classifier') return node
    const data = normalizeIntentNodeData(node.data)
    return { ...node, data }
  })
  return { nodes: migratedNodes, edges: migratedEdges }
}

/**
 * 意图数量上限。
 * @returns {number}
 */
export function intentMaxCount() {
  return INTENT_MAX_INTENTS
}

/**
 * 表单行：intents → UI 行。
 */
export function intentFormToRow(item, idx, prevRows, idFn) {
  return {
    name: item?.name || '',
    examplesText: examplesToText(item?.examples),
    _id: prevRows?.[idx]?._id || idFn('intent')
  }
}

/**
 * UI 行 → intents 序列化。
 * @param {Array} rows
 * @returns {Array<{ name: string, examples: string[] }>}
 */
export function serializeIntentRows(rows) {
  return (rows || []).map(({ _id, examplesText, ...rest }) => ({
    name: (rest.name || '').trim(),
    examples: textToExamples(examplesText)
  }))
}
