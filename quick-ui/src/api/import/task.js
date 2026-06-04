import request, { importRequest } from '@/utils/request'
import { appendImportFormFields } from '@/utils/excelImportForm'

/**
 * 提交 Excel 导入。
 * @param {File} file
 * @param {string} bizType 如 system:user
 * @param {boolean} updateSupport
 * @param {{ mode?: string, syncMaxRows?: number, forceAsync?: boolean, contextJson?: object|string }} [opts]
 */
export function submitImport(file, bizType, updateSupport, opts = {}) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  appendImportFormFields(formData, updateSupport, opts)
  return importRequest({
    url: '/import/submit',
    method: 'post',
    data: formData
  })
}

/**
 * 查询导入任务。
 * @param {number|string} taskId
 */
export function getImportTask(taskId) {
  return request({
    url: `/import/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 导入任务分页列表。
 * @param {object} params
 */
export function listImportTasks(params) {
  return request({
    url: '/import/task/list',
    method: 'get',
    params
  })
}

/**
 * 下载导入失败明细 xlsx（按任务归属校验，无需单独 file:download 权限）。
 * @param {number|string} fileId
 */
export function downloadImportErrorFile(fileId) {
  return request({
    url: `/import/error-file/${fileId}`,
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}
