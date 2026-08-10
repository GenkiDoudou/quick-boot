/**
 * 将低优先级任务推迟到浏览器空闲时执行，避免阻塞 click / axios 响应链。
 *
 * @param {() => void} fn
 * @param {number} [timeout=2500] 最长等待 ms，超时后仍强制执行
 */
export function scheduleIdleTask(fn, timeout = 2500) {
  if (typeof requestIdleCallback === 'function') {
    requestIdleCallback(fn, { timeout })
    return
  }
  setTimeout(fn, 0)
}

/**
 * @param {string} reason
 * @returns {boolean}
 */
export function isUrgentFlushReason(reason) {
  return reason === 'error' || reason === 'leave' || reason === 'page_leave'
}

export default {
  scheduleIdleTask,
  isUrgentFlushReason
}
