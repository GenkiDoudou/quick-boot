import request, { downloadRequest } from '@/utils/request'

export function listData(query) { return request({ url: '/system/dict/data/list', method: 'get', params: query }) }
export function getData(dictCode) { return request({ url: '/system/dict/data/' + dictCode, method: 'get' }) }
export function getDicts(dictType) { return request({ url: '/system/dict/data/type/' + dictType, method: 'get' }) }
export function addData(data) { return request({ url: '/system/dict/data', method: 'post', data }) }
export function updateData(data) { return request({ url: '/system/dict/data/update', method: 'post', data }) }
export function delData(dictCode) { return request({ url: '/system/dict/data/remove/' + dictCode, method: 'post' }) }
export function exportData(data) {
  return downloadRequest('/system/dict/data/export', data, { returnBlobWithHeaders: true })
}
export function importData(file, dictType, updateSupport = false) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dictType', dictType || '')
  formData.append('updateSupport', updateSupport ? 'true' : 'false')
  return request({
    url: '/system/dict/data/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export function importDataTemplate() {
  return downloadRequest('/system/dict/data/import/template', {}, { returnBlobWithHeaders: true })
}
