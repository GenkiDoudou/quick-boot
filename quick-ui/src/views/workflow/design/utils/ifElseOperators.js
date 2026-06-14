import { findVariableInTree } from './variableTreeUtils'

/** @type {Record<string, Array<{ value: string, label: string }>>} */
export const IF_ELSE_OPERATORS_BY_CATEGORY = {
  string: [
    { value: 'eq', label: '等于' },
    { value: 'ne', label: '不等于' },
    { value: 'length_gt', label: '长度大于' },
    { value: 'length_eq', label: '长度等于' },
    { value: 'length_lt', label: '长度小于' },
    { value: 'length_lte', label: '长度小于等于' },
    { value: 'contains', label: '包含' },
    { value: 'not_contains', label: '不包含' },
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' }
  ],
  number: [
    { value: 'eq', label: '等于' },
    { value: 'ne', label: '不等于' },
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' },
    { value: 'gt', label: '大于' },
    { value: 'gte', label: '大于等于' },
    { value: 'lt', label: '小于' },
    { value: 'lte', label: '小于等于' }
  ],
  object: [
    { value: 'has_key', label: '包含键名' },
    { value: 'not_has_key', label: '不包含键名' },
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' }
  ],
  array: [
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' },
    { value: 'length_gt', label: '长度大于' },
    { value: 'length_eq', label: '长度等于' },
    { value: 'length_lt', label: '长度小于' },
    { value: 'length_lte', label: '长度小于等于' },
    { value: 'contains', label: '包含' },
    { value: 'not_contains', label: '不包含' }
  ],
  boolean: [
    { value: 'eq', label: '等于' },
    { value: 'ne', label: '不等于' },
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' }
  ],
  time: [
    { value: 'eq', label: '等于' },
    { value: 'ne', label: '不等于' },
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' },
    { value: 'gt', label: '大于' },
    { value: 'gte', label: '大于等于' },
    { value: 'lt', label: '小于' },
    { value: 'lte', label: '小于等于' }
  ],
  file: [
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' }
  ],
  any: [
    { value: 'eq', label: '等于' },
    { value: 'ne', label: '不等于' },
    { value: 'empty', label: '为空' },
    { value: 'not_empty', label: '不为空' },
    { value: 'contains', label: '包含' },
    { value: 'not_contains', label: '不包含' }
  ]
}

const ALL_OPERATORS = Object.values(IF_ELSE_OPERATORS_BY_CATEGORY).flat()

/**
 * @param {string|undefined} type
 * @returns {string}
 */
export function resolveVariableTypeCategory(type) {
  const t = String(type || 'string').toLowerCase()
  if (t === 'integer' || t === 'number') return 'number'
  if (IF_ELSE_OPERATORS_BY_CATEGORY[t]) return t
  return 'string'
}

/**
 * @param {string|undefined} type
 */
export function getOperatorsForVariableType(type) {
  const cat = resolveVariableTypeCategory(type)
  return IF_ELSE_OPERATORS_BY_CATEGORY[cat] || IF_ELSE_OPERATORS_BY_CATEGORY.string
}

/**
 * @param {string|undefined} op
 */
export function ifElseOperatorNeedsRight(op) {
  return !['empty', 'not_empty'].includes(op)
}

/**
 * @param {string|undefined} op
 */
export function getIfElseOperatorSymbol(op) {
  const map = {
    eq: '=',
    ne: '≠',
    gt: '>',
    gte: '≥',
    lt: '<',
    lte: '≤',
    contains: '包含',
    not_contains: '不包含',
    empty: '为空',
    not_empty: '不为空',
    length_gt: '长度>',
    length_eq: '长度=',
    length_lt: '长度<',
    length_lte: '长度≤',
    has_key: '含键',
    not_has_key: '不含键'
  }
  return map[op] || getIfElseOperatorLabel(op)
}

/**
 * @param {string|undefined} op
 */
export function getIfElseOperatorLabel(op) {
  return ALL_OPERATORS.find((item) => item.value === op)?.label || op || ''
}

/**
 * @param {string|undefined} currentOp
 * @param {string|undefined} variableType
 */
export function ensureIfElseOperator(currentOp, variableType) {
  const ops = getOperatorsForVariableType(variableType)
  if (ops.some((item) => item.value === currentOp)) return currentOp
  return ops[0]?.value || 'not_empty'
}

/**
 * @param {string|undefined} type
 */
export function ifElseValueTypePrefix(type) {
  const cat = resolveVariableTypeCategory(type)
  const map = {
    string: 'str.',
    number: 'num.',
    object: 'obj.',
    array: 'arr.',
    boolean: 'bool.',
    time: 'time.',
    file: 'file.',
    any: 'any.'
  }
  return map[cat] || 'str.'
}

/**
 * 从条件操作数（字符串或 {{变量}}）推断类型，用于运算符列表。
 * @param {string|undefined} value
 * @param {Array} variableTree
 */
export function resolveConditionOperandType(value, variableTree) {
  const v = (value || '').trim()
  if (!v) return 'string'

  const exact = findVariableInTree(variableTree, v)
  if (exact?.item?.type) return exact.item.type

  const match = v.match(/\{\{([^}]+)\}\}/)
  if (match) {
    const found = findVariableInTree(variableTree, `{{${match[1]}}}`)
    if (found?.item?.type) return found.item.type
  }

  if (/^-?\d+(\.\d+)?$/.test(v)) return 'number'
  if (v === 'true' || v === 'false') return 'boolean'
  return 'string'
}

/**
 * @param {string} leftInsert
 * @param {Array} variableTree
 */
export function resolveVariableTypeFromTree(leftInsert, variableTree) {
  return resolveConditionOperandType(leftInsert, variableTree)
}

/**
 * 画布/预览：展示操作数（变量友好名或原文字符串）。
 * @param {string|undefined} raw
 * @param {Array} [variableTree]
 */
export function formatConditionOperandPreview(raw, variableTree = []) {
  const v = (raw || '').trim()
  if (!v) return '…'
  const exact = findVariableInTree(variableTree, v)
  if (exact) return formatVariableDisplayLabel(v, variableTree)
  if (/^\{\{[^}]+\}\}$/.test(v)) {
    return v.replace(/^\{\{|\}\}$/g, '').replace('.', ' - ')
  }
  return v.length > 24 ? `${v.slice(0, 24)}…` : v
}

/**
 * @param {string} leftInsert
 * @param {Array} variableTree
 */
export function formatVariableDisplayLabel(leftInsert, variableTree) {
  const found = findVariableInTree(variableTree, leftInsert)
  if (found) {
    const nodeLabel = found.group?.label || ''
    const varLabel = found.item?.label || found.item?.key || ''
    return nodeLabel && varLabel ? `${nodeLabel} - ${varLabel}` : varLabel || leftInsert
  }
  return (leftInsert || '').replace(/^\{\{|\}\}$/g, '').replace('.', ' - ') || '未选择变量'
}

/**
 * @param {object} cond
 * @param {Array} [variableTree]
 */
export function formatIfElseConditionPreview(cond, variableTree = []) {
  if (!cond?.left?.trim()) return '未配置条件'
  const leftLabel = formatConditionOperandPreview(cond.left, variableTree)
  const opLabel = getIfElseOperatorSymbol(cond.operator)
  if (!ifElseOperatorNeedsRight(cond.operator)) {
    return `${leftLabel} ${opLabel}`
  }
  const rightLabel = formatConditionOperandPreview(cond.right, variableTree)
  return `${leftLabel} ${opLabel} ${rightLabel}`
}

/**
 * @param {object} branch
 * @param {Array} [variableTree]
 */
export function formatIfElseBranchPreview(branch, variableTree = []) {
  const conds = branch?.conditions || []
  const parts = conds.filter((c) => c?.left).map((c) => formatIfElseConditionPreview(c, variableTree))
  if (!parts.length) return '未配置'
  const joiner = branch?.logic === 'OR' ? ' 或 ' : ' 且 '
  return parts.join(joiner)
}

/**
 * @param {string|undefined} op
 * @param {string|undefined} variableType
 */
export function ifElseRightValuePlaceholder(op, variableType) {
  if (op === 'has_key' || op === 'not_has_key') return '键名'
  const cat = resolveVariableTypeCategory(variableType)
  if (cat === 'boolean') return 'true / false'
  if (cat === 'number') return '数字'
  return '输入或引用参数值'
}
