import request from '@/utils/request'

export function pageDictData(pageRequest) {
  return request({ url: '/sys/dict/data/page', method: 'post', data: pageRequest })
}

export function getData(dictCode) {
  return request({ url: `/sys/dict/data/${dictCode}`, method: 'get' })
}

export function getDicts(dictType) {
  return request({ url: `/sys/dict/data/type/${encodeURIComponent(dictType)}`, method: 'get' })
}

export function addData(data) {
  return request({ url: '/sys/dict/data/add', method: 'post', data })
}

export function updateData(data) {
  return request({ url: '/sys/dict/data/update', method: 'post', data })
}

export function delData(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/dict/data/remove', method: 'post', data: list })
}

export function exportData(snapshot) {
  return request({
    url: '/sys/dict/data/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

export function downloadDataImportTemplate() {
  return request({
    url: '/sys/dict/data/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

export function importData(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/dict/data/import', method: 'post', data: form, timeout: 120000 })
}
