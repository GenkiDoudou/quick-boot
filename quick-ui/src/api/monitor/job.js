import request from '@/utils/request'

/** 定时任务分页列表 */
export function listJob(query) {
  return request({ url: '/monitor/job/list', method: 'get', params: query })
}

/** 定时任务详情 */
export function getJob(jobId) {
  return request({ url: '/monitor/job/' + jobId, method: 'get' })
}

/** 可选调用目标（实现 ITask 的 Spring Bean） */
export function listJobInvokeTargets() {
  return request({ url: '/monitor/job/invokeTargets', method: 'get' })
}

/** 新增定时任务 */
export function addJob(data) {
  return request({ url: '/monitor/job', method: 'post', data })
}

/** 修改定时任务 */
export function updateJob(data) {
  return request({ url: '/monitor/job/edit', method: 'post', data })
}

/** 批量删除 */
export function removeJob(jobIds) {
  return request({ url: '/monitor/job/remove', method: 'post', data: jobIds })
}

/** 修改状态 */
export function changeJobStatus(data) {
  return request({ url: '/monitor/job/changeStatus', method: 'post', data })
}

/** 立即执行 */
export function runJob(data) {
  return request({ url: '/monitor/job/run', method: 'post', data })
}

/** 同步导出 */
export function exportJob(snapshot) {
  return request({
    url: '/monitor/job/export',
    method: 'post',
    params: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}
