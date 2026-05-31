/**
 * 将后端 ClientTrackTimelineVo 转为 ECharts graph/tree 所需结构，并维护叶子节点 → 事件映射。
 */

/**
 * @param {Record<string, unknown>|null|undefined} page
 * @param {number} [index]
 * @returns {string}
 */
export function pageNodeId(page, index = 0) {
  if (!page) {
    return `p:idx:${index}`
  }
  const id = page.pageVisitId || page.pagePath
  return id ? `p:${id}` : `p:idx:${index}`
}

/**
 * @param {string|undefined|null} breadcrumb
 * @param {string|undefined|null} menuName
 * @param {string|undefined|null} pagePath
 * @returns {string}
 */
export function pageDisplayLabel(breadcrumb, menuName, pagePath) {
  if (breadcrumb) return String(breadcrumb)
  if (menuName) return String(menuName)
  if (pagePath) return String(pagePath)
  return '未知页面'
}

/**
 * @param {Record<string, unknown>|undefined|null} ev
 * @returns {Record<string, unknown>}
 */
function parseEventRaw(ev) {
  if (!ev || typeof ev !== 'object') {
    return {}
  }
  const raw = ev.rawJson
  if (typeof raw === 'string' && raw.trim()) {
    try {
      const parsed = JSON.parse(raw)
      return typeof parsed === 'object' && parsed ? parsed : { ...ev }
    } catch {
      return { ...ev }
    }
  }
  return { ...ev }
}

/**
 * @param {Record<string, unknown>|undefined|null} batch
 * @param {boolean} isVisit
 * @returns {string}
 */
function batchLabel(batch, isVisit) {
  if (!batch) return isVisit ? '[访问] 页面' : '[操作] 未知'
  const raw = batch.triggerAction ? String(batch.triggerAction) : isVisit ? '页面' : '操作'
  if (isVisit) {
    return `[访问] ${raw.replace(/^访问:/, '')}`
  }
  return `[操作] ${raw}`
}

/**
 * @param {Record<string, unknown>|undefined|null} ev
 * @returns {string}
 */
function eventTreeLabel(ev) {
  if (!ev) return '[事件] 未知'
  const type = ev.type ? String(ev.type) : ''
  const label = ev.label ? String(ev.label) : type || '事件'
  if (type === 'click') return `[点击] ${label.replace(/^点击\s*/, '')}`
  if (type === 'api_call' || type === 'api_slow' || type === 'api_error') return `[API] ${label}`
  if (type === 'route_enter') return `[进入] ${label.replace(/^进入\s*/, '')}`
  if (type === 'route_leave') return '[离开] 页面'
  if (type === 'js_error' || type === 'promise_error') return `[错误] ${label}`
  return `[${type || '事件'}] ${label}`
}

/**
 * @param {Record<string, unknown>|undefined|null} timelineVo
 * @returns {{
 *   summary: Record<string, unknown>,
 *   graph: { nodes: Record<string, unknown>[], links: Record<string, unknown>[] },
 *   tree: { name: string, nodeId: string, children: Record<string, unknown>[] },
 *   eventMap: Record<string, Record<string, unknown>>
 * }}
 */
export function buildTimelineModel(timelineVo) {
  const vo = timelineVo && typeof timelineVo === 'object' ? timelineVo : {}
  const pages = Array.isArray(vo.pages) ? vo.pages : []
  const edges = Array.isArray(vo.pageFlowEdges) ? vo.pageFlowEdges : []

  /** @type {{ page: Record<string, unknown>, id: string }[]} */
  const pageMeta = pages.map((page, index) => ({
    page,
    id: pageNodeId(page, index)
  }))

  /** @type {Record<string, unknown>[]} */
  const graphNodes = pageMeta.map(({ page, id }, index) => {
    const name = pageDisplayLabel(page.menuBreadcrumb, page.menuName, page.pagePath)
    return {
      id,
      name,
      step: index + 1,
      pageVisitId: page.pageVisitId || '',
      pagePath: page.pagePath || '',
      label: name
    }
  })

  /** @type {Record<string, unknown>[]} */
  const flowSteps = pageMeta.map(({ page, id }, index) => ({
    id,
    step: index + 1,
    title: `第 ${index + 1} 步`,
    description: pageDisplayLabel(page.menuBreadcrumb, page.menuName, page.pagePath),
    pagePath: page.pagePath || ''
  }))

  /** @type {Record<string, unknown>[]} */
  const graphLinks = edges
    .map((edge, edgeIndex) => {
      const source = pageMeta[edgeIndex]?.id
      const target = pageMeta[edgeIndex + 1]?.id
      if (!source || !target) {
        return null
      }
      return {
        source,
        target,
        value: edge.toMenuLabel || edge.toPagePath || '',
        lineStyle: { curveness: 0.15 }
      }
    })
    .filter(Boolean)

  /** @type {Record<string, Record<string, unknown>>} */
  const eventMap = {}

  /** @type {Record<string, unknown>[]} */
  const treeChildren = pageMeta.map(({ page, id: pageId }, pageIndex) => {
    const pageName = pageDisplayLabel(page.menuBreadcrumb, page.menuName, page.pagePath)
    /** @type {Record<string, unknown>[]} */
    const batchChildren = []

    const visitBatch = page.pageVisitBatch
    if (visitBatch) {
      batchChildren.push(buildBatchTreeNode(visitBatch, true, eventMap))
    }

    const actions = Array.isArray(page.actions) ? page.actions : []
    actions.forEach((action) => {
      batchChildren.push(buildBatchTreeNode(action, false, eventMap))
    })

    return {
      name: `[${pageIndex + 1}] ${pageName}`,
      nodeId: `t:${pageId}`,
      pageNodeId: pageId,
      nodeRole: 'page',
      children: batchChildren
    }
  })

  const userName = vo.userName ? String(vo.userName) : ''
  const sessionId = vo.sessionId ? String(vo.sessionId) : ''
  const rootName = userName
    ? `${userName} 的行为轨迹`
    : sessionId
      ? `会话 ${sessionId.slice(0, 8)}…`
      : '行为轨迹'

  return {
    summary: {
      browserVisitId: vo.browserVisitId || '',
      sessionId: vo.sessionId || '',
      userName: vo.userName || '',
      totalBatches: vo.totalBatches ?? 0,
      truncated: Boolean(vo.truncated)
    },
    graph: { nodes: graphNodes, links: graphLinks },
    flowSteps,
    tree: {
      name: rootName,
      nodeId: 't:root',
      children: treeChildren
    },
    eventMap
  }
}

/**
 * @param {Record<string, unknown>} batch
 * @param {boolean} isVisit
 * @param {Record<string, Record<string, unknown>>} eventMap
 * @returns {Record<string, unknown>}
 */
function buildBatchTreeNode(batch, isVisit, eventMap) {
  const batchId = batch.batchId != null ? String(batch.batchId) : `${isVisit ? 'visit' : 'action'}-${Math.random()}`
  const nodeId = `t:batch:${batchId}`
  const events = Array.isArray(batch.events) ? batch.events : []
  /** @type {Record<string, unknown>[]} */
  const eventChildren = events.map((ev, idx) => {
    const leafId = `t:ev:${batchId}:${idx}`
    eventMap[leafId] = parseEventRaw(ev)
    return {
      name: eventTreeLabel(ev),
      nodeId: leafId,
      isEventLeaf: true,
      eventType: ev?.type || ''
    }
  })

  return {
    name: batchLabel(batch, isVisit),
    nodeId,
    nodeRole: isVisit ? 'visit' : 'action',
    batchId: batch.batchId,
    isVisitBatch: isVisit,
    children: eventChildren
  }
}
