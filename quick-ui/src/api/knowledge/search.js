/**
 * 知识库检索 API：语义检索与检索历史（/knowledge/search）。
 */
import request from '@/utils/request'

/**
 * 知识库语义检索。
 * @param {{ kbId: number|string, query: string, topK?: number, similarityThreshold?: number, searchMode?: 'VECTOR'|'HYBRID', saveHistory?: boolean }} data 检索参数
 */
export function searchKnowledge(data) {
  return request({ url: '/knowledge/search', method: 'post', data })
}

/**
 * 检索测试历史。
 * @param {{ kbId: string|number, pageNum?: number, pageSize?: number }} params
 */
export function listRetrievalHistory(params) {
  return request({ url: '/knowledge/search/history', method: 'get', params: { ...params, kbId: String(params.kbId) } })
}
