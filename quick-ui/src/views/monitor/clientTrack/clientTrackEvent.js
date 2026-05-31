import { formatTrackLabel } from '@/monitor/trackLabel'

/**
 * 上报原因静态选项（与后端 reason 字段一致）。
 */
export const reasonOptions = [
  { label: '普通', value: 'normal' },
  { label: '错误', value: 'error' },
  { label: '离开', value: 'leave' },
  { label: '定时', value: 'timer' },
  { label: '空闲', value: 'idle' },
  { label: '页面离开', value: 'page_leave' },
  { label: '页面操作', value: 'page_action' },
  { label: '查询点击', value: 'action_click' },
  { label: '操作结束', value: 'operation_end' }
]

/**
 * @param {string | undefined} reason
 * @returns {string}
 */
export function reasonLabel(reason) {
  const hit = reasonOptions.find((o) => o.value === reason)
  return hit ? hit.label : reason || '—'
}

/**
 * @param {string | undefined} reason
 * @returns {'success'|'danger'|'info'|'warning'|''}
 */
export function reasonTagType(reason) {
  if (reason === 'error') return 'danger'
  if (reason === 'leave' || reason === 'page_leave') return 'info'
  if (reason === 'timer') return 'warning'
  if (reason === 'idle' || reason === 'page_action' || reason === 'operation_end' || reason === 'normal') return 'success'
  if (reason === 'action_click') return ''
  return ''
}

/**
 * @param {string | undefined} type
 * @returns {string}
 */
export function eventTypeLabel(type) {
  const map = {
    click: '点击',
    api_call: 'API',
    api_slow: '慢API',
    api_error: 'API错误',
    route_enter: '进入页面',
    route_leave: '离开页面',
    js_error: 'JS错误',
    promise_error: 'Promise错误'
  }
  return map[type] || type || '—'
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {string}
 */
export function eventTriggerLabel(ev) {
  if (ev.trigger) {
    return formatTrackLabel(String(ev.trigger))
  }
  if (ev.type === 'click' && ev.target) {
    const t = String(ev.target)
    if (!/^(BUTTON|A|SPAN|DIV|INPUT|I|SVG)$/i.test(t)) {
      return formatTrackLabel(t)
    }
  }
  return ''
}

/**
 * 时间轴节点主标题。
 * @param {Record<string, unknown>} ev
 * @returns {string}
 */
export function eventHeadline(ev) {
  const type = String(ev.type || '')
  if (type === 'click') {
    return eventTriggerLabel(ev) || (ev.target ? `点击「${ev.target}」` : '点击')
  }
  if (type === 'api_call' || type === 'api_slow' || type === 'api_error') {
    const method = ev.method ? String(ev.method).toUpperCase() : 'GET'
    const url = ev.url ? String(ev.url) : '—'
    if (type === 'api_error' && ev.msg) {
      return `${method} ${url} · ${ev.msg}`
    }
    return `${method} ${url}`
  }
  if (type === 'route_enter') {
    return ev.path ? `进入 ${ev.path}` : '进入页面'
  }
  if (type === 'route_leave') {
    const from = ev.from ? String(ev.from) : '—'
    const to = ev.to ? String(ev.to) : '—'
    return `${from} → ${to}`
  }
  if (type === 'js_error' || type === 'promise_error') {
    return ev.msg ? String(ev.msg) : '运行时错误'
  }
  return type || '—'
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {Array<{ label: string, value: string, copyable?: boolean }>}
 */
export function eventDetailItems(ev) {
  /** @type {Array<{ label: string, value: string, copyable?: boolean }>} */
  const items = []
  const trigger = eventTriggerLabel(ev)
  if (trigger && ev.type !== 'click') {
    items.push({ label: '触发操作', value: trigger })
  }
  if (ev.type === 'click' && ev.target && !trigger) {
    items.push({ label: '点击目标', value: String(ev.target) })
  }
  if (ev.page) {
    items.push({ label: '页面', value: String(ev.page) })
  }
  if (ev.operationId) {
    items.push({ label: 'operationId', value: String(ev.operationId), copyable: true })
  }
  if (ev.sessionId) {
    items.push({ label: 'sessionId', value: String(ev.sessionId), copyable: true })
  }
  if (ev.browserVisitId) {
    items.push({ label: 'browserVisitId', value: String(ev.browserVisitId), copyable: true })
  }
  if (ev.pageVisitId) {
    items.push({ label: 'pageVisitId', value: String(ev.pageVisitId), copyable: true })
  }
  if (ev.serverTraceId) {
    items.push({ label: 'serverTraceId', value: String(ev.serverTraceId), copyable: true, linkOperLog: true })
  } else if (ev.clientTraceId) {
    items.push({ label: 'clientTraceId', value: String(ev.clientTraceId), copyable: true })
  }
  if (ev.responseTraceId && ev.responseTraceId !== ev.serverTraceId) {
    items.push({ label: 'responseTraceId', value: String(ev.responseTraceId), copyable: true })
  }
  if (ev.httpStatus != null) {
    items.push({ label: 'HTTP', value: String(ev.httpStatus) })
  }
  if (ev.bizCode != null) {
    items.push({ label: '业务码', value: String(ev.bizCode) })
  }
  return items
}

/**
 * @param {Record<string, unknown>} ev
 * @param {number} idx
 * @returns {string}
 */
export function formatEventTs(ev, idx) {
  const ts = ev.ts
  if (ts == null || ts === '') {
    return `#${idx + 1}`
  }
  const n = Number(ts)
  if (!Number.isFinite(n)) {
    return String(ts)
  }
  const d = new Date(n)
  const pad = (v) => String(v).padStart(2, '0')
  const ms = pad(d.getMilliseconds()).padStart(3, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${ms}`
}

/**
 * @param {string | undefined} type
 * @returns {'success'|'warning'|'danger'|'info'|'primary'|''}
 */
export function eventTagType(type) {
  if (type === 'click') return 'primary'
  if (type === 'api_call') return 'success'
  if (type === 'api_slow') return 'warning'
  if (type === 'api_error' || type === 'js_error' || type === 'promise_error') return 'danger'
  if (type === 'route_enter' || type === 'route_leave') return 'info'
  return ''
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {'primary'|'success'|'warning'|'danger'|'info'|undefined}
 */
export function timelineNodeType(ev) {
  const type = ev.type
  if (type === 'click') return 'primary'
  if (type === 'api_call') return 'success'
  if (type === 'api_slow') return 'warning'
  if (type === 'api_error' || type === 'js_error' || type === 'promise_error') return 'danger'
  return undefined
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {string|undefined}
 */
export function timelineNodeColor(ev) {
  if (ev.type === 'route_enter' || ev.type === 'route_leave') {
    return '#909399'
  }
  return undefined
}

/**
 * 解析批次 eventsJson。
 * @param {string|undefined|null} raw
 * @returns {Record<string, unknown>[]}
 */
export function parseEventsJson(raw) {
  if (!raw) {
    return []
  }
  try {
    const arr = JSON.parse(String(raw))
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

/**
 * 将批次列表展开为扁平事件行（按批次入库时间、事件顺序排列）。
 * @param {Record<string, unknown>[]} batches
 * @returns {Record<string, unknown>[]}
 */
export function flattenBatchEvents(batches) {
  if (!Array.isArray(batches) || !batches.length) {
    return []
  }
  /** @type {Record<string, unknown>[]} */
  const rows = []
  for (const batch of batches) {
    const events = parseEventsJson(batch.eventsJson)
    events.forEach((ev, idx) => {
      rows.push({
        rowKey: `${batch.batchId}-${idx}`,
        batchId: batch.batchId,
        batchTriggerAction: batch.triggerAction,
        batchReason: batch.reason,
        batchCreateTime: batch.createTime,
        sessionId: ev.sessionId || batch.sessionId,
        browserVisitId: ev.browserVisitId || batch.browserVisitId,
        pageVisitId: ev.pageVisitId || batch.pageVisitId,
        operationId: ev.operationId || batch.operationId,
        traceId: batch.traceId,
        userName: batch.userName,
        menuName: batch.menuName,
        menuBreadcrumb: batch.menuBreadcrumb,
        pagePath: batch.pagePath,
        eventIndex: idx + 1,
        eventType: ev.type,
        eventTs: ev.ts,
        eventHeadline: eventHeadline(ev),
        eventDetailItems: eventDetailItems(ev),
        eventTagType: eventTagType(ev.type),
        eventRaw: ev
      })
    })
  }
  return rows
}

/**
 * 将日期范围转为后端 beginDate/endDate。
 * @param {Record<string, unknown>} raw
 */
export function normalizeListParams(raw) {
  const p = { ...raw }
  const range = p.createTimeRange
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p.beginDate = range[0]
    p.endDate = range[1]
  }
  delete p.createTimeRange
  ;['reason', 'browserVisitId', 'sessionId', 'pageVisitId', 'operationId', 'traceId', 'userName', 'menuName', 'pagePath', 'triggerAction', 'eventType'].forEach((key) => {
    if (p[key] === '' || p[key] == null) delete p[key]
  })
  return p
}

/** 批次/事件链路共用默认搜索参数 */
export const defaultSearchParam = {
  browserVisitId: '',
  sessionId: '',
  pageVisitId: '',
  operationId: '',
  traceId: '',
  userName: '',
  menuName: '',
  pagePath: '',
  triggerAction: '',
  reason: '',
  eventType: '',
  createTimeRange: []
}

/** 共用搜索表单项 */
export const searchColumns = [
  { prop: 'browserVisitId', label: 'browserVisitId', type: 'input', span: 8, props: { placeholder: '精确匹配，串联同次浏览器访问', clearable: true } },
  { prop: 'sessionId', label: 'sessionId', type: 'input', span: 8, props: { placeholder: '精确匹配，串联同一次登录', clearable: true } },
  { prop: 'pageVisitId', label: 'pageVisitId', type: 'input', span: 8, props: { placeholder: '精确匹配', clearable: true } },
  { prop: 'operationId', label: 'operationId', type: 'input', span: 8, props: { placeholder: '精确匹配', clearable: true } },
  { prop: 'traceId', label: 'serverTraceId', type: 'input', span: 8, props: { placeholder: '精确匹配', clearable: true } },
  { prop: 'userName', label: '用户名', type: 'input', span: 8, props: { placeholder: '模糊匹配', clearable: true } },
  { prop: 'menuName', label: '所属菜单', type: 'input', span: 8, props: { placeholder: '菜单名/面包屑模糊', clearable: true } },
  { prop: 'pagePath', label: '页面路径', type: 'input', span: 8, props: { placeholder: '路径模糊', clearable: true } },
  { prop: 'triggerAction', label: '触发操作', type: 'input', span: 8, props: { placeholder: '如 修改 / 访问:用户管理', clearable: true } },
  {
    prop: 'reason',
    label: '上报原因',
    type: 'select',
    span: 8,
    options: reasonOptions,
    props: { placeholder: '全部', clearable: true, style: 'width: 240px' }
  },
  {
    prop: 'createTimeRange',
    label: '入库时间',
    type: 'daterange',
    span: 16,
    props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' }
  }
]

/** 事件链路 Tab 额外搜索：事件类型 */
export const eventSearchColumns = [
  ...searchColumns,
  {
    prop: 'eventType',
    label: '事件类型',
    type: 'select',
    span: 8,
    options: [
      { label: '点击', value: 'click' },
      { label: 'API', value: 'api_call' },
      { label: '慢API', value: 'api_slow' },
      { label: 'API错误', value: 'api_error' },
      { label: '进入页面', value: 'route_enter' },
      { label: '离开页面', value: 'route_leave' },
      { label: 'JS错误', value: 'js_error' },
      { label: 'Promise错误', value: 'promise_error' }
    ],
    props: { placeholder: '全部', clearable: true, style: 'width: 240px' }
  }
]
