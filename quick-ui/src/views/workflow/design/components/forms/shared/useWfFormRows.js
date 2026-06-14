import { ref, watch } from 'vue'

/**
 * 工作流表单行编辑：本地 rows + syncing 守卫，避免 emit 后 watch 重置导致删除失效。
 * @param {object} options
 * @param {() => unknown} options.getSource 从 modelValue 读取源数组
 * @param {(item: unknown, idx: number, prevRows: Array, nextRowId: (prefix?: string) => string) => object} options.toRow
 * @param {(rows: Array) => unknown} options.fromRows 同步回 model 的数据结构
 * @param {(val: unknown) => void} options.emitModel 合并写回 modelValue
 * @param {(nextRowId: (prefix?: string) => string) => object} [options.createRow] 新建空行
 * @param {boolean} [options.allowEmpty=false] 是否允许空列表
 */
export function useWfFormRows(options) {
  const {
    getSource,
    toRow,
    fromRows,
    emitModel,
    createRow,
    allowEmpty = false
  } = options

  const rows = ref([])
  let rowSeq = 0
  let syncing = false

  /** @param {string} [prefix] */
  function nextRowId(prefix = 'row') {
    return `${prefix}_${++rowSeq}`
  }

  watch(
    getSource,
    (val) => {
      if (syncing) return
      const list = Array.isArray(val) ? val : []
      if (!list.length && createRow && !allowEmpty) {
        rows.value = [createRow(nextRowId)]
        return
      }
      rows.value = list.map((item, idx) => toRow(item, idx, rows.value, nextRowId))
    },
    { immediate: true, deep: true }
  )

  function sync() {
    syncing = true
    emitModel(fromRows(rows.value))
    queueMicrotask(() => {
      syncing = false
    })
  }

  function addRow() {
    if (!createRow) return
    rows.value.push(createRow(nextRowId))
    sync()
  }

  function removeRow(idx) {
    rows.value.splice(idx, 1)
    if (!rows.value.length && createRow && !allowEmpty) {
      rows.value.push(createRow(nextRowId))
    }
    sync()
  }

  return { rows, sync, addRow, removeRow, nextRowId }
}
