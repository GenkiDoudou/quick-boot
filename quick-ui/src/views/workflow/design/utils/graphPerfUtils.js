/**
 * 工作流画布性能相关工具：生成忽略坐标的结构指纹，避免拖拽时触发全量重算。
 */

/**
 * 剔除不影响变量解析的运行时/UI 字段。
 * @param {Record<string, any>|undefined|null} data
 * @returns {string}
 */
function stableDataSnippet(data) {
  if (!data) return ''
  const copy = { ...data }
  delete copy._icon
  delete copy._kbNameMap
  delete copy.validationError
  delete copy.runStatus
  try {
    return JSON.stringify(copy)
  } catch {
    return ''
  }
}

/**
 * 构建图结构指纹（不含节点 position，用于变量树缓存与自动保存防抖）。
 * @param {Array} nodes Vue Flow 节点
 * @param {Array} edges Vue Flow 边
 * @param {string} [currentNodeId] 当前选中节点
 * @returns {string}
 */
export function buildGraphStructureFingerprint(nodes, edges, currentNodeId = '') {
  const nodeParts = (nodes || [])
    .map((n) => {
      const d = n.data || {}
      const parent = n.parentNode || d.parentId || ''
      return `${n.id}|${d.wfType || ''}|${parent}|${stableDataSnippet(d)}`
    })
    .sort()
    .join(';')

  const edgeParts = (edges || [])
    .map((e) => `${e.source}|${e.sourceHandle || ''}|${e.target}|${e.targetHandle || ''}`)
    .sort()
    .join(';')

  return `${currentNodeId || ''}::${nodeParts}::${edgeParts}`
}

/**
 * 轻量节点快照，供 Loop/Batch 等表单读取子节点结构（不含 position）。
 * @param {Array} nodes
 * @returns {Array<{ id: string, parentNode: string|null, data: object }>}
 */
export function buildCanvasNodeSummaries(nodes) {
  return (nodes || []).map((n) => ({
    id: n.id,
    parentNode: n.parentNode || n.data?.parentId || null,
    data: n.data ? { ...n.data } : {}
  }))
}
