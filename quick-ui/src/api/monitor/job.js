/**
 * 定时任务 API（前缀 {@code /monitor/job}）。
 */
import request from '@/utils/request'
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'

const crud = createCrudApi('/monitor/job', {
  export: true,
  exportAsQuery: true,
  paths: { add: '', update: 'edit' }
})

/** 定时任务分页（POST page）。 */
export const pageJob = crud.page

/** 定时任务分页列表（兼容 C7JsonTable 扁平 query）。 */
export function listJob(query) {
  return crud.page(toPageRequest(query))
}

/** 定时任务详情。 */
export const getJob = crud.get
/** 新增定时任务。 */
export const addJob = crud.add
/** 修改定时任务。 */
export const updateJob = crud.update
/** 批量删除。 */
export const removeJob = crud.remove
/** 同步导出。 */
export const exportJob = crud.export

/** 可选调用目标（实现 ITask 的 Spring Bean） */
export function listJobInvokeTargets() {
  return request({ url: '/monitor/job/invokeTargets', method: 'get' })
}

/** 修改状态 */
export function changeJobStatus(data) {
  return request({ url: '/monitor/job/changeStatus', method: 'post', data })
}

/** 立即执行 */
export function runJob(data) {
  return request({ url: '/monitor/job/run', method: 'post', data })
}
