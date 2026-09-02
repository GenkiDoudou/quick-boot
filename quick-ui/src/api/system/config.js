/**
 * 系统参数配置 API。
 * 封装 `/sys/config` 分页、CRUD、缓存刷新及 Excel 导入导出。
 */
import { createCrudApi } from '@/api/_factory/createCrudApi'
import request from '@/utils/request'

const crud = createCrudApi('/sys/config', { export: true })

/** 参数分页列表。 */
export const pageConfig = crud.page
/** 参数详情。 */
export const getConfig = crud.get
/** 新增参数。 */
export const addConfig = crud.add
/** 修改参数。 */
export const updateConfig = crud.update
/** 批量删除。 */
export const removeConfig = crud.remove
/** 同步导出 xlsx。 */
export const exportConfig = crud.export
/** 下载参数导入模板。 */
export const downloadConfigImportTemplate = crud.downloadImportTemplate
/** 同步导入参数。 */
export const importConfig = crud.importExcel

/** 刷新系统参数缓存（后端 Redis/内存） */
export function refreshConfigCache() {
  return request({ url: '/sys/config/refreshCache', method: 'post' })
}
