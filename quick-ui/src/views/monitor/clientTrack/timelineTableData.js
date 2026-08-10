/**
 * 行为轨迹页表格视图行数据（与 buildTimelineOverview / buildPageDetailModel 配套）。
 */

/**
 * @param {Record<string, unknown>|null|undefined} sessionModel
 * @returns {Record<string, unknown>[]}
 */
export function buildFlowEdgeTableRows(sessionModel) {
  const nodes = sessionModel?.graph?.nodes || []
  const links = sessionModel?.graph?.links || []
  const nameById = new Map(nodes.map((n) => [n.id, n.name || n.label || '']))
  const pathById = new Map(nodes.map((n) => [n.id, n.pagePath || '']))

  if (links.length > 0) {
    return links.map((link, index) => ({
      step: index + 1,
      fromLabel: nameById.get(link.source) || String(link.source || '—'),
      toLabel: nameById.get(link.target) || String(link.target || '—'),
      toPagePath: pathById.get(link.target) || '',
      jumpLabel: link.value ? String(link.value) : '页面跳转',
      targetPageId: link.target || ''
    }))
  }

  return nodes.map((node, index) => ({
    step: index + 1,
    fromLabel: index > 0 ? nameById.get(nodes[index - 1]?.id) || '—' : '—',
    toLabel: node.name || node.label || '未知页面',
    toPagePath: node.pagePath || '',
    jumpLabel: index > 0 ? '顺序访问' : '入口页',
    targetPageId: node.id || ''
  }))
}

/**
 * @param {Record<string, unknown>[]|undefined|null} pageIndex
 * @returns {Record<string, unknown>[]}
 */
export function buildPageNavTableRows(pageIndex) {
  if (!Array.isArray(pageIndex)) return []
  return pageIndex.map((item) => ({
    id: item.id,
    step: item.step,
    label: item.label || '',
    pagePath: item.pagePath || '',
    actionCount: item.actionCount ?? 0,
    eventCount: item.eventCount ?? 0
  }))
}

/**
 * @param {ReturnType<import('./buildTimelineModel.js').buildPageDetailModel>|null|undefined} pageDetailModel
 * @returns {Record<string, unknown>[]}
 */
export function buildDetailEventTableRows(pageDetailModel) {
  const groups = pageDetailModel?.batchGroups
  if (!Array.isArray(groups)) return []

  /** @type {Record<string, unknown>[]} */
  const rows = []
  groups.forEach((group) => {
    const batchType = group.isVisit ? '访问' : '操作'
    const events = Array.isArray(group.events) ? group.events : []
    if (events.length === 0) {
      rows.push({
        batchLabel: group.label || '',
        batchType,
        eventName: '（无事件）',
        eventType: '—',
        nodeId: ''
      })
      return
    }
    events.forEach((ev) => {
      rows.push({
        batchLabel: group.label || '',
        batchType,
        eventName: ev.name || '',
        eventType: ev.eventType || '—',
        nodeId: ev.nodeId || ''
      })
    })
  })
  return rows
}
