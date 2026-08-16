/**
 * 字典类型管理 API，对接 `/sys/dict/type`（与只读 `dict.ts` 分离）。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'

/** 字典类型 */
export type SysDictType = {
  dictId?: number | string
  dictName?: string
  dictType?: string
  status?: string
  remark?: string
}

/** 分页查询字典类型 */
export function pageDictType(pageRequest: PageRequest<Partial<SysDictType>>) {
  return request<PageInfo<SysDictType>>({
    url: '/sys/dict/type/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 字典类型详情 */
export function getDictType(dictId: number | string) {
  return request<SysDictType>({
    url: `/sys/dict/type/${encodeURIComponent(String(dictId))}`,
    method: 'GET',
  })
}

/** 新增字典类型 */
export function addDictType(data: Partial<SysDictType>) {
  return request<string | number>({
    url: '/sys/dict/type/add',
    method: 'POST',
    data,
  })
}

/** 修改字典类型 */
export function updateDictType(data: Partial<SysDictType>) {
  return request<boolean>({
    url: '/sys/dict/type/update',
    method: 'POST',
    data,
  })
}

/** 批量删除字典类型 */
export function removeDictType(ids: Array<number | string>) {
  return request<void>({
    url: '/sys/dict/type/remove',
    method: 'POST',
    data: ids.map(String),
  })
}

/** 刷新全部字典类型缓存 */
export function refreshAllDictType() {
  return request<void>({
    url: '/sys/dict/type/refresh',
    method: 'POST',
  })
}
