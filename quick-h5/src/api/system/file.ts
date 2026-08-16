/**
 * 系统文件管理 API：列表/上传/删除；预览与下载走带鉴权的 uni.downloadFile。
 */
import { request, getStoredToken } from '../http'
import type { PageInfo } from '../types'

/** 文件登记行 */
export type SysFile = {
  fileId?: number | string
  relativePath?: string
  originalName?: string
  classify?: string
  ext?: string
  sizeBytes?: number
  contentType?: string
  uploaderUserName?: string
  createTime?: string
}

/** 上传分类（启用中） */
export type FileClassifyOption = {
  classify?: string
  classifyName?: string
  limitExt?: string
  limitSizeBytes?: number
}

/**
 * 分页列表（GET pageNum/pageSize → PageInfo）。
 */
export function listFile(query: {
  pageNum: number
  pageSize: number
  originalName?: string
  uploaderUserName?: string
  classify?: string
}) {
  return request<PageInfo<SysFile>>({
    url: '/system/file/list',
    method: 'GET',
    data: query,
  })
}

/** 启用中的上传分类，供上传前选择 */
export function listFileClassifies() {
  return request<FileClassifyOption[]>({
    url: '/file/classifies',
    method: 'GET',
  })
}

/** 批量删除文件 */
export function removeFile(fileIds: Array<number | string>) {
  return request<void>({
    url: '/system/file/remove',
    method: 'POST',
    data: fileIds,
  })
}

/** 解析当前端 baseUrl（与 request 一致） */
function apiBase(): string {
  const isH5 = process.env.UNI_PLATFORM === 'h5'
  const rawBase = isH5
    ? (import.meta.env.VITE_APP_BASE_API || '')
    : (import.meta.env.VITE_APP_BASE_API_NATIVE || import.meta.env.VITE_APP_BASE_API || '')
  return String(rawBase).replace(/\/$/, '')
}

/**
 * 使用 uni.uploadFile 上传并登记 sys_file。
 * @param filePath 本地临时路径
 * @param classify 分类键（必填）
 */
export function uploadFile(filePath: string, classify: string): Promise<unknown> {
  const key = String(classify || '').trim()
  if (!key) {
    return Promise.reject(new Error('classify 不能为空'))
  }
  const token = getStoredToken()
  const url = `${apiBase()}/system/file/upload/${encodeURIComponent(key)}`
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (res.statusCode === 401 || body?.code === 401) {
            reject(new Error(body?.msg || '未登录或登录已过期'))
            return
          }
          if (body && typeof body.code === 'number' && body.code !== 200) {
            reject(new Error(body.msg || `业务错误 ${body.code}`))
            return
          }
          resolve(body?.data)
        }
        catch (e) {
          reject(e instanceof Error ? e : new Error('上传响应解析失败'))
        }
      },
      fail: (err) => reject(new Error(err.errMsg || '上传失败')),
    })
  })
}

/**
 * 鉴权下载到本地临时路径，供预览或另存。
 * @param kind preview | download
 */
export function downloadFileTemp(
  fileId: number | string,
  kind: 'preview' | 'download' = 'download',
): Promise<string> {
  const token = getStoredToken()
  const path = kind === 'preview'
    ? `/system/file/preview/${encodeURIComponent(String(fileId))}`
    : `/system/file/download/${encodeURIComponent(String(fileId))}`
  return new Promise((resolve, reject) => {
    uni.downloadFile({
      url: `${apiBase()}${path}`,
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.tempFilePath) {
          resolve(res.tempFilePath)
          return
        }
        reject(new Error(`下载失败 HTTP ${res.statusCode}`))
      },
      fail: (err) => reject(new Error(err.errMsg || '下载失败')),
    })
  })
}
