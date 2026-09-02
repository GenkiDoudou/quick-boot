/** C7JsonTable 可单测纯函数：分页请求体、搜索列排序与列显隐。 */

const KNOWN_SEARCH_TYPES = new Set(['input', 'select', 'date', 'daterange', 'slot', undefined, ''])

/**
 * 过滤未知类型并按 order 升序排列搜索列。
 *
 * @param {Array<Record<string, unknown>> | undefined} cols
 * @returns {Array<Record<string, unknown>>}
 */
export function sortSearchColumns(cols) {
  return (cols || [])
    .filter((col) => {
      const t = col.type
      return !t || KNOWN_SEARCH_TYPES.has(t) || t === ''
    })
    .slice()
    .sort((a, b) => {
      const oa = a.order
      const ob = b.order
      if (oa == null && ob == null) return 0
      if (oa == null) return 1
      if (ob == null) return -1
      return Number(oa) - Number(ob)
    })
}

/**
 * 组装对齐后端 PageRequest 的列表请求参数。
 *
 * @param {{ current: number, size: number, searchParam?: Record<string, unknown>, orderByColumn?: string, isAsc?: string }} input
 */
export function buildListRequest({ current, size, searchParam = {}, orderByColumn = '', isAsc = '' }) {
  const param = { ...searchParam }
  if (orderByColumn) {
    param.orderByColumn = orderByColumn
    param.isAsc = isAsc
  }
  return {
    current,
    size,
    param,
  }
}

/**
 * 根据列显隐勾选映射有效表格列（过滤 visible=false）。
 *
 * @param {Array<Record<string, unknown>> | undefined} tableColumns
 * @param {Record<string, boolean>} columnCheck
 */
export function resolveEffectiveTableColumns(tableColumns, columnCheck) {
  return (tableColumns || [])
    .filter((col) => col && typeof col === 'object')
    .map((col) => {
      if (!col.prop) {
        return col
      }
      const checked = columnCheck[col.prop]
      const visible = checked === undefined ? col.visible !== false && col._visible !== false : checked
      return { ...col, visible }
    })
    .filter((col) => col.visible !== false)
}
