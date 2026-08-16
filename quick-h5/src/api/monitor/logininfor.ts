/**
 * 登录日志 API。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'

/** 登录日志行 */
export type SysLogininfor = {
  infoId?: number | string
  userName?: string
  ipaddr?: string
  loginLocation?: string
  browser?: string
  os?: string
  status?: string
  msg?: string
  loginTime?: string
}

/** 分页 */
export function pageLogininfor(pageRequest: PageRequest<Partial<SysLogininfor>>) {
  return request<PageInfo<SysLogininfor>>({
    url: '/monitor/logininfor/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 批量删除 */
export function removeLogininfor(ids: Array<number | string>) {
  return request<void>({
    url: '/monitor/logininfor/remove',
    method: 'POST',
    data: ids.map(String),
  })
}

/** 清空 */
export function cleanLogininfor() {
  return request<void>({
    url: '/monitor/logininfor/clean',
    method: 'POST',
  })
}

/** 解锁用户 */
export function unlockLogininfor(userName: string) {
  return request<void>({
    url: `/monitor/logininfor/unlock/${encodeURIComponent(userName)}`,
    method: 'GET',
  })
}
