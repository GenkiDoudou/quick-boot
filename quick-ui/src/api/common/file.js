import request from '@/utils/request'

/**
 * 查询全部上传分类配置。
 * @returns {Promise<any>}
 */
export function listFileClassifies() {
  return request({ url: '/file/classifies', method: 'get' })
}

/**
 * 查询单个上传分类配置。
 * @param {string} classify 分类名
 * @returns {Promise<any>}
 */
export function getFileClassify(classify) {
  return request({ url: '/file/classifies/' + encodeURIComponent(classify), method: 'get' })
}

/**
 * 通用文件上传（走 /file/upload）。
 * @param {File} file 文件
 * @param {string} classify 分类（必填）
 * @returns {Promise<any>}
 */
export function uploadCommonFile(file, classify) {
  const key = classify == null ? '' : String(classify).trim()
  if (!key) {
    return Promise.reject(new Error('classify 不能为空'))
  }
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload/' + encodeURIComponent(key),
    method: 'post',
    data: formData
  })
}

/**
 * limitExt 转 el-upload accept（如 png,jpg -> .png,.jpg）。
 * @param {string} limitExt
 * @returns {string}
 */
export function limitExtToAccept(limitExt) {
  if (limitExt == null || String(limitExt).trim() === '') {
    return ''
  }
  return String(limitExt)
    .split(',')
    .map((s) => s.trim().toLowerCase())
    .filter(Boolean)
    .map((ext) => (ext.startsWith('.') ? ext : '.' + ext))
    .join(',')
}

/**
 * 字节数转可读大小。
 * @param {number} bytes
 * @returns {string}
 */
export function formatFileSize(bytes) {
  const n = Number(bytes)
  if (!Number.isFinite(n) || n <= 0) return '0B'
  if (n < 1024) return `${n}B`
  if (n < 1024 * 1024) return `${Math.round(n / 1024)}KB`
  if (n < 1024 * 1024 * 1024) return `${(n / (1024 * 1024)).toFixed(1).replace(/\.0$/, '')}MB`
  return `${(n / (1024 * 1024 * 1024)).toFixed(1).replace(/\.0$/, '')}GB`
}
