/**
 * 扁平列表转树形结构工具（自 ruoyi 拆出）。
 */

/**
 * 扁平数组转树形结构（DFS 挂载 children）。
 * @param {Array<Record<string, unknown>>} data 扁平节点列表
 * @param {string} [id='id'] 主键字段名
 * @param {string} [parentId='parentId'] 父键字段名
 * @param {string} [children='children'] 子节点字段名
 * @returns {Array} 根节点数组
 */
export function handleTree(data, id, parentId, children) {
  const config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    children: children || 'children'
  }

  const childrenMap = {}
  const nodeIds = {}
  const tree = []

  for (const d of data) {
    const pid = d[config.parentId]
    if (childrenMap[pid] == null) {
      childrenMap[pid] = []
    }
    childrenMap[pid].push(d)
    nodeIds[d[config.id]] = d
  }

  for (const d of data) {
    const pid = d[config.parentId]
    if (nodeIds[pid] == null) {
      tree.push(d)
    }
  }

  for (const t of tree) {
    dfs(t, childrenMap, config)
  }

  return tree
}

/**
 * handleTree 内部 DFS。
 * @param {Record<string, unknown>} node
 * @param {Record<string, Array>} childrenMap
 * @param {{ id: string, parentId: string, children: string }} config
 */
export function dfs(node, childrenMap, config) {
  if (childrenMap[node[config.id]] != null) {
    node[config.children] = childrenMap[node[config.id]]
    for (const child of node[config.children]) {
      dfs(child, childrenMap, config)
    }
  }
}
