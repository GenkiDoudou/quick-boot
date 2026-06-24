<template>
  <div class="wf-copy-block">
    <div v-if="label || copyable" class="wf-copy-block__head">
      <span v-if="label" class="wf-copy-block__label">{{ label }}</span>
      <C7Copy
        v-if="copyable && displayText"
        mode="icon"
        :text="displayText"
        success-message="已复制"
        class="wf-copy-block__copy"
      />
    </div>
    <pre class="wf-copy-block__pre">{{ displayText || emptyText }}</pre>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import C7Copy from '@/packages/C7Copy/index.vue'

defineOptions({ name: 'CopyableCodeBlock' })

const props = defineProps({
  /** 展示文本 */
  content: { type: String, default: '' },
  /** 区块标题 */
  label: { type: String, default: '' },
  /** 空内容占位 */
  emptyText: { type: String, default: '—' },
  /** 是否显示复制按钮 */
  copyable: { type: Boolean, default: true }
})

const displayText = computed(() => {
  const text = props.content == null ? '' : String(props.content)
  return text.trim() ? text : ''
})
</script>

<style scoped lang="scss">
.wf-copy-block {
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  background: #f7f8fa;
  overflow: hidden;
}

.wf-copy-block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 8px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
}

.wf-copy-block__label {
  font-size: 11px;
  font-weight: 600;
  color: #86909c;
}

.wf-copy-block__copy {
  margin-left: auto;
}

.wf-copy-block__pre {
  margin: 0;
  padding: 8px 10px;
  max-height: 180px;
  overflow: auto;
  font-size: 11px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'SF Mono', Consolas, Monaco, 'Courier New', monospace;
  color: #1d2129;
}
</style>
