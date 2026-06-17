import { NODE_META_MAP } from './nodeMeta'
import { ensureFixedWorkflowNodes } from './utils/workflowNodePolicy'
import { ensureLoopGraphStructure } from './utils/loopUtils'
import { ensureBatchGraphStructure } from './utils/batchUtils'
import { migrateIntentRecognitionGraph } from './utils/intentUtils'

const GRAPH_VERSION = 1

/**
 * 后端 graph DSL 转为 Vue Flow nodes/edges。
 * @param {object|null|undefined} graph 后端 DSL
 * @returns {{ nodes: Array, edges: Array }}
 */
export function graphToVueFlow(graph) {
  if (!graph?.nodes?.length) {
    const nodes = ensureFixedWorkflowNodes([], (type, position, existing) =>
      createVueFlowNode(type, position, existing)
    )
    return { nodes, edges: [] }
  }

  const migrated = migrateIntentRecognitionGraph(graph.nodes, graph.edges || [])

  const nodes = migrated.nodes.map((node) => {
    const nodeData = { ...(node.data || {}) }
    const parentId = node.parentId || nodeData.parentId
    if (node.type === 'answer' || node.type === 'end') {
      if (!nodeData.outputMode) {
        nodeData.outputMode = Array.isArray(nodeData.outputVariables) && nodeData.outputVariables.length
          ? 'variables'
          : (nodeData.output ? 'text' : 'variables')
      }
      if (!Array.isArray(nodeData.outputVariables)) {
        nodeData.outputVariables = []
      }
    }
    return {
      id: node.id,
      type: 'workflow',
      position: {
        x: Number(node.position?.x ?? 0),
        y: Number(node.position?.y ?? 0)
      },
      ...(parentId ? { parentNode: parentId, extent: 'parent' } : {}),
      ...(node.type === 'loop-body' || node.type === 'batch-body'
        ? {
            style: {
              width: `${node.data?.width || 380}px`,
              height: `${node.data?.height || 260}px`,
              zIndex: -1
            }
          }
        : {}),
      data: {
        wfType: node.type,
        label: NODE_META_MAP[node.type]?.label || node.type,
        _icon: NODE_META_MAP[node.type]?.icon,
        ...(parentId ? { parentId } : {}),
        ...nodeData
      }
    }
  })

  const edges = migrated.edges.map((edge) => ({
    id: edge.id,
    source: edge.source,
    target: edge.target,
    sourceHandle: edge.sourceHandle || undefined,
    targetHandle: edge.targetHandle || undefined,
    type: 'workflow',
    animated: true,
    style: { stroke: '#94b8ff' }
  }))

  const ensuredNodes = ensureFixedWorkflowNodes(nodes, (type, position, existing) =>
    createVueFlowNode(type, position, existing)
  )

  const loopEnsured = ensureLoopGraphStructure(ensuredNodes, edges)
  const batchEnsured = ensureBatchGraphStructure(loopEnsured.nodes, loopEnsured.edges)

  return { nodes: batchEnsured.nodes, edges: batchEnsured.edges }
}

/**
 * Vue Flow nodes/edges 转为后端 graph DSL。
 * @param {Array} nodes Vue Flow 节点
 * @param {Array} edges Vue Flow 连线
 * @returns {object}
 */
export function vueFlowToGraph(nodes, edges) {
  return {
    version: GRAPH_VERSION,
    nodes: (nodes || []).map((node) => {
      const { wfType, label, parentId: _pid, ...rest } = node.data || {}
      const data = { label, ...rest }
      if (node.parentNode) {
        data.parentId = node.parentNode
      }
      const graphNode = {
        id: node.id,
        type: wfType || node.type,
        position: {
          x: Math.round(node.position?.x ?? 0),
          y: Math.round(node.position?.y ?? 0)
        },
        data
      }
      if (node.parentNode) {
        graphNode.parentId = node.parentNode
      }
      if ((wfType === 'loop-body' || wfType === 'batch-body') && node.style) {
        const w = parseInt(String(node.style.width || '380'), 10)
        const h = parseInt(String(node.style.height || '260'), 10)
        graphNode.data.width = Number.isFinite(w) ? w : 380
        graphNode.data.height = Number.isFinite(h) ? h : 260
      }
      return graphNode
    }),
    edges: (edges || []).map((edge) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      sourceHandle: edge.sourceHandle || null,
      targetHandle: edge.targetHandle || null
    }))
  }
}

/**
 * 生成唯一节点 ID。
 * @param {string} type 节点类型
 * @param {Array} existingNodes 已有节点
 * @returns {string}
 */
export function createNodeId(type, existingNodes) {
  const prefix = type.replace(/[^a-z0-9]/gi, '_')
  let index = (existingNodes?.length || 0) + 1
  let id = `${prefix}_${index}`
  const ids = new Set((existingNodes || []).map((n) => n.id))
  while (ids.has(id)) {
    index += 1
    id = `${prefix}_${index}`
  }
  return id
}

/**
 * 复制已有节点（新 ID、偏移位置，不复制运行/校验状态）。
 * @param {object} sourceNode 源 Vue Flow 节点
 * @param {Array} existingNodes 已有节点
 * @param {{ x?: number, y?: number }} [offset] 位置偏移
 * @returns {object|null}
 */
export function cloneVueFlowNode(sourceNode, existingNodes, offset = { x: 48, y: 48 }) {
  const wfType = sourceNode?.data?.wfType
  if (!wfType) return null

  const id = createNodeId(wfType, existingNodes)
  const meta = NODE_META_MAP[wfType]
  const data = JSON.parse(JSON.stringify(sourceNode.data || {}))
  delete data.runStatus
  delete data.validationError

  if (data.label) {
    data.label = `${data.label} 副本`
  }

  return {
    id,
    type: 'workflow',
    position: {
      x: Math.round((sourceNode.position?.x ?? 0) + (offset.x ?? 48)),
      y: Math.round((sourceNode.position?.y ?? 0) + (offset.y ?? 48))
    },
    data: {
      ...data,
      wfType,
      _icon: meta?.icon || data._icon
    }
  }
}

/**
 * 创建 Vue Flow 节点实例。
 * @param {string} type 节点类型
 * @param {{ x: number, y: number }} position 画布坐标
 * @param {Array} existingNodes 已有节点（用于 ID 去重）
 * @returns {object}
 */
export function createVueFlowNode(type, position, existingNodes) {
  const meta = NODE_META_MAP[type]
  const id = createNodeId(type, existingNodes)
  return {
    id,
    type: 'workflow',
    position,
    data: {
      wfType: type,
      label: meta?.label || type,
      _icon: meta?.icon,
      ...(JSON.parse(JSON.stringify(meta?.defaults || {})))
    }
  }
}
