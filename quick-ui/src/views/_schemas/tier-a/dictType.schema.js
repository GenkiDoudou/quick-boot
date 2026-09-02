/**
 * 字典类型 dict/type 页 schema。
 */
export const rowKey = 'dictId'
export const defaultSearch = { dictName: '', dictType: '', status: '' }

/** @param {import('vue').Ref|Array} sysNormalDisable */
export function buildSearchColumns(sysNormalDisable) {
  const options = sysNormalDisable?.value ?? sysNormalDisable ?? []
  return [
    { prop: 'dictName', label: '字典名称', type: 'input', span: 8 },
    { prop: 'dictType', label: '字典类型', type: 'input', span: 8 },
    { prop: 'status', label: '状态', type: 'select', span: 8, props: { options } }
  ]
}

export const tableColumns = [
  { prop: 'dictName', label: '字典名称', minWidth: 140 },
  { prop: 'dictType', label: '字典类型', minWidth: 160, columnType: 'slot', slotName: 'dictType' },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'remark', label: '备注', minWidth: 140 },
  { prop: 'action', label: '操作', width: 160, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

export function formInitial() {
  return { dictId: null, dictName: '', dictType: '', status: '0', remark: '' }
}

export const formRules = {
  dictName: [{ required: true, message: '必填', trigger: 'blur' }],
  dictType: [{ required: true, message: '必填', trigger: 'blur' }]
}
