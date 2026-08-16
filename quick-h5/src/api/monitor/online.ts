/**
 * 在线用户 API。
 */
import { request } from '../http'
import type { PageInfo } from '../types'
import { toGetPageQuery } from '../pageQuery'

/** 在线会话 */
export type SysUserOnline = {
  tokenId?: string
  userId?: number | string
  userName?: string
  deptName?: string
  ipaddr?: string
  loginLocation?: string
  browser?: string
  os?: string
  loginTime?: string
}

/** 分页列表 */
export function listOnline(query: {
  pageNum: number
  pageSize: number
  userName?: string
  ipaddr?: string
}) {
  return request<PageInfo<SysUserOnline>>({
    url: '/monitor/online/list',
    method: 'GET',
    data: query,
  })
}

export function pageOnline(current: number, size: number, userName?: string) {
  return listOnline({ ...toGetPageQuery(current, size), userName: userName || undefined })
}

/** 强退 */
export function forceLogout(tokenId: string) {
  return request<void>({
    url: '/monitor/online/forceLogout',
    method: 'POST',
    data: { tokenId },
  })
}
