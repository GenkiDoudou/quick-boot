/**
 * 字典数据管理 API，对接 `/sys/dict/data`。
 */
import { request } from '../http'
import type { PageInfo, PageRequest } from '../types'
import type { SysDictData } from './dict'

export type { SysDictData }

/** 分页查询字典数据（param 常含 dictType） */
export function pageDictData(pageRequest: PageRequest<Partial<SysDictData>>) {
  return request<PageInfo<SysDictData>>({
    url: '/sys/dict/data/page',
    method: 'POST',
    data: pageRequest,
  })
}

/** 字典数据详情 */
export function getDictData(dictCode: number | string) {
  return request<SysDictData>({
    url: `/sys/dict/data/${encodeURIComponent(String(dictCode))}`,
    method: 'GET',
  })
}

/** 新增字典数据 */
export function addDictData(data: Partial<SysDictData>) {
  return request<string | number>({
    url: '/sys/dict/data/add',
    method: 'POST',
    data,
  })
}

/** 修改字典数据 */
export function updateDictData(data: Partial<SysDictData>) {
  return request<boolean>({
    url: '/sys/dict/data/update',
    method: 'POST',
    data,
  })
}

/** 批量删除字典数据 */
export function removeDictData(ids: Array<number | string>) {
  return request<void>({
    url: '/sys/dict/data/remove',
    method: 'POST',
    data: ids.map(String),
  })
}
