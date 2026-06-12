import { deepParseJsonStringValues } from '../components/forms/startFieldTypes'

/**
 * 从运行结果 outputs 中提取用于展示的键值。
 * 文本模式（仅 text/output）与变量模式（结构化 JSON）分开处理。
 * @param {Record<string, unknown>} outputs
 * @returns {Record<string, unknown>}
 */
export function pickRunDisplayOutputs(outputs) {
  if (!outputs || typeof outputs !== 'object') return {}

  const keys = Object.keys(outputs).filter(
    (k) => outputs[k] !== undefined && outputs[k] !== null && outputs[k] !== ''
  )
  if (!keys.length) return {}

  const structuredKeys = keys.filter((k) => k !== 'text' && k !== 'output' && k !== 'citations')

  if (structuredKeys.length > 0) {
    return structuredKeys.reduce((acc, k) => {
      acc[k] = outputs[k]
      return acc
    }, {})
  }

  if (keys.includes('text')) {
    return { text: outputs.text }
  }
  if (keys.includes('output')) {
    return { output: outputs.output }
  }
  return { [keys[0]]: outputs[keys[0]] }
}

/**
 * 展示前规范化 outputs 值（递归解析嵌套 JSON 字符串）。
 * @param {Record<string, unknown>} outputs
 * @returns {Record<string, unknown>}
 */
function normalizeDisplayOutputs(outputs) {
  const picked = pickRunDisplayOutputs(outputs)
  const normalized = {}
  Object.entries(picked).forEach(([k, v]) => {
    normalized[k] = deepParseJsonStringValues(v)
  })
  return normalized
}

/**
 * 将运行 outputs 格式化为运行面板展示文本。
 * 文本模式：直接展示一段话；变量模式：格式化为 JSON。
 * @param {Record<string, unknown>} outputs
 * @returns {string}
 */
export function formatRunDisplayOutputs(outputs) {
  const picked = normalizeDisplayOutputs(outputs)
  const keys = Object.keys(picked)
  if (!keys.length) return ''

  const isTextOnly =
    keys.length === 1 && (keys[0] === 'text' || keys[0] === 'output')

  if (isTextOnly) {
    return String(picked[keys[0]] ?? '')
  }

  try {
    return JSON.stringify(picked, null, 2)
  } catch {
    return keys.map((k) => `${k}: ${picked[k]}`).join('\n')
  }
}
