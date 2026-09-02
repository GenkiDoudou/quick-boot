/**
 * 系统部门管理 API。
 * 封装 `/sys/dept` 树形列表、下拉树、CRUD 及 Excel 导入导出。
 */
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'
import request from '@/utils/request'

const crud = createCrudApi('/sys/dept', { export: true })

/** 部门树形分页（POST page；records 为根节点树）。 */
export const pageDept = crud.page

/**
 * 部门树形列表（含 children）；内部走 POST page 并 unwrap records。
 * @param {Record<string, any>} [query] deptName、status 等筛选
 */
export function listDept(query) {
  return crud.page(toPageRequest({ ...query, current: 1, size: 9999 })).then((res) => ({
    ...res,
    data: res.data?.records ?? []
  }))
}

/** 部门下拉树（用于表单上级部门选择） */
export function treeselectDept() {
  return request({ url: '/sys/dept/treeselect', method: 'get' })
}

/** 部门详情。 */
export const getDept = crud.get
/** 新增部门。 */
export const addDept = crud.add
/** 修改部门。 */
export const updateDept = crud.update

/**
 * 删除单条部门（按主键路径）。
 * @param {number|string} deptId
 */
export function delDept(deptId) {
  return request({ url: `/sys/dept/remove/${deptId}`, method: 'get' })
}

/** 批量删除。 */
export const removeDept = crud.remove
/** 同步导出 xlsx。 */
export const exportDept = crud.export
/** 下载部门导入模板。 */
export const downloadDeptImportTemplate = crud.downloadImportTemplate
/** 同步导入部门。 */
export const importDept = crud.importExcel
