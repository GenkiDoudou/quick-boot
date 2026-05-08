/**
 * C7MessageBox：在 Element Plus `ElMessageBox` / `ElLoading` 之上提供函数式封装。
 *
 * - 统一 **`setMessageBoxDefaults`** 浅合并默认 options。
 * - 对话框类 API **始终 resolve** 为 **`{ action, value? }`**，用户取消/关闭路径 **不**向业务抛 **reject**（与 EP 常见「点取消则 reject」行为解耦）。
 * - **`asyncConfirm`**：在 **`beforeClose`** 内 **await** 异步任务；优先 **`confirmButtonLoading`**；失败 **不** `done()` 保持弹窗。
 *
 * 依赖 **element-plus ^2.10**：`MessageBoxData.action` 取值与 **`beforeClose(action, instance, done)`** 契约以该版本为准。
 */
import { ElLoading, ElMessageBox } from 'element-plus'

/** @type {Record<string, unknown>} */
let messageBoxDefaults = {}

/** 仅 C7 使用、不可透传给 EP 的键 */
const C7_ONLY_OPTION_KEYS = new Set(['asyncConfirm', 'errorNotify', 'asyncConfirmLoadingText'])

/**
 * 设置全局 MessageBox 默认 options（浅合并到模块级对象；后写覆盖同名字段）。
 *
 * @param {import('element-plus').ElMessageBoxOptions & {
 *   asyncConfirm?: () => Promise<unknown>,
 *   errorNotify?: (err: unknown) => void,
 *   asyncConfirmLoadingText?: string
 * }} config 与单次调用合并时，单次调用优先。
 */
export function setMessageBoxDefaults(config) {
  messageBoxDefaults = { ...messageBoxDefaults, ...(config || {}) }
}

/**
 * 合并模块默认与单次入参（浅合并，后者优先）。
 *
 * @param {Record<string, unknown>} perCall 单次 `c7*` 的 options
 * @returns {Record<string, unknown>}
 */
export function mergeMessageBoxOptions(perCall) {
  return { ...messageBoxDefaults, ...(perCall || {}) }
}

/**
 * 从 EP resolve 的 payload 映射为对外结构。
 *
 * @param {unknown} data `MessageBoxData` 或兼容形态
 * @returns {{ action: 'confirm'|'cancel'|'close', value?: string }}
 */
export function mapMessageBoxResolve(data) {
  if (data == null || typeof data !== 'object') {
    return { action: 'confirm' }
  }
  const d = /** @type {{ action?: string, value?: unknown }} */ (data)
  const raw = d.action
  const action =
    raw === 'cancel' || raw === 'close' || raw === 'confirm' ? raw : 'confirm'
  let value
  if (d.value !== undefined && d.value !== null) {
    value = typeof d.value === 'string' ? d.value : String(d.value)
  }
  return value !== undefined ? { action, value } : { action }
}

/**
 * 将 EP 在用户关闭取消路径上的 **reject** 映射为 **`{ action }`**。
 * EP 2.x 常见：`reject('cancel' | 'close')` 或 reject 带 `action` 的对象。
 *
 * @param {unknown} reason reject 原因
 * @returns {{ action: 'confirm'|'cancel'|'close', value?: string }}
 */
export function mapMessageBoxReject(reason) {
  if (reason === 'cancel' || reason === 'close') {
    return { action: reason }
  }
  if (reason === 'confirm') {
    return { action: 'confirm' }
  }
  if (reason && typeof reason === 'object') {
    const r = /** @type {{ action?: string, value?: unknown }} */ (reason)
    const a = r.action
    if (a === 'cancel' || a === 'close' || a === 'confirm') {
      if (r.value !== undefined && r.value !== null) {
        const value = typeof r.value === 'string' ? r.value : String(r.value)
        return { action: a, value }
      }
      return { action: a }
    }
  }
  if (typeof reason === 'string') {
    // 兜底：未知字符串按 close 处理，避免向业务抛异常
    return { action: 'close' }
  }
  return { action: 'close' }
}

/**
 * @param {Promise<unknown>} promise EP 返回的 Promise
 * @returns {Promise<{ action: 'confirm'|'cancel'|'close', value?: string }>}
 */
function wrapMessageBoxPromise(promise) {
  return promise.then(
    (data) => mapMessageBoxResolve(data),
    (err) => mapMessageBoxReject(err)
  )
}

/**
 * @param {unknown} title 第二参：标题字符串，或与 EP 一致时可为 options 对象
 * @param {Record<string, unknown>|undefined} options 第三参
 * @returns {{ title: string|undefined, options: Record<string, unknown> }}
 */
export function splitTitleAndOptions(title, options) {
  if (title !== undefined && title !== null && typeof title === 'object' && !Array.isArray(title)) {
    return { title: undefined, options: /** @type {Record<string, unknown>} */ (title) }
  }
  if (title === undefined || title === null) {
    return { title: undefined, options: options || {} }
  }
  return { title: String(title), options: options || {} }
}

/**
 * 去掉 C7 扩展字段，得到可传给 EP 的 options。
 *
 * @param {Record<string, unknown>} merged
 * @returns {Record<string, unknown>}
 */
function toElementPlusOptions(merged) {
  const out = { ...merged }
  for (const k of C7_ONLY_OPTION_KEYS) {
    delete out[k]
  }
  return out
}

/**
 * 在 `beforeClose` 内驱动 `asyncConfirm`：成功则调用 `onSuccess`（由上层负责 `done`）；失败不 `done()` 保持弹窗。
 * 使用 **`instance.confirmButtonLoading`**（EP 2.10+）；不在此默认启用全屏 **`ElLoading`**，避免与弹窗焦点抢叠。
 *
 * @param {import('element-plus').MessageBoxState} instance
 * @param {() => Promise<unknown>} asyncFn
 * @param {(err: unknown) => void} [errorNotify]
 * @param {string} loadingText 确定钮临时文案（如「处理中...」）
 * @param {() => void} onSuccess 异步成功后关闭弹窗（通常调用 EP 的 `done`）
 */
async function runAsyncConfirmInBeforeClose(
  instance,
  asyncFn,
  errorNotify,
  loadingText,
  onSuccess
) {
  const prevText = instance.confirmButtonText
  const prevLoading = instance.confirmButtonLoading
  instance.confirmButtonLoading = true
  if (loadingText) {
    instance.confirmButtonText = loadingText
  }
  try {
    await asyncFn()
    instance.confirmButtonLoading = !!prevLoading
    instance.confirmButtonText = prevText
    onSuccess()
  } catch (err) {
    instance.confirmButtonLoading = !!prevLoading
    instance.confirmButtonText = prevText
    if (typeof errorNotify === 'function') {
      errorNotify(err)
    } else if (import.meta.env?.DEV) {
      console.error('[C7MessageBox] asyncConfirm 失败:', err)
    }
    // 不调用 onSuccess / done：弹窗保持；外层 Promise 仍挂起直至用户取消/关闭
  }
}

/**
 * 包装 `beforeClose`：存在 `asyncConfirm` 时在 **confirm** 分支内 **await** 异步逻辑。
 * 若调用方同时传入 **`beforeClose`**，在异步成功后在 **`confirm`** 上继续转交（由用户或默认 `done` 完成关闭）。
 *
 * @param {Record<string, unknown>} epOpts 已剔除 C7 键、待传给 EP 的 options
 * @param {Record<string, unknown>} merged 含 C7 扩展字段的完整 merged
 */
function attachAsyncBeforeClose(epOpts, merged) {
  const asyncConfirm = merged.asyncConfirm
  if (typeof asyncConfirm !== 'function') {
    return
  }
  const errorNotify = merged.errorNotify
  const loadingText =
    typeof merged.asyncConfirmLoadingText === 'string'
      ? merged.asyncConfirmLoadingText
      : '处理中...'
  const userBeforeClose =
    typeof epOpts.beforeClose === 'function' ? epOpts.beforeClose : undefined

  epOpts.beforeClose = (action, instance, done) => {
    if (action === 'cancel' || action === 'close') {
      if (userBeforeClose) {
        userBeforeClose(action, instance, done)
      } else {
        done()
      }
      return
    }
    if (action === 'confirm') {
      void runAsyncConfirmInBeforeClose(
        instance,
        asyncConfirm,
        errorNotify,
        loadingText,
        () => {
          if (userBeforeClose) {
            userBeforeClose(action, instance, done)
          } else {
            done()
          }
        }
      )
      return
    }
    if (userBeforeClose) {
      userBeforeClose(action, instance, done)
    } else {
      done()
    }
  }
}

/**
 * @param {string|import('vue').VNode|(() => import('vue').VNode)} message
 * @param {string|Record<string, unknown>|undefined} title
 * @param {Record<string, unknown>|undefined} options
 * @param {(msg: typeof message, title: string|undefined, opts: Record<string, unknown>) => Promise<unknown>} invoke
 */
function invokeBox(message, title, options, invoke) {
  const { title: ttl, options: per } = splitTitleAndOptions(title, options)
  const merged = mergeMessageBoxOptions(per)
  const epOpts = toElementPlusOptions(merged)
  attachAsyncBeforeClose(epOpts, merged)
  return wrapMessageBoxPromise(invoke(message, ttl, epOpts))
}

/**
 * 确认对话框（取消/关闭不 reject）。
 *
 * @param {string|import('vue').VNode|(() => import('vue').VNode)} message
 * @param {string|import('element-plus').ElMessageBoxOptions|undefined} [title]
 * @param {import('element-plus').ElMessageBoxOptions & {
 *   asyncConfirm?: () => Promise<unknown>,
 *   errorNotify?: (err: unknown) => void,
 *   asyncConfirmLoadingText?: string
 * }} [options]
 * @returns {Promise<{ action: 'confirm'|'cancel'|'close', value?: string }>}
 */
export function c7Confirm(message, title, options) {
  return invokeBox(message, title, options, (m, t, opts) => {
    if (t !== undefined) {
      return ElMessageBox.confirm(m, t, opts)
    }
    return ElMessageBox.confirm(m, opts)
  })
}

/**
 * 仅确定按钮。
 *
 * @param {string|import('vue').VNode|(() => import('vue').VNode)} message
 * @param {string|import('element-plus').ElMessageBoxOptions|undefined} [title]
 * @param {import('element-plus').ElMessageBoxOptions} [options]
 * @returns {Promise<{ action: 'confirm'|'cancel'|'close', value?: string }>}
 */
export function c7Alert(message, title, options) {
  return invokeBox(message, title, options, (m, t, opts) => {
    if (t !== undefined) {
      return ElMessageBox.alert(m, t, opts)
    }
    return ElMessageBox.alert(m, opts)
  })
}

/**
 * 带输入框；校验字段仅透传 EP。
 *
 * @param {string|import('vue').VNode|(() => import('vue').VNode)} message
 * @param {string|import('element-plus').ElMessageBoxOptions|undefined} [title]
 * @param {import('element-plus').ElMessageBoxOptions} [options]
 * @returns {Promise<{ action: 'confirm'|'cancel'|'close', value?: string }>}
 */
export function c7Prompt(message, title, options) {
  return invokeBox(message, title, options, (m, t, opts) => {
    if (t !== undefined) {
      return ElMessageBox.prompt(m, t, opts)
    }
    return ElMessageBox.prompt(m, opts)
  })
}

/**
 * 危险操作确认：在 `c7Confirm` 上叠加 `type: 'warning'` 与危险按钮样式（可被入参覆盖 class）。
 *
 * @param {string|import('vue').VNode|(() => import('vue').VNode)} message
 * @param {string|import('element-plus').ElMessageBoxOptions|undefined} [title]
 * @param {import('element-plus').ElMessageBoxOptions & {
 *   asyncConfirm?: () => Promise<unknown>,
 *   errorNotify?: (err: unknown) => void,
 *   asyncConfirmLoadingText?: string
 * }} [options]
 * @returns {Promise<{ action: 'confirm'|'cancel'|'close', value?: string }>}
 */
export function c7DangerConfirm(message, title, options) {
  const { title: ttl, options: per } = splitTitleAndOptions(title, options)
  const withPreset = mergeMessageBoxOptions({
    ...per,
    type: 'warning',
    confirmButtonClass: per.confirmButtonClass || 'el-button--danger',
  })
  return c7Confirm(message, ttl, withPreset)
}

/**
 * 全屏/局部 Loading，封装 `ElLoading.service`。
 *
 * @param {string} [text] 展示文案，写入 `options.text`
 * @param {import('element-plus').LoadingOptions} [options] 透传 EP（除 `text` 外）
 * @returns {{ close: () => void }}
 */
export function c7Loading(text, options = {}) {
  const opts = { ...options }
  if (text !== undefined && text !== null) {
    opts.text = String(text)
  }
  const inst = ElLoading.service(opts)
  return {
    close: () => {
      inst.close()
    },
  }
}
