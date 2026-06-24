<template>
  <div
    v-if="visible"
    class="wf-node-trace"
    :class="{
      'wf-node-trace--running': step?.status === 'RUNNING',
      'wf-node-trace--failed': step?.status === 'FAILED'
    }"
    @click.stop
    @mousedown.stop
  >
    <div class="wf-node-trace__head" @click="$emit('toggle')">
      <span class="wf-node-trace__title">运行详情</span>
      <span v-if="step?.durationMs != null" class="wf-node-trace__meta">{{ formatDurationMs(step.durationMs) }}</span>
      <span v-if="tokenLabel" class="wf-node-trace__meta wf-node-trace__meta--token">{{ tokenLabel }}</span>
      <el-icon class="wf-node-trace__caret" :class="{ 'is-expanded': expanded }"><ArrowRight /></el-icon>
    </div>

    <div v-show="expanded" class="wf-node-trace__body">
      <div class="wf-node-trace__seg">
        <button
          v-for="tab in visibleTabs"
          :key="tab.name"
          type="button"
          class="wf-node-trace__seg-btn"
          :class="{ 'is-active': activeTab === tab.name }"
          @click="activeTab = tab.name"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-show="activeTab === 'input'" class="wf-node-trace__pane">
        <CopyableCodeBlock :content="formatStepIo(step?.inputs)" label="输入" />
      </div>

      <div v-show="activeTab === 'output'" class="wf-node-trace__pane">
        <CopyableCodeBlock
          v-if="step?.outputs != null"
          :content="formatStepOutputs(step.outputs)"
          label="输出"
        />
        <p v-else-if="step?.status === 'RUNNING'" class="wf-node-trace__hint">执行中…</p>
        <p v-else class="wf-node-trace__hint">无输出</p>
      </div>

      <div v-show="activeTab === 'mcp'" class="wf-node-trace__pane">
        <template v-if="mcpCalls.length">
          <div v-for="(call, idx) in mcpCalls" :key="`${call.toolName}_${idx}`" class="wf-node-trace__mcp">
            <div class="wf-node-trace__mcp-name">{{ call.toolName }} <span>#{{ idx + 1 }}</span></div>
            <CopyableCodeBlock :content="formatMcpPayload(call.input)" label="参数" />
            <CopyableCodeBlock :content="formatMcpPayload(call.output)" label="返回" />
          </div>
        </template>
        <div v-else class="wf-node-trace__mcp-empty">
          <p v-if="mcpAvailableTools.length" class="wf-node-trace__hint">
            已挂载工具：{{ mcpAvailableTools.join('、') }}
          </p>
          <p class="wf-node-trace__hint wf-node-trace__hint--warn">
            本次模型未调用任何 MCP 工具（mcpToolsUsed 为空）。请确认模型支持 Function Calling，或在系统提示词中明确要求调用工具名。
          </p>
        </div>
      </div>

      <div v-show="activeTab === 'meta'" class="wf-node-trace__pane">
        <CopyableCodeBlock :content="metaText" label="详情" />
      </div>

      <p v-if="step?.errorMsg" class="wf-node-trace__error">{{ step.errorMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import CopyableCodeBlock from './CopyableCodeBlock.vue'
import {
  extractMcpAvailableTools,
  extractMcpToolResults,
  extractMcpToolsUsedNames,
  extractStepMeta,
  extractTokenUsage,
  formatDurationMs,
  formatMcpPayload,
  formatStepIo,
  formatStepOutputs,
  formatTokenUsage,
  formatTokenUsageDetail,
  hasMcpTrace
} from '../utils/runTraceUtils'

defineOptions({ name: 'NodeRunTrace' })

const props = defineProps({
  step: { type: Object, default: null },
  expanded: { type: Boolean, default: true },
  /** 无 step 但处于运行态时仍展示占位 */
  running: { type: Boolean, default: false }
})

defineEmits(['toggle'])

const activeTab = ref('output')

const visible = computed(() => !!props.step || props.running)

const tokenUsage = computed(() => extractTokenUsage(props.step))
const tokenLabel = computed(() => formatTokenUsage(tokenUsage.value))
const mcpCalls = computed(() => extractMcpToolResults(props.step))
const mcpAvailableTools = computed(() => extractMcpAvailableTools(props.step))
const mcpToolsUsedNames = computed(() => extractMcpToolsUsedNames(props.step))
const showMcpTab = computed(() => hasMcpTrace(props.step))

const visibleTabs = computed(() => {
  const tabs = [
    { name: 'input', label: '输入' },
    { name: 'output', label: '输出' }
  ]
  if (showMcpTab.value) tabs.push({ name: 'mcp', label: 'MCP' })
  tabs.push({ name: 'meta', label: '详情' })
  return tabs
})

const metaText = computed(() => {
  const step = props.step
  if (!step) return '—'
  const meta = extractStepMeta(step)
  const lines = [
    `节点 ID: ${step.nodeId ?? '—'}`,
    `节点类型: ${step.nodeType ?? '—'}`,
    `状态: ${step.status ?? '—'}`,
    `耗时: ${formatDurationMs(step.durationMs)}`,
    `Token: ${formatTokenUsageDetail(tokenUsage.value)}`
  ]
  if (meta.mcpEnabled) {
    const available = mcpAvailableTools.value
    const used = mcpToolsUsedNames.value
    if (available.length) {
      lines.push(`可用 MCP 工具: ${available.join('、')}`)
    }
    lines.push(`实际调用: ${used.length ? used.join('、') : '无'}`)
  }
  if (meta.mcpStatusNote) {
    lines.push(`MCP 说明: ${meta.mcpStatusNote}`)
  }
  if (step.errorMsg) lines.push(`错误: ${step.errorMsg}`)
  return lines.join('\n')
})

watch(
  () => props.step?.status,
  (status) => {
    if (status === 'RUNNING') activeTab.value = 'output'
  }
)
</script>

<style scoped lang="scss">
.wf-node-trace {
  margin-top: 8px;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  text-align: left;

  &--running {
    border-color: #bedaff;
    box-shadow: 0 0 0 1px rgba(51, 112, 255, 0.12);
  }

  &--failed {
    border-color: #fdcdc5;
  }
}

.wf-node-trace__head {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  cursor: pointer;
  background: #f7f8fa;
  border-bottom: 1px solid #e5e6eb;

  &:hover {
    background: #f2f3f5;
  }
}

.wf-node-trace__title {
  font-size: 11px;
  font-weight: 600;
  color: #1d2129;
}

.wf-node-trace__meta {
  font-size: 10px;
  color: #86909c;

  &--token {
    color: #3370ff;
    background: #e8f3ff;
    padding: 1px 5px;
    border-radius: 4px;
  }
}

.wf-node-trace__caret {
  margin-left: auto;
  font-size: 12px;
  color: #c9cdd4;
  transition: transform 0.15s;

  &.is-expanded {
    transform: rotate(90deg);
  }
}

.wf-node-trace__body {
  padding: 8px;
}

.wf-node-trace__seg {
  display: flex;
  gap: 2px;
  padding: 2px;
  margin-bottom: 8px;
  background: #f2f3f5;
  border-radius: 6px;
}

.wf-node-trace__seg-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 4px 0;
  font-size: 11px;
  color: #86909c;
  border-radius: 4px;
  cursor: pointer;

  &.is-active {
    background: #fff;
    color: #1d2129;
    font-weight: 500;
    box-shadow: 0 1px 2px rgba(29, 33, 41, 0.06);
  }
}

.wf-node-trace__pane {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.wf-node-trace__mcp {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-bottom: 6px;
  border-bottom: 1px dashed #e5e6eb;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.wf-node-trace__mcp-name {
  font-size: 11px;
  font-weight: 600;
  color: #ff7d00;

  span {
    color: #86909c;
    font-weight: 400;
  }
}

.wf-node-trace__hint {
  margin: 0;
  font-size: 11px;
  color: #86909c;

  &--warn {
    color: #ff7d00;
  }
}

.wf-node-trace__mcp-empty {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.wf-node-trace__error {
  margin: 8px 0 0;
  padding: 6px 8px;
  font-size: 11px;
  color: #f53f3f;
  background: #ffece8;
  border-radius: 4px;
}
</style>
