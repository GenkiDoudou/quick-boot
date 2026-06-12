import request from '@/utils/request'

/**
 * 知识库文档分页列表。
 * @param {Record<string, any>} params 查询参数
 */
export function listDocument(params) {
  return request({ url: '/knowledge/doc/list', method: 'get', params })
}

/** @param {string|number} docId */
export function getDocument(docId) {
  return request({ url: '/knowledge/doc/getInfo', method: 'get', params: { docId: String(docId) } })
}

/** @param {string|number} docId */
export function listDocumentChunks(docId) {
  return request({ url: '/knowledge/doc/chunks', method: 'get', params: { docId: String(docId) } })
}

/**
 * 上传文档并触发异步入库。
 * @param {number|string} kbId
 * @param {File} file
 * @param {Record<string, any>|null} [segmentConfig]
 */
export function uploadDocument(kbId, file, segmentConfig) {
  const formData = new FormData()
  formData.append('file', file)
  if (segmentConfig) {
    formData.append(
      'segmentConfig',
      new Blob([JSON.stringify(segmentConfig)], { type: 'application/json' })
    )
  }
  return request({
    url: '/knowledge/doc/upload',
    method: 'post',
    params: { kbId: kbId != null ? String(kbId) : kbId },
    data: formData
  })
}

/** @param {Record<string, any>} data kbId, title, content, segmentConfig? */
export function addManualDocument(data) {
  return request({ url: '/knowledge/doc/addManual', method: 'post', data })
}

/** @param {Record<string, any>} data kbId, url, title?, segmentConfig? */
export function addFromWebDocument(data) {
  return request({ url: '/knowledge/doc/addFromWeb', method: 'post', data })
}

/** @param {Record<string, any>} data kbId, libFileId, title?, segmentConfig? */
export function addFromLibraryDocument(data) {
  return request({ url: '/knowledge/doc/addFromLibrary', method: 'post', data })
}

export function reindexDocument(docId) {
  return request({ url: '/knowledge/doc/reindex', method: 'post', params: { docId: String(docId) } })
}

export function removeDocument(docIds) {
  const ids = (docIds || []).map((id) => String(id))
  return request({ url: '/knowledge/doc/remove', method: 'post', data: ids })
}

/**
 * 入库前分段预览（手动/网页/文档库）。
 * @param {Record<string, any>} data
 */
export function previewSegments(data) {
  return request({ url: '/knowledge/doc/previewSegments', method: 'post', data })
}

/**
 * 入库前分段预览（文件）。
 * @param {string} kbId
 * @param {File} file
 * @param {Record<string, any>|null} [segmentConfig]
 */
export function previewSegmentsFile(kbId, file, segmentConfig) {
  const formData = new FormData()
  formData.append('file', file)
  if (segmentConfig) {
    formData.append(
      'segmentConfig',
      new Blob([JSON.stringify(segmentConfig)], { type: 'application/json' })
    )
  }
  return request({
    url: '/knowledge/doc/previewSegmentsFile',
    method: 'post',
    params: { kbId: String(kbId) },
    data: formData
  })
}

/**
 * 更新分块正文或启用状态。
 * @param {{ chunkId: string|number, content?: string, enabled?: 0|1 }} data
 */
export function updateDocumentChunk(data) {
  return request({
    url: '/knowledge/doc/chunk/update',
    method: 'post',
    data: { ...data, chunkId: String(data.chunkId) }
  })
}
