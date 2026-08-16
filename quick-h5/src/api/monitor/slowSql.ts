/**
 * 慢 SQL 日志 API。
 */
import { request } from '../http'
import type { PageInfo } from '../types'
import { toGetPageQuery } from '../pageQuery'

/** 慢 SQL 记录 */
export type SysSlowSql = {
  slowId?: number | string
  sqlSource?: string
  sqlType?: string
  mapperId?: string
  sqlText?: string
  costTime?: number
  traceId?: string
  requestUri?: string
  operName?: string
  createTime?: string
}

/** 分页列表 */
export function listSlowSql(query: {
  pageNum: number
  pageSize: number
  operName?: string
  requestUri?: string
}) {
  return request<PageInfo<SysSlowSql>>({
    url: '/monitor/slowSql/list',
    method: 'GET',
    data: query,
  })
}

export function pageSlowSql(current: number, size: number, keyword?: string) {
  return listSlowSql({
    ...toGetPageQuery(current, size),
    requestUri: keyword || undefined,
  })
}

/** 详情 */
export function getSlowSql(slowId: number | string) {
  return request<SysSlowSql>({
    url: `/monitor/slowSql/${encodeURIComponent(String(slowId))}`,
    method: 'GET',
  })
}

/** 批量删除 */
export function removeSlowSql(ids: Array<number | string>) {
  return request<void>({
    url: '/monitor/slowSql/remove',
    method: 'POST',
    data: ids,
  })
}
