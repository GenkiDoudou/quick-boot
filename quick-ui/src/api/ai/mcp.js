import request from '@/utils/request'

/**
 * MCP 分页列表。
 * @param {object} params 查询参数
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export function listMcp(params) {
  return request({ url: '/ai/mcp/list', method: 'get', params })
}

/**
 * MCP 详情。
 * @param {string|number} mcpId MCP ID
 * @param {boolean} [revealSecrets=false] 是否展示密钥
 */
export function getMcpInfo(mcpId, revealSecrets = false) {
  return request({
    url: '/ai/mcp/getInfo',
    method: 'get',
    params: { mcpId, revealSecrets }
  })
}

/** 新增 MCP */
export function addMcp(data) {
  return request({ url: '/ai/mcp/add', method: 'post', data })
}

/** 修改 MCP */
export function updateMcp(data) {
  return request({ url: '/ai/mcp/update', method: 'post', data })
}

/** 删除 MCP */
export function removeMcp(mcpIds) {
  return request({ url: '/ai/mcp/remove', method: 'post', data: mcpIds })
}

/** 连接测试 */
export function testMcp(mcpId) {
  return request({ url: '/ai/mcp/test', method: 'post', params: { mcpId } })
}

/**
 * 导出 mcp.json 片段。
 * @param {string} [ids] 逗号分隔 ID
 * @param {boolean} [includeSecrets=false]
 */
export function exportMcp(ids, includeSecrets = false) {
  return request({
    url: '/ai/mcp/export',
    method: 'get',
    params: { ids, includeSecrets }
  })
}

/** MCP 下拉选项（知识库绑定） */
export function listMcpOptions() {
  return request({ url: '/ai/mcp/options', method: 'get' })
}
