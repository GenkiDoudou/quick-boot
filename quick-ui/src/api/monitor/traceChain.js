import request from '@/utils/request'

/**
 * 全链路监控：聚合页面跳转、行为明细、HTTP、操作日志、慢 SQL。
 *
 * @param {Record<string, unknown>} query operationId / traceId / batchId / pageVisitId / browserVisitId / sessionId / userName / beginDate / endDate
 * @returns {Promise<{ data: import('./traceChain.types').TraceChainGraph }>}
 */
export function getTraceChainGraph(query) {
  return request({
    url: '/monitor/traceChain/graph',
    method: 'get',
    params: query
  })
}
