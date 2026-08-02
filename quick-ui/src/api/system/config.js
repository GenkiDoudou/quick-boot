import request from '@/utils/request'

/**
 * 查询参数列表。
 * @param {Record<string, any>} params 查询参数
 * @returns {Promise<any>}
 */
export function listConfig(params) {
  return request({ url: '/system/config/list', method: 'get', params })
}

/**
 * 查询参数详情。
 * @param {number|string} configId 参数ID
 * @returns {Promise<any>}
 */
export function getConfig(configId) {
  return request({ url: '/system/config/' + configId, method: 'get' })
}

/**
 * 新增参数。
 * @param {Record<string, any>} data 参数数据
 * @returns {Promise<any>}
 */
export function addConfig(data) {
  return request({ url: '/system/config/create', method: 'post', data })
}

/**
 * 修改参数。
 * @param {Record<string, any>} data 参数数据
 * @returns {Promise<any>}
 */
export function updateConfig(data) {
  return request({ url: '/system/config/update', method: 'post', data })
}

/**
 * 删除参数。
 * @param {Array<number|string>} configIds 参数ID集合
 * @returns {Promise<any>}
 */
export function removeConfig(configIds) {
  return request({ url: '/system/config/remove', method: 'post', data: configIds })
}

/**
 * 刷新全部参数缓存。
 * @returns {Promise<any>}
 */
export function refreshConfigCache() {
  return request({ url: '/system/config/refreshCache', method: 'post' })
}
