import request from '@/utils/request'

/**
 * 系统菜单管理 API，与后端 `/system/menu` 契约一致（写操作为 POST）。
 */

/** 菜单树列表 */
export function listMenu(query) {
  return request({ url: '/system/menu/list', method: 'get', params: query })
}

/**
 * 菜单下拉树
 * @param {{ excludeButton?: boolean, directoryOnly?: boolean }} [params]
 * directoryOnly 为 true 时仅目录（M）；excludeButton 为 true 时排除按钮（F）
 */
export function treeselectMenu(params) {
  return request({ url: '/system/menu/treeselect', method: 'get', params })
}

/** 角色菜单树（含已勾选 id） */
export function roleMenuTreeselect(roleId) {
  return request({ url: '/system/menu/roleMenuTreeselect/' + roleId, method: 'get' })
}

/** 菜单详情 */
export function getMenu(menuId) {
  return request({ url: '/system/menu/' + menuId, method: 'get' })
}

/** 新增菜单 */
export function addMenu(data) {
  return request({ url: '/system/menu', method: 'post', data })
}

/** 修改菜单 */
export function updateMenu(data) {
  return request({ url: '/system/menu/update', method: 'post', data })
}

/** 删除菜单 */
export function delMenu(menuId) {
  return request({ url: '/system/menu/remove/' + menuId, method: 'post' })
}
