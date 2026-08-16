/**
 * 定时任务 API（H5 仅列表 / 改状态 / 执行一次）。
 */
import { request } from '../http'
import type { PageInfo } from '../types'
import { toGetPageQuery } from '../pageQuery'

/** 定时任务 */
export type SysJob = {
  jobId?: number | string
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  cronExpression?: string
  /** 0 正常 / 1 暂停 */
  status?: string
  remark?: string
}

/** 分页列表 */
export function listJob(query: {
  pageNum: number
  pageSize: number
  jobName?: string
  jobGroup?: string
  status?: string
}) {
  return request<PageInfo<SysJob>>({
    url: '/monitor/job/list',
    method: 'GET',
    data: query,
  })
}

/**
 * 便捷分页：对齐 usePagedList。
 * @param status 可选；空串/undefined 表示不筛选
 */
export function pageJobs(current: number, size: number, jobName?: string, status?: string) {
  return listJob({
    ...toGetPageQuery(current, size),
    jobName: jobName || undefined,
    status: status || undefined,
  })
}

/** 修改状态 */
export function changeJobStatus(data: { jobId: number | string; status: string }) {
  return request<void>({
    url: '/monitor/job/changeStatus',
    method: 'POST',
    data,
  })
}

/** 立即执行一次 */
export function runJob(jobId: number | string) {
  return request<string>({
    url: '/monitor/job/run',
    method: 'POST',
    data: { jobId },
  })
}

/** 任务详情 */
export function getJob(jobId: number | string) {
  return request<SysJob>({
    url: `/monitor/job/${encodeURIComponent(String(jobId))}`,
    method: 'GET',
  })
}

/** 批量删除任务；请求体为 jobId 数组 */
export function removeJob(jobIds: Array<number | string>) {
  return request<void>({
    url: '/monitor/job/remove',
    method: 'POST',
    data: jobIds.map((id) => Number(id)),
  })
}
