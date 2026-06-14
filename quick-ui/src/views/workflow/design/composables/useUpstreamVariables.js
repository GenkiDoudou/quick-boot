import { computed, shallowRef, watch } from 'vue'
import { NODE_META_MAP, resolveBatchNodeOutputs, resolveNodeOutputs } from '../nodeMeta'
import { normalizeLoopArrayParameters } from '../utils/loopUtils'
import { buildGraphStructureFingerprint } from '../utils/graphPerfUtils'

/**
 * 根据 loop-body 或反向 bodyId 查找循环头节点。
 * @param {string} bodyId
 * @param {Array} nodes
 * @returns {object|null}
 */
function findLoopHeadByBodyId(bodyId, nodes) {
  if (!bodyId) return null
  const body = (nodes || []).find((n) => n.id === bodyId)
  const loopId = body?.data?.loopNodeId
  if (loopId) {
    const byId = (nodes || []).find((n) => n.id === loopId)
    if (byId?.data?.wfType === 'loop') return byId
  }
  return (
    (nodes || []).find((n) => n.data?.wfType === 'loop' && n.data?.bodyId === bodyId) || null
  )
}

/** 解析节点所处的循环上下文。 */
function findLoopContext(currentNodeId, nodes) {
  const node = (nodes || []).find((n) => n.id === currentNodeId)
  if (!node) return null
  let bodyId = null
  if (node.data?.wfType === 'loop-body') {
    bodyId = node.id
  } else {
    const parentId = node.parentNode || node.data?.parentId
    if (parentId) {
      const parent = (nodes || []).find((n) => n.id === parentId)
      if (parent?.data?.wfType === 'loop-body') bodyId = parent.id
    }
  }
  if (!bodyId) return null
  const headNode = findLoopHeadByBodyId(bodyId, nodes)
  return {
    kind: 'loop',
    bodyId,
    headId: headNode?.id || null,
    headNode
  }
}

/** 解析节点所处的批处理上下文。 */
function findBatchContext(currentNodeId, nodes) {
  const node = (nodes || []).find((n) => n.id === currentNodeId)
  if (!node) return null
  let bodyId = null
  if (node.data?.wfType === 'batch-body') {
    bodyId = node.id
  } else {
    const parentId = node.parentNode || node.data?.parentId
    if (parentId) {
      const parent = (nodes || []).find((n) => n.id === parentId)
      if (parent?.data?.wfType === 'batch-body') bodyId = parent.id
    }
  }
  if (!bodyId) return null
  const body = (nodes || []).find((n) => n.id === bodyId)
  const batchId = body?.data?.batchNodeId
  const batchNode = (nodes || []).find((n) => n.id === batchId)
  return { kind: 'batch', bodyId, headId: batchId, headNode: batchNode }
}

/**
 * 容器体内节点的上游 ID：同容器内 BFS + 主图上头节点的上游。
 */
function collectContainerBodyUpstreamIds(nodeId, bodyId, headId, nodes, edges) {
  const bodyNodeIds = new Set(
    (nodes || [])
      .filter((n) => n.parentNode === bodyId || n.data?.parentId === bodyId)
      .map((n) => n.id)
  )

  const bodyEdges = (edges || []).filter((e) => {
    const srcInChildren = bodyNodeIds.has(e.source)
    const tgtInChildren = bodyNodeIds.has(e.target)
    const srcIsBody = e.source === bodyId
    const tgtIsBody = e.target === bodyId
    return (
      (srcInChildren && tgtInChildren) ||
      (srcIsBody && tgtInChildren) ||
      (srcInChildren && tgtIsBody)
    )
  })

  const upstream = collectUpstreamNodeIds(nodeId, bodyEdges)
  upstream.delete(bodyId)

  if (headId) {
    collectUpstreamNodeIds(headId, edges).forEach((id) => upstream.add(id))
    upstream.add(headId)
  }
  return upstream
}

/**
 * 循环头在循环体迭代内可用的输出（index / item / 数组参数 / 中间变量）。
 * @param {object|null|undefined} loopNode
 * @returns {Array<{ key: string, label: string, type: string }>}
 */
export function resolveLoopScopeOutputs(loopNode) {
  const loopType = loopNode?.data?.loopType || 'count'
  const outputs = [{ key: 'index', label: '当前索引 (index)', type: 'number' }]

  const params = normalizeLoopArrayParameters(loopNode?.data)
  if (loopType === 'array') {
    params.forEach((p) => {
      if (p.key) {
        outputs.push({ key: p.key, label: `当前元素 (${p.key})`, type: 'any' })
      }
    })
  }

  if (!outputs.some((o) => o.key === 'item')) {
    outputs.push({
      key: 'item',
      label: loopType === 'array' ? '当前元素 (item)' : '当前轮次 (item)',
      type: 'any'
    })
  }

  const ivs = Array.isArray(loopNode?.data?.intermediateVariables)
    ? loopNode.data.intermediateVariables
    : []
  ivs.forEach((v) => {
    const key = (v?.key || '').trim()
    if (key) outputs.push({ key, label: `中间变量 ${key}`, type: v?.type || 'any' })
  })
  return outputs
}

/** 判断节点是否为循环体内的业务子节点（不含 loop-body 容器自身）。 */
function isLoopBodyChildNode(nodeId, nodes) {
  const node = (nodes || []).find((n) => n.id === nodeId)
  if (!node?.data?.wfType || node.data.wfType === 'loop-body') return false
  const parentId = node.parentNode || node.data?.parentId
  if (!parentId) return false
  const parent = (nodes || []).find((n) => n.id === parentId)
  return parent?.data?.wfType === 'loop-body'
}

/** 判断节点是否为批处理体内的业务子节点。 */
function isBatchBodyChildNode(nodeId, nodes) {
  const node = (nodes || []).find((n) => n.id === nodeId)
  if (!node?.data?.wfType || node.data.wfType === 'batch-body') return false
  const parentId = node.parentNode || node.data?.parentId
  if (!parentId) return false
  const parent = (nodes || []).find((n) => n.id === parentId)
  return parent?.data?.wfType === 'batch-body'
}

/**
 * 循环节点在「循环体外」可选的输出（不含 index/item 等仅迭代内有效的字段）。
 * @param {object|null|undefined} loopData
 * @returns {Array<{ key: string, label: string, type: string }>}
 */
function resolveLoopExternalOutputs(loopData) {
  const hideKeys = new Set(['index', 'item'])
  normalizeLoopArrayParameters(loopData).forEach((p) => {
    if (p.key) hideKeys.add(p.key)
  })
  return resolveNodeOutputs('loop', loopData).filter((o) => !hideKeys.has(o.key))
}

/** 批处理头节点在体内的可用输出（index + 各输入参数当前元素）。 */
function resolveBatchScopeOutputs(batchNode) {
  const outputs = [{ key: 'index', label: '当前索引 (index)', type: 'number' }]
  const params = Array.isArray(batchNode?.data?.inputParameters) ? batchNode.data.inputParameters : []
  params.forEach((p) => {
    const key = (p?.key || '').trim()
    if (key) {
      outputs.push({ key, label: `当前元素 (${key})`, type: 'any' })
    }
  })
  return outputs
}

/**
 * 批处理节点在「批处理体外」可选的输出（outputParameters 汇总数组 + count）。
 * @param {object|null|undefined} batchData
 * @returns {Array<{ key: string, label: string, type: string }>}
 */
function resolveBatchExternalOutputs(batchData) {
  return resolveBatchNodeOutputs(batchData)
}

/** 系统变量固定项 */
const SYS_VARIABLES = [
  { key: 'sys.kbId', label: '知识库 ID', type: 'string' },
  { key: 'sys.runId', label: '运行 ID', type: 'string' },
  { key: 'sys.userId', label: '用户 ID', type: 'string' }
]

/**
 * 从图中 BFS 收集当前节点的所有上游节点 ID。
 * @param {string} nodeId 当前节点 ID
 * @param {Array} edges Vue Flow 边列表
 * @returns {Set<string>}
 */
export function collectUpstreamNodeIds(nodeId, edges) {
  const upstream = new Set()
  if (!nodeId) return upstream

  const incoming = new Map()
  ;(edges || []).forEach((e) => {
    if (!incoming.has(e.target)) incoming.set(e.target, [])
    incoming.get(e.target).push(e.source)
  })

  const queue = [...(incoming.get(nodeId) || [])]
  while (queue.length) {
    const id = queue.shift()
    if (upstream.has(id)) continue
    upstream.add(id)
    const parents = incoming.get(id) || []
    parents.forEach((p) => queue.push(p))
  }
  return upstream
}

/**
 * 构建上游变量树（供 VariablePicker 使用）。
 * start 节点入参全局可用；其余节点仅展示图上的上游节点输出。
 * @param {string} currentNodeId 当前配置节点 ID
 * @param {Array} nodes Vue Flow 节点
 * @param {Array} edges Vue Flow 边
 * @returns {Array<{ id: string, label: string, children?: Array }>}
 */
export function buildUpstreamVariableTree(currentNodeId, nodes, edges) {
  const insideLoopBody = isLoopBodyChildNode(currentNodeId, nodes)
  const insideBatchBody = isBatchBodyChildNode(currentNodeId, nodes)
  const containerCtx =
    (insideLoopBody && findLoopContext(currentNodeId, nodes)) ||
    (insideBatchBody && findBatchContext(currentNodeId, nodes)) ||
    null
  const upstreamIds = containerCtx
    ? collectContainerBodyUpstreamIds(
        currentNodeId,
        containerCtx.bodyId,
        containerCtx.headId,
        nodes,
        edges
      )
    : collectUpstreamNodeIds(currentNodeId, edges)
  const tree = []
  const added = new Set()

  ;(nodes || []).forEach((node) => {
    const wfType = node.data?.wfType
    if (wfType === 'loop-body' || wfType === 'batch-body') return
    if (wfType === 'loop-body-start' || wfType === 'loop-body-end') return
    if (containerCtx && node.id === containerCtx.headId) return
    const isStart = wfType === 'start'
    if (!isStart && !upstreamIds.has(node.id)) return
    if (containerCtx && (node.parentNode || node.data?.parentId) && node.parentNode !== containerCtx.bodyId && node.data?.parentId !== containerCtx.bodyId && wfType !== 'start') {
      return
    }
    if (
      containerCtx &&
      !node.parentNode &&
      wfType !== 'start' &&
      wfType !== 'loop' &&
      wfType !== 'batch' &&
      node.id !== containerCtx.headId
    ) {
      const inMainUpstream = collectUpstreamNodeIds(containerCtx.headId, edges).has(node.id)
      if (!inMainUpstream && node.id !== containerCtx.headId) return
    }

    const outputs =
      wfType === 'loop' && !insideLoopBody
        ? resolveLoopExternalOutputs(node.data)
        : wfType === 'batch' && !insideBatchBody
          ? resolveBatchExternalOutputs(node.data)
          : resolveNodeOutputs(wfType, node.data)
    if (!outputs.length) return
    if (added.has(node.id)) return
    added.add(node.id)

    tree.push({
      id: node.id,
      label: formatGroupLabel(node),
      children: outputs.map((o) => ({
        id: `${node.id}.${o.key}`,
        label: o.label || o.key,
        name: o.key,
        description: o.description || '',
        insert: `{{${node.id}.${o.key}}}`,
        type: o.type
      }))
    })
  })

  if (insideLoopBody && containerCtx?.kind === 'loop' && containerCtx.headId && containerCtx.headNode) {
    const loopOutputs = resolveLoopScopeOutputs(containerCtx.headNode)
    tree.unshift({
      id: `__loop_scope_${containerCtx.headId}`,
      label: `循环变量 · ${containerCtx.headId}`,
      children: loopOutputs.map((o) => ({
        id: `${containerCtx.headId}.${o.key}`,
        label: o.label || o.key,
        name: o.key,
        description: '当前迭代内可用',
        insert: `{{${containerCtx.headId}.${o.key}}}`,
        type: o.type
      }))
    })
  }

  if (insideBatchBody && containerCtx?.kind === 'batch' && containerCtx.headNode) {
    const batchOutputs = resolveBatchScopeOutputs(containerCtx.headNode)
    if (batchOutputs.length) {
      tree.unshift({
        id: `__batch_scope_${containerCtx.headId}`,
        label: `批处理变量 · ${containerCtx.headId}`,
        children: batchOutputs.map((o) => ({
          id: `${containerCtx.headId}.${o.key}`,
          label: o.label || o.key,
          name: o.key,
          description: '当前批处理轮次内可用',
          insert: `{{${containerCtx.headId}.${o.key}}}`,
          type: o.type
        }))
      })
    }
  }

  const inputVars = collectStartInputVariables(nodes)
  const hasStartGroup = tree.some((group) => {
    const node = (nodes || []).find((n) => n.id === group.id)
    return node?.data?.wfType === 'start'
  })
  if (inputVars.length && !hasStartGroup) {
    tree.push({
      id: '__inputs__',
      label: '用户输入',
      children: inputVars.map((item) => ({
        id: `inputs.${item.key}`,
        label: item.label || item.key,
        name: item.key,
        description: item.description || '',
        insert: `{{inputs.${item.key}}}`,
        type: item.type
      }))
    })
  }

  tree.push({
    id: '__sys__',
    label: '系统变量',
    children: SYS_VARIABLES.map((v) => ({
      id: v.key,
      label: v.label,
      insert: `{{${v.key}}}`,
      type: v.type
    }))
  })

  return tree
}

/**
 * 提取模板中 {{...}} 占位符的根键名（点号或方括号前）。
 * @param {string} template
 * @returns {string[]}
 */
export function extractTemplatePlaceholderRoots(template) {
  const roots = new Set()
  const re = /\{\{([^}]+)\}\}/g
  let match = re.exec(template || '')
  while (match) {
    const expr = (match[1] || '').trim().replace(/\[(\d+)]/g, '.$1')
    const root = expr.split('.')[0]?.trim()
    if (root) roots.add(root)
    match = re.exec(template || '')
  }
  return [...roots]
}

/**
 * 大模型节点提示词区域变量树：仅展示本节点已定义的输入参数（Coze 式）。
 * @param {Array<{ key?: string, value?: string }>} inputVariables
 * @returns {Array<{ id: string, label: string, children?: Array }>}
 */
export function buildLlmPromptVariableTree(inputVariables) {
  const children = (Array.isArray(inputVariables) ? inputVariables : [])
    .map((row) => (row?.key || '').trim())
    .filter(Boolean)
    .map((key) => ({
      id: `__llm_input__.${key}`,
      label: key,
      name: key,
      description: `输入参数，提示词中使用 {{${key}}}`,
      insert: `{{${key}}}`,
      type: 'any'
    }))
  if (!children.length) return []
  return [
    {
      id: '__llm_input__',
      label: '本节点输入参数',
      children
    }
  ]
}

/**
 * 校验大模型提示词是否引用了未声明的输入参数。
 * @param {string} template
 * @param {string[]} declaredKeys
 * @returns {string[]} 未声明的根键名
 */
export function findUndeclaredLlmPromptReferences(template, declaredKeys) {
  const declared = new Set((declaredKeys || []).map((k) => (k || '').trim()).filter(Boolean))
  return extractTemplatePlaceholderRoots(template).filter((root) => !declared.has(root))
}

/**
 * 变量分组标题：节点名 + ID，便于与模板 {{nodeId.field}} 对齐。
 * @param {object} node
 * @returns {string}
 */
function formatGroupLabel(node) {
  const wfType = node.data?.wfType
  if (wfType === 'start') {
    return '用户输入'
  }
  const meta = NODE_META_MAP[wfType]
  const name = node.data?.label || meta?.label || wfType || '节点'
  return `${name} · ${node.id}`
}

/**
 * 汇总画布上所有 start 节点定义的入参（去重）。
 * @param {Array} nodes
 * @returns {Array<{ key: string, label: string, type: string }>}
 */
function collectStartInputVariables(nodes) {
  const map = new Map()
  ;(nodes || []).forEach((node) => {
    if (node.data?.wfType !== 'start') return
    resolveNodeOutputs('start', node.data).forEach((item) => {
      if (!map.has(item.key)) {
        map.set(item.key, item)
      }
    })
  })
  return [...map.values()]
}

/**
 * 上游变量 composable（结构指纹缓存，拖拽节点时不重算）。
 * @param {import('vue').Ref<string|null>} currentNodeId 当前选中节点 ID
 * @param {import('vue').Ref<Array>} nodes 画布节点
 * @param {import('vue').Ref<Array>} edges 画布边
 * @returns {{ variableTree: import('vue').ShallowRef<Array> }}
 */
export function useUpstreamVariables(currentNodeId, nodes, edges) {
  const variableTree = shallowRef([])

  const fingerprint = computed(() =>
    buildGraphStructureFingerprint(nodes.value, edges.value, currentNodeId.value || '')
  )

  watch(
    fingerprint,
    () => {
      variableTree.value = buildUpstreamVariableTree(
        currentNodeId.value,
        nodes.value,
        edges.value
      )
    },
    { immediate: true }
  )

  return { variableTree }
}
