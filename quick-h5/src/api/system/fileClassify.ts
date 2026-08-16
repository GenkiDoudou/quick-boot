/**
 * 文件分类管理 API，对接 `/system/fileClassify`。
 * 列表为 GET pageNum/pageSize，响应仍为 PageInfo。
 */
import { request } from '../http'
import type { PageInfo } from '../types'

/** 文件分类 */
export type SysFileClassify = {
  classifyId?: number | string
  classify?: string
  classifyName?: string
  limitExt?: string
  limitSizeBytes?: number | null
  limitCount?: number | null
  status?: string
  remark?: string
}

/**
 * 分页列表（GET 查询参数）。
 * @param query pageNum/pageSize 及筛选字段
 */
export function listFileClassify(query: {
  pageNum: number
  pageSize: number
  classify?: string
  classifyName?: string
  status?: string
}) {
  return request<PageInfo<SysFileClassify>>({
    url: '/system/fileClassify/list',
    method: 'GET',
    data: query,
  })
}

/** 分类详情 */
export function getFileClassify(id: number | string) {
  return request<SysFileClassify>({
    url: `/system/fileClassify/${encodeURIComponent(String(id))}`,
    method: 'GET',
  })
}

/** 新增分类 */
export function addFileClassify(data: Partial<SysFileClassify>) {
  return request<string | number>({
    url: '/system/fileClassify/add',
    method: 'POST',
    data,
  })
}

/** 修改分类（不可改 classify 键） */
export function updateFileClassify(data: Partial<SysFileClassify>) {
  return request<boolean>({
    url: '/system/fileClassify/update',
    method: 'POST',
    data,
  })
}

/** 批量删除分类 */
export function removeFileClassify(ids: Array<number | string>) {
  return request<void>({
    url: '/system/fileClassify/remove',
    method: 'POST',
    data: ids,
  })
}
