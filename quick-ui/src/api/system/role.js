import request, { downloadRequest } from '@/utils/request'

/**
 * 角色分页列表。
 * @param {Record<string, any>} params 查询参数（含 pageNum、pageSize）
 * @returns {Promise<any>}
 */
export function listRole(params) {
  return request({ url: '/system/role/list', method: 'get', params })
}

/**
 * 角色详情。
 * @param {number|string} roleId 角色ID
 * @returns {Promise<any>}
 */
export function getRole(roleId) {
  return request({ url: '/system/role/' + roleId, method: 'get' })
}

/**
 * 新增角色。
 * @param {Record<string, any>} data 表单
 * @returns {Promise<any>}
 */
export function addRole(data) {
  return request({ url: '/system/role/create', method: 'post', data })
}

/**
 * 修改角色。
 * @param {Record<string, any>} data 表单
 * @returns {Promise<any>}
 */
export function updateRole(data) {
  return request({ url: '/system/role/update', method: 'post', data })
}

/**
 * 删除角色（批量）。
 * @param {Array<number|string>} roleIds 角色ID
 * @returns {Promise<any>}
 */
export function removeRole(roleIds) {
  return request({ url: '/system/role/remove', method: 'post', data: roleIds })
}

/**
 * 修改角色状态。
 * @param {{ roleId: number, status: string }} data 入参
 * @returns {Promise<any>}
 */
export function changeRoleStatus(data) {
  return request({ url: '/system/role/changeStatus', method: 'post', data })
}

/**
 * 保存数据权限。
 * @param {{ roleId: number, dataScope: string, deptIds?: number[] }} data 入参
 * @returns {Promise<any>}
 */
export function updateRoleDataScope(data) {
  return request({ url: '/system/role/dataScope', method: 'post', data })
}

/**
 * 保存角色菜单。
 * @param {{ roleId: number, menuIds: number[] }} data 入参
 * @returns {Promise<any>}
 */
export function updateRoleMenu(data) {
  return request({ url: '/system/role/menu', method: 'post', data })
}

/**
 * 角色菜单树（含已勾选 keys）。
 * @param {number|string} roleId 角色ID
 * @returns {Promise<any>}
 */
export function roleMenuTreeselect(roleId) {
  return request({ url: '/system/menu/roleMenuTreeselect/' + roleId, method: 'get' })
}

/**
 * 已分配用户分页。
 * @param {Record<string, any>} params 查询参数
 * @returns {Promise<any>}
 */
export function listRoleAllocatedUsers(params) {
  return request({ url: '/system/role/authUser/allocatedList', method: 'get', params })
}

/**
 * 未分配用户分页。
 * @param {Record<string, any>} params 查询参数
 * @returns {Promise<any>}
 */
export function listRoleUnallocatedUsers(params) {
  return request({ url: '/system/role/authUser/unallocatedList', method: 'get', params })
}

/**
 * 批量授权用户到角色。
 * @param {{ roleId: number, userIds: number[] }} data 入参
 * @returns {Promise<any>}
 */
export function grantRoleUsers(data) {
  return request({ url: '/system/role/authUser/selectAll', method: 'post', data })
}

/**
 * 取消单个用户角色。
 * @param {{ roleId: number, userId: number }} data 入参
 * @returns {Promise<any>}
 */
export function cancelRoleUser(data) {
  return request({ url: '/system/role/authUser/cancel', method: 'post', data })
}

/**
 * 批量取消用户角色。
 * @param {{ roleId: number, userIds: number[] }} data 入参
 * @returns {Promise<any>}
 */
export function cancelRoleUsers(data) {
  return request({ url: '/system/role/authUser/cancelAll', method: 'post', data })
}

/**
 * 导入角色。
 * @param {File} file 上传文件
 * @param {boolean} [updateSupport=false] 是否更新已存在（按权限字符）
 * @returns {Promise<any>}
 */
export function importRole(file, updateSupport = false) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('updateSupport', updateSupport ? 'true' : 'false')
  return request({
    url: '/system/role/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 下载角色导入模板。
 * @returns {Promise<{ data: Blob, headers: import('axios').AxiosResponse['headers'] }>}
 */
export function importRoleTemplate() {
  return downloadRequest('/system/role/import/template', {}, { returnBlobWithHeaders: true })
}

/**
 * 导出角色。
 * @param {Record<string, any>} data 筛选条件
 * @returns {Promise<{ data: Blob, headers: import('axios').AxiosResponse['headers'] }>}
 */
export function exportRole(data) {
  return downloadRequest('/system/role/export', data, { returnBlobWithHeaders: true })
}
