<template>
  <div
    class="wf-node"
    :class="{ 'wf-node--selected': selected }"
    :style="{ '--wf-node-color': nodeColor }"
  >
    <Handle type="target" :position="Position.Left" class="wf-node__handle" />
    <div class="wf-node__header">
      <span class="wf-node__badge">{{ typeLabel }}</span>
    </div>
    <div class="wf-node__body">
      <div class="wf-node__title">{{ displayLabel }}</div>
      <div v-if="nodeId" class="wf-node__id">{{ nodeId }}</div>
    </div>
    <Handle
      v-if="showTrueHandle"
      id="true"
      type="source"
      :position="Position.Right"
      class="wf-node__handle wf-node__handle--true"
      :style="{ top: '35%' }"
    />
    <Handle
      v-if="showFalseHandle"
      id="false"
      type="source"
      :position="Position.Right"
      class="wf-node__handle wf-node__handle--false"
      :style="{ top: '65%' }"
    />
    <Handle
      v-if="showDefaultSource"
      type="source"
      :position="Position.Right"
      class="wf-node__handle"
    />
    <template v-if="showClassifierHandles">
      <Handle
        v-for="(row, idx) in intentCanvasRows"
        :key="`${intentHandleKey}_${row.id}`"
        :id="row.id"
        type="source"
        :position="Position.Right"
        class="wf-node__handle wf-node__handle--class"
        :style="{ top: `${18 + idx * 16}%` }"
      />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import { getNodeColor, getNodeLabel } from '../nodeMeta'
import { resolveIntentCanvasRows } from '../utils/intentUtils'

defineOptions({ name: 'BaseWorkflowNode' })

const props = defineProps({
  id: { type: String, default: '' },
  data: { type: Object, default: () => ({}) },
  selected: { type: Boolean, default: false }
})

const wfType = computed(() => props.data?.wfType || 'unknown')
const typeLabel = computed(() => getNodeLabel(wfType.value))
const nodeColor = computed(() => getNodeColor(wfType.value))
const displayLabel = computed(() => props.data?.label || typeLabel.value)
const nodeId = computed(() => props.id)

const showTrueHandle = computed(() => wfType.value === 'if-else')
const showFalseHandle = computed(() => wfType.value === 'if-else')
const showClassifierHandles = computed(() => wfType.value === 'question-classifier')
const showDefaultSource = computed(() => !showTrueHandle.value && !showClassifierHandles.value)

const intentCanvasRows = computed(() => resolveIntentCanvasRows(props.data))
const intentHandleKey = computed(() => {
  const intents = props.data?.intents
  const n = Array.isArray(intents) ? intents.length : 0
  return `intent_handles_${n}`
})
</script>

<style scoped lang="scss">
.wf-node {
  min-width: 160px;
  border-radius: 8px;
  background: #fff;
  border: 2px solid var(--wf-node-color, #409eff);
  box-shadow: 0 2px 8px rgba(10, 36, 99, 0.08);
  font-size: 13px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(10, 36, 99, 0.12);
  }

  &--selected {
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.25);
  }
}

.wf-node__header {
  padding: 6px 10px;
  border-bottom: 1px solid #f0f2f5;
  background: color-mix(in srgb, var(--wf-node-color) 12%, #fff);
  border-radius: 6px 6px 0 0;
}

.wf-node__badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--wf-node-color);
  background: #fff;
}

.wf-node__body {
  padding: 10px;
}

.wf-node__title {
  font-weight: 600;
  color: #0a2463;
  margin-bottom: 4px;
}

.wf-node__id {
  font-size: 11px;
  color: #909399;
  word-break: break-all;
}

.wf-node__handle {
  width: 10px;
  height: 10px;
  background: var(--wf-node-color, #409eff);
  border: 2px solid #fff;
}
</style>
