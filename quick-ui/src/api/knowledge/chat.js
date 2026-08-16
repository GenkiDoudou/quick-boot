/**
 * 知识库问答 API：RAG 问答与引用溯源（/knowledge/chat）。
 */
import request from '@/utils/request'

/**
 * 知识库 RAG 问答。
 * @param {{ kbId: number|string, question: string, topK?: number, similarityThreshold?: number }} data 问答参数
 * @returns {Promise<{ data: { answer: string, citations: Array<{ docId: number, chunkId: number, fileName: string, contentPreview: string, score: number, pageNumber?: number }> } }>}
 */
export function chatKnowledge(data) {
  return request({ url: '/knowledge/chat', method: 'post', data })
}
