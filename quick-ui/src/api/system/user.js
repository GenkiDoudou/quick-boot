/**
 * 系统用户管理 API。
 * 封装 `/sys/user` 分页、CRUD、状态切换、密码重置、角色授权及 Excel 导入导出。
 */
import request from '@/utils/request'

/**
 * 用户分页列表。
 * @param {object} pageRequest { current, size, param }
 */
export function pageUser(pageRequest) {
  return request({ url: '/sys/user/page', method: 'post', data: pageRequest })
}

/**
 * 用户详情（含 roleIds）。
 * @param {number|string} userId
 */
export function getUser(userId) {
  return request({ url: `/sys/user/${userId}`, method: 'get' })
}

/**
 * 新增用户。
 * @param {Record<string, any>} data
 */
export function addUser(data) {
  return request({ url: '/sys/user/add', method: 'post', data })
}

/**
 * 修改用户。
 * @param {Record<string, any>} data 含 userId
 */
export function updateUser(data) {
  return request({ url: '/sys/user/update', method: 'post', data })
}

/**
 * 批量删除。请求体为主键 userId 数组。
 * @param {Array<string|number>|string|number} ids
 */
export function removeUser(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/user/remove', method: 'post', data: list })
}

/**
 * 修改用户状态（sys_normal_disable：0 正常 / 1 停用）。
 * @param {{ userId: number|string, status: string }} data
 */
export function changeUserStatus(data) {
  return request({ url: '/sys/user/changeStatus', method: 'post', data })
}

/**
 * 重置用户密码。
 * @param {{ userId: number|string, password: string }} data
 */
export function resetUserPwd(data) {
  return request({ url: '/sys/user/resetPwd', method: 'post', data })
}

/**
 * 获取用户已授权角色列表与勾选 roleIds。
 * @param {number|string} userId
 */
export function getAuthRole(userId) {
  return request({ url: `/sys/user/authRole/${userId}`, method: 'get' })
}

/**
 * 保存用户角色授权。
 * @param {{ userId: number|string, roleIds: Array<number|string> }} data
 */
export function updateAuthRole(data) {
  return request({ url: '/sys/user/authRole', method: 'post', data })
}

/**
 * 同步导出 xlsx。
 * @param {Record<string, unknown>} snapshot C7JsonTable 导出快照
 */
export function exportUser(snapshot) {
  return request({
    url: '/sys/user/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

/** 下载用户导入模板（blob） */
export function downloadUserImportTemplate() {
  return request({
    url: '/sys/user/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 同步导入用户。
 * @param {File} file
 * @param {string} strategy overwrite|ignore
 */
export function importUser(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/user/import', method: 'post', data: form, timeout: 120000 })
}
