/**
 * 文件分类 fileClassify 页 schema（检索与表格；表单字段较多仍保留在 SFC）。
 */
export const rowKey = 'classifyId'
export const rowsKey = 'data.records'
export const totalKey = 'data.total'
export const defaultSearch = { classify: '', classifyName: '', status: '' }

/** @param {import('vue').Ref|Array} sysNormalDisable */
export function buildSearchColumns(sysNormalDisable) {
  const options = sysNormalDisable?.value ?? sysNormalDisable ?? []
  return [
    { prop: 'classify', label: '分类键', type: 'input', span: 8, props: { clearable: true } },
    { prop: 'classifyName', label: '展示名', type: 'input', span: 8, props: { clearable: true } },
    { prop: 'status', label: '状态', type: 'select', span: 8, props: { options, clearable: true } }
  ]
}

export const tableColumns = [
  { prop: 'classify', label: '分类键', minWidth: 120 },
  { prop: 'classifyName', label: '展示名', minWidth: 120 },
  { prop: 'limitExt', label: '允许后缀', minWidth: 140, showOverflowTooltip: true },
  { prop: 'limitSizeBytes', label: '大小上限', width: 110, columnType: 'slot', slotName: 'limitSizeBytes' },
  { prop: 'limitCount', label: '数量', width: 80, align: 'center' },
  { prop: 'compressEnabled', label: '压缩', width: 80, columnType: 'slot', slotName: 'compressEnabled' },
  { prop: 'anonymous', label: '匿名', width: 80, columnType: 'slot', slotName: 'anonymous' },
  { prop: 'status', label: '状态', width: 80, columnType: 'slot', slotName: 'status' },
  { prop: 'action', label: '操作', width: 140, fixed: 'right', columnType: 'slot', slotName: 'action' }
]
