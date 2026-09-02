/**
 * 调度日志 job-log 页 schema 与查询规范化。
 */
import { applyTimeFormatters, omitEmptyStringFields, splitDateRangeParam, toLegacyPageQuery } from './_shared'

export const rowKey = 'jobLogId'
export const rowsKey = 'data.records'
export const totalKey = 'data.total'

export const defaultSearch = {
  jobName: '',
  jobGroup: '',
  status: '',
  timeRange: []
}

/** @param {object} dicts sys_job_group / sys_job_log_status */
export function buildSearchColumns(dicts) {
  return [
    { prop: 'jobName', label: '任务名称', type: 'input', span: 8, props: { clearable: true } },
    {
      prop: 'jobGroup',
      label: '任务组名',
      type: 'select',
      span: 8,
      options: dicts.sys_job_group?.value ?? dicts.sys_job_group ?? [],
      props: { clearable: true, style: 'width: 240px' }
    },
    {
      prop: 'status',
      label: '执行状态',
      type: 'select',
      span: 8,
      options: dicts.sys_job_log_status?.value ?? dicts.sys_job_log_status ?? [],
      props: { clearable: true, style: 'width: 240px' }
    },
    {
      prop: 'timeRange',
      label: '执行时间',
      type: 'daterange',
      span: 16,
      props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' }
    }
  ]
}

/** @param {object} dicts 字典 ref，供 jobGroup 列 tag 渲染 */
export function buildTableColumns(dicts) {
  return applyTimeFormatters([
    { prop: 'jobLogId', label: '日志编号', width: 100 },
    { prop: 'jobName', label: '任务名称', minWidth: 120, showOverflowTooltip: true },
    {
      prop: 'jobGroup',
      label: '任务组名',
      width: 100,
      columnType: 'tag',
      dictList: dicts.sys_job_group?.value ?? dicts.sys_job_group ?? []
    },
    { prop: 'invokeTarget', label: '调用目标', minWidth: 120, showOverflowTooltip: true },
    { prop: 'jobMessage', label: '日志信息', minWidth: 120, showOverflowTooltip: true },
    { prop: 'status', label: '执行状态', columnType: 'slot', slotName: 'status', width: 100 },
    { prop: 'createTime', label: '执行时间', width: 180 },
    { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 90, fixed: 'right' }
  ])
}

/** job-log 列表/导出 query 规范化 */
export function normalizeJobLogParams(raw) {
  let p = splitDateRangeParam({ ...raw }, 'timeRange')
  p = omitEmptyStringFields(p, ['status', 'jobGroup', 'jobName'])
  return p
}

/** 包装 listJobLog / exportJobLog 的 page 请求 */
export function toJobLogQuery(pageReq) {
  return toLegacyPageQuery(pageReq, normalizeJobLogParams)
}
