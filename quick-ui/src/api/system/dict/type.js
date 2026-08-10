import request from '@/utils/request'

export function pageDictType(pageRequest) {
  return request({ url: '/sys/dict/type/page', method: 'post', data: pageRequest })
}

export function getType(dictId) {
  return request({ url: `/sys/dict/type/${dictId}`, method: 'get' })
}

export function addType(data) {
  return request({ url: '/sys/dict/type/add', method: 'post', data })
}

export function updateType(data) {
  return request({ url: '/sys/dict/type/update', method: 'post', data })
}

export function removeType(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/dict/type/remove', method: 'post', data: list })
}

export function refreshAllType() {
  return request({ url: '/sys/dict/type/refresh', method: 'post' })
}

export function refreshType(dictType) {
  return request({ url: `/sys/dict/type/refresh/${encodeURIComponent(dictType)}`, method: 'post' })
}

export function exportType(snapshot) {
  return request({
    url: '/sys/dict/type/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

export function downloadTypeImportTemplate() {
  return request({
    url: '/sys/dict/type/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

export function importType(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/dict/type/import', method: 'post', data: form, timeout: 120000 })
}
