/**
 * 系统菜单管理 API，与后端 `/system/menu` 契约一致（写操作为 POST）。
 * <p>
 * 树形列表：优先 {@link pageMenu}（POST page），{@link listMenu} 为兼容别名（unwrap records）。
 */
import request from '@/utils/request'
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'

const crud = createCrudApi('/system/menu', { export: true })

/** 菜单树形分页（POST page；records 为根节点树）。 */
export const pageMenu = crud.page

/**
 * 菜单树列表（兼容层：内部走 POST page 并 unwrap records）。
 * @param {Record<string, any>} [query] menuName、status
 */
export function listMenu(query) {
  return crud.page(toPageRequest({ ...query, current: 1, size: 9999 })).then((res) => ({
    ...res,
    data: res.data?.records ?? []
  }))
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
export const getMenu = crud.get
/** 新增菜单 */
export const addMenu = crud.add
/** 修改菜单 */
export const updateMenu = crud.update

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

/** 批量删除。 */
export const removeMenu = crud.remove
/** 同步导出 xlsx。 */
export const exportMenu = crud.export

/** 下载导入模板（菜单模块若后端未开放则调用方需自行处理 404） */
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
