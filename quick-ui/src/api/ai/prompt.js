import request from '@/utils/request'

/**
 * 提示词分页列表。
 * @param {object} params 查询参数（name、category）
 */
export function listPrompt(params) {
  return request({ url: '/ai/prompt/list', method: 'get', params })
}

/**
 * 提示词下拉选项（工作流等场景引用）。
 */
export function listPromptOptions() {
  return request({ url: '/ai/prompt/options', method: 'get' })
}

/**
 * 提示词详情。
 * @param {string|number} promptId 提示词 ID
 */
export function getPromptInfo(promptId) {
  return request({ url: '/ai/prompt/getInfo', method: 'get', params: { promptId } })
}

/**
 * 新增提示词。
 * @param {{ name: string, category?: string, description?: string, content?: string }} data
 */
export function addPrompt(data) {
  return request({ url: '/ai/prompt/add', method: 'post', data })
}

/**
 * 修改提示词。
 * @param {{ promptId: number|string, name: string, category?: string, description?: string, content?: string }} data
 */
export function updatePrompt(data) {
  return request({ url: '/ai/prompt/update', method: 'post', data })
}

/**
 * 删除提示词。
 * @param {Array<number|string>} promptIds ID 集合
 */
export function removePrompt(promptIds) {
  return request({ url: '/ai/prompt/remove', method: 'post', data: promptIds })
}

/**
 * AI 优化提示词内容（同步，超时 60s）。
 * @param {{ content: string, modelId?: number|string }} data
 */
export function optimizePromptContent(data) {
  return request({ url: '/ai/prompt/optimize', method: 'post', data, timeout: 65000 })
}
