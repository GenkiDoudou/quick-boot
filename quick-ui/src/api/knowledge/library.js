/**
 * 知识库文档库 API：文件夹树、文档移动与库内检索（/knowledge/library）。
 */
import request from '@/utils/request'

/** @returns {Promise<{ data: Array }>} 文档库目录树 */
export function listLibraryFolderTree() {
  return request({ url: '/knowledge/library/folder/tree', method: 'get' })
}

/** @param {{ parentId: number|string, name: string, orderNum?: number }} data */
export function addLibraryFolder(data) {
  return request({ url: '/knowledge/library/folder/add', method: 'post', data })
}

/** @param {{ folderId: number|string, parentId?: number|string, name: string, orderNum?: number }} data */
export function updateLibraryFolder(data) {
  return request({ url: '/knowledge/library/folder/update', method: 'post', data })
}

/** @param {number|string} folderId */
export function removeLibraryFolder(folderId) {
  return request({ url: '/knowledge/library/folder/remove', method: 'post', params: { folderId } })
}

/** @param {Record<string, any>} params pageNum, pageSize, folderId, title */
export function listLibraryFile(params) {
  return request({ url: '/knowledge/library/file/list', method: 'get', params })
}

/** @param {number|string} folderId @param {File} file @param {string} [remark] */
export function uploadLibraryFile(folderId, file, remark) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/knowledge/library/file/upload',
    method: 'post',
    params: { folderId, remark: remark || undefined },
    data: formData
  })
}

/** @param {Array<number|string>} libFileIds */
export function removeLibraryFile(libFileIds) {
  return request({ url: '/knowledge/library/file/remove', method: 'post', data: libFileIds })
}
