/**
 * OAuth 第三方登录提供方管理 API。
 * 封装 `/system/oauthProvider` 列表、详情及 CRUD。
 */
import request from '@/utils/request'

/**
 * 提供方列表。
 * @param {Record<string, any>} [params] 查询条件
 */
export function listOauthProvider(params) {
  return request({ url: '/system/oauthProvider/list', method: 'get', params })
}

/**
 * 提供方详情（按 providerCode 主键）。
 * @param {string} providerCode
 */
export function getOauthProvider(providerCode) {
  return request({ url: `/system/oauthProvider/${providerCode}`, method: 'get' })
}

/**
 * 新增提供方。
 * @param {Record<string, any>} data
 */
export function addOauthProvider(data) {
  return request({ url: '/system/oauthProvider/create', method: 'post', data })
}

/**
 * 修改提供方。
 * @param {Record<string, any>} data 含 providerCode
 */
export function updateOauthProvider(data) {
  return request({ url: '/system/oauthProvider/update', method: 'post', data })
}

/**
 * 批量删除提供方。
 * @param {Array<string|number>} ids 主键集合
 */
export function removeOauthProvider(ids) {
  return request({ url: '/system/oauthProvider/remove', method: 'post', data: { ids } })
}
