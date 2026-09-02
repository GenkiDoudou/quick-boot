/**
 * 日期时间格式化（dayjs 解析 + 若依占位符输出）；供表格列与日志展示统一使用。
 */
import dayjs from 'dayjs'

/** 默认日期时间格式（dayjs 语法）。 */
export const DATETIME_PATTERN = 'YYYY-MM-DD HH:mm:ss'

/** 仅日期格式（dayjs 语法）。 */
export const DATE_PATTERN = 'YYYY-MM-DD'

/**
 * 规范化输入为 dayjs（兼容 10 位秒级时间戳与 Date）。
 * @param {Date|string|number} time
 * @returns {dayjs.Dayjs|null}
 */
function toDayjs(time) {
  if (time == null || time === '') {
    return null
  }
  if (time instanceof Date) {
    return dayjs(time)
  }
  if (typeof time === 'number') {
    const ms = time.toString().length === 10 ? time * 1000 : time
    return dayjs(ms)
  }
  if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
    const n = Number(time)
    const ms = time.length === 10 ? n * 1000 : n
    return dayjs(ms)
  }
  const parsed = dayjs(time)
  return parsed.isValid() ? parsed : null
}

/**
 * 格式化日期时间；pattern 支持 dayjs 格式或若依 `{y}-{m}-{d}` 占位符（与旧 parseTime 一致）。
 * @param {Date|string|number} time
 * @param {string} [pattern=DATETIME_PATTERN]
 * @returns {string|null}
 */
export function formatTime(time, pattern = DATETIME_PATTERN) {
  const d = toDayjs(time)
  if (!d) {
    return null
  }
  const raw = pattern || DATETIME_PATTERN
  if (!raw.includes('{')) {
    return d.format(raw)
  }
  const formatObj = {
    y: d.year(),
    m: d.month() + 1,
    d: d.date(),
    h: d.hour(),
    i: d.minute(),
    s: d.second(),
    a: d.day()
  }
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return raw.replace(/\{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    if (key === 'a') {
      return weekdays[value] ?? ''
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return String(value ?? 0)
  })
}

/** 与 {@link formatTime} 同义，供 schema 列 formatter 引用。 */
export const formatDateTime = formatTime

/** @deprecated 请改用 {@link formatTime} */
export const parseTime = formatTime

/**
 * C7JsonTable 时间列 formatter 工厂。
 * @param {string} [pattern=DATETIME_PATTERN]
 */
export function createTimeColumnFormatter(pattern = DATETIME_PATTERN) {
  return (_row, _column, cellValue) => formatTime(cellValue, pattern) || ''
}
