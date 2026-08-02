import request from '@/utils/request'

/**
 * OAuth 客户端分页。
 * @param {object} pageRequest { current, size, param }
 */
export function pageOauthClient(pageRequest) {
  return request({
    url: '/sys/oauthclient/page',
    method: 'post',
    data: pageRequest
  })
}

/**
 * 详情（含 secret）。对应 `GET /sys/oauthclient/{id}`（主键，非 clientId）。
 * @param {string|number} id
 */
export function getOauthClient(id) {
  return request({
    url: `/sys/oauthclient/${encodeURIComponent(String(id))}`,
    method: 'get'
  })
}

/**
 * 新增；响应 data 为新建主键 id 字符串（secret 需再调 get）。
 * @param {Record<string, any>} data
 */
export function addOauthClient(data) {
  return request({
    url: '/sys/oauthclient/add',
    method: 'post',
    data
  })
}

/**
 * 修改（不变更 secret / clientId）。
 * @param {Record<string, any>} data 含 id、clientId
 */
export function updateOauthClient(data) {
  return request({
    url: '/sys/oauthclient/update',
    method: 'post',
    data
  })
}

/**
 * 批量删除。请求体为主键 id 数组。
 * @param {Array<string|number>|string|number} ids
 */
export function removeOauthClient(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map((id) => String(id))
  return request({
    url: '/sys/oauthclient/remove',
    method: 'post',
    data: list
  })
}

/**
 * 同步导出 xlsx（JSON body + blob）。有勾选时 snapshot.ids 优先。
 * @param {Record<string, unknown>} snapshot C7JsonTable 导出快照
 * @returns {Promise<{ data: Blob, headers: unknown }>}
 */
export function exportOauthClient(snapshot) {
  return request({
    url: '/sys/oauthclient/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}

/**
 * 下载导入模板（blob）。
 * @returns {Promise<{ data: Blob, headers: unknown }>}
 */
export function downloadOauthClientImportTemplate() {
  return request({
    url: '/sys/oauthclient/import/template',
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 同步导入。strategy: overwrite|ignore。
 * @param {File} file
 * @param {string} strategy
 */
export function importOauthClient(file, strategy) {
  const form = new FormData()
  form.append('file', file)
  form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
  return request({
    url: '/sys/oauthclient/import',
    method: 'post',
    data: form,
    timeout: 120000
  })
}
