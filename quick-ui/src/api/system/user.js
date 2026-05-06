import request, { downloadRequest } from '@/utils/request'
import { parseStrEmpty } from '@/utils/ruoyi'

// 查询用户列表
export function listUser(query) {
  return request({ url: '/sys/user/page', method: 'get', params: query })
}

// 查询用户详细
export function getUser(userId) {
  return request({ url: '/system/user/' + parseStrEmpty(userId), method: 'get' })
}

// 新增用户
export function addUser(data) {
  return request({ url: '/system/user', method: 'post', data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/sys/user/update', method: 'post', data })
}

// 删除用户
export function delUser(userIds) {
  return request({ url: '/sys/user/delete', method: 'post', data: userIds })
}

// 用户密码重置
export function resetUserPwd(userId) {
  return request({ url: '/sys/user/resetPwd/' + userId, method: 'post' })
}

// 用户状态修改
export function changeUserStatus(userId, status) {
  return request({ url: '/sys/user/updateStatus/' + userId + '/' + status, method: 'post' })
}

// 查询用户个人信息
export function getUserProfile() {
  return request({ url: '/system/user/profile', method: 'get' })
}

// 修改用户个人信息
export function updateUserProfile(data) {
  return request({ url: '/system/user/profile/update', method: 'post', data })
}

// 用户密码重置
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

// 导出用户
export function exportUser(queryParams) {
  return downloadRequest('/sys/user/exportExcel', queryParams)
}

// 导入用户
export function importUser(file, updateSupport) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('updateSupport', updateSupport)
  return request({
    url: '/sys/user/importExcel',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
