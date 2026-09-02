/**
 * 字典类型 API。
 */
import { createCrudApi } from '@/api/_factory/createCrudApi'
import request from '@/utils/request'

const crud = createCrudApi('/sys/dict/type', { export: true })

export const pageDictType = crud.page
export const getType = crud.get
export const addType = crud.add
export const updateType = crud.update
export const removeType = crud.remove
export const exportType = crud.export
export const downloadTypeImportTemplate = crud.downloadImportTemplate
export const importType = crud.importExcel

/** 刷新全部字典类型缓存 */
export function refreshAllType() {
  return request({ url: '/sys/dict/type/refresh', method: 'post' })
}

/** 按 dictType 刷新单类字典缓存 */
export function refreshType(dictType) {
  return request({ url: `/sys/dict/type/refresh/${encodeURIComponent(dictType)}`, method: 'post' })
}
