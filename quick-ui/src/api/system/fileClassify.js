/**
 * 文件分类配置 API。
 * 封装 `/system/fileClassify` 分页、详情及 CRUD；分类键 classify 创建后不可改。
 */
import { createCrudApi, toPageRequest } from '@/api/_factory/createCrudApi'

const crud = createCrudApi('/system/fileClassify')

/** 文件分类分页（POST page）。 */
export const pageFileClassify = crud.page
/** 分类详情。 */
export const getFileClassify = crud.get
/** 新增分类。 */
export const addFileClassify = crud.add
/** 修改分类（不可改 classify 键）。 */
export const updateFileClassify = crud.update
/** 批量删除分类。 */
export const removeFileClassify = crud.remove

/**
 * 文件分类分页列表（兼容 C7JsonTable 扁平 query）。
 * @param {Record<string, any>} params pageNum/pageSize/classify/classifyName/status
 * @returns {Promise<any>}
 */
export function listFileClassify(params) {
  return crud.page(toPageRequest(params))
}
