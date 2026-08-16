/**
 * 系统用户管理 API，对接 /sys/user 系列接口。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'

export type { PageInfo, PageRequest }

/** 用户实体（列表/详情/表单共用字段） */
export type SysUser = {
  userId?: number | string
  userName?: string
  nickName?: string
  phonenumber?: string
  email?: string
  /** 性别(sys_user_sex) */
  sex?: string
  status?: string
  password?: string
  roleIds?: Array<number | string>
  roleNames?: string
  deptId?: number | string | null
  deptName?: string
  remark?: string
  createTime?: string
}

/** 分页查询用户 */
export function pageUser(pageRequest: PageRequest<Partial<SysUser>>) {
  return request<PageInfo<SysUser>>({
    url: '/sys/user/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 按 userId 查询用户详情 */
export function getUser(userId: number | string) {
  return request<SysUser>({
    url: `/sys/user/${encodeURIComponent(String(userId))}`,
    method: 'GET',
  })
}

/** 新增用户，返回新 userId */
export function addUser(data: Partial<SysUser>) {
  return request<string>({
    url: '/sys/user/add',
    method: 'POST',
    data,
  })
}

/** 更新用户信息 */
export function updateUser(data: Partial<SysUser>) {
  return request<boolean>({
    url: '/sys/user/update',
    method: 'POST',
    data,
  })
}

/** 启用/停用用户 */
export function changeUserStatus(data: { userId: number | string; status: string }) {
  return request<void>({
    url: '/sys/user/changeStatus',
    method: 'POST',
    data,
  })
}

/** 重置用户密码 */
export function resetUserPwd(data: { userId: number | string; password: string }) {
  return request<void>({
    url: '/sys/user/resetPwd',
    method: 'POST',
    data,
  })
}

/** 批量删除用户；请求体为 userId 数组 */
export function removeUser(userIds: Array<number | string>) {
  return request<void>({
    url: '/sys/user/remove',
    method: 'POST',
    data: userIds.map(String),
  })
}
