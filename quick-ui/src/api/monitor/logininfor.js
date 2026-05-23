import request, { downloadRequest } from '@/utils/request'

/**
 * 登录日志分页列表。
 * @param {Record<string, any>} query 查询参数（含 pageNum、pageSize）
 * @returns {Promise<any>}
 */
export function listLogininfor(query) {
  return request({ url: '/monitor/logininfor/list', method: 'get', params: query })
}

/**
 * 批量删除登录日志。
 * @param {number[]} infoIds 主键数组
 * @returns {Promise<any>}
 */
export function removeLogininfor(infoIds) {
  return request({ url: '/monitor/logininfor/remove', method: 'post', data: infoIds })
}

/**
 * 清空登录日志。
 * @returns {Promise<any>}
 */
export function cleanLogininfor() {
  return request({ url: '/monitor/logininfor/clean', method: 'post' })
}

/**
 * 解锁用户（清除登录失败锁定缓存）。
 * @param {string} userName 登录名
 * @returns {Promise<any>}
 */
export function unlockLogininfor(userName) {
  return request({
    url: '/monitor/logininfor/unlock/' + encodeURIComponent(userName),
    method: 'get',
  })
}

/**
 * 导出登录日志（Blob + Content-Disposition）。
 * @param {Record<string, any>} data 筛选条件（与列表一致，无分页）
 * @returns {Promise<any>}
 */
export function exportLogininfor(data) {
  return downloadRequest('/monitor/logininfor/export', data, { returnBlobWithHeaders: true })
}
