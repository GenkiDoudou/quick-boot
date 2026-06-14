/** 内置固定节点：创建工作流后默认存在，不可从节点库添加或删除。 */
export const FIXED_WORKFLOW_NODE_TYPES = ['start', 'end']

/**
 * @param {string|undefined} wfType
 */
export function isFixedWorkflowNodeType(wfType) {
  return FIXED_WORKFLOW_NODE_TYPES.includes(wfType)
}

/**
 * @param {object|null|undefined} node Vue Flow 节点
 */
export function isFixedWorkflowNode(node) {
  return isFixedWorkflowNodeType(node?.data?.wfType)
}

/**
 * 补全画布上缺失的开始/结束节点（兼容历史图）。
 * @param {Array} nodes
 * @param {(type: string, position: {x:number,y:number}, existing: Array) => object} createNode
 * @returns {Array}
 */
export function ensureFixedWorkflowNodes(nodes, createNode) {
  const list = [...(nodes || [])]
  if (!list.some((n) => n.data?.wfType === 'start')) {
    list.push(createNode('start', { x: 80, y: 280 }, list))
  }
  if (!list.some((n) => n.data?.wfType === 'end')) {
    list.push(createNode('end', { x: 720, y: 280 }, list))
  }
  return list
}

/**
 * @param {string|undefined} wfType
 * @returns {string}
 */
export function fixedNodeGuardMessage(wfType) {
  if (wfType === 'start') return '开始节点为内置节点，不可添加、复制或删除'
  if (wfType === 'end') return '结束节点为内置节点，不可添加、复制或删除'
  return '该节点不可操作'
}
