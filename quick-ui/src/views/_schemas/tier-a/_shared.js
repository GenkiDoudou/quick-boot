/**
 * Tier A CRUD 页 schema 共用工具：分页参数适配、时间列 formatter、日期范围拆解。
 */
import { createTimeColumnFormatter } from '@/utils/formatTime'

/** 时间列 prop 默认集合（loginTime / operTime / createTime 等）。 */
export const DEFAULT_TIME_COLUMN_PROPS = ['createTime', 'loginTime', 'operTime']

/**
 * 为 tableColumns 中指定 prop 注入 formatter（dayjs 统一格式）。
 *
 * @param {Array} columns 列配置
 * @param {string[]} [timeProps=DEFAULT_TIME_COLUMN_PROPS] 需格式化的 prop 列表
 * @returns {Array} 新数组（不 mutate 原列）
 */
export function applyTimeFormatters(columns, timeProps = DEFAULT_TIME_COLUMN_PROPS) {
  const formatter = createTimeColumnFormatter()
  const props = new Set(timeProps)
  return (columns || []).map((col) => {
    if (!col?.prop || !props.has(col.prop) || col.formatter || col.columnType === 'slot') {
      return col
    }
    return { ...col, formatter }
  })
}

/**
 * C7JsonTable 标准 POST page 请求 → 遗留 GET list（pageNum/pageSize 扁平 query）。
 *
 * @param {object} pageReq C7 传入的 { current, size, param?, ... }
 * @param {(object) => object} [normalizeParam] 对 param 的额外规范化
 */
export function toLegacyPageQuery(pageReq, normalizeParam) {
  const raw = pageReq && typeof pageReq === 'object' ? pageReq : {}
  const nested =
    raw.param && typeof raw.param === 'object' && !Array.isArray(raw.param)
      ? { ...raw.param }
      : { ...raw }
  delete nested.current
  delete nested.size
  delete nested.param
  let merged = {
    ...nested,
    pageNum: raw.current ?? raw.pageNum ?? 1,
    pageSize: raw.size ?? raw.pageSize ?? 10
  }
  if (normalizeParam) {
    merged = normalizeParam(merged)
  }
  return merged
}

/**
 * 将 daterange 字段拆为 beginTime/endTime 并删除 range 字段。
 *
 * @param {object} params 查询参数
 * @param {string} rangeKey 范围字段名，如 timeRange / createTimeRange
 * @param {string} [beginKey='beginTime']
 * @param {string} [endKey='endTime']
 */
export function splitDateRangeParam(params, rangeKey, beginKey = 'beginTime', endKey = 'endTime') {
  const p = { ...params }
  const range = p[rangeKey]
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p[beginKey] = range[0]
    p[endKey] = range[1]
  }
  delete p[rangeKey]
  return p
}

/**
 * 空字符串筛选字段删除，避免后端误判。
 *
 * @param {object} params
 * @param {string[]} keys
 */
export function omitEmptyStringFields(params, keys) {
  const p = { ...params }
  for (const key of keys) {
    if (p[key] === '') {
      delete p[key]
    }
  }
  return p
}

/**
 * 数字型筛选：空串转 undefined。
 *
 * @param {unknown} value
 * @returns {number|undefined}
 */
export function toNumberOrUndefined(value) {
  if (value === '' || value == null) {
    return undefined
  }
  const n = Number(value)
  return Number.isFinite(n) ? n : undefined
}
