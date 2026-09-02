/**
 * 字典数据 dict/data 页 schema。
 */
export const rowKey = 'dictCode'
export const defaultSearch = { dictLabel: '', status: '' }

/** @param {import('vue').Ref|Array} sysNormalDisable */
export function buildSearchColumns(sysNormalDisable) {
  const options = sysNormalDisable?.value ?? sysNormalDisable ?? []
  return [
    { prop: 'dictLabel', label: '字典标签', type: 'input', span: 8 },
    { prop: 'status', label: '状态', type: 'select', span: 8, props: { options } }
  ]
}

export const tableColumns = [
  { prop: 'dictLabel', label: '标签', minWidth: 120 },
  { prop: 'dictValue', label: '键值', minWidth: 120 },
  { prop: 'dictSort', label: '排序', width: 80 },
  { prop: 'status', label: '状态', width: 80, columnType: 'slot', slotName: 'status' },
  { prop: 'remark', label: '备注', minWidth: 120 },
  { prop: 'action', label: '操作', width: 100, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

export function formInitial(dictType = '') {
  return {
    dictCode: null,
    dictType,
    dictLabel: '',
    dictValue: '',
    dictSort: 0,
    listClass: 'default',
    status: '0',
    remark: ''
  }
}

export const formRules = {
  dictLabel: [{ required: true, message: '必填', trigger: 'blur' }],
  dictValue: [{ required: true, message: '必填', trigger: 'blur' }],
  dictSort: [{ required: true, message: '必填', trigger: 'change' }]
}
