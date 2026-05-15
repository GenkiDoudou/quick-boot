import { ElMessageBox, ElMessage, ElLoading } from 'element-plus'

/**
 * C7MessageBox — 函数式对话框工具集
 * 封装 ElMessageBox，统一 API 风格，支持异步确认自动 loading、全局默认配置
 */

// ── 全局默认配置 ──
let _defaults = {
  confirmButtonText: '确 定',
  cancelButtonText: '取 消',
  draggable: true,
  closeOnClickModal: false,
}

/**
 * 设置全局默认配置（建议在 main.js 中调用一次）
 * @param {object} config
 */
export function setMessageBoxDefaults(config) {
  _defaults = { ..._defaults, ...config }
}

/**
 * 结构化返回值
 * @typedef {{ action: 'confirm' | 'cancel' | 'close', value?: string }} C7MessageBoxResult
 */

/**
 * 通用内部调用，处理 cancel/close 异常统一返回结构化结果
 * @param {() => Promise} fn
 * @returns {Promise<C7MessageBoxResult>}
 */
async function _call(fn) {
  try {
    const result = await fn()
    // ElMessageBox resolve 时 result 为 'confirm' 或 { value, action }
    if (result && typeof result === 'object') {
      return { action: result.action ?? 'confirm', value: result.value }
    }
    return { action: 'confirm' }
  } catch (action) {
    // ElMessageBox reject 时 action 为 'cancel' | 'close'
    return { action: action === 'cancel' ? 'cancel' : 'close' }
  }
}

/**
 * 确认对话框
 * @param {string} message 提示内容
 * @param {string} [title='提示'] 标题
 * @param {object} [options] 扩展选项
 * @param {() => Promise<any>} [options.asyncConfirm] 异步确认函数，执行期间自动显示 loading
 * @param {(err: any) => void} [options.errorNotify] asyncConfirm 失败时的通知函数
 * @param {object} [options.modalProps] 额外透传给 ElMessageBox 的属性
 * @returns {Promise<C7MessageBoxResult>}
 */
export async function c7Confirm(message, title = '提示', options = {}) {
  const { asyncConfirm, errorNotify, modalProps, ...rest } = options
  const mergedOptions = {
    ..._defaults,
    type: 'warning',
    ...rest,
    ...modalProps,
  }

  if (asyncConfirm) {
    return _call(() =>
      ElMessageBox.confirm(message, title, {
        ...mergedOptions,
        beforeClose: async (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true
            instance.confirmButtonText = '处理中...'
            try {
              await asyncConfirm()
              done()
            } catch (err) {
              const notify = errorNotify ||
                ((e) => ElMessage.error(e?.message || '操作失败'))
              notify(err)
            } finally {
              instance.confirmButtonLoading = false
              instance.confirmButtonText = mergedOptions.confirmButtonText || '确 定'
            }
          } else {
            done()
          }
        },
      })
    )
  }

  return _call(() => ElMessageBox.confirm(message, title, mergedOptions))
}

/**
 * 警告/提示对话框（只有确定按钮）
 * @param {string} message 提示内容
 * @param {string} [title='提示'] 标题
 * @param {object} [options] 额外选项
 * @returns {Promise<C7MessageBoxResult>}
 */
export async function c7Alert(message, title = '提示', options = {}) {
  const { modalProps, ...rest } = options
  return _call(() =>
    ElMessageBox.alert(message, title, {
      ..._defaults,
      type: 'info',
      showCancelButton: false,
      ...rest,
      ...modalProps,
    })
  )
}

/**
 * 输入框对话框
 * @param {string} message 提示内容
 * @param {string} [title='请输入'] 标题
 * @param {object} [options] 额外选项
 * @returns {Promise<C7MessageBoxResult>}
 */
export async function c7Prompt(message, title = '请输入', options = {}) {
  const { modalProps, ...rest } = options
  return _call(() =>
    ElMessageBox.prompt(message, title, {
      ..._defaults,
      ...rest,
      ...modalProps,
    })
  )
}

/**
 * 危险操作确认框（红色确认按钮）
 * @param {string} message 提示内容
 * @param {string} [title='危险操作'] 标题
 * @param {object} [options] 额外选项
 * @returns {Promise<C7MessageBoxResult>}
 */
export async function c7DangerConfirm(message, title = '危险操作', options = {}) {
  const { asyncConfirm, errorNotify, modalProps, ...rest } = options
  return c7Confirm(message, title, {
    type: 'error',
    confirmButtonClass: 'el-button--danger',
    asyncConfirm,
    errorNotify,
    ...rest,
    modalProps,
  })
}

/**
 * 全局 Loading（基于 ElLoading.service）
 * @param {string} [text='加载中...'] 加载文字
 * @param {object} [options] 额外透传给 ElLoading.service 的属性
 * @returns {{ close: () => void }}
 */
export function c7Loading(text = '加载中...', options = {}) {
  const instance = ElLoading.service({
    text,
    background: 'rgba(0, 0, 0, 0.7)',
    ...options,
  })
  return {
    close: () => instance.close(),
  }
}

/**
 * 命名空间统一导出
 */
export const c7MessageBox = {
  confirm: c7Confirm,
  alert: c7Alert,
  prompt: c7Prompt,
  dangerConfirm: c7DangerConfirm,
  loading: c7Loading,
  setDefaults: setMessageBoxDefaults,
}

export default c7MessageBox
