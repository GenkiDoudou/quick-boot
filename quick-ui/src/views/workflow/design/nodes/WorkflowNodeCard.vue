<template>
  <div
    class="wf-card"
    :class="cardClasses"
    :style="cardStyle"
  >
    <div class="wf-card__bar" />
    <Handle
      v-if="showBodyInHandle"
      id="body-in"
      type="target"
      :position="Position.Top"
      class="wf-card__handle wf-card__handle--body-in"
    />
    <Handle
      v-if="showBodyEntryHandle"
      id="body-entry"
      type="source"
      :position="Position.Left"
      class="wf-card__handle wf-card__handle--body-entry"
    />
    <Handle
      v-if="showBodyExitHandle"
      id="body-exit"
      type="target"
      :position="Position.Right"
      class="wf-card__handle wf-card__handle--body-exit"
    />
    <Handle
      v-if="showTargetHandle"
      type="target"
      :position="Position.Left"
      class="wf-card__handle"
    />

    <div class="wf-card__inner" :class="{ 'wf-card__inner--selector': showIfElseHandles }">
      <div class="wf-card__header">
        <el-icon class="wf-card__icon" :size="16">
          <component :is="iconComponent" />
        </el-icon>
        <span class="wf-card__type">{{ typeLabel }}</span>
        <span v-if="showWarning" class="wf-card__warn" title="必填项未配置" />
      </div>
      <div v-if="!showIfElseHandles && wfType !== 'loop-body' && wfType !== 'batch-body'" class="wf-card__title">{{ displayLabel }}</div>
      <div v-if="wfType === 'loop'" class="wf-card__loop-sections">
        <div class="wf-card__loop-row">
          <span class="wf-card__loop-label">输入</span>
          <span class="wf-card__loop-value">{{ loopSections.input }}</span>
        </div>
        <div class="wf-card__loop-row">
          <span class="wf-card__loop-label">中间变量</span>
          <span class="wf-card__loop-value">{{ loopSections.intermediate }}</span>
        </div>
        <div class="wf-card__loop-row">
          <span class="wf-card__loop-label">输出</span>
          <span class="wf-card__loop-value">{{ loopSections.output }}</span>
        </div>
      </div>
      <div v-else-if="!showIfElseHandles && wfType !== 'loop-body' && wfType !== 'batch-body'" class="wf-card__summary">{{ summaryText }}</div>
      <div v-else-if="wfType === 'loop-body' || wfType === 'batch-body'" class="wf-card__body-port-labels">
        <span class="wf-card__body-port wf-card__body-port--entry">开始</span>
        <span class="wf-card__body-port wf-card__body-port--exit">结束</span>
      </div>

      <!-- 选择器：分支行 + 每行一个出口 -->
      <div v-if="showIfElseHandles" class="wf-card__selector">
        <div
          v-for="row in ifElseCanvasRows"
          :key="row.id"
          class="wf-card__selector-row"
          :class="{ 'wf-card__selector-row--else': row.id === 'false' }"
        >
          <span class="wf-card__selector-label">{{ row.label }}</span>
          <span v-if="row.preview" class="wf-card__selector-preview">{{ row.preview }}</span>
          <Handle
            :id="row.id"
            type="source"
            :position="Position.Right"
            class="wf-card__handle wf-card__handle--row"
          />
        </div>
      </div>
    </div>

    <Handle
      v-if="showContainerBodySource"
      id="body"
      type="source"
      :position="Position.Bottom"
      class="wf-card__handle wf-card__handle--body-out"
    />
    <span v-if="containerBodyPortTip" class="wf-card__port-tip wf-card__port-tip--body">{{ containerBodyPortTip }}</span>
    <Handle
      v-if="showFlowOutSource"
      id="flow-out"
      type="source"
      :position="Position.Right"
      class="wf-card__handle wf-card__handle--flow-out"
    />
    <span v-if="showFlowOutSource" class="wf-card__port-tip wf-card__port-tip--flow">下一步</span>
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
  List,
  CircleCheck,
  Refresh,
  CircleClose,
  DArrowRight,
  VideoPlay
} from '@element-plus/icons-vue'
import { getNodeColor, getNodeLabel, summarizeNode, hasNodeValidationWarning } from '../nodeMeta'
import { resolveIfElseCanvasRows } from '../utils/ifElseBranchUtils'
import { resolveLoopCardSections } from '../utils/loopUtils'

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
  List,
  CircleCheck,
  Refresh,
  CircleClose,
  DArrowRight,
  VideoPlay
}

const wfType = computed(() => props.data?.wfType || 'unknown')
const typeLabel = computed(() => getNodeLabel(wfType.value))
const nodeColor = computed(() => getNodeColor(wfType.value))
const cardStyle = computed(() => {
  const style = { '--wf-color': nodeColor.value }
  if (wfType.value === 'loop-body' || wfType.value === 'batch-body') {
    style.width = '100%'
    style.height = '100%'
    style.minHeight = '200px'
  }
  return style
})
const displayLabel = computed(() => props.data?.label || typeLabel.value)
const summaryText = computed(() =>
  summarizeNode(wfType.value, props.data, props.data?._kbNameMap || {})
)
const loopSections = computed(() =>
  wfType.value === 'loop' ? resolveLoopCardSections(props.data) : { input: '', intermediate: '', output: '' }
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
  'wf-card--error': !!props.data?.validationError,
  'wf-card--selector': showIfElseHandles.value,
  'wf-card--loop-body': wfType.value === 'loop-body',
  'wf-card--batch-body': wfType.value === 'batch-body',
  'wf-card--loop-head': wfType.value === 'loop',
  'wf-card--batch-head': wfType.value === 'batch'
}))

const showBodyInHandle = computed(() => wfType.value === 'loop-body' || wfType.value === 'batch-body')
const showBodyEntryHandle = computed(() => wfType.value === 'loop-body' || wfType.value === 'batch-body')
const showBodyExitHandle = computed(() => wfType.value === 'loop-body' || wfType.value === 'batch-body')
const showContainerBodySource = computed(() => wfType.value === 'loop' || wfType.value === 'batch')
const showFlowOutSource = computed(() => wfType.value === 'loop' || wfType.value === 'batch')
const containerBodyPortTip = computed(() => {
  if (wfType.value === 'loop') return '循环体'
  if (wfType.value === 'batch') return '批处理体'
  return ''
})
const showTargetHandle = computed(() => {
  if (wfType.value === 'loop-body' || wfType.value === 'batch-body') return false
  return true
})
const showIfElseHandles = computed(() => wfType.value === 'if-else')
const showClassifierHandles = computed(() => wfType.value === 'question-classifier')
const showDefaultSource = computed(() => {
  if (showIfElseHandles.value || showClassifierHandles.value) return false
  if (wfType.value === 'loop-body' || wfType.value === 'batch-body') return false
  if (wfType.value === 'loop' || wfType.value === 'batch') return false
  return true
})

const ifElseCanvasRows = computed(() => resolveIfElseCanvasRows(props.data))

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

  &--selector {
    width: 280px;
  }

  &--loop-body {
    width: 100%;
    height: 100%;
    min-height: 200px;
    border-style: dashed;
    border-color: #b3d8ff;
    background: rgba(236, 245, 255, 0.45);
    box-shadow: none;

    .wf-card__inner {
      opacity: 0.85;
    }
  }

  &--loop-head {
    padding-bottom: 18px;
  }

  &--batch-head {
    padding-bottom: 18px;
  }

  &--batch-body {
    width: 100%;
    height: 100%;
    min-height: 200px;
    border-style: dashed;
    border-color: #c2e7b0;
    background: rgba(240, 249, 235, 0.45);
    box-shadow: none;

    .wf-card__inner {
      opacity: 0.85;
    }
  }

  &--loop-anchor {
    display: none;
  }

  &__handle--body-in {
    top: -4px;
  }

  &__handle--body-out {
    bottom: -6px;
    left: 50%;
    transform: translateX(-50%);
  }

  &__handle--flow-out {
    right: -4px;
    top: 42%;
  }

  &__handle--body-entry {
    left: -4px;
    top: 50%;
  }

  &__handle--body-exit {
    right: -4px;
    top: 50%;
  }

  &__port-tip {
    position: absolute;
    font-size: 10px;
    color: #909399;
    pointer-events: none;
    white-space: nowrap;

    &--body {
      bottom: 2px;
      left: 50%;
      transform: translateX(-50%);
    }

    &--flow {
      right: -2px;
      top: calc(42% - 14px);
    }
  }

  &__body-port-labels {
    display: flex;
    justify-content: space-between;
    padding: 4px 8px 0;
    font-size: 11px;
    color: #909399;
  }

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

  &--selector {
    padding-bottom: 6px;
  }
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

.wf-card__selector {
  margin-top: 8px;
  border-top: 1px solid #f0f2f5;
  padding-top: 4px;
}

.wf-card__selector-row {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  padding: 6px 18px 6px 0;
  border-bottom: 1px solid #f5f7fa;

  &:last-child {
    border-bottom: none;
  }

  &--else {
    .wf-card__selector-label {
      color: #e6a23c;
    }
  }
}

.wf-card__selector-label {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  min-width: 52px;
}

.wf-card__selector-preview {
  flex: 1;
  min-width: 0;
  font-size: 11px;
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

  &--row {
    position: absolute !important;
    right: -6px;
    top: 50%;
    transform: translateY(-50%);
  }
}

.wf-card__loop-sections {
  margin-top: 4px;
  font-size: 11px;
  line-height: 1.5;
}

.wf-card__loop-row {
  display: flex;
  gap: 6px;
  margin-bottom: 2px;
}

.wf-card__loop-label {
  flex-shrink: 0;
  color: #909399;
  min-width: 52px;
}

.wf-card__loop-value {
  flex: 1;
  min-width: 0;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
