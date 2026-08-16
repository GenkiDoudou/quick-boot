/**
 * H5 工作台 / 首页快捷菜单 API。
 * 约定：后端仅返回 path 以 /pages/ 开头的授权入口。
 */
import { request } from '../http'

export type H5WorkbenchItem = {
  id: string
  label: string
  path: string
  icon?: string
  orderNum?: number
}

export type H5WorkbenchGroup = {
  id: string
  title: string
  orderNum?: number
  items: H5WorkbenchItem[]
}

/** 拉取当前用户可见的 H5 工作台分组 */
export function fetchH5Workbench() {
  return request<H5WorkbenchGroup[]>({
    url: '/system/menu/h5Workbench',
    method: 'GET',
  })
}

/** 首页最终快捷宫格（偏好或默认，已按权限过滤） */
export function fetchH5HomeShortcuts() {
  return request<H5WorkbenchItem[]>({
    url: '/system/menu/h5HomeShortcuts',
    method: 'GET',
  })
}

/** 首页快捷候选池（与工作台叶子同源） */
export function fetchH5HomeShortcutCandidates() {
  return request<H5WorkbenchItem[]>({
    url: '/system/menu/h5HomeShortcutCandidates',
    method: 'GET',
  })
}

/**
 * 保存首页快捷偏好（全量覆盖）。
 * @param menuIds 有序菜单 id；空数组表示恢复默认
 */
export function saveH5HomeShortcuts(menuIds: string[]) {
  return request<void>({
    url: '/system/menu/h5HomeShortcuts/save',
    method: 'POST',
    data: { menuIds },
  })
}
