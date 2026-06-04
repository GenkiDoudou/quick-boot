import request, { exportRequest } from '@/utils/request'

/**
 * 提交 Excel 导出。
 * @param {string} bizType 如 monitor:logininfor
 * @param {Record<string, any>} queryParams 筛选条件
 * @param {{ mode?: string, syncMaxRows?: number }} [opts]
 */
export function submitExport(bizType, queryParams, opts = {}) {
  return exportRequest({
    url: '/export/submit',
    method: 'post',
    responseType: 'blob',
    returnBlobWithHeaders: true,
    data: {
      bizType,
      queryParams: queryParams || {},
      mode: opts.mode,
      syncMaxRows: opts.syncMaxRows,
    },
  })
}

/**
 * 查询导出任务。
 * @param {number|string} taskId
 */
export function getExportTask(taskId) {
  return request({
    url: `/export/task/${taskId}`,
    method: 'get',
  })
}

/**
 * 导出任务分页列表。
 * @param {object} params
 */
export function listExportTasks(params) {
  return request({
    url: '/export/task/list',
    method: 'get',
    params,
  })
}
