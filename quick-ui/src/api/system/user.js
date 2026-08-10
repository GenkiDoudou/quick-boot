import request from '@/utils/request'

export function pageUser(pageRequest) {
  return request({ url: '/sys/user/page', method: 'post', data: pageRequest })
}

export function getUser(userId) {
  return request({ url: `/sys/user/${userId}`, method: 'get' })
}

export function addUser(data) {
  return request({ url: '/sys/user/add', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/sys/user/update', method: 'post', data })
}

export function removeUser(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/user/remove', method: 'post', data: list })
}

export function changeUserStatus(data) {
  return request({ url: '/sys/user/changeStatus', method: 'post', data })
}

export function resetUserPwd(data) {
  return request({ url: '/sys/user/resetPwd', method: 'post', data })
}

export function getAuthRole(userId) {
  return request({ url: `/sys/user/authRole/${userId}`, method: 'get' })
}

export function updateAuthRole(data) {
  return request({ url: '/sys/user/authRole', method: 'post', data })
}

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

export function downloadUserImportTemplate() {
  return request({
    url: '/sys/user/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

export function importUser(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/user/import', method: 'post', data: form, timeout: 120000 })
}
