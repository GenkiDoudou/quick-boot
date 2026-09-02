/**
 * ${tableComment!} API：基于 createCrudApi 的标准 POST /page CRUD 契约。
 */
import { createCrudApi } from '@/api/_factory/createCrudApi'

const crud = createCrudApi('/${moduleName}/${businessName}', { export: true })

export const page${className} = crud.page
export const get${className} = crud.get
export const add${className} = crud.add
export const update${className} = crud.update
export const del${className} = crud.remove
export const export${className} = crud.export
export const download${className}ImportTemplate = crud.downloadImportTemplate
export const import${className} = crud.importExcel
