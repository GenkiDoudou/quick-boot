import { onBeforeUnmount, ref } from 'vue'

/**
 * 水平拖拽调整面板宽度（用于右侧配置栏等）。
 * @param {{ initial?: number, min?: number, max?: number, direction?: 'left' | 'right' }} [options]
 */
export function usePanelResize(options = {}) {
  const {
    initial = 320,
    min = 260,
    max = 560,
    direction = 'left'
  } = options

  const width = ref(initial)
  const resizing = ref(false)

  let startX = 0
  let startWidth = 0

  function onMouseMove(event) {
    const delta = event.clientX - startX
    const next =
      direction === 'left' ? startWidth - delta : startWidth + delta
    width.value = Math.min(max, Math.max(min, next))
  }

  function onMouseUp() {
    resizing.value = false
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', onMouseUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }

  /**
   * 绑定到拖拽把手 mousedown。
   * @param {MouseEvent} event
   */
  function startResize(event) {
    event.preventDefault()
    resizing.value = true
    startX = event.clientX
    startWidth = width.value
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'
    window.addEventListener('mousemove', onMouseMove)
    window.addEventListener('mouseup', onMouseUp)
  }

  onBeforeUnmount(onMouseUp)

  return { width, resizing, startResize }
}
