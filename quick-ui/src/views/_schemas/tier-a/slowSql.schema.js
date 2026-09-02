/**
 * 慢 SQL slowSql 页 schema：筛选项、列定义、列表参数规范化。
 */
import { splitDateRangeParam } from './_shared'

export const rowKey = 'slowId'
export const rowsKey = 'data.records'
export const totalKey = 'data.total'

export const SQL_SOURCE_OPTIONS = [
  { label: '业务', value: 'BUSINESS' },
  { label: '积木', value: 'JIMU' },
  { label: '系统', value: 'SYSTEM' }
]

export const SQL_TYPE_OPTIONS = [
  { label: 'SELECT', value: 'SELECT' },
  { label: 'INSERT', value: 'INSERT' },
  { label: 'UPDATE', value: 'UPDATE' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'MERGE', value: 'MERGE' },
  { label: 'EXEC', value: 'EXEC' },
  { label: 'CREATE', value: 'CREATE' },
  { label: 'ALTER', value: 'ALTER' },
  { label: 'DROP', value: 'DROP' },
  { label: 'TRUNCATE', value: 'TRUNCATE' },
  { label: 'OTHER', value: 'OTHER' }
]

export const defaultSearch = {
  sqlSource: '',
  sqlType: '',
  mapperId: '',
  sqlText: '',
  requestUri: '',
  traceId: '',
  minCostTime: '',
  createTimeRange: []
}

export const searchColumns = [
  { prop: 'sqlSource', label: '来源', type: 'select', span: 8, options: SQL_SOURCE_OPTIONS, props: { placeholder: '来源', clearable: true, style: 'width: 240px' } },
  { prop: 'sqlType', label: '操作类型', type: 'select', span: 8, options: SQL_TYPE_OPTIONS, props: { placeholder: '操作类型', clearable: true, style: 'width: 240px' } },
  { prop: 'traceId', label: '链路ID', type: 'input', span: 8, props: { placeholder: 'traceId', clearable: true } },
  { prop: 'mapperId', label: 'Mapper', type: 'input', span: 8, props: { placeholder: 'Mapper 片段', clearable: true } },
  { prop: 'sqlText', label: 'SQL', type: 'input', span: 8, props: { placeholder: 'SQL 片段', clearable: true } },
  { prop: 'requestUri', label: '请求URI', type: 'input', span: 8, props: { placeholder: 'URI 片段', clearable: true } },
  { prop: 'minCostTime', label: '最小耗时(ms)', type: 'input', span: 8, props: { clearable: true } },
  { prop: 'createTimeRange', label: '记录时间', type: 'daterange', span: 16, props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' } }
]

export const tableColumns = [
  { prop: 'slowId', label: '编号', width: 110 },
  { prop: 'sqlSource', label: '来源', columnType: 'slot', slotName: 'sqlSource', width: 90 },
  { prop: 'sqlType', label: '操作类型', columnType: 'slot', slotName: 'sqlType', width: 100 },
  { prop: 'costTime', label: '耗时(ms)', width: 100, sortable: 'custom' },
  { prop: 'sqlText', label: 'SQL', columnType: 'slot', slotName: 'sqlText', minWidth: 280 },
  { prop: 'traceId', label: '链路ID', minWidth: 120, showOverflowTooltip: true },
  { prop: 'mapperId', label: 'Mapper', minWidth: 160, showOverflowTooltip: true },
  { prop: 'requestUri', label: '请求URI', minWidth: 140, showOverflowTooltip: true },
  { prop: 'createTime', label: '记录时间', columnType: 'slot', slotName: 'createTime', width: 170, sortable: 'custom' },
  { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 90, fixed: 'right' }
]

/** C7JsonTable 列表/导出 query：日期范围与空串清理 */
export function normalizeListParams(raw) {
  const p = splitDateRangeParam({ ...(raw || {}) }, 'createTimeRange')
  Object.keys(p).forEach((k) => {
    if (p[k] === '') delete p[k]
  })
  if (p.minCostTime != null && p.minCostTime !== '') {
    p.minCostTime = Number(p.minCostTime)
  }
  return p
}

/** SQL 类型 → el-tag type */
export function sqlTypeTagType(sqlType) {
  switch (sqlType) {
    case 'SELECT':
      return 'success'
    case 'INSERT':
      return ''
    case 'UPDATE':
      return 'warning'
    case 'DELETE':
      return 'danger'
    case 'MERGE':
    case 'EXEC':
      return 'primary'
    default:
      return 'info'
  }
}

/** 来源 → el-tag type */
export function sourceTagType(source) {
  if (source === 'JIMU') return 'warning'
  if (source === 'SYSTEM') return 'info'
  return 'primary'
}

/** 列表 SQL 单行预览 */
export function sqlPreview(text, maxLen = 160) {
  if (!text) return '—'
  const oneLine = String(text).replace(/\s+/g, ' ').trim()
  return oneLine.length > maxLen ? `${oneLine.slice(0, maxLen)}…` : oneLine
}
