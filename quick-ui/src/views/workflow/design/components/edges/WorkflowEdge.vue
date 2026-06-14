<template>
  <g>
    <BaseEdge :id="id" :path="path" :style="edgeStyle" :marker-end="markerEnd" />
    <EdgeLabelRenderer>
      <div v-if="labelText" class="wf-edge-label" :style="labelStyle">
        {{ labelText }}
      </div>
      <button
        v-if="selected"
        type="button"
        class="wf-edge-delete"
        :style="deleteBtnStyle"
        title="删除连线"
        @click.stop="onDelete"
      >
        <el-icon :size="12"><Close /></el-icon>
      </button>
    </EdgeLabelRenderer>
  </g>
</template>

<script setup>
import { computed, inject } from 'vue'
import { BaseEdge, EdgeLabelRenderer, getBezierPath } from '@vue-flow/core'
import { Close } from '@element-plus/icons-vue'

defineOptions({ name: 'WorkflowEdge' })

const props = defineProps({
  id: { type: String, required: true },
  source: { type: String, required: true },
  target: { type: String, required: true },
  sourceHandle: { type: String, default: null },
  sourceX: { type: Number, required: true },
  sourceY: { type: Number, required: true },
  targetX: { type: Number, required: true },
  targetY: { type: Number, required: true },
  sourcePosition: { type: String, required: true },
  targetPosition: { type: String, required: true },
  data: { type: Object, default: () => ({}) },
  markerEnd: { type: String, default: undefined },
  sourceNode: { type: Object, default: null },
  selected: { type: Boolean, default: false }
})

/** @type {import('vue').Inject<(id: string) => void>} */
const deleteEdge = inject('deleteWorkflowEdge', null)

const path = computed(() => {
  const [p] = getBezierPath({
    sourceX: props.sourceX,
    sourceY: props.sourceY,
    targetX: props.targetX,
    targetY: props.targetY,
    sourcePosition: props.sourcePosition,
    targetPosition: props.targetPosition
  })
  return p
})

const edgeStyle = computed(() => ({
  stroke: props.selected ? '#409eff' : '#94b8ff',
  strokeWidth: props.selected ? 2.5 : 2
}))

const labelText = computed(() => {
  if (props.data?.label) return props.data.label
  const handle = props.sourceHandle
  if (!handle) return ''
  const wfType = props.sourceNode?.data?.wfType
  if (wfType === 'if-else') {
    if (handle === 'false') return '否则'
    const branches = props.sourceNode?.data?.branches || []
    const branch = branches.find((b) => b.id === handle)
    if (branch?.name) return branch.name
    if (handle === 'true') return '如果'
    return handle
  }
  if (wfType === 'question-classifier') {
    const classes = props.sourceNode?.data?.classes || []
    const cls = classes.find((c) => c.id === handle)
    return cls?.name || handle
  }
  return handle
})

const midPoint = computed(() => ({
  x: (props.sourceX + props.targetX) / 2,
  y: (props.sourceY + props.targetY) / 2
}))

const labelStyle = computed(() => ({
  position: 'absolute',
  transform: `translate(-50%, -50%) translate(${midPoint.value.x}px, ${midPoint.value.y - 14}px)`,
  pointerEvents: 'none'
}))

const deleteBtnStyle = computed(() => ({
  position: 'absolute',
  transform: `translate(-50%, -50%) translate(${midPoint.value.x}px, ${midPoint.value.y + (labelText.value ? 8 : 0)}px)`,
  pointerEvents: 'all'
}))

function onDelete() {
  deleteEdge?.(props.id)
}
</script>

<style scoped lang="scss">
.wf-edge-label {
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  color: #0a2463;
  background: #fff;
  border: 1px solid #dfe3ea;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(10, 36, 99, 0.08);
}

.wf-edge-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  padding: 0;
  border: 1px solid #dcdfe6;
  border-radius: 50%;
  background: #fff;
  color: #909399;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: color 0.15s, border-color 0.15s, background 0.15s;

  &:hover {
    color: #f56c6c;
    border-color: #f56c6c;
    background: #fef0f0;
  }
}
</style>
