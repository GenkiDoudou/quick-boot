/**
 * 操作日志 operlog 页 schema 与查询参数规范化。
 */
import { applyTimeFormatters, omitEmptyStringFields, toNumberOrUndefined } from './_shared'

export const rowKey = 'operId'
export const defaultSearch = {
  title: '',
  operName: '',
  businessType: '',
  status: '',
  traceId: '',
  clientId: '',
  costTimeMin: undefined,
  costTimeMax: undefined
}

/** @param {object} dicts sys_oper_business_type / sys_oper_status */
export function buildSearchColumns(dicts) {
  return [
    { prop: 'title', label: '系统模块', type: 'input', span: 8 },
    { prop: 'operName', label: '操作人员', type: 'input', span: 8 },
    {
      prop: 'businessType',
      label: '业务类型',
      type: 'select',
      span: 8,
      options: dicts.sys_oper_business_type?.value ?? dicts.sys_oper_business_type ?? []
    },
    {
      prop: 'status',
      label: '状态',
      type: 'select',
      span: 8,
      options: dicts.sys_oper_status?.value ?? dicts.sys_oper_status ?? []
    },
    { prop: 'traceId', label: '链路ID', type: 'input', span: 8 },
    { prop: 'clientId', label: '客户端ID', type: 'input', span: 8 },
    {
      prop: 'costTimeMin',
      label: '耗时≥(ms)',
      type: 'input',
      span: 8,
      props: { type: 'number', clearable: true, placeholder: '最小耗时' }
    },
    {
      prop: 'costTimeMax',
      label: '耗时≤(ms)',
      type: 'input',
      span: 8,
      props: { type: 'number', clearable: true, placeholder: '最大耗时' }
    }
  ]
}

export const tableColumns = applyTimeFormatters([
  { prop: 'title', label: '系统模块', minWidth: 160, showOverflowTooltip: true },
  { prop: 'businessType', label: '业务类型', width: 110, columnType: 'slot', slotName: 'businessType' },
  { prop: 'operName', label: '操作人员', width: 110 },
  { prop: 'clientId', label: '客户端', width: 110, showOverflowTooltip: true },
  { prop: 'operIp', label: 'IP', width: 130 },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'operTime', label: '操作时间', width: 170 },
  { prop: 'costTime', label: '耗时(ms)', width: 100 },
  { prop: 'traceId', label: '链路ID', minWidth: 140, showOverflowTooltip: true },
  { prop: 'action', label: '操作', width: 90, fixed: 'right', columnType: 'slot', slotName: 'action' }
])

/** 规范化耗时区间等筛选参数 */
export function normalizeOperParam(param) {
  const p = { ...(param || {}) }
  p.costTimeMin = toNumberOrUndefined(p.costTimeMin)
  p.costTimeMax = toNumberOrUndefined(p.costTimeMax)
  return omitEmptyStringFields(p, ['businessType', 'status'])
}
