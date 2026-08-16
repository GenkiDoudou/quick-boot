/**
 * 将原始 span 列表合并为瀑布展示行（fe_api + service → 一条接口），对齐请求链路。
 */

/** 解析 span.attrsJson 为对象，失败时返回空对象或 raw 字段 */
export function parseSpanAttrs(span) {
  if (!span || !span.attrsJson) return {}
  try {
    const o = typeof span.attrsJson === 'string' ? JSON.parse(span.attrsJson) : span.attrsJson
    return o && typeof o === 'object' ? o : {}
  } catch {
    return { raw: span.attrsJson }
  }
}

/** 根据 attrs.kind 或 sourceType 推断 span 业务类型（api/sql/page/action） */
export function resolveSpanKind(span, attrs) {
  if (!span) return ''
  const kind = attrs && attrs.kind
  if (kind === 'api' || kind === 'sql' || kind === 'page' || kind === 'action') return kind
  const t = span.sourceType || ''
  if (t === 'sql') return 'sql'
  if (t === 'fe_api' || t === 'service') return 'api'
  if (t === 'fe_pv') return 'page'
  if (t === 'fe_action') return 'action'
  return ''
}

function normalizeUrlKey(name) {
  return String(name || '')
    .trim()
    .toUpperCase()
    .replace(/\s+/g, ' ')
}

function spanApiKey(span, attrs) {
  const method = attrs.method || ''
  const url = attrs.url || span.spanName || ''
  const name = span.spanName || `${method} ${url}`.trim()
  return normalizeUrlKey(name)
}

/**
 * @param {any[]} rawSpansList
 * @returns {any[]}
 */
export function mergeSpansForWaterfall(rawSpansList) {
  const list = rawSpansList || []
  if (!list.length) return []

  const totalMs = Math.max(
    ...list.map((s) => Number(s.startOffsetMs || 0) + Number(s.durationMs || 0)),
    1
  )

  const apis = list.filter((s) => {
    const attrs = parseSpanAttrs(s)
    const kind = resolveSpanKind(s, attrs)
    return kind === 'api' || s.sourceType === 'fe_api' || s.sourceType === 'service'
  })
  const others = list.filter((s) => {
    const attrs = parseSpanAttrs(s)
    const kind = resolveSpanKind(s, attrs)
    return kind !== 'api' && s.sourceType !== 'fe_api' && s.sourceType !== 'service'
  })

  const byKey = new Map()
  for (const s of apis) {
    const attrs = parseSpanAttrs(s)
    const key = spanApiKey(s, attrs)
    const isFe = s.sourceType === 'fe_api'
    const cur = byKey.get(key) || {
      id: `api-${key}`,
      kind: 'api',
      kindLabel: '接口',
      name: s.spanName || attrs.url || key,
      bar: 'api',
      durationMs: 0,
      start: Number(s.startOffsetMs || 0),
      requestParams: '',
      requestBody: '',
      responsePreview: '',
      bizCode: null,
      bizMsg: '',
      feMs: null,
      beMs: null,
      method: attrs.method || '',
      url: attrs.url || s.spanName || '',
      query: attrs.query || '',
      status: s.statusCode || attrs.status || '—',
      totalMs
    }
    if (isFe) {
      cur.requestParams = attrs.requestParams || cur.requestParams
      cur.requestBody = attrs.requestBody || cur.requestBody
      cur.responsePreview = attrs.responsePreview || cur.responsePreview
      cur.bizCode = attrs.bizCode != null ? attrs.bizCode : cur.bizCode
      cur.bizMsg = attrs.bizMsg || cur.bizMsg
      cur.feMs = Number(s.durationMs || 0)
      cur.method = attrs.method || cur.method
      cur.url = attrs.url || cur.url
      cur.query = attrs.query || cur.query
      cur.status = s.statusCode || attrs.status || cur.status
      if (!cur.durationMs) cur.durationMs = Number(s.durationMs || 0)
      cur.start = Math.min(cur.start, Number(s.startOffsetMs || 0))
    } else {
      cur.beMs = Number(s.durationMs || 0)
      cur.durationMs = Number(s.durationMs || 0)
      cur.start = Number(s.startOffsetMs || 0)
      cur.status = s.statusCode || attrs.status || cur.status
      cur.bar = 'api'
    }
    byKey.set(key, cur)
  }

  for (const cur of byKey.values()) {
    if (!cur.durationMs) {
      cur.durationMs = cur.beMs != null ? cur.beMs : cur.feMs || 0
    }
    cur.totalMs = totalMs
  }

  const mappedOthers = others
    .map((s) => {
      const attrs = parseSpanAttrs(s)
      const kind = resolveSpanKind(s, attrs)
      if (kind === 'page') return null
      return {
        id: String(s.spanId || `${s.sourceType}-${s.spanName}-${s.startOffsetMs}`),
        kind: kind || 'other',
        kindLabel:
          kind === 'sql' ? 'SQL' : kind === 'action' ? '操作' : s.sourceType || 'Span',
        name: kind === 'sql' ? attrs.mapperId || s.spanName || '—' : s.spanName || attrs.url || '—',
        bar: kind === 'sql' ? 'sql' : 'api',
        durationMs: Number(s.durationMs || 0),
        start: Number(s.startOffsetMs || 0),
        sql: attrs.sql || '',
        mapperId: attrs.mapperId || s.spanName || '',
        action: attrs.action || s.spanName || '',
        operationId: attrs.operationId || '',
        totalMs
      }
    })
    .filter(Boolean)

  return [...byKey.values(), ...mappedOthers].sort((a, b) => a.start - b.start)
}

/** 计算瀑布条 left/width 百分比样式 */
export function spanBarStyle(s) {
  const total = Math.max(Number(s.totalMs || 1), 1)
  const start = Number(s.start || 0)
  const dur = Math.max(Number(s.durationMs || 0), 1)
  return {
    left: `${(start / total) * 100}%`,
    width: `${Math.max((dur / total) * 100, 1)}%`
  }
}
