import { createNodeId, createVueFlowNode } from '../graphConverter'
import { NODE_META_MAP } from '../nodeMeta'

/** 循环体容器默认尺寸（px） */
export const LOOP_BODY_DEFAULT_SIZE = { width: 380, height: 260 }

/** 循环节点：主流程出口（连下一节点） */
export const LOOP_HANDLE_FLOW_OUT = 'flow-out'
/** 循环节点：进入循环体（连 loop-body 顶部） */
export const LOOP_HANDLE_BODY = 'body'
/** 循环体：接收循环头连线（顶部） */
export const LOOP_BODY_HANDLE_IN = 'body-in'
/** 循环体：左侧入口，连体内第一个节点 */
export const LOOP_BODY_HANDLE_ENTRY = 'body-entry'
/** 循环体：右侧出口，接收体内最后节点 */
export const LOOP_BODY_HANDLE_EXIT = 'body-exit'

/** @deprecated 已迁移为容器左右 handle，仅用于历史图迁移 */
export const LOOP_BODY_ANCHOR_TYPES = ['loop-body-start', 'loop-body-end']

/**
 * @param {string|undefined} wfType
 * @returns {boolean}
 */
export function isLoopBodyAnchorType(wfType) {
  return LOOP_BODY_ANCHOR_TYPES.includes(wfType)
}

/**
 * @param {object} node
 * @returns {boolean}
 */
export function isLoopBodyBusinessChild(node) {
  const wfType = node?.data?.wfType
  if (!wfType || wfType === 'loop-body') return false
  return !isLoopBodyAnchorType(wfType)
}

/**
 * 创建 loop → loop-body 结构连线。
 * @param {string} loopId
 * @param {string} bodyId
 * @returns {object}
 */
export function createLoopBodyEdge(loopId, bodyId) {
  return {
    id: `e_${loopId}_${bodyId}_body`,
    source: loopId,
    target: bodyId,
    sourceHandle: LOOP_HANDLE_BODY,
    targetHandle: LOOP_BODY_HANDLE_IN,
    type: 'workflow',
    animated: true,
    style: { stroke: '#94b8ff' }
  }
}

/**
 * 创建 loop + loop-body 节点对（无体内开始/结束子节点）。
 * @param {{ x: number, y: number }} position
 * @param {Array} existingNodes
 * @returns {{ nodes: Array<object>, edges: Array<object> }}
 */
export function createLoopNodePair(position, existingNodes) {
  const loopNode = createVueFlowNode('loop', position, existingNodes)
  const bodyId = createNodeId('loop_body', [...(existingNodes || []), loopNode])
  loopNode.data.bodyId = bodyId
  if (!Array.isArray(loopNode.data.arrayParameters) || !loopNode.data.arrayParameters.length) {
    loopNode.data.arrayParameters = [{ key: 'item', source: '' }]
  }
  if (!loopNode.data.outputVariableName) {
    loopNode.data.outputVariableName = 'results'
  }

  const bodyMeta = NODE_META_MAP['loop-body']
  const bodyNode = {
    id: bodyId,
    type: 'workflow',
    position: {
      x: Math.round(position.x - 40),
      y: Math.round(position.y + 72)
    },
    style: {
      width: `${LOOP_BODY_DEFAULT_SIZE.width}px`,
      height: `${LOOP_BODY_DEFAULT_SIZE.height}px`,
      zIndex: -1
    },
    data: {
      wfType: 'loop-body',
      label: bodyMeta?.label || '循环体',
      _icon: bodyMeta?.icon,
      loopNodeId: loopNode.id,
      ...(JSON.parse(JSON.stringify(bodyMeta?.defaults || {})))
    }
  }

  return {
    nodes: [loopNode, bodyNode],
    edges: [createLoopBodyEdge(loopNode.id, bodyId)]
  }
}

/**
 * 将历史 loop-body-start/end 锚点边迁移到容器左右 handle，并移除锚点节点。
 * @param {Array} nodes
 * @param {Array} edges
 * @returns {{ nodes: Array, edges: Array }}
 */
export function migrateLoopBodyAnchorNodes(nodes, edges) {
  const nodeList = [...(nodes || [])]
  let edgeList = [...(edges || [])]
  const anchorIds = new Set(
    nodeList.filter((n) => isLoopBodyAnchorType(n.data?.wfType)).map((n) => n.id)
  )
  if (!anchorIds.size) {
    return { nodes: nodeList, edges: edgeList }
  }

  edgeList = edgeList.map((edge) => {
    let next = { ...edge }
    if (anchorIds.has(edge.source)) {
      const anchor = nodeList.find((n) => n.id === edge.source)
      const bodyId = anchor?.parentNode || anchor?.data?.parentId
      if (anchor?.data?.wfType === 'loop-body-start' && bodyId) {
        next = { ...next, source: bodyId, sourceHandle: LOOP_BODY_HANDLE_ENTRY }
      }
    }
    if (anchorIds.has(edge.target)) {
      const anchor = nodeList.find((n) => n.id === edge.target)
      const bodyId = anchor?.parentNode || anchor?.data?.parentId
      if (anchor?.data?.wfType === 'loop-body-end' && bodyId) {
        next = { ...next, target: bodyId, targetHandle: LOOP_BODY_HANDLE_EXIT }
      }
    }
    return next
  })

  const filteredNodes = nodeList.filter((n) => !anchorIds.has(n.id))
  return { nodes: filteredNodes, edges: edgeList }
}

/**
 * 补全 loop→body 连线；迁移/移除体内锚点；补全体内业务连线。
 * @param {Array} nodes
 * @param {Array} edges
 * @returns {{ nodes: Array, edges: Array }}
 */
export function ensureLoopGraphStructure(nodes, edges) {
  let nodeList = [...(nodes || [])]
  let edgeList = [...(edges || [])]

  const migrated = migrateLoopBodyAnchorNodes(nodeList, edgeList)
  nodeList = migrated.nodes
  edgeList = migrated.edges

  nodeList
    .filter((n) => n.data?.wfType === 'loop' && n.data?.bodyId)
    .forEach((loopNode) => {
      const bodyId = loopNode.data.bodyId
      const bodyNode = nodeList.find((n) => n.id === bodyId)
      if (bodyNode && bodyNode.data) {
        bodyNode.data.loopNodeId = loopNode.id
      }
      if (!nodeList.some((n) => n.id === bodyId)) return

      const hasBodyEdge = edgeList.some(
        (e) => e.source === loopNode.id && e.target === bodyId && e.sourceHandle === LOOP_HANDLE_BODY
      )
      if (!hasBodyEdge) {
        edgeList.push(createLoopBodyEdge(loopNode.id, bodyId))
      }

      repairLoopBodyPipeline(bodyId, nodeList, edgeList)
      repairLoopOutputReference(loopNode, nodeList)
    })

  return { nodes: nodeList, edges: edgeList }
}

/** 可作为循环「每轮结果」来源的体内节点类型（按优先级） */
const LOOP_OUTPUT_NODE_TYPES = ['answer', 'template-transform', 'text-process', 'llm', 'variable-assign', 'loop-set-variable']

/** 各节点类型默认输出字段名 */
const LOOP_OUTPUT_FIELD_BY_TYPE = {
  answer: 'text',
  'template-transform': 'result',
  'text-process': 'output',
  llm: 'text',
  'variable-assign': 'result',
  'loop-set-variable': 'value'
}

/**
 * 校验循环节点 outputNodeId 是否仍指向循环体内的有效节点。
 * @param {object} loopData
 * @param {string} bodyId
 * @param {Array} nodeList
 * @returns {boolean}
 */
export function isLoopOutputNodeValid(loopData, bodyId, nodeList) {
  if (!loopData || loopData.outputMode === 'variable') return true
  const outputNodeId = (loopData.outputNodeId || '').trim()
  if (!outputNodeId) return true
  const childIds = collectLoopBodyChildIds(bodyId, nodeList)
  if (!childIds.has(outputNodeId)) return false
  const node = (nodeList || []).find((n) => n.id === outputNodeId)
  return LOOP_OUTPUT_NODE_TYPES.includes(node?.data?.wfType)
}

/** 从循环体内 answer 节点的 outputVariables 推断聚合字段。 */
function applyAnswerOutputField(loopData, answerNode) {
  const keys = (Array.isArray(answerNode?.data?.outputVariables) ? answerNode.data.outputVariables : [])
    .map((v) => (v?.key || '').trim())
    .filter(Boolean)
  if (keys.length === 1) {
    loopData.outputField = keys[0]
  } else if (keys.length > 1) {
    loopData.outputField = ''
  } else {
    loopData.outputField = 'text'
  }
}

/**
 * 修复失效的循环输出节点引用（如模板遗留 answer_in_1 但体内已删）。
 * @param {object} loopNode
 * @param {Array} nodeList
 */
export function repairLoopOutputReference(loopNode, nodeList) {
  const data = loopNode?.data
  const bodyId = data?.bodyId
  if (!data || !bodyId || data.outputMode === 'variable') return

  const outputNodeId = (data.outputNodeId || '').trim()
  const childIds = collectLoopBodyChildIds(bodyId, nodeList)
  const children = (nodeList || []).filter((n) => childIds.has(n.id))
  const answer = children.find((n) => n.data?.wfType === 'answer')

  if (outputNodeId && isLoopOutputNodeValid(data, bodyId, nodeList)) {
    if (answer && outputNodeId === answer.id) {
      applyAnswerOutputField(data, answer)
    }
    return
  }

  const pick =
    LOOP_OUTPUT_NODE_TYPES.map((t) => children.find((n) => n.data?.wfType === t)).find(Boolean) ||
    null

  if (pick) {
    data.outputNodeId = pick.id
    if (pick.data?.wfType === 'answer') {
      applyAnswerOutputField(data, pick)
    } else {
      data.outputField = LOOP_OUTPUT_FIELD_BY_TYPE[pick.data?.wfType] || 'result'
    }
    return
  }

  data.outputNodeId = ''
  data.outputField = 'text'
}

/**
 * 补全循环体内业务节点连线（不含锚点）。
 * @param {string} bodyId
 * @param {Array} nodeList
 * @param {Array} edgeList
 */
function repairLoopBodyPipeline(bodyId, nodeList, edgeList) {
  const children = nodeList.filter(
    (n) =>
      (n.parentNode === bodyId || n.data?.parentId === bodyId) && isLoopBodyBusinessChild(n)
  )
  const findByType = (wfType) => children.find((n) => n.data?.wfType === wfType)
  const tmpl = findByType('template-transform')
  const assign = findByType('loop-set-variable')
  const answer = children.find((n) => n.data?.wfType === 'answer')

  if (tmpl) {
    ensureBodyEdge(edgeList, bodyId, tmpl.id, LOOP_BODY_HANDLE_ENTRY, null)
  }
  if (tmpl && assign) {
    ensureBodyEdge(edgeList, tmpl.id, assign.id, null, null)
  }
  if (assign && answer) {
    ensureBodyEdge(edgeList, assign.id, answer.id, null, null)
  }
  const terminal = answer || assign || tmpl
  if (terminal) {
    ensureBodyEdge(edgeList, terminal.id, bodyId, null, LOOP_BODY_HANDLE_EXIT)
  }
}

/**
 * @param {Array} edgeList
 * @param {string} sourceId
 * @param {string} targetId
 * @param {string|null} sourceHandle
 * @param {string|null} targetHandle
 */
function ensureBodyEdge(edgeList, sourceId, targetId, sourceHandle, targetHandle) {
  if (!sourceId || !targetId) return
  const exists = edgeList.some(
    (e) =>
      e.source === sourceId &&
      e.target === targetId &&
      (e.sourceHandle || null) === (sourceHandle || null) &&
      (e.targetHandle || null) === (targetHandle || null)
  )
  if (exists) return
  edgeList.push({
    id: `e_${sourceId}_${targetId}_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
    source: sourceId,
    target: targetId,
    sourceHandle: sourceHandle || undefined,
    targetHandle: targetHandle || undefined,
    type: 'workflow',
    animated: true,
    style: { stroke: '#94b8ff' }
  })
}

export function loopBodyAnchorGuardMessage(wfType) {
  if (wfType === 'loop-body-start') return '循环体「开始」已改为容器左侧连接点，请重新打开工作流以自动迁移'
  if (wfType === 'loop-body-end') return '循环体「结束」已改为容器右侧连接点，请重新打开工作流以自动迁移'
  return null
}

/**
 * @param {object} connection
 * @param {Array} nodes
 * @returns {string|null} 错误文案，合法时 null
 */
export function validateLoopConnection(connection, nodes) {
  const { source, target, sourceHandle, targetHandle } = connection || {}
  const sourceNode = (nodes || []).find((n) => n.id === source)
  const targetNode = (nodes || []).find((n) => n.id === target)
  if (!sourceNode || !targetNode) return null

  const sourceType = sourceNode.data?.wfType
  const targetType = targetNode.data?.wfType

  if (sourceType === 'loop') {
    if (targetType === 'loop-body') {
      if (sourceHandle && sourceHandle !== LOOP_HANDLE_BODY) {
        return '请使用循环节点底部的「循环体」出口连接到循环体'
      }
      return null
    }
    if (sourceHandle === LOOP_HANDLE_BODY) {
      return '「循环体」出口只能连接到循环体容器'
    }
    return null
  }

  if (sourceType === 'loop-body') {
    const childOk =
      (targetNode.parentNode === source || targetNode.data?.parentId === source) &&
      isLoopBodyBusinessChild(targetNode)
    if (!childOk) {
      return '循环体左侧「开始」只能连接到循环体内的业务节点'
    }
    if (sourceHandle && sourceHandle !== LOOP_BODY_HANDLE_ENTRY) {
      return '请从循环体左侧「开始」连接到体内节点'
    }
    return null
  }

  if (targetType === 'loop-body') {
    if (targetHandle === LOOP_BODY_HANDLE_IN) {
      if (sourceType !== 'loop') {
        return '循环体顶部入口仅接受循环节点的「循环体」连线'
      }
      return null
    }
    if (targetHandle === LOOP_BODY_HANDLE_EXIT) {
      const childOk =
        (sourceNode.parentNode === target || sourceNode.data?.parentId === target) &&
        isLoopBodyBusinessChild(sourceNode)
      if (!childOk) {
        return '仅循环体内的业务节点可连接到循环体右侧「结束」'
      }
      return null
    }
    return '请连接到循环体左侧「开始」或右侧「结束」'
  }

  return null
}

/**
 * 收集循环体容器内的子节点 ID（不含 loop-body 自身）。
 */
export function collectLoopBodyChildIds(bodyId, nodes) {
  const ids = new Set()
  ;(nodes || []).forEach((n) => {
    if (n.parentNode === bodyId || n.data?.parentId === bodyId) {
      if (isLoopBodyAnchorType(n.data?.wfType)) return
      ids.add(n.id)
    }
  })
  return ids
}

export function expandLoopDeletionIds(nodeId, nodes) {
  const ids = new Set([nodeId])
  const node = (nodes || []).find((n) => n.id === nodeId)
  if (!node) return ids

  const wfType = node.data?.wfType
  if (wfType === 'loop') {
    const bodyId = node.data?.bodyId
    if (bodyId) {
      ids.add(bodyId)
      collectLoopBodyChildIds(bodyId, nodes).forEach((id) => ids.add(id))
    }
  } else if (wfType === 'loop-body') {
    const loopId = node.data?.loopNodeId
    if (loopId) ids.add(loopId)
    collectLoopBodyChildIds(node.id, nodes).forEach((id) => ids.add(id))
  }
  return ids
}

export function resolveActiveLoopBodyId(selectedNode, nodes) {
  if (!selectedNode) return null
  if (selectedNode.data?.wfType === 'loop-body') return selectedNode.id
  const parentId = selectedNode.parentNode || selectedNode.data?.parentId
  if (parentId) {
    const parent = (nodes || []).find((n) => n.id === parentId)
    if (parent?.data?.wfType === 'loop-body') return parent.id
  }
  return null
}

export function isLoopInternalCanvasNode(node) {
  const wfType = node?.data?.wfType
  if (wfType === 'loop-body') return true
  if (isLoopBodyAnchorType(wfType)) return true
  return !!(node?.parentNode || node?.data?.parentId)
}

export const LOOP_BODY_ONLY_TYPES = ['break-loop', 'continue-loop', 'loop-set-variable']

export function isLoopBodyOnlyNodeType(wfType) {
  return LOOP_BODY_ONLY_TYPES.includes(wfType)
}

export function findLoopNodeByBodyId(bodyId, nodes) {
  const body = (nodes || []).find((n) => n.id === bodyId)
  const loopId = body?.data?.loopNodeId
  if (!loopId) return null
  return (nodes || []).find((n) => n.id === loopId) || null
}

export function getLoopIntermediateKeys(bodyId, nodes) {
  const loopNode = findLoopNodeByBodyId(bodyId, nodes)
  const list = loopNode?.data?.intermediateVariables
  if (!Array.isArray(list)) return []
  return list.map((item) => (item?.key || '').trim()).filter(Boolean)
}

export function createLoopBodyChildNode(type, position, bodyId, existingNodes) {
  const node = createVueFlowNode(type, position, existingNodes)
  node.parentNode = bodyId
  node.extent = 'parent'
  node.data.parentId = bodyId
  return node
}

export function normalizeLoopArrayParameters(loopData) {
  const params = Array.isArray(loopData?.arrayParameters) ? loopData.arrayParameters : []
  const normalized = params
    .map((p) => ({ key: (p?.key || '').trim(), source: p?.source || '' }))
    .filter((p) => p.key)
  if (normalized.length) return normalized
  const legacy = (loopData?.arraySource || '').trim()
  if (legacy) return [{ key: 'item', source: legacy }]
  return [{ key: 'item', source: '' }]
}

export function resolveLoopCardSections(data) {
  if (!data) {
    return { input: '—', intermediate: '未配置', output: '—' }
  }
  let input = '—'
  if (data.loopType === 'array') {
    const params = normalizeLoopArrayParameters(data)
    const keys = params.map((p) => p.key).filter(Boolean)
    const firstSource = (params.find((p) => (p.source || '').trim())?.source || '').trim()
    if (keys.length) {
      input = keys.join(', ')
    } else {
      input = '未配置'
    }
    if (firstSource) {
      const ref = firstSource.replace(/^\{\{|\}\}$/g, '')
      input = `${input} ← ${ref}`
    }
  } else if (data.loopType === 'infinite') {
    input = '无限'
  } else if (data.countSource) {
    input = '引用次数'
  } else {
    input = `${data.count ?? 10} 次`
  }

  const ivs = Array.isArray(data.intermediateVariables)
    ? data.intermediateVariables.filter((v) => (v?.key || '').trim())
    : []
  const intermediate = ivs.length
    ? ivs.map((v) => v.key).join(', ')
    : '未配置中间变量'

  const outName = (data.outputVariableName || 'results').trim() || 'results'
  let output = outName
  if (data.outputMode === 'variable' && data.outputVariableKey) {
    output = `${outName} ← ${data.outputVariableKey}`
  } else if (data.outputNodeId) {
    output = `${outName} ← ${data.outputNodeId}.${data.outputField || 'result'}`
  }

  return { input, intermediate, output }
}

export function resolveLoopItemVariableKeys(loopData) {
  const keys = normalizeLoopArrayParameters(loopData).map((p) => p.key)
  return [...keys, 'index']
}
