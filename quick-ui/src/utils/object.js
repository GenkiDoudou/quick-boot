/**
 * 轻量对象工具，替代 C7 组件对 lodash 的 get / cloneDeep / debounce 依赖。
 */

/**
 * 按路径读取对象属性（支持 `a.b`、`a[0].b`）。
 *
 * @param {unknown} object 源对象
 * @param {string|Array<string|number>} path 点分路径或路径段数组
 * @param {unknown} [defaultValue] 未命中时的默认值
 * @returns {unknown}
 */
export function get(object, path, defaultValue) {
  if (object == null) {
    return defaultValue
  }
  const segments = Array.isArray(path)
    ? path
    : String(path)
      .replace(/\[(\d+)\]/g, '.$1')
      .replace(/^\./, '')
      .split('.')
      .filter(Boolean)
  let current = object
  for (const segment of segments) {
    if (current == null) {
      return defaultValue
    }
    current = current[segment]
  }
  return current === undefined ? defaultValue : current
}

/**
 * 深拷贝可序列化结构（Date/RegExp 等按 JSON 语义处理）。
 *
 * @template T
 * @param {T} value 待拷贝值
 * @returns {T}
 */
export function cloneDeep(value) {
  if (value === null || typeof value !== 'object') {
    return value
  }
  if (typeof structuredClone === 'function') {
    try {
      return structuredClone(value)
    } catch {
      // 含不可克隆类型时回退递归实现
    }
  }
  if (Array.isArray(value)) {
    return value.map((item) => cloneDeep(item))
  }
  const result = {}
  for (const key of Object.keys(value)) {
    result[key] = cloneDeep(value[key])
  }
  return result
}

/**
 * 防抖：默认 trailing；可通过 options.leading / options.trailing 控制首尾触发。
 *
 * @param {Function} fn 原函数
 * @param {number} wait 等待毫秒
 * @param {{ leading?: boolean, trailing?: boolean, maxWait?: number }} [options] 行为选项
 * @returns {Function & { cancel?: () => void, flush?: () => void }}
 */
export function debounce(fn, wait, options = {}) {
  const leading = options.leading === true
  const trailing = options.trailing !== false
  let timer = null
  let lastArgs = null
  let lastThis = null
  let invokedLeading = false

  function invoke() {
    timer = null
    if (trailing && lastArgs) {
      fn.apply(lastThis, lastArgs)
      lastArgs = null
      lastThis = null
    }
    invokedLeading = false
  }

  function debounced(...args) {
    lastArgs = args
    lastThis = this
    if (timer) {
      clearTimeout(timer)
    }
    if (leading && !invokedLeading) {
      fn.apply(this, args)
      invokedLeading = true
      lastArgs = null
      lastThis = null
    }
    if (wait <= 0) {
      invoke()
      return
    }
    timer = setTimeout(invoke, wait)
  }

  debounced.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    lastArgs = null
    lastThis = null
    invokedLeading = false
  }

  debounced.flush = () => {
    if (timer) {
      clearTimeout(timer)
      invoke()
    } else if (lastArgs) {
      fn.apply(lastThis, lastArgs)
      lastArgs = null
      lastThis = null
      invokedLeading = false
    }
  }

  return debounced
}
