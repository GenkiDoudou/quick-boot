/**
 * 发布记录 deployRecord 页 schema。
 */
import { applyTimeFormatters } from './_shared'

export const rowKey = 'recordId'
export const defaultSearch = { appName: '', env: '', operate: '' }

export const searchColumns = [
  { prop: 'appName', label: '应用', type: 'input', span: 8 },
  { prop: 'env', label: '环境', type: 'input', span: 8 },
  { prop: 'operate', label: '操作', type: 'input', span: 8 }
]

export const tableColumns = applyTimeFormatters([
  { prop: 'appName', label: '应用', minWidth: 100 },
  { prop: 'env', label: '环境', width: 90 },
  { prop: 'operate', label: '操作', width: 100 },
  { prop: 'branch', label: '分支', minWidth: 120 },
  { prop: 'hosts', label: '主机', minWidth: 140, showOverflowTooltip: true },
  { prop: 'buildNumber', label: '构建号', width: 90 },
  { prop: 'gitCommit', label: 'Commit', width: 100 },
  { prop: 'status', label: '状态', width: 80, columnType: 'slot', slotName: 'status' },
  { prop: 'createTime', label: '时间', width: 170 },
  { prop: 'action', label: '操作', width: 90, fixed: 'right', columnType: 'slot', slotName: 'action' }
])
