import { expandLoopDeletionIds } from './loopUtils'
import { expandBatchDeletionIds, resolveActiveBatchBodyId, validateBatchConnection } from './batchUtils'
import { resolveActiveLoopBodyId, validateLoopConnection } from './loopUtils'

/**
 * 解析当前选中节点所处的容器体（循环体 / 批处理体）。
 * @param {object|null|undefined} selectedNode
 * @param {Array} nodes
 * @returns {{ kind: 'loop'|'batch', bodyId: string }|null}
 */
export function resolveActiveContainerBody(selectedNode, nodes) {
  const loopBodyId = resolveActiveLoopBodyId(selectedNode, nodes)
  if (loopBodyId) return { kind: 'loop', bodyId: loopBodyId }
  const batchBodyId = resolveActiveBatchBodyId(selectedNode, nodes)
  if (batchBodyId) return { kind: 'batch', bodyId: batchBodyId }
  return null
}

/**
 * 容器体及其子节点不可单独复制。
 * @param {object|null|undefined} node
 * @returns {boolean}
 */
export function isContainerInternalCanvasNode(node) {
  const wfType = node?.data?.wfType
  if (wfType === 'loop-body' || wfType === 'batch-body') return true
  if (wfType === 'loop-body-start' || wfType === 'loop-body-end') return true
  return !!(node?.parentNode || node?.data?.parentId)
}

/**
 * 删除节点时合并 loop / batch 关联 ID。
 * @param {string} nodeId
 * @param {Array} nodes
 * @returns {Set<string>}
 */
export function expandContainerDeletionIds(nodeId, nodes) {
  const ids = new Set()
  expandLoopDeletionIds(nodeId, nodes).forEach((id) => ids.add(id))
  expandBatchDeletionIds(nodeId, nodes).forEach((id) => ids.add(id))
  return ids
}

/**
 * 校验循环 / 批处理容器相关连线。
 * @param {object} connection
 * @param {Array} nodes
 * @returns {string|null}
 */
export function validateContainerConnection(connection, nodes) {
  return validateLoopConnection(connection, nodes) || validateBatchConnection(connection, nodes)
}
