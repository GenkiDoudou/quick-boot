/**
 * 调度日志 API。
 */
import { request } from '../http'
import type { PageInfo } from '../types'
import { toGetPageQuery } from '../pageQuery'

/** 调度日志 */
export type SysJobLog = {
  jobLogId?: number | string
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  jobMessage?: string
  status?: string
  exceptionInfo?: string
  createTime?: string
}

/** 分页列表 */
export function listJobLog(query: {
  pageNum: number
  pageSize: number
  jobName?: string
  status?: string
}) {
  return request<PageInfo<SysJobLog>>({
    url: '/monitor/jobLog/list',
    method: 'GET',
    data: query,
  })
}

/**
 * 便捷分页：对齐 usePagedList。
 * @param status 可选；0 成功 / 1 失败；空串不传
 */
export function pageJobLogs(current: number, size: number, jobName?: string, status?: string) {
  return listJobLog({
    ...toGetPageQuery(current, size),
    jobName: jobName || undefined,
    status: status || undefined,
  })
}

/** 详情 */
export function getJobLog(jobLogId: number | string) {
  return request<SysJobLog>({
    url: `/monitor/jobLog/${encodeURIComponent(String(jobLogId))}`,
    method: 'GET',
  })
}

/** 批量删除 */
export function removeJobLog(ids: Array<number | string>) {
  return request<void>({
    url: '/monitor/jobLog/remove',
    method: 'POST',
    data: ids,
  })
}

/** 清空 */
export function cleanJobLog() {
  return request<void>({
    url: '/monitor/jobLog/clean',
    method: 'POST',
  })
}
