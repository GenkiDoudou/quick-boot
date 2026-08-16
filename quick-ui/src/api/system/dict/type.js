/**
 * 字典类型 API。
 * 封装 `/sys/dict/type` 分页、CRUD、缓存刷新及 Excel 导入导出。
 */
import request from '@/utils/request'

/**
 * 字典类型分页。
 * @param {object} pageRequest { current, size, param }
 */
export function pageDictType(pageRequest) {
  return request({ url: '/sys/dict/type/page', method: 'post', data: pageRequest })
}

/**
 * 字典类型详情。
 * @param {number|string} dictId
 */
export function getType(dictId) {
  return request({ url: `/sys/dict/type/${dictId}`, method: 'get' })
}

/**
 * 新增字典类型。
 * @param {Record<string, any>} data
 */
export function addType(data) {
  return request({ url: '/sys/dict/type/add', method: 'post', data })
}

/**
 * 修改字典类型。
 * @param {Record<string, any>} data 含 dictId
 */
export function updateType(data) {
  return request({ url: '/sys/dict/type/update', method: 'post', data })
}

/**
 * 批量删除。请求体为主键 dictId 数组。
 * @param {Array<string|number>|string|number} ids
 */
export function removeType(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/dict/type/remove', method: 'post', data: list })
}

/** 刷新全部字典类型缓存（后端 + 前端 Pinia 需另行 cleanDict） */
export function refreshAllType() {
  return request({ url: '/sys/dict/type/refresh', method: 'post' })
}

/**
 * 按 dictType 刷新单类字典缓存。
 * @param {string} dictType
 */
export function refreshType(dictType) {
  return request({ url: `/sys/dict/type/refresh/${encodeURIComponent(dictType)}`, method: 'post' })
}

/**
 * 同步导出 xlsx。
 * @param {Record<string, unknown>} snapshot
 */
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

/** 下载字典类型导入模板（blob） */
export function downloadTypeImportTemplate() {
  return request({
    url: '/sys/dict/type/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 同步导入字典类型。
 * @param {File} file
 * @param {string} strategy overwrite|ignore
 */
export function importType(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/dict/type/import', method: 'post', data: form, timeout: 120000 })
}
