/**
 * 字典数据项 API。
 */
import { createCrudApi } from '@/api/_factory/createCrudApi'
import request from '@/utils/request'

const crud = createCrudApi('/sys/dict/data', { export: true })

export const pageDictData = crud.page
export const getData = crud.get
export const addData = crud.add
export const updateData = crud.update
export const delData = crud.remove
export const exportData = crud.export
export const downloadDataImportTemplate = crud.downloadImportTemplate
export const importData = crud.importExcel

/** 按字典类型查询全部数据项（供 useDict 使用） */
export function getDicts(dictType) {
  return request({ url: `/sys/dict/data/type/${encodeURIComponent(dictType)}`, method: 'get' })
}
