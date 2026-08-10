import request, { downloadRequest } from '@/utils/request'

/**
 * 慢 SQL 分页列表。
 * @param {Record<string, any>} query 查询参数
 * @returns {Promise<any>}
 */
export function listSlowSql(query) {
  return request({ url: '/monitor/slowSql/list', method: 'get', params: query })
}

/**
 * 慢 SQL 详情。
 * @param {number} slowId 主键
 * @returns {Promise<any>}
 */
export function getSlowSql(slowId) {
  return request({ url: '/monitor/slowSql/' + slowId, method: 'get' })
}

/**
 * 批量删除慢 SQL。
 * @param {number[]} slowIds 主键数组
 * @returns {Promise<any>}
 */
export function removeSlowSql(slowIds) {
  return request({ url: '/monitor/slowSql/remove', method: 'post', data: slowIds })
}

/**
 * 清空慢 SQL。
 * @returns {Promise<any>}
 */
export function cleanSlowSql() {
  return request({ url: '/monitor/slowSql/clean', method: 'post' })
}

/**
 * 导出慢 SQL。
 * @param {Record<string, any>} data 筛选条件
 * @returns {Promise<any>}
 */
export function exportSlowSql(data) {
  return downloadRequest('/monitor/slowSql/export', data, { returnBlobWithHeaders: true })
}
