import request from '@/utils/request'

/**
 * 前端用户行为监控批次上报（一般由全局插件自动调用，业务侧无需手动调用）。
 *
 * @param {{ reason: string, events: Record<string, unknown>[] }} data
 * @returns {Promise<import('@/utils/request').default>}
 */
export function reportClientTrack(data) {
  return request({
    url: '/monitor/clientTrack/report',
    method: 'post',
    data
  })
}

/**
 * 管理端：分页查询监控批次。
 *
 * @param {Record<string, unknown>} query pageNum/pageSize/batchId/browserVisitId/sessionId/pageVisitId/operationId/traceId/userName/menuName/pagePath/triggerAction/reason/beginDate/endDate
 * @returns {Promise<{ rows: Record<string, unknown>[], total: number }>}
 */
export function listClientTrack(query) {
  return request({
    url: '/monitor/clientTrack/list',
    method: 'get',
    params: query
  })
}

/**
 * 管理端：行为轨迹概览（页面跳转链，不含事件明细，最多 500 批）。
 *
 * @param {{ browserVisitId?: string, sessionId?: string, userName?: string, beginDate?: string, endDate?: string }} query
 * @returns {Promise<{ data: Record<string, unknown> }>}
 */
export function getClientTrackTimeline(query) {
  return request({
    url: '/monitor/clientTrack/timeline',
    method: 'get',
    params: query
  })
}

/**
 * 管理端：行为轨迹单页明细（操作批与事件，点击页面后懒加载）。
 *
 * @param {{ browserVisitId?: string, sessionId?: string, userName?: string, beginDate?: string, endDate?: string, pageVisitId?: string, pagePath?: string }} query
 * @returns {Promise<{ data: Record<string, unknown> }>}
 */
export function getClientTrackTimelinePage(query) {
  return request({
    url: '/monitor/clientTrack/timeline/page',
    method: 'get',
    params: query
  })
}

/**
 * 管理端：批量删除监控批次。
 *
 * @param {number[]} batchIds
 * @returns {Promise<void>}
 */
export function removeClientTrack(batchIds) {
  return request({
    url: '/monitor/clientTrack/remove',
    method: 'post',
    data: batchIds
  })
}

/**
 * 管理端：清空全部监控批次（不可恢复）。
 *
 * @returns {Promise<void>}
 */
export function cleanClientTrack() {
  return request({
    url: '/monitor/clientTrack/clean',
    method: 'post'
  })
}
