import request from '@/utils/request'

/**
 * 查询异步入库任务进度。
 * @param {number|string} taskId 任务 ID
 * @returns {Promise<{ data: { taskId: number, docId: number, status: string, progress: number, errorMsg?: string } }>}
 */
export function getIngestTask(taskId) {
  return request({ url: '/knowledge/task/getInfo', method: 'get', params: { taskId } })
}
