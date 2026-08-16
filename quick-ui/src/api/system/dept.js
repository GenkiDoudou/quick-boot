/**
 * 系统部门管理 API。
 * 封装 `/sys/dept` 树形列表、下拉树、CRUD 及 Excel 导入导出。
 */
import request from '@/utils/request'

/**
 * 部门树形列表（含 children）。
 * @param {Record<string, any>} [query] deptName、status 等筛选
 */
export function listDept(query) {
  return request({ url: '/sys/dept/list', method: 'get', params: query })
}

/** 部门下拉树（用于表单上级部门选择） */
export function treeselectDept() {
  return request({ url: '/sys/dept/treeselect', method: 'get' })
}

/**
 * 部门详情。
 * @param {number|string} deptId
 */
export function getDept(deptId) {
  return request({ url: `/sys/dept/${deptId}`, method: 'get' })
}

/**
 * 新增部门。
 * @param {Record<string, any>} data
 */
export function addDept(data) {
  return request({ url: '/sys/dept/add', method: 'post', data })
}

/**
 * 修改部门。
 * @param {Record<string, any>} data 含 deptId
 */
export function updateDept(data) {
  return request({ url: '/sys/dept/update', method: 'post', data })
}

/**
 * 删除单条部门（按主键路径）。
 * @param {number|string} deptId
 */
export function delDept(deptId) {
  return request({ url: `/sys/dept/remove/${deptId}`, method: 'get' })
}

/**
 * 批量删除。请求体为主键 deptId 数组。
 * @param {Array<string|number>|string|number} ids
 */
export function removeDept(ids) {
  return request({ url: '/sys/dept/remove', method: 'post', data: (Array.isArray(ids) ? ids : [ids]).map(String) })
}

/**
 * 同步导出 xlsx。
 * @param {Record<string, unknown>} snapshot
 */
export function exportDept(snapshot) {
  return request({
    url: '/sys/dept/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

/** 下载部门导入模板（blob） */
export function downloadDeptImportTemplate() {
  return request({
    url: '/sys/dept/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 同步导入部门。
 * @param {File} file
 * @param {string} strategy overwrite|ignore
 */
export function importDept(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/dept/import', method: 'post', data: form, timeout: 120000 })
}
