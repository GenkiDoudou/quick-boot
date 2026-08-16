/**
 * Lite Trace 请求链路 API：索引、页面访问、Span 明细。
 */
import request from '@/utils/request'

/** 链路索引分页列表（接口 / 定时任务等 trace 入口） */
export function listLiteTraceIndex(query) {
  return request({
    url: '/monitor/liteTrace/index/list',
    method: 'get',
    params: query
  })
}

/** 页面访问（PV）分页列表 */
export function listLiteTracePageVisits(query) {
  return request({
    url: '/monitor/liteTrace/pageVisit/list',
    method: 'get',
    params: query
  })
}

/** 某次页面访问关联的 trace 列表 */
export function listLiteTraceByPageVisit(pageVisitId, query) {
  return request({
    url: `/monitor/liteTrace/pageVisit/${encodeURIComponent(pageVisitId)}/traces`,
    method: 'get',
    params: query
  })
}

/** 链路索引详情（根 trace 元数据） */
export function getLiteTraceIndex(traceId) {
  return request({
    url: `/monitor/liteTrace/index/${encodeURIComponent(traceId)}`,
    method: 'get'
  })
}

/** 指定 traceId 下的 Span 列表（瀑布图数据源） */
export function listLiteTraceSpans(traceId) {
  return request({
    url: `/monitor/liteTrace/spans/${encodeURIComponent(traceId)}`,
    method: 'get'
  })
}

/** 确保存在根 trace 索引（前端/SDK 补录场景） */
export function ensureLiteTraceRoot(params) {
  return request({
    url: '/monitor/liteTrace/index/ensureRoot',
    method: 'post',
    params
  })
}
