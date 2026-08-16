/**
 * 工作流模板 API：模板分页、详情、发布与从模板创建工作流（/workflow/template）。
 */
import request from '@/utils/request'

/**
 * 工作流模板分页列表（管理页）。
 * @param {Record<string, any>} params pageNum、pageSize、name、code、status
 * @returns {Promise<{ data: { records: Array, total: number } }>}
 */
export function pageWorkflowTemplate(params) {
  return request({ url: '/workflow/template/page', method: 'get', params })
}

/**
 * 查询模板详情（含 graph）。
 * @param {number|string} templateId 模板 ID
 * @returns {Promise<any>}
 */
export function getWorkflowTemplateInfo(templateId) {
  return request({ url: '/workflow/template/getInfo', method: 'get', params: { templateId } })
}

/**
 * 启用模板列表（新建工作流下拉）。
 * @returns {Promise<{ data: Array }>}
 */
export function listWorkflowTemplateOptions() {
  return request({ url: '/workflow/template/list', method: 'get' })
}

/**
 * 新增工作流模板。
 * @param {Record<string, any>} data 模板数据
 * @returns {Promise<{ data: number }>}
 */
export function addWorkflowTemplate(data) {
  return request({ url: '/workflow/template/add', method: 'post', data })
}

/**
 * 修改工作流模板。
 * @param {Record<string, any>} data 模板数据（含 templateId）
 * @returns {Promise<any>}
 */
export function updateWorkflowTemplate(data) {
  return request({ url: '/workflow/template/update', method: 'post', data })
}

/**
 * 保存模板图 DSL。
 * @param {Record<string, any>} data 含 templateId 与 graph
 * @returns {Promise<any>}
 */
export function saveWorkflowTemplateGraph(data) {
  return request({ url: '/workflow/template/saveGraph', method: 'post', data })
}

/**
 * 校验模板图（不落库）。
 * @param {Record<string, any>} data 含 graph
 * @returns {Promise<any>}
 */
export function validateWorkflowTemplateGraph(data) {
  return request({ url: '/workflow/template/validateGraph', method: 'post', data })
}

/**
 * 从工作流草稿导入模板。
 * @param {Record<string, any>} data 含 workflowId 与 template 元数据
 * @returns {Promise<{ data: number }>}
 */
export function importWorkflowTemplateFromWorkflow(data) {
  return request({ url: '/workflow/template/importFromWorkflow', method: 'post', data })
}

/**
 * 批量删除工作流模板。
 * @param {Array<number|string>} templateIds 模板 ID 集合
 * @returns {Promise<any>}
 */
export function removeWorkflowTemplate(templateIds) {
  return request({ url: '/workflow/template/remove', method: 'post', data: templateIds })
}
