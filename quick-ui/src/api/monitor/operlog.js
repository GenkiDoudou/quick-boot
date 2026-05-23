import request, { downloadRequest } from '@/utils/request'

/**
 * 操作日志分页列表。
 * @param {Record<string, any>} query 查询参数（含 pageNum、pageSize）
 * @returns {Promise<any>}
 */
export function listOperlog(query) {
  return request({ url: '/monitor/operlog/list', method: 'get', params: query })
}

/**
 * 操作日志详情。
 * @param {number} operId 主键
 * @returns {Promise<any>}
 */
export function getOperlog(operId) {
  return request({ url: '/monitor/operlog/' + operId, method: 'get' })
}

/**
 * 批量删除操作日志。
 * @param {number[]} operIds 主键数组
 * @returns {Promise<any>}
 */
export function removeOperlog(operIds) {
  return request({ url: '/monitor/operlog/remove', method: 'post', data: operIds })
}

/**
 * 清空操作日志。
 * @returns {Promise<any>}
 */
export function cleanOperlog() {
  return request({ url: '/monitor/operlog/clean', method: 'post' })
}

/**
 * 导出操作日志（Blob + Content-Disposition）。
 * @param {Record<string, any>} data 筛选条件（与列表一致，无分页）
 * @returns {Promise<any>}
 */
export function exportOperlog(data) {
  return downloadRequest('/monitor/operlog/export', data, { returnBlobWithHeaders: true })
}
