/**
 * 慢 SQL 监控 API（前缀 {@code /monitor/slowSql}）。
 */
import request, { downloadRequest } from '@/utils/request'
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'

const crud = createCrudApi('/monitor/slowSql')

/** 慢 SQL 分页（POST page）。 */
export const pageSlowSql = crud.page

/**
 * 慢 SQL 分页列表（兼容 C7JsonTable）。
 * @param {Record<string, any>} query 查询参数
 * @returns {Promise<any>}
 */
export function listSlowSql(query) {
  return crud.page(toPageRequest(query))
}

/** 慢 SQL 详情。 */
export const getSlowSql = crud.get
/** 批量删除慢 SQL。 */
export const removeSlowSql = crud.remove

/** 清空慢 SQL。 */
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
