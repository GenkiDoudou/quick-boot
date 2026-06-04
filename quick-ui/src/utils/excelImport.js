import { h, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { saveAs } from 'file-saver'
import { downloadImportErrorFile } from '@/api/import/task'
import { downloadFile } from '@/api/system/file'
import { importData as importDictData } from '@/api/system/dict/data'
import { importType } from '@/api/system/dict/type'
import { importRole } from '@/api/system/role'
import { importUser } from '@/api/system/user'

/** 导入导出中心路由（与菜单 component 路径一致） */
export const IMPORT_EXPORT_CENTER_PATH = '/system/importExportCenter'

/** 平台导入 bizType 常量 */
export const IMPORT_BIZ = {
  USER: 'system:user',
  ROLE: 'system:role',
  DICT_TYPE: 'system:dict:type',
  DICT_DATA: 'system:dict:data',
}

export { appendImportFormFields } from '@/utils/excelImportForm'

/**
 * 按 bizType 生成 C7JsonTable 使用的 importFunction。
 * @param {string} bizType
 * @param {() => object} [contextProvider] 返回 contextJson 对象（如 dictType）
 */
export function createBizImportFunction(bizType, contextProvider) {
  return (file, strategy, uploadOpts = {}) => {
    const overwrite = strategy === 'overwrite'
    const opts = { ...uploadOpts }
    if (uploadOpts.forceAsync) opts.mode = 'async'
    const ctx = typeof contextProvider === 'function' ? contextProvider() : null
    if (ctx && Object.keys(ctx).length) opts.contextJson = ctx
    switch (bizType) {
      case IMPORT_BIZ.USER:
        return importUser(file, overwrite, opts)
      case IMPORT_BIZ.ROLE:
        return importRole(file, overwrite, opts)
      case IMPORT_BIZ.DICT_TYPE:
        return importType(file, overwrite, opts)
      case IMPORT_BIZ.DICT_DATA: {
        const dictType = ctx?.dictType
        if (!dictType) return Promise.reject(new Error('字典数据导入缺少 dictType'))
        return importDictData(file, dictType, overwrite, opts)
      }
      default:
        return Promise.reject(new Error(`未注册导入 bizType: ${bizType}`))
    }
  }
}

/** 后端占位文件名，不与页面 {@code import-error-file-name} 混用 */
const GENERIC_IMPORT_ERROR_NAME = 'import-error.xlsx'

/**
 * 统一失败明细本地下载文件名（确认框与弹框内按钮共用）。
 * 优先使用页面 `import-error-file-name`，避免后端通用名覆盖业务配置。
 * @param {string} [preferred] 页面配置，如 dict-type-import-error.xlsx
 * @param {string} [fromApi] 接口 errorFileName
 * @returns {string}
 */
export function resolveImportErrorDownloadName(preferred, fromApi) {
  const p = String(preferred ?? '').trim()
  const a = String(fromApi ?? '').trim()
  if (p && p !== GENERIC_IMPORT_ERROR_NAME) {
    return ensureXlsxFileName(p)
  }
  if (a && a !== GENERIC_IMPORT_ERROR_NAME) {
    return ensureXlsxFileName(a)
  }
  return ensureXlsxFileName(p || a || GENERIC_IMPORT_ERROR_NAME)
}

/** @param {string} name */
function ensureXlsxFileName(name) {
  const n = String(name).trim()
  if (!n) return GENERIC_IMPORT_ERROR_NAME
  return n.toLowerCase().endsWith('.xlsx') ? n : `${n.replace(/\.[^.]+$/, '')}.xlsx`
}

/**
 * 从响应中解析失败明细 fileId（字符串，避免雪花 ID 精度丢失）。
 * @param {object} r
 * @param {string} [errorKey]
 * @returns {string|undefined}
 */
function resolveErrorFileId(r, errorKey) {
  if (errorKey && String(errorKey).startsWith('file:')) {
    const id = String(errorKey).slice(5).trim()
    return id || undefined
  }
  if (r?.errorFileId != null && r.errorFileId !== '') {
    return String(r.errorFileId)
  }
  return undefined
}

/**
 * 将后端导入响应（用户 VO / ExcelImportResult）规范为 C7ExcelUpload 结构。
 * @param {object} raw 接口 data 或整包响应
 * @returns {{ mode: string, taskId?: number|string, total: number, successCount: number, failCount: number, errorKey?: string, errorFileId?: number, errorFileName?: string, errorFileBase64?: string }}
 */
export function mapImportPayload(raw) {
  const r = raw?.data ?? raw ?? {}
  const errorKey = r.errorKey
  if (errorKey && String(errorKey).startsWith('task:')) {
    const taskId = r.taskId ?? errorKey.replace('task:', '')
    return {
      mode: 'async',
      taskId: Number(taskId) || taskId,
      total: Number(r.total ?? 0),
      successCount: 0,
      failCount: 0,
    }
  }
  if (r.mode === 'async' && r.taskId != null) {
    return {
      mode: 'async',
      taskId: r.taskId,
      total: Number(r.total ?? 0),
      successCount: 0,
      failCount: 0,
      errorKey: errorKey || `task:${r.taskId}`,
    }
  }
  const total = Number(r.total ?? 0)
  const successCount = Number(r.successCount ?? r.success ?? 0)
  const failCount = Number(r.failCount ?? r.failure ?? 0)
  const errorFileId = resolveErrorFileId(r, errorKey)
  return {
    mode: 'sync',
    total,
    successCount,
    failCount,
    errorKey,
    errorFileId,
    errorFileName: r.errorFileName || 'import-error.xlsx',
    errorFileBase64: r.errorFileBase64 || '',
  }
}

/**
 * 异步导入提交后确认框：标准双按钮布局，主按钮进入导入导出中心。
 * @param {{ taskId?: number|string }} [mapped]
 * @returns {Promise<'center'|'close'>}
 */
export function promptAsyncImportSubmitted(mapped) {
  const id = mapped?.taskId
  const taskIdStr = id != null && String(id) !== '' ? String(id) : ''
  const message = h('div', { class: 'qc-import-async-msgbox__body' }, [
    h('p', { class: 'qc-import-async-msgbox__lead' }, '已提交后台导入，任务正在后台处理中。'),
    taskIdStr
      ? h('div', { class: 'qc-import-async-msgbox__task' }, [
          h('span', { class: 'qc-import-async-msgbox__task-label' }, '任务编号'),
          h('span', { class: 'qc-import-async-msgbox__task-id', title: taskIdStr }, taskIdStr),
        ])
      : null,
    h('p', { class: 'qc-import-async-msgbox__hint' }, '可在导入导出中心查看进度与下载失败明细。'),
  ])
  return ElMessageBox.confirm(message, '导入已提交', {
    type: 'success',
    customClass: 'qc-import-async-msgbox',
    confirmButtonText: '前往导入导出中心',
    cancelButtonText: '关闭',
    closeOnClickModal: false,
    distinguishCancelAndClose: true,
  })
    .then(() => 'center')
    .catch(() => 'close')
}

/**
 * 同步失败时提示下载失败明细（fileId / errorKey / 用户 importError）。
 * @param {object} mapped mapImportPayload 结果
 * @param {{ errorFileName?: string, downloadByErrorKey?: (key: string) => Promise<Blob> }} [opts]
 */
export function promptImportErrorDownload(mapped, opts = {}) {
  if (!mapped || mapped.mode === 'async' || mapped.failCount <= 0) return
  const fileId = mapped.errorFileId
  const errorKey = mapped.errorKey
  const canFile = fileId != null && String(fileId).length > 0
  const canKey = typeof opts.downloadByErrorKey === 'function' && errorKey && !String(errorKey).startsWith('task:')
  if (!canFile && !canKey) return

  nextTick(() => {
    ElMessageBox.confirm('存在失败行，是否下载失败明细？', '提示', { type: 'info' })
      .then(async () => {
        const saveName = resolveImportErrorDownloadName(opts.errorFileName, mapped.errorFileName)
        if (canFile) {
          try {
            const { data } = await downloadImportErrorFile(fileId)
            saveAs(data, saveName)
          } catch {
            const { data } = await downloadFile(fileId)
            saveAs(data, saveName)
          }
          return
        }
        if (canKey) {
          const blob = await opts.downloadByErrorKey(errorKey)
          saveAs(blob, saveName)
          return
        }
      })
      .catch(() => {})
  })
}
