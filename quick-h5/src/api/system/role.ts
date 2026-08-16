/**
 * 系统角色管理 API，对接 /sys/role 系列接口。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'

/** 角色实体 */
export type SysRole = {
  roleId?: number | string
  roleName?: string
  roleKey?: string
  roleSort?: number
  status?: string
  remark?: string
  dataScope?: string
}

/** 分页查询角色 */
export function pageRole(pageRequest: PageRequest<Partial<SysRole>>) {
  return request<PageInfo<SysRole>>({
    url: '/sys/role/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 按 roleId 查询角色详情 */
export function getRole(roleId: number | string) {
  return request<SysRole>({
    url: `/sys/role/${encodeURIComponent(String(roleId))}`,
    method: 'GET',
  })
}

/** 新增角色 */
export function addRole(data: Partial<SysRole>) {
  return request<string | number>({
    url: '/sys/role/add',
    method: 'POST',
    data,
  })
}

/** 更新角色 */
export function updateRole(data: Partial<SysRole>) {
  return request<boolean>({
    url: '/sys/role/update',
    method: 'POST',
    data,
  })
}

/** 启用/停用角色 */
export function changeRoleStatus(data: { roleId: number | string; status: string }) {
  return request<void>({
    url: '/sys/role/changeStatus',
    method: 'POST',
    data,
  })
}

/** 批量删除角色；请求体为 roleId 数组 */
export function removeRole(roleIds: Array<number | string>) {
  return request<void>({
    url: '/sys/role/remove',
    method: 'POST',
    data: roleIds.map(String),
  })
}
