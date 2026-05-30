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
 * @param {Record<string, unknown>} query pageNum/pageSize/traceId/userName/reason/beginDate/endDate
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
