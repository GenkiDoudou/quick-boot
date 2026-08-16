/**
 * 系统参数配置 API，对接 `/sys/config`。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'

/** 参数实体 */
export type SysConfig = {
  configId?: number | string
  configName?: string
  configKey?: string
  configValue?: string
  /** 系统内置(sys_yes_no)：0=否，1=是 */
  configType?: string
  remark?: string
}

/** 分页查询参数 */
export function pageConfig(pageRequest: PageRequest<Partial<SysConfig>>) {
  return request<PageInfo<SysConfig>>({
    url: '/sys/config/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 参数详情 */
export function getConfig(configId: number | string) {
  return request<SysConfig>({
    url: `/sys/config/${encodeURIComponent(String(configId))}`,
    method: 'GET',
  })
}

/** 新增参数 */
export function addConfig(data: Partial<SysConfig>) {
  return request<string | number>({
    url: '/sys/config/add',
    method: 'POST',
    data,
  })
}

/** 修改参数 */
export function updateConfig(data: Partial<SysConfig>) {
  return request<boolean>({
    url: '/sys/config/update',
    method: 'POST',
    data,
  })
}

/** 批量删除；请求体为 configId 数组 */
export function removeConfig(ids: Array<number | string>) {
  return request<void>({
    url: '/sys/config/remove',
    method: 'POST',
    data: ids.map(String),
  })
}

/** 刷新参数缓存 */
export function refreshConfigCache() {
  return request<void>({
    url: '/sys/config/refreshCache',
    method: 'POST',
  })
}
