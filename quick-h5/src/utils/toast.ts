/**
 * 轻量 toast 封装，基于 uni.showToast，无业务耦合。
 * 页面与 composable 统一用本模块提示，避免散落调用。
 */

/** 普通文本提示（无图标） */
export function toastInfo(title: string) {
  uni.showToast({ title, icon: 'none' })
}

/** 操作成功提示，默认文案「操作成功」 */
export function toastOk(title = '操作成功') {
  uni.showToast({ title, icon: 'success' })
}

/**
 * 错误提示：优先取 Error.message，否则用 fallback。
 * @param e 捕获的异常或任意值
 * @param fallback message 不可用时的兜底文案
 */
export function toastErr(e: unknown, fallback = '请求失败') {
  const msg = e instanceof Error ? e.message : fallback
  uni.showToast({ title: msg || fallback, icon: 'none' })
}
