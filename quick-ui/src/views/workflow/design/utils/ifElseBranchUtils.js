import { formatIfElseBranchPreview } from './ifElseOperators'

export const IF_ELSE_ELSE_HANDLE = 'false'

/**
 * 规范化运算符（兼容历史值）。
 * @param {string|undefined} op
 * @returns {string}
 */
export function normalizeIfElseOperator(op) {
  if (op === 'not-empty') return 'not_empty'
  if (op === 'neq') return 'ne'
  return op || 'eq'
}

/**
 * @param {object} cond
 * @param {number} idx
 * @param {Array|undefined} prevConditions
 * @param {(prefix?: string) => string} idFn
 */
export function normalizeIfElseCondition(cond, idx, prevConditions, idFn) {
  return {
    left: cond?.left || '',
    operator: normalizeIfElseOperator(cond?.operator),
    right: cond?.right || '',
    _id: prevConditions?.[idx]?._id || idFn('cond')
  }
}

/**
 * 从节点 data 解析分支列表（兼容旧版 conditions + logic）。
 * @param {object|undefined} data
 * @param {(prefix?: string) => string} idFn
 * @param {Array|undefined} [prevBranches] 保留 UI _id
 * @returns {Array}
 */
export function normalizeIfElseBranches(data, idFn, prevBranches) {
  if (Array.isArray(data?.branches) && data.branches.length) {
    return data.branches.map((branch, bIdx) => {
      const prevBranch = prevBranches?.[bIdx]
      const conditions = Array.isArray(branch?.conditions) ? branch.conditions : []
      return {
        _id: prevBranch?._id || idFn('branch'),
        id: branch?.id || (bIdx === 0 ? 'true' : `elif_${bIdx + 1}`),
        name: branch?.name || (bIdx === 0 ? '如果' : `否则如果 ${bIdx}`),
        logic: branch?.logic === 'OR' ? 'OR' : 'AND',
        conditions: conditions.length
          ? conditions.map((c, cIdx) =>
              normalizeIfElseCondition(c, cIdx, prevBranch?.conditions, idFn)
            )
          : [createEmptyIfElseCondition(idFn)]
      }
    })
  }

  const legacyConditions = Array.isArray(data?.conditions) ? data.conditions : []
  const prevBranch = prevBranches?.[0]
  return [
    {
      _id: prevBranch?._id || idFn('branch'),
      id: 'true',
      name: '如果',
      logic: data?.logic === 'OR' ? 'OR' : 'AND',
      conditions: legacyConditions.length
        ? legacyConditions.map((c, cIdx) =>
            normalizeIfElseCondition(c, cIdx, prevBranch?.conditions, idFn)
          )
        : [createEmptyIfElseCondition(idFn)]
    }
  ]
}

/**
 * @param {(prefix?: string) => string} idFn
 */
export function createEmptyIfElseCondition(idFn) {
  return {
    _id: idFn('cond'),
    left: '',
    operator: 'eq',
    right: ''
  }
}

/**
 * @param {number} index
 * @param {(prefix?: string) => string} idFn
 */
export function createIfElseBranch(index, idFn) {
  return {
    _id: idFn('branch'),
    id: index === 0 ? 'true' : `elif_${index + 1}_${Date.now()}`,
    name: index === 0 ? '如果' : `否则如果 ${index}`,
    logic: 'AND',
    conditions: [createEmptyIfElseCondition(idFn)]
  }
}

/**
 * 序列化分支配置写回节点 data。
 * @param {Array} branches
 * @returns {Array}
 */
export function serializeIfElseBranches(branches) {
  return (branches || []).map(({ _id, conditions, ...branch }) => ({
    id: (branch.id || '').trim() || 'true',
    name: (branch.name || '').trim() || '如果',
    logic: branch.logic === 'OR' ? 'OR' : 'AND',
    conditions: (conditions || []).map(({ _id: _cid, ...cond }) => ({
      left: (cond.left || '').trim(),
      operator: normalizeIfElseOperator(cond.operator),
      right: cond.right || ''
    }))
  }))
}

/**
 * 画布节点：每个分支一行 + 否则行，各对应一个连线 handle。
 * @param {object|undefined} data
 * @returns {Array<{ id: string, label: string, preview: string }>}
 */
export function resolveIfElseCanvasRows(data) {
  const serialized = serializeIfElseBranches(normalizeIfElseBranches(data, () => 'tmp'))
  const rows = serialized.map((branch, idx) => ({
    id: branch.id || (idx === 0 ? 'true' : `elif_${idx + 1}`),
    label: idx === 0 ? '如果' : '否则如果',
    preview: formatIfElseBranchPreview(branch)
  }))
  rows.push({ id: IF_ELSE_ELSE_HANDLE, label: '否则', preview: '' })
  return rows
}

/**
 * 画布展示用分支 handle 列表。
 * @param {object|undefined} data
 * @returns {Array<{ id: string, name: string }>}
 */
export function resolveIfElseBranchHandles(data) {
  const branches = serializeIfElseBranches(normalizeIfElseBranches(data, () => 'tmp'))
  return branches.map((b, idx) => ({
    id: b.id || (idx === 0 ? 'true' : `elif_${idx + 1}`),
    name: b.name || (idx === 0 ? '如果' : `否则如果 ${idx}`)
  }))
}

/**
 * @param {object|undefined} data
 * @returns {boolean}
 */
export function hasIfElseValidationWarning(data) {
  const branches = serializeIfElseBranches(normalizeIfElseBranches(data, () => 'tmp'))
  if (!branches.length) return true
  return !branches.some(
    (b) =>
      Array.isArray(b.conditions) &&
      b.conditions.some((c) => c.left && c.operator)
  )
}
