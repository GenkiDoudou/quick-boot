import request from '@/utils/request'

export function listDept(query) {
  return request({ url: '/sys/dept/list', method: 'get', params: query })
}

export function treeselectDept() {
  return request({ url: '/sys/dept/treeselect', method: 'get' })
}

export function getDept(deptId) {
  return request({ url: `/sys/dept/${deptId}`, method: 'get' })
}

export function addDept(data) {
  return request({ url: '/sys/dept/add', method: 'post', data })
}

export function updateDept(data) {
  return request({ url: '/sys/dept/update', method: 'post', data })
}

export function delDept(deptId) {
  return request({ url: `/sys/dept/remove/${deptId}`, method: 'get' })
}

export function removeDept(ids) {
  return request({ url: '/sys/dept/remove', method: 'post', data: (Array.isArray(ids) ? ids : [ids]).map(String) })
}

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

export function downloadDeptImportTemplate() {
  return request({
    url: '/sys/dept/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

export function importDept(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/dept/import', method: 'post', data: form, timeout: 120000 })
}
