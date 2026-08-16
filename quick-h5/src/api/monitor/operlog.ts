/**
 * 操作日志 API。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'

/** 操作日志 */
export type SysOperlog = {
  operId?: number | string
  title?: string
  businessType?: number | string
  method?: string
  requestMethod?: string
  operatorType?: number | string
  operName?: string
  deptName?: string
  operUrl?: string
  operIp?: string
  operLocation?: string
  operParam?: string
  jsonResult?: string
  status?: number | string
  errorMsg?: string
  operTime?: string
  costTime?: number
}

/** 分页 */
export function pageOperlog(pageRequest: PageRequest<Partial<SysOperlog>>) {
  return request<PageInfo<SysOperlog>>({
    url: '/monitor/operlog/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 详情 */
export function getOperlog(operId: number | string) {
  return request<SysOperlog>({
    url: `/monitor/operlog/${encodeURIComponent(String(operId))}`,
    method: 'GET',
  })
}

/** 批量删除 */
export function removeOperlog(ids: Array<number | string>) {
  return request<void>({
    url: '/monitor/operlog/remove',
    method: 'POST',
    data: ids.map(String),
  })
}

/** 清空 */
export function cleanOperlog() {
  return request<void>({
    url: '/monitor/operlog/clean',
    method: 'POST',
  })
}
