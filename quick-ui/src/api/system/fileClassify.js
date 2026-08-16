/**
 * 文件分类配置 API。
 * 封装 `/system/fileClassify` 分页、详情及 CRUD；分类键 classify 创建后不可改。
 */
import request from '@/utils/request'

/**
 * 文件分类分页列表。
 * @param {Record<string, any>} params pageNum/pageSize/classify/classifyName/status
 * @returns {Promise<any>}
 */
export function listFileClassify(params) {
  return request({ url: '/system/fileClassify/list', method: 'get', params })
}

/**
 * 分类详情。
 * @param {number|string} id
 * @returns {Promise<any>}
 */
export function getFileClassify(id) {
  return request({ url: '/system/fileClassify/' + id, method: 'get' })
}

/**
 * 新增分类。
 * @param {Record<string, any>} data
 * @returns {Promise<any>}
 */
export function addFileClassify(data) {
  return request({ url: '/system/fileClassify/add', method: 'post', data })
}

/**
 * 修改分类（不可改 classify 键）。
 * @param {Record<string, any>} data
 * @returns {Promise<any>}
 */
export function updateFileClassify(data) {
  return request({ url: '/system/fileClassify/update', method: 'post', data })
}

/**
 * 批量删除分类。
 * @param {Array<number|string>} ids
 * @returns {Promise<any>}
 */
export function removeFileClassify(ids) {
  return request({ url: '/system/fileClassify/remove', method: 'post', data: ids })
}
