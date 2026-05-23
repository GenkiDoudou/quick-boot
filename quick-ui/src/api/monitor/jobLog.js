import request, { downloadRequest } from '@/utils/request'

/** 调度日志分页列表 */
export function listJobLog(query) {
  return request({ url: '/monitor/jobLog/list', method: 'get', params: query })
}

/** 调度日志详情 */
export function getJobLog(jobLogId) {
  return request({ url: '/monitor/jobLog/' + jobLogId, method: 'get' })
}

/** 批量删除 */
export function removeJobLog(jobLogIds) {
  return request({ url: '/monitor/jobLog/remove', method: 'post', data: jobLogIds })
}

/** 清空 */
export function cleanJobLog() {
  return request({ url: '/monitor/jobLog/clean', method: 'post' })
}

/** 导出 */
export function exportJobLog(data) {
  return downloadRequest('/monitor/jobLog/export', data, { returnBlobWithHeaders: true })
}
