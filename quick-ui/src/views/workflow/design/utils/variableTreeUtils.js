/**
 * 在变量树中按 insert 表达式查找变量项。
 * @param {Array} tree 变量树
 * @param {string} insert 如 {{start_1.input}}
 * @returns {{ group: object, item: object }|null}
 */
export function findVariableInTree(tree, insert) {
  if (!insert || !tree?.length) return null
  const normalized = insert.trim()
  for (const group of tree) {
    for (const item of group.children || []) {
      if (item.insert === normalized) {
        return { group, item }
      }
    }
  }
  return null
}

/**
 * 扁平化变量树为可搜索列表。
 * @param {Array} tree
 * @returns {Array<{ group: object, item: object }>}
 */
export function flattenVariableTree(tree) {
  const rows = []
  ;(tree || []).forEach((group) => {
    ;(group.children || []).forEach((item) => {
      if (item.insert) rows.push({ group, item })
    })
  })
  return rows
}

/**
 * 格式化变量类型标签。
 * @param {string} [type]
 * @returns {string}
 */
export function formatVariableType(type) {
  const map = {
    string: 'String',
    integer: 'Integer',
    number: 'Number',
    boolean: 'Boolean',
    time: 'Time',
    object: 'Object',
    array: 'Array',
    file: 'File',
    any: 'Any'
  }
  const key = String(type || '').toLowerCase()
  return map[key] || 'String'
}
