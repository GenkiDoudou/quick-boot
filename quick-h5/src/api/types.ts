/**
 * API 层通用分页类型，与 quickboot 后端 PageInfo / PageRequest 对齐。
 */

/** 分页查询结果 */
export type PageInfo<T> = {
  /** 当前页码（从 1 起） */
  current: number
  /** 每页条数 */
  size: number
  /** 总记录数 */
  total: number
  /** 总页数 */
  pages: number
  /** 当前页数据列表 */
  records: T[]
}

/** 分页查询请求体 */
export type PageRequest<T> = {
  current: number
  size: number
  /** 业务筛选条件，对应后端 param */
  param?: T
}
