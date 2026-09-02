/**
 * 系统参数 config 页 schema：检索列、表格列、表单初始值与校验。
 */
import { applyTimeFormatters } from './_shared'

export const rowKey = 'configId'

export const defaultSearch = { configName: '', configKey: '', configType: '' }

/** @param {import('vue').Ref|Array} sysYesNo 字典 sys_yes_no */
export function buildSearchColumns(sysYesNo) {
  const options = sysYesNo?.value ?? sysYesNo ?? []
  return [
    { prop: 'configName', label: '参数名称', type: 'input', span: 8 },
    { prop: 'configKey', label: '参数键名', type: 'input', span: 8 },
    { prop: 'configType', label: '系统内置', type: 'select', span: 8, props: { options } }
  ]
}

export const tableColumns = [
  { prop: 'configName', label: '参数名称', minWidth: 140 },
  { prop: 'configKey', label: '参数键名', minWidth: 200 },
  { prop: 'configValue', label: '参数键值', minWidth: 180 },
  { prop: 'configType', label: '系统内置', width: 100, columnType: 'slot', slotName: 'configType' },
  { prop: 'remark', label: '备注', minWidth: 140 },
  { prop: 'action', label: '操作', width: 160, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

export function formInitial() {
  return {
    configId: null,
    configName: '',
    configKey: '',
    configValue: '',
    configType: '0',
    remark: ''
  }
}

export const formRules = {
  configName: [{ required: true, message: '必填', trigger: 'blur' }],
  configKey: [{ required: true, message: '必填', trigger: 'blur' }],
  configValue: [{ required: true, message: '必填', trigger: 'blur' }]
}
