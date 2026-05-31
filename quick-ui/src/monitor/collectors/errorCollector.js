/**
 * 全局 JS 错误与未处理 Promise 拒绝采集。
 */

/**
 * @param {{ pushEvent: (row: Record<string, unknown>) => void }} deps
 * @returns {() => void} dispose
 */
export function bindErrorCollector(deps) {
  const { pushEvent } = deps

  const onError = (e) => {
    pushEvent({
      type: 'js_error',
      level: 'error',
      msg: e.message,
      file: e.filename,
      line: e.lineno,
      col: e.colno
    })
  }

  const onRejection = (e) => {
    const reason = e.reason
    const msg =
      reason && typeof reason === 'object' && reason.message
        ? String(reason.message)
        : String(reason)
    pushEvent({
      type: 'promise_error',
      level: 'error',
      msg
    })
  }

  window.addEventListener('error', onError)
  window.addEventListener('unhandledrejection', onRejection)
  return () => {
    window.removeEventListener('error', onError)
    window.removeEventListener('unhandledrejection', onRejection)
  }
}
