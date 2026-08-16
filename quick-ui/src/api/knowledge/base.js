/**
 * 知识库基础 API：知识库 CRUD 与状态管理（/knowledge/base）。
 */
import request from '@/utils/request'

/**
 * 知识库分页列表。
 * @param {Record<string, any>} params 查询参数（pageNum、pageSize、name、status）
 * @returns {Promise<{ data: { records: Array, total: number } }>}
 */
export function listKnowledgeBase(params) {
  return request({ url: '/knowledge/base/list', method: 'get', params })
}

/**
 * 查询知识库详情。
 * @param {number|string} kbId 知识库 ID
 * @returns {Promise<any>}
 */
export function getKnowledgeBase(kbId) {
  return request({ url: '/knowledge/base/getInfo', method: 'get', params: { kbId } })
}

/**
 * 新增知识库。
 * @param {Record<string, any>} data 知识库数据（name、description、chunkSize、chunkOverlap、status）
 * @returns {Promise<any>}
 */
export function addKnowledgeBase(data) {
  return request({ url: '/knowledge/base/add', method: 'post', data })
}

/**
 * 修改知识库。
 * @param {Record<string, any>} data 知识库数据（含 kbId）
 * @returns {Promise<any>}
 */
export function updateKnowledgeBase(data) {
  return request({ url: '/knowledge/base/update', method: 'post', data })
}

/**
 * 批量删除知识库。
 * @param {Array<number|string>} kbIds 知识库 ID 集合
 * @returns {Promise<any>}
 */
export function removeKnowledgeBase(kbIds) {
  return request({ url: '/knowledge/base/remove', method: 'post', data: kbIds })
}
