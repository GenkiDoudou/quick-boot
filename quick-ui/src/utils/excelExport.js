import { ElMessage } from 'element-plus'
import errorCode from '@/utils/errorCode'
import { blobValidate } from '@/utils/ruoyi'
import { submitExport } from '@/api/export/task'

const EXPORT_CENTER_HINT = '已提交后台导出，可在「导入导出中心」查看任务'

/**
 * 从 Blob 响应解析平台 R 包装 JSON。
 * @param {Blob} blob
 * @returns {Promise<Record<string, unknown>|null>}
 */
async function parseExportJsonBlob(blob) {
  try {
    const text = await blob.text()
    const parsed = JSON.parse(text || '{}')
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch {
    return null
  }
}

/**
 * 处理编排导出 submit 响应：同步 xlsx Blob，或异步/业务错误 JSON。
 * @param {{ data: Blob, headers?: Record<string, string> }} raw
 * @param {string} [defaultFileName]
 */
export async function resolvePlatformExportResponse(raw, defaultFileName = 'export.xlsx') {
  const blob = raw?.data ?? raw
  if (!(blob instanceof Blob)) {
    throw new Error('导出响应无效')
  }
  if (blobValidate(blob)) {
    const headers = raw?.headers || {}
    const cd = headers['content-disposition'] || headers['Content-Disposition']
    if (cd) {
      return { data: blob, headers }
    }
    return {
      data: blob,
      headers: {
        'content-disposition': `attachment; filename*=UTF-8''${encodeURIComponent(defaultFileName)}`,
      },
    }
  }
  const body = await parseExportJsonBlob(blob)
  if (!body) {
    throw new Error('导出失败')
  }
  const code = Number(body.code ?? 200)
  const payload = body.data ?? body
  if (code === 200 && payload?.mode === 'async' && payload?.taskId != null) {
    ElMessage.info(`${EXPORT_CENTER_HINT} ${payload.taskId}`)
    const err = new Error('async export submitted')
    err.code = 'EXPORT_ASYNC'
    err.taskId = payload.taskId
    throw err
  }
  const msg = errorCode[String(code)] || body.msg || errorCode['default'] || '导出失败'
  throw new Error(msg)
}

/**
 * 平台导出：提交任务；同步返回 Blob，异步抛出 EXPORT_ASYNC。
 * @param {string} bizType
 * @param {Record<string, any>} queryParams
 * @param {string} [defaultFileName]
 */
export async function runPlatformExport(bizType, queryParams, defaultFileName) {
  const raw = await submitExport(bizType, queryParams)
  return resolvePlatformExportResponse(raw, defaultFileName)
}
