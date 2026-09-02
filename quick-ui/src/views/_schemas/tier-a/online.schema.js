/**
 * 在线用户 online 页 schema。
 */
import { applyTimeFormatters } from './_shared'

export const rowKey = 'tokenId'
export const defaultSearch = { ipaddr: '', userName: '' }
export const rowsKey = 'data.records'
export const totalKey = 'data.total'

export const searchColumns = [
  { prop: 'ipaddr', label: '登录地址', type: 'input', span: 8, props: { placeholder: '请输入登录地址', clearable: true } },
  { prop: 'userName', label: '用户名称', type: 'input', span: 8, props: { placeholder: '请输入用户名称', clearable: true } }
]

export const tableColumns = applyTimeFormatters([
  { prop: 'tokenId', label: '会话编号', minWidth: 200, showOverflowTooltip: true },
  { prop: 'userName', label: '登录名称', width: 120 },
  { prop: 'deptName', label: '部门', width: 140, showOverflowTooltip: true },
  { prop: 'ipaddr', label: '主机', width: 140 },
  { prop: 'loginLocation', label: '登录地点', width: 120 },
  { prop: 'browser', label: '浏览器', width: 120, showOverflowTooltip: true },
  { prop: 'os', label: '操作系统', width: 120, showOverflowTooltip: true },
  { prop: 'loginTime', label: '登录时间', width: 180 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 100, fixed: 'right' }
])
