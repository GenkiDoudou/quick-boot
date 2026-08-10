import request from '@/utils/request'

export function pageConfig(pageRequest) {
  return request({ url: '/sys/config/page', method: 'post', data: pageRequest })
}

export function getConfig(configId) {
  return request({ url: `/sys/config/${configId}`, method: 'get' })
}

export function addConfig(data) {
  return request({ url: '/sys/config/add', method: 'post', data })
}

export function updateConfig(data) {
  return request({ url: '/sys/config/update', method: 'post', data })
}

export function removeConfig(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/config/remove', method: 'post', data: list })
}

export function refreshConfigCache() {
  return request({ url: '/sys/config/refreshCache', method: 'post' })
}

export function exportConfig(snapshot) {
  return request({
    url: '/sys/config/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

export function downloadConfigImportTemplate() {
  return request({
    url: '/sys/config/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

export function importConfig(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/config/import', method: 'post', data: form, timeout: 120000 })
}
