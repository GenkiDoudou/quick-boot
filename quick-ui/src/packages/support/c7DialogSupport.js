/** C7Dialog 可单测纯函数：双 v-model 显隐解析。 */

/**
 * 解析弹窗显隐：优先 modelValue，其次 visible，均缺省为 false。
 *
 * @param {boolean | undefined} modelValue
 * @param {boolean | undefined} visible
 * @returns {boolean}
 */
export function resolveDialogOpen(modelValue, visible) {
  if (modelValue !== undefined) {
    return Boolean(modelValue)
  }
  if (visible !== undefined) {
    return Boolean(visible)
  }
  return false
}

/**
 * 关闭时应同时 emit 的双向绑定更新值。
 */
export function dialogCloseEmits() {
  return { modelValue: false, visible: false }
}
