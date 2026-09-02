/**
 * 登录日志 logininfor 页 schema。
 */
import { applyTimeFormatters } from './_shared'

export const rowKey = 'infoId'
export const defaultSearch = { ipaddr: '', userName: '', clientId: '', status: '' }

/** @param {import('vue').Ref|Array} sysLoginStatus */
export function buildSearchColumns(sysLoginStatus) {
  const options = sysLoginStatus?.value ?? sysLoginStatus ?? []
  return [
    { prop: 'ipaddr', label: '登录地址', type: 'input', span: 8 },
    { prop: 'userName', label: '用户名', type: 'input', span: 8 },
    { prop: 'clientId', label: '客户端ID', type: 'input', span: 8 },
    { prop: 'status', label: '状态', type: 'select', span: 8, options }
  ]
}

export const tableColumns = applyTimeFormatters([
  { prop: 'userName', label: '用户名', minWidth: 110 },
  { prop: 'clientId', label: '客户端', width: 100 },
  { prop: 'ipaddr', label: 'IP', width: 130 },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'msg', label: '描述', minWidth: 160, showOverflowTooltip: true },
  { prop: 'loginTime', label: '访问时间', width: 170 },
  { prop: 'action', label: '操作', width: 90, fixed: 'right', columnType: 'slot', slotName: 'action' }
])
