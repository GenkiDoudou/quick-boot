import request from '@/utils/request'
import { listFileClassifies } from '@/api/common/file'

export { listFileClassifies }

/**
 * 文件管理分页列表。
 * @param {Record<string, any>} params 查询参数（含 pageNum/pageSize）
 * @returns {Promise<any>}
 */
export function listFile(params) {
  return request({ url: '/system/file/list', method: 'get', params })
}

/**
 * 文件上传。
 * @param {File} file 文件对象
 * @param {string} classify 分类（必填）
 * @returns {Promise<any>}
 */
export function uploadFile(file, classify) {
  const key = classify == null ? '' : String(classify).trim()
  if (!key) {
    return Promise.reject(new Error('classify 不能为空'))
  }
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/system/file/upload/' + encodeURIComponent(key),
    method: 'post',
    data: formData
  })
}

/**
 * 构建按相对路径预览的 URL（GET 返回 inline 文件流，供 img/video 或新窗口打开）。
 * @param {string} relativePath 列表返回的 relativePath
 * @returns {string}
 */
export function buildFileViewUrl(relativePath) {
  if (relativePath == null || String(relativePath).trim() === '') {
    return ''
  }
  const raw = String(relativePath).trim()
  if (raw.startsWith('http://') || raw.startsWith('https://')) {
    return raw
  }
  const base = (import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '')
  const normalized = raw.replace(/^\/+/, '')
  const encodedPath = normalized.split('/').map((s) => encodeURIComponent(s)).join('/')
  return `${base}/system/file/view/${encodedPath}`
}

/**
 * 下载文件（GET blob，可读取 headers）。
 * @param {number|string} fileId 文件ID
 * @returns {Promise<{ data: Blob, headers: Record<string, any> }>}
 */
export function downloadFile(fileId) {
  return request({
    url: '/system/file/download/' + fileId,
    method: 'get',
    responseType: 'blob',
    returnBlobWithHeaders: true
  })
}

/**
 * 批量删除文件。
 * @param {Array<number|string>} fileIds 文件ID集合
 * @returns {Promise<any>}
 */
export function removeFile(fileIds) {
  return request({ url: '/system/file/remove', method: 'post', data: fileIds })
}

