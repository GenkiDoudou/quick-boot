<template>
  <div
    class="wf-run-step"
    :class="{
      'wf-run-step--failed': step.status === 'FAILED',
      'wf-run-step--running': step.status === 'RUNNING',
      'wf-run-step--expanded': expanded,
      'wf-run-step--active': active
    }"
  >
    <div class="wf-run-step__rail">
      <span class="wf-run-step__order">{{ displayOrder }}</span>
      <span v-if="!isLast" class="wf-run-step__rail-line" />
    </div>

    <div class="wf-run-step__main">
      <div class="wf-run-step__row" @click="$emit('toggle')">
        <span class="wf-run-step__icon-box" :style="iconBoxStyle">
          <el-icon :size="14">
            <component :is="iconComponent" />
          </el-icon>
        </span>

        <div class="wf-run-step__info">
          <span class="wf-run-step__name">{{ nodeTitle }}</span>
          <span class="wf-run-step__type">{{ typeLabel }}</span>
        </div>

        <div class="wf-run-step__badges">
          <span v-if="loopBadge" class="wf-run-step__chip">{{ loopBadge }}</span>
          <span v-if="mcpBadge" class="wf-run-step__chip wf-run-step__chip--mcp">{{ mcpBadge }}</span>
        </div>

        <div class="wf-run-step__metrics">
          <span v-if="tokenLabel" class="wf-run-step__token">{{ tokenLabel }}</span>
          <span v-if="step.durationMs != null" class="wf-run-step__duration">{{ formatDurationMs(step.durationMs) }}</span>
        </div>

        <span class="wf-run-step__status" :class="`wf-run-step__status--${statusTone}`" :title="statusLabel">
          <el-icon v-if="step.status === 'SUCCESS'" :size="14"><CircleCheck /></el-icon>
          <el-icon v-else-if="step.status === 'FAILED'" :size="14"><CircleClose /></el-icon>
          <span v-else class="wf-run-step__status-spinner" />
        </span>

        <el-icon class="wf-run-step__caret" :class="{ 'is-expanded': expanded }">
          <ArrowRight />
        </el-icon>

        <button type="button" class="wf-run-step__locate" title="定位节点" @click.stop="$emit('focus')">
          定位
        </button>
      </div>

      <div v-show="expanded" class="wf-run-step__detail">
        <div class="wf-run-step__seg">
          <button
            v-for="tab in visibleTabs"
            :key="tab.name"
            type="button"
            class="wf-run-step__seg-btn"
            :class="{ 'is-active': activeTab === tab.name }"
            @click="activeTab = tab.name"
          >
            {{ tab.label }}
          </button>
        </div>

      <div v-show="activeTab === 'input'" class="wf-run-step__block">
        <CopyableCodeBlock :content="formatStepIo(step.inputs)" label="输入" />
      </div>

      <div v-show="activeTab === 'output'" class="wf-run-step__block">
        <CopyableCodeBlock
          v-if="step.outputs != null"
          :content="formatStepOutputs(step.outputs)"
          label="输出"
        />
          <p v-else-if="step.status === 'RUNNING'" class="wf-run-step__hint">执行中…</p>
          <p v-else class="wf-run-step__hint">无输出</p>
        </div>

        <div v-show="activeTab === 'mcp'" class="wf-run-step__block">
          <div v-if="mcpCalls.length" class="wf-run-step__mcp-list">
            <div
              v-for="(call, callIdx) in mcpCalls"
              :key="`${call.toolName}_${callIdx}`"
              class="wf-run-step__mcp-item"
            >
              <div class="wf-run-step__mcp-head">
                <span class="wf-run-step__mcp-name">{{ call.toolName }}</span>
                <span class="wf-run-step__mcp-index">#{{ callIdx + 1 }}</span>
              </div>
                <div class="wf-run-step__mcp-field">
                  <CopyableCodeBlock :content="formatMcpPayload(call.input)" label="参数" />
                </div>
                <div class="wf-run-step__mcp-field">
                  <CopyableCodeBlock :content="formatMcpPayload(call.output)" label="返回" />
                </div>
            </div>
          </div>
          <p v-else class="wf-run-step__hint">MCP 已启用，本次未调用工具</p>
        </div>

        <div v-show="activeTab === 'meta'" class="wf-run-step__block">
          <dl class="wf-run-step__meta">
            <div class="wf-run-step__meta-row">
              <dt>节点 ID</dt>
              <dd><code>{{ step.nodeId }}</code></dd>
            </div>
            <div class="wf-run-step__meta-row">
              <dt>节点类型</dt>
              <dd>{{ typeLabel }}</dd>
            </div>
            <div v-if="step.orderNo != null" class="wf-run-step__meta-row">
              <dt>执行序号</dt>
              <dd>{{ step.orderNo }}</dd>
            </div>
            <div class="wf-run-step__meta-row">
              <dt>耗时</dt>
              <dd>{{ formatDurationMs(step.durationMs) }}</dd>
            </div>
            <div class="wf-run-step__meta-row">
              <dt>Token</dt>
              <dd>{{ formatTokenUsageDetail(tokenUsage) }}</dd>
            </div>
            <div v-if="step.errorMsg" class="wf-run-step__meta-row">
              <dt>错误</dt>
              <dd class="is-error">{{ step.errorMsg }}</dd>
            </div>
            <div v-if="loopBadge" class="wf-run-step__meta-row">
              <dt>循环</dt>
              <dd>{{ loopBadge }}</dd>
            </div>
            <div v-if="meta.mcpEnabled" class="wf-run-step__meta-row">
              <dt>MCP</dt>
              <dd>
                <template v-if="mcpCalls.length">
                  已调用 {{ mcpCalls.length }} 次：{{ mcpCalls.map((c) => c.toolName).join('、') }}
                </template>
                <template v-else>已启用，本次未调用</template>
              </dd>
            </div>
          </dl>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  ArrowRight,
  Box,
  CircleCheck,
  CircleClose,
  Collection,
  Connection,
  Cpu,
  DArrowRight,
  Document,
  DocumentCopy,
  Edit,
  EditPen,
  Filter,
  Flag,
  Grid,
  Link,
  List,
  Refresh,
  Switch,
  Upload,
  VideoPlay
} from '@element-plus/icons-vue'
import { getNodeColor, getNodeIcon, getNodeLabel } from '../nodeMeta'
import {
  extractMcpToolResults,
  extractStepMeta,
  extractTokenUsage,
  formatDurationMs,
  formatLoopIterationBadge,
  formatMcpCallBadge,
  formatMcpPayload,
  formatStepIo,
  formatStepOutputs,
  formatTokenUsage,
  formatTokenUsageDetail,
  hasMcpTrace
} from '../utils/runTraceUtils'
import CopyableCodeBlock from './CopyableCodeBlock.vue'

defineOptions({ name: 'RunStepCard' })

const ICON_MAP = {
  Upload,
  CircleCheck,
  Flag,
  Cpu,
  Collection,
  Switch,
  Document,
  EditPen,
  Connection,
  Grid,
  Refresh,
  VideoPlay,
  CircleClose,
  DArrowRight,
  Edit,
  DocumentCopy,
  Link,
  Filter,
  List,
  Box
}

const props = defineProps({
  step: { type: Object, required: true },
  stepIndex: { type: Number, default: 0 },
  expanded: { type: Boolean, default: false },
  active: { type: Boolean, default: false },
  isLast: { type: Boolean, default: false },
  nodeTitle: { type: String, default: '' }
})

defineEmits(['toggle', 'focus'])

const activeTab = ref('output')

const typeLabel = computed(() => getNodeLabel(props.step?.nodeType) || props.step?.nodeType || '节点')
const nodeColor = computed(() => getNodeColor(props.step?.nodeType))
const iconComponent = computed(() => ICON_MAP[getNodeIcon(props.step?.nodeType)] || Box)
const loopBadge = computed(() => formatLoopIterationBadge(props.step))
const tokenUsage = computed(() => extractTokenUsage(props.step))
const tokenLabel = computed(() => formatTokenUsage(tokenUsage.value))
const meta = computed(() => extractStepMeta(props.step))
const mcpCalls = computed(() => extractMcpToolResults(props.step))
const mcpBadge = computed(() => formatMcpCallBadge(props.step))
const showMcpTab = computed(() => hasMcpTrace(props.step))

const displayOrder = computed(() => {
  const order = props.step?.orderNo
  if (order != null && order !== '') return Number(order) + 1
  return props.stepIndex + 1
})

const iconBoxStyle = computed(() => ({
  color: nodeColor.value,
  background: `${nodeColor.value}14`
}))

const visibleTabs = computed(() => {
  const tabs = [
    { name: 'input', label: '输入' },
    { name: 'output', label: '输出' }
  ]
  if (showMcpTab.value) {
    tabs.push({ name: 'mcp', label: 'MCP' })
  }
  tabs.push({ name: 'meta', label: '详情' })
  return tabs
})

const statusLabel = computed(() => {
  const s = props.step?.status
  if (s === 'FAILED') return '失败'
  if (s === 'SUCCESS') return '成功'
  if (s === 'RUNNING') return '运行中'
  return s || '—'
})

const statusTone = computed(() => {
  const s = props.step?.status
  if (s === 'FAILED') return 'danger'
  if (s === 'SUCCESS') return 'success'
  if (s === 'RUNNING') return 'running'
  return 'neutral'
})
</script>

<style scoped lang="scss">
.wf-run-step {
  display: flex;
  gap: 10px;
  padding: 2px 4px;
  border-radius: 8px;
  transition: background 0.15s;

  &:hover .wf-run-step__row {
    background: #f7f8fa;
  }

  &--active {
    background: rgba(51, 112, 255, 0.04);
  }

  &--failed .wf-run-step__row {
    background: #fff7f7;
  }

  &--running .wf-run-step__row {
    background: #fffbf5;
  }

  &--expanded .wf-run-step__row {
    background: #f7f8fa;
  }
}

.wf-run-step__rail {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 22px;
  flex-shrink: 0;
  padding-top: 10px;
}

.wf-run-step__order {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  font-size: 10px;
  font-weight: 600;
  color: #86909c;
  background: #f2f3f5;
  flex-shrink: 0;
}

.wf-run-step__rail-line {
  flex: 1;
  width: 1px;
  min-height: 8px;
  margin-top: 4px;
  background: #e5e6eb;
}

.wf-run-step__main {
  flex: 1;
  min-width: 0;
}

.wf-run-step__row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  padding: 6px 8px;
  border-radius: 8px;
  cursor: pointer;
}

.wf-run-step__icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  flex-shrink: 0;
}

.wf-run-step__info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
  flex: 1;
}

.wf-run-step__name {
  font-size: 13px;
  font-weight: 500;
  color: #1d2129;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.wf-run-step__type {
  font-size: 11px;
  color: #86909c;
}

.wf-run-step__badges {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.wf-run-step__chip {
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10px;
  color: #86909c;
  background: #f2f3f5;
  white-space: nowrap;

  &--mcp {
    color: #ff7d00;
    background: #fff7e8;
  }
}

.wf-run-step__metrics {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.wf-run-step__token {
  font-size: 11px;
  color: #3370ff;
  background: #e8f3ff;
  padding: 1px 6px;
  border-radius: 4px;
  white-space: nowrap;
}

.wf-run-step__duration {
  font-size: 11px;
  color: #86909c;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  min-width: 48px;
  text-align: right;
}

.wf-run-step__status {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  flex-shrink: 0;

  &--success {
    color: #00b42a;
  }

  &--danger {
    color: #f53f3f;
  }

  &--running {
    color: #3370ff;
  }
}

.wf-run-step__status-spinner {
  width: 12px;
  height: 12px;
  border: 2px solid #e8f3ff;
  border-top-color: #3370ff;
  border-radius: 50%;
  animation: wf-step-spin 0.8s linear infinite;
}

.wf-run-step__caret {
  flex-shrink: 0;
  color: #c9cdd4;
  font-size: 12px;
  transition: transform 0.15s;

  &.is-expanded {
    transform: rotate(90deg);
  }
}

.wf-run-step__locate {
  border: none;
  background: none;
  padding: 0 2px;
  font-size: 11px;
  color: #3370ff;
  cursor: pointer;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.15s;

  .wf-run-step:hover &,
  .wf-run-step--active & {
    opacity: 1;
  }
}

.wf-run-step__detail {
  margin: 0 0 8px 36px;
  padding: 10px;
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
}

.wf-run-step__seg {
  display: inline-flex;
  gap: 2px;
  padding: 2px;
  margin-bottom: 10px;
  background: #f2f3f5;
  border-radius: 6px;
}

.wf-run-step__seg-btn {
  border: none;
  background: transparent;
  padding: 4px 12px;
  font-size: 12px;
  color: #86909c;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    color: #1d2129;
  }

  &.is-active {
    background: #fff;
    color: #1d2129;
    font-weight: 500;
    box-shadow: 0 1px 2px rgba(29, 33, 41, 0.06);
  }
}

.wf-run-step__block-label {
  font-size: 11px;
  font-weight: 600;
  color: #86909c;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.wf-run-step__code {
  margin: 0;
  padding: 10px 12px;
  max-height: 200px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'SF Mono', Consolas, Monaco, 'Courier New', monospace;
  color: #1d2129;
  background: #f7f8fa;
  border: 1px solid #e5e6eb;
  border-radius: 6px;

  &--nested {
    max-height: 140px;
  }
}

.wf-run-step__hint {
  margin: 0;
  padding: 8px 0;
  font-size: 12px;
  color: #86909c;
}

.wf-run-step__meta {
  margin: 0;
}

.wf-run-step__meta-row {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 8px;
  padding: 5px 0;
  border-bottom: 1px dashed #f2f3f5;
  font-size: 12px;

  &:last-child {
    border-bottom: none;
  }

  dt {
    margin: 0;
    color: #86909c;
  }

  dd {
    margin: 0;
    color: #1d2129;
    word-break: break-word;

    &.is-error {
      color: #f53f3f;
    }

    code {
      font-size: 11px;
      padding: 1px 4px;
      background: #f2f3f5;
      border-radius: 4px;
    }
  }
}

.wf-run-step__mcp-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.wf-run-step__mcp-item {
  border: 1px solid #ffe4ba;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.wf-run-step__mcp-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background: #fff7e8;
}

.wf-run-step__mcp-name {
  font-size: 12px;
  font-weight: 600;
  color: #ff7d00;
}

.wf-run-step__mcp-index {
  font-size: 11px;
  color: #86909c;
}

.wf-run-step__mcp-field {
  padding: 8px 10px 0;

  &:last-child {
    padding-bottom: 10px;
  }
}

.wf-run-step__mcp-label {
  display: block;
  margin-bottom: 4px;
  font-size: 11px;
  font-weight: 500;
  color: #86909c;
}

@keyframes wf-step-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
