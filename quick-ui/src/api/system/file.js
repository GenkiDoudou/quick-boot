/**
 * 系统文件管理 API。
 * 封装上传、预览、下载、删除；预览优先走 fileId 鉴权 Blob 接口。
 */
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
 * 将相对路径规范为 `/system/file/view/` 后的 path 段（分段 encode）。
 * @param {string} relativePath 列表返回的 relativePath
 * @returns {string} 空字符串表示无效
 */
function encodeViewRelativePath(relativePath) {
  if (relativePath == null || String(relativePath).trim() === '') {
    return ''
  }
  const raw = String(relativePath).trim()
  if (raw.startsWith('http://') || raw.startsWith('https://')) {
    return ''
  }
  const normalized = raw.replace(/^\/+/, '')
  return normalized.split('/').map((s) => encodeURIComponent(s)).join('/')
}

/**
 * 构建按相对路径预览的 URL（裸链，无 Authorization；仅调试/外链场景）。
 * 管理端弹窗预览请用 {@link fetchFileViewBlob}。
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
  const encodedPath = encodeViewRelativePath(raw)
  if (!encodedPath) {
    return ''
  }
  const base = (import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '')
  return `${base}/system/file/view/${encodedPath}`
}

/**
 * 鉴权拉取预览文件流（Blob），供 createObjectURL 后给 img/video/iframe 使用。
 * @param {string} relativePath 列表返回的 relativePath
 * @returns {Promise<Blob>}
 * @deprecated 管理端预览请用 {@link fetchFilePreviewBlob}
 */
export function fetchFileViewBlob(relativePath) {
  const encodedPath = encodeViewRelativePath(relativePath)
  if (!encodedPath) {
    return Promise.reject(new Error('文件路径无效'))
  }
  return request({
    url: '/system/file/view/' + encodedPath,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 按 fileId 鉴权拉取 inline 预览流（后端设置 Content-Type）。
 * @param {number|string} fileId 文件主键
 * @returns {Promise<Blob>}
 */
export function fetchFilePreviewBlob(fileId) {
  if (fileId == null || fileId === '') {
    return Promise.reject(new Error('文件 ID 无效'))
  }
  return request({
    url: '/system/file/preview/' + fileId,
    method: 'get',
    responseType: 'blob'
  })
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

