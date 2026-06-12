import request from '@/utils/request'

/**
 * AI 大模型分页列表。
 * @param {object} params 查询参数（name、code、modelType、provider、status、defaultSlot）
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export function listModel(params) {
  return request({ url: '/ai/model/list', method: 'get', params })
}

/**
 * AI 大模型详情。
 * @param {string|number} modelId 模型 ID
 * @param {boolean} [revealSecrets=false] 是否展示密钥明文
 */
export function getModelInfo(modelId, revealSecrets = false) {
  return request({
    url: '/ai/model/getInfo',
    method: 'get',
    params: { modelId, revealSecrets }
  })
}

/**
 * 新增 AI 大模型。
 * @param {Record<string, any>} data AiModelBo
 */
export function addModel(data) {
  return request({ url: '/ai/model/add', method: 'post', data })
}

/**
 * 修改 AI 大模型。
 * @param {Record<string, any>} data AiModelBo（含 modelId）
 */
export function updateModel(data) {
  return request({ url: '/ai/model/update', method: 'post', data })
}

/**
 * 删除 AI 大模型。
 * @param {Array<number|string>} modelIds 模型 ID 集合
 */
export function removeModel(modelIds) {
  return request({ url: '/ai/model/remove', method: 'post', data: modelIds })
}

/**
 * 模型连接测试。
 * @param {string|number} modelId 模型 ID
 */
export function testModel(modelId) {
  return request({ url: '/ai/model/test', method: 'post', params: { modelId } })
}

/**
 * 设为全局默认。
 * @param {{ modelId: number|string, defaultSlot: string }} data defaultSlot: CHAT / EMBEDDING / WORKFLOW_CHAT
 */
export function setModelDefault(data) {
  return request({ url: '/ai/model/setDefault', method: 'post', data })
}

/**
 * 清除全局默认槽位。
 * @param {string} defaultSlot CHAT / EMBEDDING / WORKFLOW_CHAT
 */
export function clearModelDefault(defaultSlot) {
  return request({ url: '/ai/model/clearDefault', method: 'post', data: { defaultSlot } })
}

/**
 * 导出 YAML 或 ENV 片段。
 * @param {string} [ids] 逗号分隔模型 ID
 * @param {string} [format='yaml'] yaml 或 env
 * @param {boolean} [includeSecrets=false] 是否包含明文密钥
 */
export function exportModel(ids, format = 'yaml', includeSecrets = false) {
  return request({
    url: '/ai/model/export',
    method: 'get',
    params: { ids, format, includeSecrets }
  })
}

/**
 * 模型下拉选项（知识库/工作流绑定）。
 * @param {string} [modelType] CHAT 或 EMBEDDING
 */
export function listModelOptions(modelType) {
  return request({
    url: '/ai/model/options',
    method: 'get',
    params: modelType ? { modelType } : undefined
  })
}

/**
 * 从当前 spring.ai YAML 生成导入草稿（不落库）。
 */
export function importModelFromYaml() {
  return request({ url: '/ai/model/importFromYaml', method: 'post' })
}
