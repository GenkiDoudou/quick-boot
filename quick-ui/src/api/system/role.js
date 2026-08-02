import request from '@/utils/request'

/**
 * 角色分页。
 * @param {object} pageRequest { current, size, param }
 */
export function pageRole(pageRequest) {
  return request({
    url: '/sys/role/page',
    method: 'post',
    data: pageRequest
  })
}

/** @deprecated 使用 pageRole */
export function listRole(params) {
  return pageRole({
    current: params?.pageNum || params?.current || 1,
    size: params?.pageSize || params?.size || 10,
    param: params
  })
}

/**
 * 角色详情。对应 `GET /sys/role/{roleId}`。
 * @param {number|string} roleId
 */
export function getRole(roleId) {
  return request({
    url: `/sys/role/${encodeURIComponent(String(roleId))}`,
    method: 'get'
  })
}

/**
 * 新增角色。
 * @param {Record<string, any>} data
 */
export function addRole(data) {
  return request({
    url: '/sys/role/add',
    method: 'post',
    data
  })
}

/**
 * 修改角色。
 * @param {Record<string, any>} data
 */
export function updateRole(data) {
  return request({
    url: '/sys/role/update',
    method: 'post',
    data
  })
}

/**
 * 批量删除。请求体为主键 roleId 数组。
 * @param {Array<string|number>|string|number} roleIds
 */
export function removeRole(roleIds) {
  const list = (Array.isArray(roleIds) ? roleIds : [roleIds]).map((id) => String(id))
  return request({
    url: '/sys/role/remove',
    method: 'post',
    data: list
  })
}

/**
 * 修改角色状态。
 * @param {{ roleId: number, status: string }} data
 */
export function changeRoleStatus(data) {
  return request({
    url: '/sys/role/changeStatus',
    method: 'post',
    data
  })
}

/**
 * 保存角色菜单。
 * @param {{ roleId: number, menuIds: number[] }} data
 */
export function updateRoleMenu(data) {
  return request({
    url: '/sys/role/menu',
    method: 'post',
    data
  })
}

/**
 * 角色菜单树（含已勾选 keys）。
 * @param {number|string} roleId
 */
export function roleMenuTreeselect(roleId) {
  return request({
    url: '/sys/role/menuTree',
    method: 'get',
    params: { roleId }
  })
}

/**
 * 已分配用户分页。
 * @param {{ roleId: number, current?: number, size?: number, param?: object }} data
 */
export function allocatedUserList(data) {
  return request({
    url: '/sys/role/authUser/allocatedPage',
    method: 'post',
    data
  })
}

/**
 * 未分配用户分页。
 * @param {{ roleId: number, current?: number, size?: number, param?: object }} data
 */
export function unallocatedUserList(data) {
  return request({
    url: '/sys/role/authUser/unallocatedPage',
    method: 'post',
    data
  })
}

/**
 * 批量授权用户。
 * @param {{ roleId: number, userIds: Array<string|number> }} data
 */
export function authUserSelectAll(data) {
  return request({
    url: '/sys/role/authUser/grant',
    method: 'post',
    data: {
      roleId: data.roleId,
      userIds: (data.userIds || []).map((id) => String(id))
    }
  })
}

/**
 * 取消用户授权。
 * @param {{ roleId: number, userIds: Array<string|number> }} data
 */
export function authUserCancel(data) {
  return request({
    url: '/sys/role/authUser/cancel',
    method: 'post',
    data: {
      roleId: data.roleId,
      userIds: (data.userIds || []).map((id) => String(id))
    }
  })
}

/**
 * 批量取消用户授权。
 * @param {{ roleId: number, userIds: Array<string|number> }} data
 */
export function authUserCancelAll(data) {
  return request({
    url: '/sys/role/authUser/cancelAll',
    method: 'post',
    data: {
      roleId: data.roleId,
      userIds: (data.userIds || []).map((id) => String(id))
    }
  })
}

/**
 * 同步导出 xlsx。
 * @param {Record<string, unknown>} snapshot
 */
export function exportRole(snapshot) {
  return request({
    url: '/sys/role/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

/** 下载导入模板 */
export function downloadRoleImportTemplate() {
  return request({
    url: '/sys/role/import/template',
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
export function importRole(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({
    url: '/sys/role/import',
    method: 'post',
    data: form,
    timeout: 120000
  })
}
