import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { saveGraph } from '@/api/workflow'

/** @typedef {'saved' | 'saving' | 'dirty'} SaveStatus */

/**
 * 工作流图自动保存（debounce 3s + Ctrl+S 手动保存）。
 * @param {object} options
 * @param {import('vue').Ref<string>} options.workflowId 工作流 ID（字符串）
 * @param {import('vue').Ref<Array>} options.nodes 画布节点
 * @param {import('vue').Ref<Array>} options.edges 画布边
 * @param {() => object} options.getGraph 获取 graph DSL 的函数
 * @param {number} [options.debounceMs=3000] 防抖毫秒
 * @returns {{ saveStatus: import('vue').Ref<SaveStatus>, markDirty: () => void, saveNow: () => Promise<void> }}
 */
export function useAutoSave({ workflowId, nodes, edges, getGraph, debounceMs = 3000, enabled }) {
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
    if (!workflowId.value || saving) return
    clearTimer()
    saving = true
    saveStatus.value = 'saving'
    try {
      await saveGraph({ workflowId: workflowId.value, graph: getGraph() })
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

  watch([nodes, edges], () => {
    if (enabled?.value === false) return
    if (workflowId.value) {
      markDirty()
    }
  }, { deep: true })

  onMounted(() => {
    window.addEventListener('keydown', onKeyDown)
  })

  onBeforeUnmount(() => {
    clearTimer()
    window.removeEventListener('keydown', onKeyDown)
  })

  return { saveStatus, markDirty, saveNow }
}
