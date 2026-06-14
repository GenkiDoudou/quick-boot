import { findVariableInTree, formatVariableType } from './variableTreeUtils'

export const AGGREGATOR_STRATEGY_FIRST_NON_EMPTY = 'first_non_empty'

/**
 * @param {string|undefined} insert
 * @param {Array} variableTree
 */
export function resolveAggregatorVariableType(insert, variableTree) {
  return findVariableInTree(variableTree, insert)?.item?.type || 'string'
}

/**
 * @param {Array} variables
 * @param {Array} variableTree
 */
export function inferGroupType(variables, variableTree) {
  for (const v of variables || []) {
    const t = resolveAggregatorVariableType(v, variableTree)
    if (v?.trim()) return t
  }
  return 'string'
}

/**
 * @param {object|undefined} data
 * @param {(prefix?: string) => string} idFn
 * @param {Array|undefined} prevGroups
 */
export function normalizeAggregatorGroups(data, idFn, prevGroups) {
  if (Array.isArray(data?.groups) && data.groups.length) {
    return data.groups.map((group, gIdx) => {
      const prev = prevGroups?.[gIdx]
      const vars = Array.isArray(group?.variables) ? group.variables : []
      return {
        _id: prev?._id || idFn('agg_group'),
        id: group?.id || `group_${gIdx + 1}`,
        name: (group?.name || '').trim() || `Group${gIdx + 1}`,
        strategy: group?.strategy || AGGREGATOR_STRATEGY_FIRST_NON_EMPTY,
        variables: vars.length
          ? vars.map((v, vIdx) => ({
              value: typeof v === 'string' ? v : v?.value || '',
              _id: prev?.variables?.[vIdx]?._id || idFn('agg_var')
            }))
          : [{ value: '', _id: idFn('agg_var') }]
      }
    })
  }

  const legacy = Array.isArray(data?.variables) ? data.variables : []
  const prev = prevGroups?.[0]
  const values = legacy.map((item) => (typeof item === 'string' ? item : item?.value || ''))
  return [
    {
      _id: prev?._id || idFn('agg_group'),
      id: 'group_1',
      name: 'Group1',
      strategy: AGGREGATOR_STRATEGY_FIRST_NON_EMPTY,
      variables: values.length
        ? values.map((value, vIdx) => ({
            value,
            _id: prev?.variables?.[vIdx]?._id || idFn('agg_var')
          }))
        : [{ value: '', _id: idFn('agg_var') }]
    }
  ]
}

/**
 * @param {Array} groups UI 分组
 */
export function serializeAggregatorGroups(groups) {
  return (groups || []).map(({ _id, variables, ...group }, idx) => ({
    id: (group.id || '').trim() || `group_${idx + 1}`,
    name: (group.name || '').trim() || `Group${idx + 1}`,
    strategy: group.strategy || AGGREGATOR_STRATEGY_FIRST_NON_EMPTY,
    variables: (variables || [])
      .map((row) => (row?.value || '').trim())
      .filter(Boolean)
  }))
}

/**
 * 供 nodeMeta 解析输出字段。
 * @param {object|undefined} data
 */
export function resolveAggregatorOutputKeys(data) {
  const groups = serializeAggregatorGroups(normalizeAggregatorGroups(data, () => 'tmp'))
  return groups.map((g, idx) => g.name || `Group${idx + 1}`)
}

/**
 * @param {string} type
 */
export function aggregatorTypeLabel(type) {
  return formatVariableType(type)
}
