/**
 * 系统参数配置 API。
 * 封装 `/sys/config` 分页、CRUD、缓存刷新及 Excel 导入导出。
 */
import request from '@/utils/request'

/**
 * 参数分页列表。
 * @param {object} pageRequest { current, size, param }
 */
export function pageConfig(pageRequest) {
  return request({ url: '/sys/config/page', method: 'post', data: pageRequest })
}

/**
 * 参数详情。
 * @param {number|string} configId
 */
export function getConfig(configId) {
  return request({ url: `/sys/config/${configId}`, method: 'get' })
}

/**
 * 新增参数。
 * @param {Record<string, any>} data
 */
export function addConfig(data) {
  return request({ url: '/sys/config/add', method: 'post', data })
}

/**
 * 修改参数。
 * @param {Record<string, any>} data 含 configId
 */
export function updateConfig(data) {
  return request({ url: '/sys/config/update', method: 'post', data })
}

/**
 * 批量删除。请求体为主键 configId 数组。
 * @param {Array<string|number>|string|number} ids
 */
export function removeConfig(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/sys/config/remove', method: 'post', data: list })
}

/** 刷新系统参数缓存（后端 Redis/内存） */
export function refreshConfigCache() {
  return request({ url: '/sys/config/refreshCache', method: 'post' })
}

/**
 * 同步导出 xlsx。
 * @param {Record<string, unknown>} snapshot
 */
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

/** 下载参数导入模板（blob） */
export function downloadConfigImportTemplate() {
  return request({
    url: '/sys/config/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 同步导入参数。
 * @param {File} file
 * @param {string} strategy overwrite|ignore
 */
export function importConfig(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({ url: '/sys/config/import', method: 'post', data: form, timeout: 120000 })
}
