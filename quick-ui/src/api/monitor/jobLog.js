/**
 * 调度日志 API（前缀 {@code /monitor/jobLog}）。
 */
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'
import request from '@/utils/request'

const crud = createCrudApi('/monitor/jobLog', { export: true, exportAsQuery: true })

/** 调度日志分页（POST page）。 */
export const pageJobLog = crud.page

/** 调度日志分页列表（兼容 C7JsonTable）。 */
export function listJobLog(query) {
  return crud.page(toPageRequest(query))
}

/** 调度日志详情。 */
export const getJobLog = crud.get
/** 批量删除。 */
export const removeJobLog = crud.remove
/** 同步导出。 */
export const exportJobLog = crud.export

/** 清空调度日志 */
export function cleanJobLog() {
  return request({ url: '/monitor/jobLog/clean', method: 'post' })
}
