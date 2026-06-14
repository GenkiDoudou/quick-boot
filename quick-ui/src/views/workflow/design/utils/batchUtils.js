import { createNodeId, createVueFlowNode } from '../graphConverter'
import { NODE_META_MAP } from '../nodeMeta'

/** 批处理体容器默认尺寸（px） */
export const BATCH_BODY_DEFAULT_SIZE = { width: 380, height: 260 }

/** 批处理节点：主流程出口（连下一节点） */
export const BATCH_HANDLE_FLOW_OUT = 'flow-out'
/** 批处理节点：进入批处理体（连 batch-body 顶部） */
export const BATCH_HANDLE_BODY = 'body'
/** 批处理体：接收批处理头连线（顶部） */
export const BATCH_BODY_HANDLE_IN = 'body-in'
/** 批处理体：左侧入口，连体内第一个节点 */
export const BATCH_BODY_HANDLE_ENTRY = 'body-entry'
/** 批处理体：右侧出口，接收体内最后节点 */
export const BATCH_BODY_HANDLE_EXIT = 'body-exit'

/**
 * @param {object} node
 * @returns {boolean}
 */
export function isBatchBodyBusinessChild(node) {
  const wfType = node?.data?.wfType
  if (!wfType || wfType === 'batch-body') return false
  return true
}

/**
 * 创建 batch → batch-body 结构连线。
 * @param {string} batchId
 * @param {string} bodyId
 * @returns {object}
 */
export function createBatchBodyEdge(batchId, bodyId) {
  return {
    id: `e_${batchId}_${bodyId}_body`,
    source: batchId,
    target: bodyId,
    sourceHandle: BATCH_HANDLE_BODY,
    targetHandle: BATCH_BODY_HANDLE_IN,
    type: 'workflow',
    animated: true,
    style: { stroke: '#94b8ff' }
  }
}

/**
 * 创建 batch + batch-body 节点对及结构连线。
 * @param {{ x: number, y: number }} position 批处理头节点位置
 * @param {Array} existingNodes 已有节点
 * @returns {{ nodes: Array<object>, edges: Array<object> }}
 */
export function createBatchNodePair(position, existingNodes) {
  const batchNode = createVueFlowNode('batch', position, existingNodes)
  const bodyId = createNodeId('batch_body', [...(existingNodes || []), batchNode])
  batchNode.data.bodyId = bodyId
  if (!Array.isArray(batchNode.data.inputParameters) || !batchNode.data.inputParameters.length) {
    batchNode.data.inputParameters = [{ key: '', source: '' }]
  }

  const bodyMeta = NODE_META_MAP['batch-body']
  const bodyNode = {
    id: bodyId,
    type: 'workflow',
    position: {
      x: Math.round(position.x - 40),
      y: Math.round(position.y + 72)
    },
    style: {
      width: `${BATCH_BODY_DEFAULT_SIZE.width}px`,
      height: `${BATCH_BODY_DEFAULT_SIZE.height}px`,
      zIndex: -1
    },
    data: {
      wfType: 'batch-body',
      label: bodyMeta?.label || '批处理体',
      _icon: bodyMeta?.icon,
      batchNodeId: batchNode.id,
      ...(JSON.parse(JSON.stringify(bodyMeta?.defaults || {})))
    }
  }

  return {
    nodes: [batchNode, bodyNode],
    edges: [createBatchBodyEdge(batchNode.id, bodyId)]
  }
}

/**
 * 补全 batch→body 结构连线，并同步 batchNodeId。
 * @param {Array} nodes
 * @param {Array} edges
 * @returns {{ nodes: Array, edges: Array }}
 */
export function ensureBatchGraphStructure(nodes, edges) {
  const nodeList = [...(nodes || [])]
  const edgeList = [...(edges || [])]

  nodeList
    .filter((n) => n.data?.wfType === 'batch' && n.data?.bodyId)
    .forEach((batchNode) => {
      const bodyId = batchNode.data.bodyId
      const bodyNode = nodeList.find((n) => n.id === bodyId)
      if (bodyNode?.data) {
        bodyNode.data.batchNodeId = batchNode.id
      }
      if (!nodeList.some((n) => n.id === bodyId)) return

      const hasBodyEdge = edgeList.some(
        (e) =>
          e.source === batchNode.id &&
          e.target === bodyId &&
          e.sourceHandle === BATCH_HANDLE_BODY
      )
      if (!hasBodyEdge) {
        edgeList.push(createBatchBodyEdge(batchNode.id, bodyId))
      }
    })

  return { nodes: nodeList, edges: edgeList }
}

/**
 * @param {object} connection
 * @param {Array} nodes
 * @returns {string|null} 错误文案，合法时 null
 */
export function validateBatchConnection(connection, nodes) {
  const { source, target, sourceHandle, targetHandle } = connection || {}
  const sourceNode = (nodes || []).find((n) => n.id === source)
  const targetNode = (nodes || []).find((n) => n.id === target)
  if (!sourceNode || !targetNode) return null

  const sourceType = sourceNode.data?.wfType
  const targetType = targetNode.data?.wfType

  if (sourceType === 'batch') {
    if (targetType === 'batch-body') {
      if (sourceHandle && sourceHandle !== BATCH_HANDLE_BODY) {
        return '请使用批处理节点底部的「批处理体」出口连接到批处理体'
      }
      return null
    }
    if (sourceHandle === BATCH_HANDLE_BODY) {
      return '「批处理体」出口只能连接到批处理体容器'
    }
    return null
  }

  if (sourceType === 'batch-body') {
    const childOk =
      (targetNode.parentNode === source || targetNode.data?.parentId === source) &&
      isBatchBodyBusinessChild(targetNode)
    if (!childOk) {
      return '批处理体左侧「开始」只能连接到批处理体内的业务节点'
    }
    if (sourceHandle && sourceHandle !== BATCH_BODY_HANDLE_ENTRY) {
      return '请从批处理体左侧「开始」连接到体内节点'
    }
    return null
  }

  if (targetType === 'batch-body') {
    if (targetHandle === BATCH_BODY_HANDLE_IN) {
      if (sourceType !== 'batch') {
        return '批处理体顶部入口仅接受批处理节点的「批处理体」连线'
      }
      return null
    }
    if (targetHandle === BATCH_BODY_HANDLE_EXIT) {
      const childOk =
        (sourceNode.parentNode === target || sourceNode.data?.parentId === target) &&
        isBatchBodyBusinessChild(sourceNode)
      if (!childOk) {
        return '仅批处理体内的业务节点可连接到批处理体右侧「结束」'
      }
      return null
    }
    return '请连接到批处理体左侧「开始」或右侧「结束」'
  }

  return null
}

/**
 * 收集批处理体容器内的子节点 ID（不含 batch-body 自身）。
 * @param {string} bodyId
 * @param {Array} nodes
 * @returns {Set<string>}
 */
export function collectBatchBodyChildIds(bodyId, nodes) {
  const ids = new Set()
  ;(nodes || []).forEach((n) => {
    if (n.parentNode === bodyId || n.data?.parentId === bodyId) {
      ids.add(n.id)
    }
  })
  return ids
}

/**
 * 删除 batch 相关节点时扩展 ID 集合（含 batch-body 与子节点）。
 * @param {string} nodeId
 * @param {Array} nodes
 * @returns {Set<string>}
 */
export function expandBatchDeletionIds(nodeId, nodes) {
  const ids = new Set([nodeId])
  const node = (nodes || []).find((n) => n.id === nodeId)
  if (!node) return ids

  const wfType = node.data?.wfType
  if (wfType === 'batch') {
    const bodyId = node.data?.bodyId
    if (bodyId) {
      ids.add(bodyId)
      collectBatchBodyChildIds(bodyId, nodes).forEach((id) => ids.add(id))
    }
  } else if (wfType === 'batch-body') {
    const batchId = node.data?.batchNodeId
    if (batchId) ids.add(batchId)
    collectBatchBodyChildIds(node.id, nodes).forEach((id) => ids.add(id))
  }
  return ids
}

/**
 * 解析当前选中节点对应的 batch-body 容器 ID。
 * @param {object|null|undefined} selectedNode
 * @param {Array} nodes
 * @returns {string|null}
 */
export function resolveActiveBatchBodyId(selectedNode, nodes) {
  if (!selectedNode) return null
  if (selectedNode.data?.wfType === 'batch-body') return selectedNode.id
  const parentId = selectedNode.parentNode || selectedNode.data?.parentId
  if (parentId) {
    const parent = (nodes || []).find((n) => n.id === parentId)
    if (parent?.data?.wfType === 'batch-body') return parent.id
  }
  return null
}

/**
 * 查找批处理体对应的 batch 节点。
 * @param {string} bodyId
 * @param {Array} nodes
 * @returns {object|null}
 */
export function findBatchNodeByBodyId(bodyId, nodes) {
  const body = (nodes || []).find((n) => n.id === bodyId)
  const batchId = body?.data?.batchNodeId
  if (!batchId) return null
  return (nodes || []).find((n) => n.id === batchId) || null
}

/**
 * 在 batch-body 内创建子节点。
 * @param {string} type
 * @param {{ x: number, y: number }} position
 * @param {string} bodyId
 * @param {Array} existingNodes
 * @returns {object}
 */
export function createBatchBodyChildNode(type, position, bodyId, existingNodes) {
  const node = createVueFlowNode(type, position, existingNodes)
  node.parentNode = bodyId
  node.extent = 'parent'
  node.data.parentId = bodyId
  return node
}

/** 批处理体内禁止添加的节点类型。 */
export const BATCH_BODY_FORBIDDEN_TYPES = [
  'loop',
  'loop-body',
  'loop-body-start',
  'loop-body-end',
  'batch',
  'batch-body',
  'break-loop',
  'continue-loop',
  'loop-set-variable'
]

/**
 * @param {string|undefined} wfType
 */
export function isForbiddenInBatchBody(wfType) {
  return BATCH_BODY_FORBIDDEN_TYPES.includes(wfType)
}

export function isBatchInternalCanvasNode(node) {
  const wfType = node?.data?.wfType
  if (wfType === 'batch-body') return true
  return !!(node?.parentNode || node?.data?.parentId)
}
