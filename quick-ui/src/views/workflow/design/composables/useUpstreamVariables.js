import { computed } from 'vue'
import { NODE_META_MAP, resolveNodeOutputs } from '../nodeMeta'

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
  const upstreamIds = collectUpstreamNodeIds(currentNodeId, edges)
  const tree = []
  const added = new Set()

  ;(nodes || []).forEach((node) => {
    const wfType = node.data?.wfType
    const isStart = wfType === 'start'
    if (!isStart && !upstreamIds.has(node.id)) return

    const outputs = resolveNodeOutputs(wfType, node.data)
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
 * 上游变量 composable。
 * @param {import('vue').Ref<string|null>} currentNodeId 当前选中节点 ID
 * @param {import('vue').Ref<Array>} nodes 画布节点
 * @param {import('vue').Ref<Array>} edges 画布边
 * @returns {{ variableTree: import('vue').ComputedRef<Array> }}
 */
export function useUpstreamVariables(currentNodeId, nodes, edges) {
  const variableTree = computed(() =>
    buildUpstreamVariableTree(currentNodeId.value, nodes.value, edges.value)
  )
  return { variableTree }
}
