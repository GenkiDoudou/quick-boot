/**
 * OAuth2 客户端管理 API，对接 `/sys/oauthclient`。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'

/** OAuth 客户端 */
export type SysOauthClient = {
  id?: number | string
  clientId?: string
  clientSecret?: string
  clientName?: string
  apiPathPatterns?: string
  tokenTimeout?: number | null
  /** sys_yes_no：是否校验验证码 */
  checkCaptcha?: string
  status?: string
  remark?: string
}

/** 分页查询客户端 */
export function pageOauthClient(pageRequest: PageRequest<Partial<SysOauthClient>>) {
  return request<PageInfo<SysOauthClient>>({
    url: '/sys/oauthclient/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 详情（含 secret）；路径参数为管理端主键 id */
export function getOauthClient(id: number | string) {
  return request<SysOauthClient>({
    url: `/sys/oauthclient/${encodeURIComponent(String(id))}`,
    method: 'GET',
  })
}

/** 新增客户端 */
export function addOauthClient(data: Partial<SysOauthClient>) {
  return request<string | number>({
    url: '/sys/oauthclient/add',
    method: 'POST',
    data,
  })
}

/** 修改客户端（不变更 secret / clientId） */
export function updateOauthClient(data: Partial<SysOauthClient>) {
  return request<boolean>({
    url: '/sys/oauthclient/update',
    method: 'POST',
    data,
  })
}

/** 批量删除客户端；请求体为管理端主键 id 数组 */
export function removeOauthClient(ids: Array<number | string>) {
  return request<void>({
    url: '/sys/oauthclient/remove',
    method: 'POST',
    data: ids.map(String),
  })
}
