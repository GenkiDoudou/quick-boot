/**
 * 字典数据项 API。
 * 封装 `/sys/dict/data` 分页、按类型查询、CRUD 及 Excel 导入导出。
 */
import request from '@/utils/request'

/**
 * 字典数据分页（param 可含 dictType 筛选）。
 * @param {object} pageRequest { current, size, param }
 */
export function pageDictData(pageRequest) {
  return request({ url: '/sys/dict/data/page', method: 'post', data: pageRequest })
}

/**
 * 字典数据详情。
 * @param {number|string} dictCode
 */
export function getData(dictCode) {
  return request({ url: `/sys/dict/data/${dictCode}`, method: 'get' })
}

/**
 * 按字典类型查询全部数据项（供 useDict 下拉/标签使用）。
 * @param {string} dictType
 */
export function getDicts(dictType) {
  return request({ url: `/sys/dict/data/type/${encodeURIComponent(dictType)}`, method: 'get' })
}

/**
 * 新增字典数据。
 * @param {Record<string, any>} data
 */
export function addData(data) {
  return request({ url: '/sys/dict/data/add', method: 'post', data })
}

/**
 * 修改字典数据。
 * @param {Record<string, any>} data 含 dictCode
 */
export function updateData(data) {
  return request({ url: '/sys/dict/data/update', method: 'post', data })
}

/**
 * 批量删除。请求体为主键 dictCode 数组。
 * @param {Array<string|number>|string|number} ids
 */
export function delData(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/dict/data/remove', method: 'post', data: list })
}

/**
 * 同步导出 xlsx。
 * @param {Record<string, unknown>} snapshot
 */
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

/** 下载字典数据导入模板（blob） */
export function downloadDataImportTemplate() {
  return request({
    url: '/sys/dict/data/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 同步导入字典数据。
 * @param {File} file
 * @param {string} strategy overwrite|ignore
 */
export function importData(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/dict/data/import', method: 'post', data: form, timeout: 120000 })
}
