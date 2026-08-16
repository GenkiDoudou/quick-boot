/**
 * GET 列表分页适配：将 usePagedList 的 current/size 转为 pageNum/pageSize。
 * 后端仍返回 PageInfo（records/total），无需再转 rows。
 */
export type GetPageQuery = {
  pageNum: number
  pageSize: number
}

/** 由 composable 页码构造 GET 分页参数 */
export function toGetPageQuery(current: number, size: number): GetPageQuery {
  return {
    pageNum: current < 1 ? 1 : current,
    pageSize: size < 1 ? 10 : size,
  }
}
