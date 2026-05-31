/**
 * 将后端 ClientTrackTimelineVo 转为 ECharts 所需结构。
 * 首屏仅 buildTimelineOverview（页面跳转 + 导航）；选中页后 buildPageDetailModel 按需构建明细。
 */

/** 超过此页数时用 chip 导航替代 el-steps */
export const PAGE_NAV_STEPS_THRESHOLD = 12

/** 超过此事件数时用 el-collapse 列表替代 ECharts tree */
export const PAGE_DETAIL_TREE_THRESHOLD = 80

/** 超过此次登录数时用 el-select 替代 Tab */
export const SESSION_TAB_SELECT_THRESHOLD = 8

/**
 * @param {Record<string, unknown>|null|undefined} page
 * @param {number} [index]
 * @param {string} [sessionId]
 * @returns {string}
 */
export function pageNodeId(page, index = 0, sessionId = '') {
  if (!page) {
    const sid = sessionId ? `${sessionId}:` : ''
    return `p:${sid}idx:${index}`
  }
  const sid = sessionId || page.sessionId || ''
  const id = page.pageVisitId || page.pagePath
  const base = id ? String(id) : `idx:${index}`
  return sid ? `p:${sid}:${base}` : `p:${base}`
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
 * @param {string|undefined|null} iso
 * @returns {string}
 */
export function formatSessionTime(iso) {
  if (!iso) return ''
  const d = new Date(String(iso))
  if (Number.isNaN(d.getTime())) return String(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * @param {Record<string, unknown>} sessionBlock
 * @param {number} sessionIndex
 * @param {number} sessionTotal
 * @returns {string}
 */
export function sessionTabLabel(sessionBlock, sessionIndex, sessionTotal) {
  const time = formatSessionTime(sessionBlock.firstTime)
  const pages = sessionBlock.pageCount ?? sessionBlock.pageIndex?.length ?? sessionBlock.graph?.nodes?.length ?? 0
  if (sessionTotal > 1) {
    const order = sessionTotal - sessionIndex
    return `第 ${order} 次登录${time ? ` · ${time}` : ''} · ${pages} 页`
  }
  return time ? `登录 · ${time} · ${pages} 页` : `本次会话 · ${pages} 页`
}

/**
 * @param {Record<string, unknown>|undefined|null} ev
 * @returns {Record<string, unknown>}
 */
export function parseEventForDrawer(ev) {
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
 * @param {Record<string, unknown>|undefined|null} page
 * @returns {{ actionCount: number, eventCount: number }}
 */
export function countPageStats(page) {
  if (!page) return { actionCount: 0, eventCount: 0 }
  if (page.actionCount != null || page.eventCount != null) {
    return {
      actionCount: Number(page.actionCount) || 0,
      eventCount: Number(page.eventCount) || 0
    }
  }
  const actions = Array.isArray(page.actions) ? page.actions : []
  let eventCount = 0
  const visit = page.pageVisitBatch
  if (visit && Array.isArray(visit.events)) {
    eventCount += visit.events.length
  }
  actions.forEach((action) => {
    if (Array.isArray(action.events)) {
      eventCount += action.events.length
    }
  })
  return { actionCount: actions.length, eventCount }
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
export function eventTreeLabel(ev) {
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
 * @param {Record<string, unknown>|undefined|null} ev
 * @returns {Record<string, unknown>}
 */
function shallowEventRef(ev) {
  if (!ev || typeof ev !== 'object') return {}
  return {
    type: ev.type,
    label: ev.label,
    rawJson: ev.rawJson,
    url: ev.url,
    method: ev.method,
    cost: ev.cost
  }
}

/**
 * @param {Record<string, unknown>|undefined|null} timelineVo
 * @returns {Record<string, unknown>[]}
 */
function normalizeSessionSources(timelineVo) {
  const vo = timelineVo && typeof timelineVo === 'object' ? timelineVo : {}
  const rawSessions = Array.isArray(vo.sessions) && vo.sessions.length ? vo.sessions : null
  if (rawSessions) return rawSessions
  return [
    {
      sessionId: vo.sessionId || '',
      browserVisitId: vo.browserVisitId || '',
      firstTime: vo.pages?.[0]?.firstTime,
      pageCount: Array.isArray(vo.pages) ? vo.pages.length : 0,
      pages: vo.pages || [],
      pageFlowEdges: vo.pageFlowEdges || []
    }
  ]
}

/**
 * @param {Record<string, unknown>[]} pages
 * @param {Record<string, unknown>[]} edges
 * @param {string} sessionId
 * @returns {{ graph: object, flowSteps: object[], pageIndex: object[] }}
 */
function buildOverviewBlock(pages, edges, sessionId) {
  /** @type {{ page: Record<string, unknown>, id: string }[]} */
  const pageMeta = pages.map((page, index) => ({
    page,
    id: pageNodeId(page, index, sessionId)
  }))

  const idByVisitKey = new Map(
    pageMeta.map(({ page, id }) => [`${sessionId}:${page.pageVisitId || ''}`, id])
  )

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

  const flowSteps = pageMeta.map(({ page, id }, index) => {
    const stats = countPageStats(page)
    const label = pageDisplayLabel(page.menuBreadcrumb, page.menuName, page.pagePath)
    return {
      id,
      step: index + 1,
      title: `第 ${index + 1} 步`,
      description: label,
      pagePath: page.pagePath || '',
      actionCount: stats.actionCount,
      eventCount: stats.eventCount
    }
  })

  const pageIndex = pageMeta.map(({ page, id }, index) => {
    const stats = countPageStats(page)
    const label = pageDisplayLabel(page.menuBreadcrumb, page.menuName, page.pagePath)
    return {
      id,
      step: index + 1,
      label,
      pagePath: page.pagePath || '',
      pageVisitId: page.pageVisitId || '',
      actionCount: stats.actionCount,
      eventCount: stats.eventCount
    }
  })

  const graphLinks = edges
    .map((edge) => {
      const fromKey = `${sessionId}:${edge.fromPageVisitId || ''}`
      const toKey = `${sessionId}:${edge.toPageVisitId || ''}`
      const source = idByVisitKey.get(fromKey)
      const target = idByVisitKey.get(toKey)
      if (!source || !target) return null
      return {
        source,
        target,
        value: edge.toMenuLabel || edge.toPagePath || '',
        lineStyle: { curveness: 0.15 }
      }
    })
    .filter(Boolean)

  return {
    graph: { nodes: graphNodes, links: graphLinks },
    flowSteps,
    pageIndex
  }
}

/**
 * @param {Record<string, unknown>|undefined|null} timelineVo
 * @returns {{ summary: object, multiSession: boolean, sessions: object[] }}
 */
export function buildTimelineOverview(timelineVo) {
  const vo = timelineVo && typeof timelineVo === 'object' ? timelineVo : {}
  const sessionSources = normalizeSessionSources(vo)
  const sessionTotal = sessionSources.length

  const sessions = sessionSources.map((sessionVo, sessionIndex) => {
    const sessionId = sessionVo.sessionId ? String(sessionVo.sessionId) : `s${sessionIndex}`
    const pages = Array.isArray(sessionVo.pages) ? sessionVo.pages : []
    const edges = Array.isArray(sessionVo.pageFlowEdges) ? sessionVo.pageFlowEdges : []
    const block = buildOverviewBlock(pages, edges, sessionId)

    return {
      key: sessionId || `session-${sessionIndex}`,
      sessionId,
      browserVisitId: sessionVo.browserVisitId || '',
      firstTime: sessionVo.firstTime || pages[0]?.firstTime || '',
      lastTime: sessionVo.lastTime || '',
      pageCount: sessionVo.pageCount ?? pages.length,
      label: '',
      ...block
    }
  })

  sessions.forEach((s, i) => {
    s.label = sessionTabLabel(s, i, sessionTotal)
  })

  const totalPages = sessions.reduce((sum, s) => sum + (s.pageCount || 0), 0)

  return {
    summary: {
      browserVisitId: vo.browserVisitId || '',
      sessionId: vo.sessionId || '',
      userName: vo.userName || '',
      totalBatches: vo.totalBatches ?? 0,
      truncated: Boolean(vo.truncated),
      sessionCount: sessions.length,
      totalPages
    },
    multiSession: sessions.length > 1,
    sessions
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
  const eventChildren = events.map((ev, idx) => {
    const leafId = `t:ev:${batchId}:${idx}`
    eventMap[leafId] = shallowEventRef(ev)
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

/**
 * @param {Record<string, unknown>} pageVo
 * @param {string} sessionId
 * @param {number} pageIndex
 * @returns {{
 *   pageNodeId: string,
 *   pageLabel: string,
 *   eventCount: number,
 *   useListFallback: boolean,
 *   tree: Record<string, unknown>,
 *   batchGroups: Record<string, unknown>[],
 *   eventMap: Record<string, Record<string, unknown>>
 * }}
 */
export function buildPageDetailModel(pageVo, sessionId, pageIndex) {
  const pageId = pageNodeId(pageVo, pageIndex, sessionId)
  const pageLabel = pageDisplayLabel(pageVo.menuBreadcrumb, pageVo.menuName, pageVo.pagePath)
  const stats = countPageStats(pageVo)
  /** @type {Record<string, Record<string, unknown>>} */
  const eventMap = {}
  /** @type {Record<string, unknown>[]} */
  const batchChildren = []
  /** @type {Record<string, unknown>[]} */
  const batchGroups = []

  const visitBatch = pageVo.pageVisitBatch
  if (visitBatch) {
    const batchNode = buildBatchTreeNode(visitBatch, true, eventMap)
    batchChildren.push(batchNode)
    batchGroups.push(buildBatchGroup(visitBatch, true, eventMap))
  }

  const actions = Array.isArray(pageVo.actions) ? pageVo.actions : []
  actions.forEach((action) => {
    const batchNode = buildBatchTreeNode(action, false, eventMap)
    batchChildren.push(batchNode)
    batchGroups.push(buildBatchGroup(action, false, eventMap))
  })

  const useListFallback = stats.eventCount > PAGE_DETAIL_TREE_THRESHOLD

  return {
    pageNodeId: pageId,
    pageLabel,
    eventCount: stats.eventCount,
    actionCount: stats.actionCount,
    useListFallback,
    tree: {
      name: pageLabel,
      nodeId: `t:${pageId}`,
      pageNodeId: pageId,
      nodeRole: 'page',
      children: batchChildren
    },
    batchGroups,
    eventMap
  }
}

/**
 * @param {Record<string, unknown>} batch
 * @param {boolean} isVisit
 * @param {Record<string, Record<string, unknown>>} eventMap
 */
function buildBatchGroup(batch, isVisit, eventMap) {
  const batchId = batch.batchId != null ? String(batch.batchId) : `${isVisit ? 'visit' : 'action'}-${Math.random()}`
  const events = Array.isArray(batch.events) ? batch.events : []
  return {
    batchId: batch.batchId,
    label: batchLabel(batch, isVisit),
    isVisit,
    events: events.map((ev, idx) => {
      const leafId = `t:ev:${batchId}:${idx}`
      eventMap[leafId] = shallowEventRef(ev)
      return {
        nodeId: leafId,
        name: eventTreeLabel(ev),
        eventType: ev?.type || ''
      }
    })
  }
}

/**
 * @param {Record<string, unknown>|undefined|null} rawVo
 * @param {string} sessionKey
 * @param {string} targetPageId
 * @returns {{ page: Record<string, unknown>, sessionId: string, pageIndex: number }|null}
 */
export function findPageVo(rawVo, sessionKey, targetPageId) {
  const sessionSources = normalizeSessionSources(rawVo)
  for (let sessionIndex = 0; sessionIndex < sessionSources.length; sessionIndex++) {
    const sessionVo = sessionSources[sessionIndex]
    const sessionId = sessionVo.sessionId ? String(sessionVo.sessionId) : `s${sessionIndex}`
    const key = sessionId || `session-${sessionIndex}`
    if (key !== sessionKey && sessionId !== sessionKey) continue
    const pages = Array.isArray(sessionVo.pages) ? sessionVo.pages : []
    for (let i = 0; i < pages.length; i++) {
      if (pageNodeId(pages[i], i, sessionId) === targetPageId) {
        return { page: pages[i], sessionId, pageIndex: i }
      }
    }
  }
  return null
}

/**
 * @param {ReturnType<typeof buildTimelineOverview>} model
 * @param {string} sessionKey
 * @returns {Record<string, unknown>|undefined}
 */
export function findSessionModel(model, sessionKey) {
  if (!model?.sessions?.length) return undefined
  if (!sessionKey) return model.sessions[0]
  return model.sessions.find((s) => s.key === sessionKey) || model.sessions[0]
}

/**
 * 兼容旧调用：等价于 buildTimelineOverview
 * @param {Record<string, unknown>|undefined|null} timelineVo
 */
export function buildTimelineModel(timelineVo) {
  const overview = buildTimelineOverview(timelineVo)
  const primary = overview.sessions[0] || {
    graph: { nodes: [], links: [] },
    flowSteps: [],
    pageIndex: []
  }
  return {
    ...overview,
    graph: primary.graph,
    flowSteps: primary.flowSteps,
    pageIndex: primary.pageIndex,
    eventMap: {}
  }
}
