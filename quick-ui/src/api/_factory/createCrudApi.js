/**
 * 标准 quickboot CRUD API 工厂：统一 page/get/add/update/remove 与可选 Excel 导入导出。
 */
import request from '@/utils/request'

/**
 * @typedef {object} CreateCrudApiPaths
 * @property {string} [add='add'] 新增路径段；空字符串表示 POST 到 basePath 根
 * @property {string} [update='update'] 修改路径段
 */

/**
 * @typedef {object} CreateCrudApiOptions
 * @property {boolean} [export=false] 是否生成 export / downloadImportTemplate / importExcel
 * @property {number} [exportTimeout=120000] 导出/导入超时（毫秒）
 * @property {CreateCrudApiPaths} [paths] 非标准 CRUD 路径覆盖（如定时任务 add 在根路径）
 * @property {boolean} [exportAsQuery=false] 导出时用 query params 而非 body（遗留 monitor 接口）
 */

/**
 * 将 C7JsonTable / 遗留 GET list 的扁平 query 转为 POST page 的 PageRequest。
 *
 * @param {Record<string, any>} [query]
 * @returns {{ current: number, size: number, param?: Record<string, any> }}
 */
export function toPageRequest(query = {}) {
  const raw = query && typeof query === 'object' ? query : {}
  const {
    pageNum,
    pageSize,
    current,
    size,
    param,
    ...rest
  } = raw
  const nested =
    param && typeof param === 'object' && !Array.isArray(param)
      ? { ...param }
      : { ...rest }
  delete nested.current
  delete nested.size
  delete nested.pageNum
  delete nested.pageSize
  delete nested.param
  const pageRequest = {
    current: current ?? pageNum ?? 1,
    size: size ?? pageSize ?? 10
  }
  if (Object.keys(nested).length > 0) {
    pageRequest.param = nested
  }
  return pageRequest
}

/**
 * 创建与后端 Tier-1 CRUD 契约一致的 API 方法集。
 * <p>
 * remove 请求体为 id 数组（与现网 {@code @RequestBody List<Long>} 一致，非 {@code { ids }} 包装）。
 *
 * @param {string} basePath 如 '/sys/config'
 * @param {CreateCrudApiOptions} [options]
 */
export function createCrudApi(basePath, options = {}) {
  const {
    export: enableExport = false,
    exportTimeout = 120000,
    paths = {},
    exportAsQuery = false
  } = options
  const base = basePath.replace(/\/$/, '')
  const pathAdd = paths.add === undefined ? 'add' : paths.add
  const pathUpdate = paths.update === undefined ? 'update' : paths.update

  const addUrl = pathAdd === '' ? base : `${base}/${pathAdd}`
  const updateUrl = `${base}/${pathUpdate}`

  const api = {
    page: (pageRequest) => request({ url: `${base}/page`, method: 'post', data: pageRequest }),
    get: (id) => request({ url: `${base}/${id}`, method: 'get' }),
    add: (data) => request({ url: addUrl, method: 'post', data }),
    update: (data) => request({ url: updateUrl, method: 'post', data }),
    remove: (ids) => {
      const list = (Array.isArray(ids) ? ids : [ids]).map(String)
      return request({ url: `${base}/remove`, method: 'post', data: list })
    }
  }

  if (enableExport) {
    api.export = (snapshot) => request({
      url: `${base}/export`,
      method: 'post',
      ...(exportAsQuery ? { params: snapshot || {} } : { data: snapshot || {} }),
      responseType: 'blob',
      returnBlobWithHeaders: true,
      timeout: exportTimeout
    })
    api.downloadImportTemplate = () => request({
      url: `${base}/import/template`,
      method: 'get',
      responseType: 'blob',
      returnBlobWithHeaders: true
    })
    /**
     * @param {File} file
     * @param {string} strategy overwrite|ignore
     */
    api.importExcel = (file, strategy) => {
      const form = new FormData()
      form.append('file', file)
      form.append('updateSupport', strategy === 'overwrite' ? 'true' : 'false')
      return request({ url: `${base}/import`, method: 'post', data: form, timeout: exportTimeout })
    }
  }

  return api
}
