import request, { downloadRequest, importRequest } from '@/utils/request'
import { appendImportFormFields } from '@/utils/excelImportForm'

export function listType(params) { return request({ url: '/system/dict/type/list', method: 'get', params }) }
export function getType(dictId) { return request({ url: '/system/dict/type/' + dictId, method: 'get' }) }
export function addType(data) { return request({ url: '/system/dict/type', method: 'post', data }) }
export function updateType(data) { return request({ url: '/system/dict/type/update', method: 'post', data }) }
export function removeType(dictId) { return request({ url: '/system/dict/type/remove/' + dictId, method: 'post' }) }
export function exportType(data) {
  return downloadRequest('/system/dict/type/export', data, { returnBlobWithHeaders: true })
}
export function refreshType(dictType) { return request({ url: '/system/dict/type/refresh/' + dictType, method: 'post' }) }
export function refreshAllType() { return request({ url: '/system/dict/type/refresh', method: 'post' }) }
export function importType(file, updateSupport = false, opts = {}) {
  const formData = new FormData()
  formData.append('file', file)
  appendImportFormFields(formData, updateSupport, { mode: 'async', ...opts })
  return importRequest({
    url: '/system/dict/type/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export function importTypeTemplate() {
  return downloadRequest('/system/dict/type/import/template', {}, { returnBlobWithHeaders: true })
}
