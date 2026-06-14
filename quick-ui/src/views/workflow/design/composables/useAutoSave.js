import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

/** @typedef {'saved' | 'saving' | 'dirty'} SaveStatus */

/**
 * 画布自动保存（结构变更防抖 + 拖拽结束补存，避免拖拽时 deep watch 卡顿）。
 * @param {object} options
 * @param {import('vue').Ref<string>} options.targetId 保存目标 ID
 * @param {() => object} options.getGraph 获取 graph DSL
 * @param {(id: string, graph: object) => Promise<void>} options.saveFn 保存函数
 * @param {import('vue').Ref<string>|import('vue').ComputedRef<string>} [options.structureFingerprint] 图结构指纹（忽略坐标）
 * @param {number} [options.debounceMs=4000] 防抖毫秒
 * @param {import('vue').Ref<boolean>} [options.enabled] 是否启用
 * @returns {{ saveStatus: import('vue').Ref<SaveStatus>, markDirty: () => void, saveNow: () => Promise<void> }}
 */
export function useAutoSave({
  targetId,
  getGraph,
  saveFn,
  structureFingerprint,
  debounceMs = 4000,
  enabled
}) {
  /** @type {import('vue').Ref<SaveStatus>} */
  const saveStatus = ref('saved')
  let timer = null
  let saving = false

  function clearTimer() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  function scheduleSave() {
    if (enabled?.value === false) return
    if (!targetId.value) return
    clearTimer()
    saveStatus.value = 'dirty'
    timer = setTimeout(() => {
      saveNow()
    }, debounceMs)
  }

  function markDirty() {
    scheduleSave()
  }

  async function saveNow() {
    if (!targetId.value || saving || !saveFn) return
    clearTimer()
    saving = true
    saveStatus.value = 'saving'
    try {
      await saveFn(targetId.value, getGraph())
      saveStatus.value = 'saved'
    } catch {
      saveStatus.value = 'dirty'
    } finally {
      saving = false
    }
  }

  function onKeyDown(e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault()
      saveNow()
    }
  }

  if (structureFingerprint) {
    watch(structureFingerprint, () => {
      markDirty()
    })
  }

  onMounted(() => {
    window.addEventListener('keydown', onKeyDown)
  })

  onBeforeUnmount(() => {
    clearTimer()
    window.removeEventListener('keydown', onKeyDown)
  })

  return { saveStatus, markDirty, saveNow }
}
