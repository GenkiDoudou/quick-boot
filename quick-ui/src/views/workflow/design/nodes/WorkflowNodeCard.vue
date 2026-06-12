<template>
  <div
    class="wf-card"
    :class="cardClasses"
    :style="{ '--wf-color': nodeColor }"
  >
    <div class="wf-card__bar" />
    <Handle type="target" :position="Position.Left" class="wf-card__handle" />

    <div class="wf-card__inner">
      <div class="wf-card__header">
        <el-icon class="wf-card__icon" :size="16">
          <component :is="iconComponent" />
        </el-icon>
        <span class="wf-card__type">{{ typeLabel }}</span>
        <span v-if="showWarning" class="wf-card__warn" title="必填项未配置" />
      </div>
      <div class="wf-card__title">{{ displayLabel }}</div>
      <div class="wf-card__summary">{{ summaryText }}</div>
    </div>

    <Handle
      v-if="showTrueHandle"
      id="true"
      type="source"
      :position="Position.Right"
      class="wf-card__handle wf-card__handle--branch"
      :style="{ top: '35%' }"
    />
    <Handle
      v-if="showFalseHandle"
      id="false"
      type="source"
      :position="Position.Right"
      class="wf-card__handle wf-card__handle--branch"
      :style="{ top: '65%' }"
    />
    <Handle
      v-if="showDefaultSource"
      type="source"
      :position="Position.Right"
      class="wf-card__handle"
    />
    <template v-if="showClassifierHandles">
      <Handle
        v-for="(cls, idx) in classifierClasses"
        :key="cls.id || idx"
        :id="cls.id || `class_${idx}`"
        type="source"
        :position="Position.Right"
        class="wf-card__handle wf-card__handle--branch"
        :style="{ top: `${20 + idx * 18}%` }"
      />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import {
  Upload,
  Flag,
  Cpu,
  Collection,
  Switch,
  Document,
  EditPen,
  Connection,
  Link,
  Grid,
  Filter,
  List
} from '@element-plus/icons-vue'
import { getNodeColor, getNodeLabel, summarizeNode, hasNodeValidationWarning } from '../nodeMeta'

defineOptions({ name: 'WorkflowNodeCard' })

const props = defineProps({
  id: { type: String, default: '' },
  data: { type: Object, default: () => ({}) },
  selected: { type: Boolean, default: false }
})

const ICON_MAP = {
  Upload,
  Flag,
  Cpu,
  Collection,
  Switch,
  Document,
  EditPen,
  Connection,
  Link,
  Grid,
  Filter,
  List
}

const wfType = computed(() => props.data?.wfType || 'unknown')
const typeLabel = computed(() => getNodeLabel(wfType.value))
const nodeColor = computed(() => getNodeColor(wfType.value))
const displayLabel = computed(() => props.data?.label || typeLabel.value)
const summaryText = computed(() =>
  summarizeNode(wfType.value, props.data, props.data?._kbNameMap || {})
)
const iconComponent = computed(() => ICON_MAP[props.data?._icon] || Cpu)

const showWarning = computed(() => {
  if (props.data?.validationError) return true
  return hasNodeValidationWarning(wfType.value, props.data)
})

const cardClasses = computed(() => ({
  'wf-card--selected': props.selected,
  'wf-card--running': props.data?.runStatus === 'RUNNING',
  'wf-card--success': props.data?.runStatus === 'SUCCESS',
  'wf-card--failed': props.data?.runStatus === 'FAILED',
  'wf-card--error': !!props.data?.validationError
}))

const showTrueHandle = computed(() => wfType.value === 'if-else')
const showFalseHandle = computed(() => wfType.value === 'if-else')
const showClassifierHandles = computed(() => wfType.value === 'question-classifier')
const showDefaultSource = computed(() => !showTrueHandle.value && !showClassifierHandles.value)

const classifierClasses = computed(() => {
  const classes = props.data?.classes
  return Array.isArray(classes) && classes.length ? classes : [{ id: 'default', name: '默认' }]
})
</script>

<style scoped lang="scss">
.wf-card {
  position: relative;
  width: 240px;
  border-radius: 12px;
  background: #fff;
  border: 2px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(10, 36, 99, 0.08);
  font-size: 13px;
  font-family: 'PingFang SC', 'Helvetica Neue', sans-serif;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(10, 36, 99, 0.12);
  }

  &--selected {
    border-color: #409eff;
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
  }

  &--running {
    border-color: #409eff;
    animation: wf-pulse 1.5s ease-in-out infinite;
  }

  &--success {
    border-color: #67c23a;
  }

  &--failed {
    border-color: #f56c6c;
  }

  &--error {
    border-color: #f56c6c;
    box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.25);
  }
}

@keyframes wf-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4); }
  50% { box-shadow: 0 0 0 6px rgba(64, 158, 255, 0); }
}

.wf-card__bar {
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 4px;
  border-radius: 0 2px 2px 0;
  background: var(--wf-color, #409eff);
}

.wf-card__inner {
  padding: 10px 12px 10px 14px;
}

.wf-card__header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.wf-card__icon {
  color: var(--wf-color, #409eff);
}

.wf-card__type {
  font-size: 12px;
  font-weight: 600;
  color: var(--wf-color, #409eff);
  flex: 1;
}

.wf-card__warn {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e6a23c;
  flex-shrink: 0;
}

.wf-card__title {
  font-size: 14px;
  font-weight: 600;
  color: #0a2463;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wf-card__summary {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wf-card__handle {
  width: 10px;
  height: 10px;
  background: var(--wf-color, #409eff);
  border: 2px solid #fff;
}
</style>
