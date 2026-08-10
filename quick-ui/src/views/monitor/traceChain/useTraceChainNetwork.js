/**
 * 将后端 TraceChainGraph 转为 Network 表行（对齐原型 v3）。
 */

/**
 * @param {number} startMs
 * @param {number} endMs
 * @param {number} maxMs
 */
export function waterfallStyle(startMs, endMs, maxMs) {
  const leftPct = (startMs / maxMs) * 100
  const widthPct = Math.max(((endMs - startMs) / maxMs) * 100, 0.8)
  return {
    startMs,
    leftPct,
    widthPct: Math.min(widthPct, 100 - leftPct),
  }
}

/**
 * @param {Record<string, unknown>} graph
 * @param {string[]} filters nav|behavior|xhr|log|sql|slow
 */
export function buildNetworkRows(graph, filters) {
  if (!graph) return []
  const max = graph.timelineMaxMs || 1000
  const onlySlow = filters.includes('slow')
  const rows = []

  if (filters.includes('nav')) {
    ;(graph.pageJumps || []).forEach((j) => {
      rows.push(mapJumpRow(j, max))
    })
  }

  if (filters.includes('behavior')) {
    ;(graph.behaviorByPage || []).forEach((page) => {
      ;(page.events || []).forEach((ev) => {
        rows.push(mapBehaviorRow(ev, page, max))
      })
    })
  }

  const showXhr = filters.includes('xhr')
  const showLog = filters.includes('log')
  const showSql = filters.includes('sql')

  ;(graph.backendNodes || []).forEach((n) => {
    if (n.type === 'api' && !showXhr) return
    if (n.type === 'oper_log' && !showLog) return
    if (n.type === 'slow_sql' && !showSql) return
    if (onlySlow && n.type === 'api' && n.status !== 'warn') return
    if (onlySlow && n.type === 'slow_sql' && n.status !== 'warn') return
    rows.push(mapBackendRow(n, max))
  })

  return rows.sort((a, b) => (a.startMs || 0) - (b.startMs || 0))
}

/**
 * @param {Record<string, unknown>} j
 * @param {number} maxMs
 */
function mapJumpRow(j, maxMs) {
  const startMs = j.atMs || 0
  const endMs = startMs + 30
  return {
    key: 'jump-' + j.step,
    id: 'jump-' + j.step,
    section: 'nav',
    kind: 'nav',
    name: (j.fromLabel || '') + ' → ' + (j.toLabel || ''),
    sub: j.jumpLabel || j.pageVisitId || '',
    typeText: '跳转',
    typeTag: 'info',
    statusText: '—',
    statusTag: 'info',
    durationMs: 0,
    startMs,
    endMs,
    expandable: false,
    isChild: false,
    parentApiId: '',
    node: { ...j, type: 'page_jump', startMs, endMs, status: 'ok' },
    waterfall: waterfallStyle(startMs, endMs, maxMs),
    wfKind: 'nav',
  }
}

/**
 * @param {Record<string, unknown>} ev
 * @param {Record<string, unknown>} page
 * @param {number} maxMs
 */
function mapBehaviorRow(ev, page, maxMs) {
  const typeTextMap = {
    route_enter: '路由',
    click: '点击',
    api_call: 'xhr',
    api_slow: 'xhr',
    api_error: 'xhr',
  }
  const statusTag = ev.status === 'warn' ? 'warning' : ev.status === 'error' ? 'danger' : 'success'
  return {
    key: ev.id,
    id: ev.id,
    section: 'behavior',
    kind: 'behavior',
    name: '[' + (page.menuName || page.pagePath) + '] ' + (ev.label || ''),
    sub: ev.operationId || ev.traceId || ev.pageVisitId || '',
    typeText: typeTextMap[ev.type] || ev.type,
    typeTag: ev.type === 'click' ? 'primary' : 'info',
    statusText: ev.status === 'warn' ? '慢' : ev.status === 'error' ? '错' : 'OK',
    statusTag,
    durationMs: Math.max(0, (ev.endMs || 0) - (ev.startMs || 0)),
    startMs: ev.startMs || 0,
    endMs: ev.endMs || 0,
    expandable: false,
    isChild: false,
    parentApiId: '',
    node: ev,
    waterfall: waterfallStyle(ev.startMs || 0, ev.endMs || 0, maxMs),
    wfKind: 'behavior',
  }
}

/**
 * @param {Record<string, unknown>} n
 * @param {number} maxMs
 */
function mapBackendRow(n, maxMs) {
  const kind = n.type === 'api' ? 'api' : n.type === 'oper_log' ? 'log' : 'sql'
  const wfKind =
    n.type === 'slow_sql' ? 'sql' : n.type === 'oper_log' ? 'log' : n.status === 'warn' ? 'warn' : n.status === 'error' ? 'err' : 'ok'
  const typeText = n.type === 'api' ? 'xhr' : n.type === 'oper_log' ? 'log' : 'sql'
  const statusText =
    n.type === 'api' ? String(n.httpStatus || '') : n.status === 'warn' ? '慢' : n.status === 'error' ? '失败' : 'OK'
  const statusTag = n.status === 'warn' ? 'warning' : n.status === 'error' ? 'danger' : 'success'
  const parentApiId = n.parentApiId || ''
  return {
    key: n.id,
    id: n.id,
    section: 'backend',
    kind,
    name: n.label || '',
    sub: n.traceId || '',
    typeText,
    typeTag: n.type === 'slow_sql' ? 'danger' : '',
    statusText,
    statusTag,
    durationMs: Math.max(0, (n.endMs || 0) - (n.startMs || 0)),
    startMs: n.startMs || 0,
    endMs: n.endMs || 0,
    expandable: n.type === 'api',
    isChild: n.type !== 'api',
    parentApiId,
    node: n,
    waterfall: waterfallStyle(n.startMs || 0, n.endMs || 0, maxMs),
    wfKind,
  }
}

/**
 * @param {Array<Record<string, unknown>>} flatRows
 * @param {boolean} expandAll
 * @param {Record<string, boolean>} expandedApis
 */
export function filterVisibleRows(flatRows, expandAll, expandedApis) {
  const out = []
  flatRows.forEach((row) => {
    if (row.isChild) {
      const pid = row.parentApiId
      if (expandAll || expandedApis[pid]) out.push(row)
      return
    }
    out.push(row)
  })
  return out
}
