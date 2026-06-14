import dagre from '@dagrejs/dagre'

/** 主画布 dagre 参数（左→右，对齐 Vue Flow 官方 layout 示例） */
const MAIN_LAYOUT_OPTIONS = {
  rankdir: 'LR',
  nodesep: 56,
  ranksep: 96,
  marginx: 40,
  marginy: 40
}

/** 容器体内 dagre 参数 */
const BODY_LAYOUT_OPTIONS = {
  rankdir: 'LR',
  nodesep: 36,
  ranksep: 64,
  marginx: 0,
  marginy: 0
}

const BODY_MIN = { width: 380, height: 260 }
const BODY_PADDING = { x: 24, y: 48 }
const BODY_BELOW_HEAD_OFFSET = { x: -40, y: 32 }

/**
 * 估算节点在布局算法中的宽高（Vue Flow 节点 position 为左上角）。
 * @param {object} node
 * @returns {{ width: number, height: number }}
 */
export function getNodeLayoutSize(node) {
  const wfType = node?.data?.wfType
  if (wfType === 'loop-body' || wfType === 'batch-body') {
    const w = parseInt(String(node.style?.width || BODY_MIN.width), 10)
    const h = parseInt(String(node.style?.height || BODY_MIN.height), 10)
    return {
      width: Number.isFinite(w) ? w : BODY_MIN.width,
      height: Number.isFinite(h) ? h : BODY_MIN.height
    }
  }
  if (wfType === 'if-else' || wfType === 'question-classifier') {
    return { width: 280, height: 160 }
  }
  if (wfType === 'loop') {
    return { width: 240, height: 140 }
  }
  if (wfType === 'batch') {
    return { width: 240, height: 140 }
  }
  return { width: 240, height: 120 }
}

/**
 * @param {string|undefined} wfType
 */
function isContainerBodyType(wfType) {
  return wfType === 'loop-body' || wfType === 'batch-body'
}

/**
 * loop→body 结构边不参与主图 dagre 排序。
 * @param {object} edge
 */
function isStructuralBodyEdge(edge) {
  return edge?.sourceHandle === 'body' || edge?.targetHandle === 'body-in'
}

/**
 * @param {object} node
 */
function getParentContainerId(node) {
  return node?.parentNode || node?.data?.parentId || null
}

/**
 * 对节点子集执行 dagre 布局，返回 id → 新 position 映射。
 * @param {Array<object>} nodeList
 * @param {Array<object>} edgeList
 * @param {object} graphOptions
 * @returns {Map<string, { x: number, y: number }>}
 */
function runDagreLayout(nodeList, edgeList, graphOptions) {
  const positions = new Map()
  if (!nodeList.length) return positions

  const g = new dagre.graphlib.Graph()
  g.setDefaultEdgeLabel(() => ({}))
  g.setGraph({ ...graphOptions })

  nodeList.forEach((node) => {
    const size = getNodeLayoutSize(node)
    g.setNode(node.id, { width: size.width, height: size.height })
  })

  edgeList.forEach((edge) => {
    if (g.hasNode(edge.source) && g.hasNode(edge.target)) {
      g.setEdge(edge.source, edge.target)
    }
  })

  dagre.layout(g)

  nodeList.forEach((node) => {
    const pos = g.node(node.id)
    if (!pos) return
    const size = getNodeLayoutSize(node)
    positions.set(node.id, {
      x: Math.round(pos.x - size.width / 2),
      y: Math.round(pos.y - size.height / 2)
    })
  })

  return positions
}

/**
 * 归一化容器内子节点坐标，并计算容器所需尺寸。
 * @param {Array<object>} children
 * @param {Map<string, { x: number, y: number }>} positions
 * @returns {{ positions: Map<string, { x: number, y: number }>, width: number, height: number }}
 */
function normalizeBodyChildPositions(children, positions) {
  if (!children.length) {
    return { positions, width: BODY_MIN.width, height: BODY_MIN.height }
  }

  let minX = Infinity
  let minY = Infinity
  let maxX = -Infinity
  let maxY = -Infinity

  children.forEach((node) => {
    const pos = positions.get(node.id)
    if (!pos) return
    const size = getNodeLayoutSize(node)
    minX = Math.min(minX, pos.x)
    minY = Math.min(minY, pos.y)
    maxX = Math.max(maxX, pos.x + size.width)
    maxY = Math.max(maxY, pos.y + size.height)
  })

  if (!Number.isFinite(minX)) {
    return { positions, width: BODY_MIN.width, height: BODY_MIN.height }
  }

  const shiftX = BODY_PADDING.x - minX
  const shiftY = BODY_PADDING.y - minY
  const normalized = new Map()

  children.forEach((node) => {
    const pos = positions.get(node.id)
    if (!pos) return
    normalized.set(node.id, {
      x: Math.round(pos.x + shiftX),
      y: Math.round(pos.y + shiftY)
    })
  })

  const width = Math.max(BODY_MIN.width, Math.round(maxX - minX + BODY_PADDING.x * 2))
  const height = Math.max(BODY_MIN.height, Math.round(maxY - minY + BODY_PADDING.y * 2))

  return { positions: normalized, width, height }
}

/**
 * 布局单个循环体 / 批处理体内的子节点。
 * @param {string} bodyId
 * @param {Array<object>} allNodes
 * @param {Array<object>} allEdges
 * @returns {{ childPositions: Map<string, { x: number, y: number }>, width: number, height: number }}
 */
function layoutContainerBody(bodyId, allNodes, allEdges) {
  const children = allNodes.filter((n) => {
    if (getParentContainerId(n) !== bodyId) return false
    const wfType = n.data?.wfType
    if (wfType === 'loop-body-start' || wfType === 'loop-body-end') return false
    return true
  })
  if (!children.length) {
    return { childPositions: new Map(), width: BODY_MIN.width, height: BODY_MIN.height }
  }

  const childIds = new Set(children.map((n) => n.id))
  const childEdges = allEdges.filter((e) => childIds.has(e.source) && childIds.has(e.target))
  const rawPositions = runDagreLayout(children, childEdges, BODY_LAYOUT_OPTIONS)
  const { positions, width, height } = normalizeBodyChildPositions(children, rawPositions)
  return { childPositions: positions, width, height }
}

/**
 * 优化工作流画布布局（主图 LR + 容器体内部 LR）。
 * 参考 @vue-flow/core 官方 dagre layout 示例。
 * @param {Array<object>} nodes Vue Flow 节点
 * @param {Array<object>} edges Vue Flow 边
 * @returns {{ nodes: Array<object>, edges: Array<object> }}
 */
export function optimizeWorkflowLayout(nodes, edges) {
  const nodeList = [...(nodes || [])]
  const edgeList = [...(edges || [])]
  if (!nodeList.length) return { nodes: nodeList, edges: edgeList }

  const nodeById = new Map(nodeList.map((n) => [n.id, n]))

  /** 主画布参与 dagre 的节点（排除容器体，容器体随头节点定位） */
  const mainNodes = nodeList.filter((n) => !isContainerBodyType(n.data?.wfType))
  const mainIds = new Set(mainNodes.map((n) => n.id))
  const mainEdges = edgeList.filter(
    (e) =>
      !isStructuralBodyEdge(e) &&
      mainIds.has(e.source) &&
      mainIds.has(e.target)
  )

  const mainPositions = runDagreLayout(mainNodes, mainEdges, MAIN_LAYOUT_OPTIONS)

  /** bodyId → { width, height, childPositions } */
  const bodyLayoutMeta = new Map()
  nodeList
    .filter((n) => isContainerBodyType(n.data?.wfType))
    .forEach((bodyNode) => {
      bodyLayoutMeta.set(bodyNode.id, layoutContainerBody(bodyNode.id, nodeList, edgeList))
    })

  const resultNodes = nodeList.map((node) => {
    const wfType = node.data?.wfType

    if (isContainerBodyType(wfType)) {
      const headId = node.data?.loopNodeId || node.data?.batchNodeId
      const headNode = headId ? nodeById.get(headId) : null
      const headPos = headId ? mainPositions.get(headId) : null
      const meta = bodyLayoutMeta.get(node.id) || {
        width: BODY_MIN.width,
        height: BODY_MIN.height
      }

      let position = node.position || { x: 0, y: 0 }
      if (headNode && headPos) {
        const headSize = getNodeLayoutSize({ ...headNode, position: headPos })
        position = {
          x: Math.round(headPos.x + BODY_BELOW_HEAD_OFFSET.x),
          y: Math.round(headPos.y + headSize.height + BODY_BELOW_HEAD_OFFSET.y)
        }
      } else if (mainPositions.has(node.id)) {
        position = mainPositions.get(node.id)
      }

      return {
        ...node,
        position,
        style: {
          ...node.style,
          width: `${meta.width}px`,
          height: `${meta.height}px`,
          zIndex: -1
        },
        data: {
          ...node.data,
          width: meta.width,
          height: meta.height
        }
      }
    }

    const parentId = getParentContainerId(node)
    if (parentId) {
      const meta = bodyLayoutMeta.get(parentId)
      const childPos = meta?.childPositions?.get(node.id)
      if (childPos) {
        return { ...node, position: childPos }
      }
      return node
    }

    const mainPos = mainPositions.get(node.id)
    if (mainPos) {
      return { ...node, position: mainPos }
    }
    return node
  })

  return { nodes: resultNodes, edges: edgeList }
}
