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
  return request({ url: '/system/menu/add', method: 'post', data })
}

/** 修改菜单 */
export function updateMenu(data) {
  return request({ url: '/system/menu/update', method: 'post', data })
}

/** 批量保存菜单排序 */
export function updateMenuSort(data) {
  return request({ url: '/system/menu/updateSort', method: 'post', data })
}

/**
 * 删除菜单（单条）。
 * @param {string|number} menuId
 */
export function delMenu(menuId) {
  return request({ url: '/system/menu/remove/' + menuId, method: 'get' })
}

/**
 * 批量删除。
 * @param {Array<string|number>} menuIds
 */
export function removeMenu(menuIds) {
  const list = (Array.isArray(menuIds) ? menuIds : [menuIds]).map((id) => String(id))
  return request({
    url: '/system/menu/remove',
    method: 'post',
    data: list
  })
}

/**
 * 同步导出 xlsx。
 * @param {Record<string, unknown>} snapshot
 */
export function exportMenu(snapshot) {
  return request({
    url: '/system/menu/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

/** 下载导入模板 */
export function downloadMenuImportTemplate() {
  return request({
    url: '/system/menu/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 同步导入。
 * @param {File} file
 * @param {string} strategy overwrite|ignore
 */
export function importMenu(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({
    url: '/system/menu/import',
    method: 'post',
    data: form,
    timeout: 120000
  })
}
