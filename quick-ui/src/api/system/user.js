import request, { downloadRequest, importRequest } from '@/utils/request'
import { appendImportFormFields } from '@/utils/excelImportForm'
import { parseStrEmpty } from '@/utils/ruoyi'

/**
 * 用户分页列表。
 * @param {Record<string, any>} query 查询参数（含 pageNum、pageSize）
 * @returns {Promise<any>}
 */
export function listUser(query) {
  return request({ url: '/system/user/list', method: 'get', params: query })
}

/**
 * 用户详情。
 * @param {number|string} userId 用户ID
 * @returns {Promise<any>}
 */
export function getUser(userId) {
  return request({ url: '/system/user/' + parseStrEmpty(userId), method: 'get' })
}

/**
 * 新增用户。
 * @param {Record<string, any>} data 表单
 * @returns {Promise<any>}
 */
export function addUser(data) {
  return request({ url: '/system/user/create', method: 'post', data })
}

/**
 * 修改用户。
 * @param {Record<string, any>} data 表单
 * @returns {Promise<any>}
 */
export function updateUser(data) {
  return request({ url: '/system/user/update', method: 'post', data })
}

/**
 * 删除用户（批量）。
 * @param {Array<number|string>} userIds 用户ID
 * @returns {Promise<any>}
 */
export function delUser(userIds) {
  return request({ url: '/system/user/remove', method: 'post', data: userIds })
}

/**
 * 重置用户密码（管理员）。
 * @param {{ userId: number, newPassword: string }} data 入参
 * @returns {Promise<any>}
 */
export function resetUserPwd(data) {
  return request({ url: '/system/user/resetPwd', method: 'post', data })
}

/**
 * 修改用户状态。
 * @param {{ userId: number, status: string }} data 入参
 * @returns {Promise<any>}
 */
export function changeUserStatus(data) {
  return request({ url: '/system/user/changeStatus', method: 'post', data })
}

/**
 * 分配角色页数据。
 * @param {number|string} userId 用户ID
 * @returns {Promise<any>}
 */
export function getAuthRole(userId) {
  return request({ url: '/system/user/authRole/' + parseStrEmpty(userId), method: 'get' })
}

/**
 * 保存用户角色分配。
 * @param {{ userId: number, roleIds: number[] }} data 入参
 * @returns {Promise<any>}
 */
export function updateAuthRole(data) {
  return request({ url: '/system/user/authRole', method: 'post', data })
}

// 查询用户个人信息
export function getUserProfile() {
  return request({ url: '/system/user/profile', method: 'get' })
}

// 修改用户个人信息
export function updateUserProfile(data) {
  return request({ url: '/system/user/profile/update', method: 'post', data })
}

// 用户密码重置（个人中心）
export function updateUserPwd(oldPassword, newPassword) {
  return request({ url: '/system/user/profile/updatePwd', method: 'post', params: { oldPassword, newPassword } })
}

// 用户头像上传
export function uploadAvatar(data) {
  return request({
    url: '/system/user/profile/avatar',
    method: 'post',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    data
  })
}

/**
 * 导出用户。
 * @param {Record<string, any>} data 筛选条件
 * @returns {Promise<{ data: Blob, headers: import('axios').AxiosResponse['headers'] }>}
 */
export function exportUser(data) {
  return downloadRequest('/system/user/export', data, { returnBlobWithHeaders: true })
}

/**
 * 导入用户。
 * @param {File} file 文件
 * @param {boolean} updateSupport 是否更新已存在用户
 * @returns {Promise<any>}
 */
export function importUser(file, updateSupport, opts = {}) {
  const formData = new FormData()
  formData.append('file', file)
  appendImportFormFields(formData, updateSupport, opts)
  return importRequest({
    url: '/system/user/importData',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 下载导入模板。
 * @returns {Promise<Blob>}
 */
export function importTemplate() {
  return request({
    url: '/system/user/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 下载导入失败明细。
 * @param {string} errorKey 导入结果返回的键
 * @returns {Promise<Blob>}
 */
export function importError(errorKey) {
  return request({
    url: '/system/user/importError',
    method: 'get',
    params: { errorKey },
    responseType: 'blob'
  })
}
