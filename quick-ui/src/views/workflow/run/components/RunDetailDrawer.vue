<template>
  <el-drawer
    :model-value="visible"
    title="运行详情"
    size="760px"
    destroy-on-close
    class="wf-run-detail-drawer"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div v-loading="loading" class="wf-run-detail">
      <template v-if="detail">
        <div class="wf-run-detail__summary">
          <div class="wf-run-detail__summary-item">
            <span class="wf-run-detail__summary-label">状态</span>
            <el-tag size="small" :type="runStatusTagType(detail.status)">
              {{ formatRunStatusLabel(detail.status) }}
            </el-tag>
          </div>
          <div class="wf-run-detail__summary-item">
            <span class="wf-run-detail__summary-label">步骤</span>
            <strong>{{ runStats.stepCount }}</strong>
          </div>
          <div class="wf-run-detail__summary-item">
            <span class="wf-run-detail__summary-label">总耗时</span>
            <strong>{{ formatDurationMs(runStats.totalDurationMs) }}</strong>
          </div>
          <div class="wf-run-detail__summary-item">
            <span class="wf-run-detail__summary-label">总 Token</span>
            <strong>{{ runStats.totalTokens ? runStats.totalTokens.toLocaleString() : '—' }}</strong>
          </div>
        </div>

        <el-descriptions :column="2" border size="small" class="wf-run-detail__meta">
          <el-descriptions-item label="运行 ID">{{ detail.runId }}</el-descriptions-item>
          <el-descriptions-item label="工作流 ID">{{ detail.workflowId }}</el-descriptions-item>
          <el-descriptions-item label="触发方式">{{ detail.triggerType || '—' }}</el-descriptions-item>
          <el-descriptions-item label="运行模式">{{ detail.runMode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detail.startTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ detail.endTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ detail.createBy || '—' }}</el-descriptions-item>
          <el-descriptions-item label="流式">{{ detail.streamEnabled ? '是' : '否' }}</el-descriptions-item>
        </el-descriptions>

        <p v-if="detail.errorMsg" class="wf-run-detail__error">{{ detail.errorMsg }}</p>

        <div v-if="detail.inputs && Object.keys(detail.inputs).length" class="wf-run-detail__block">
          <div class="wf-run-detail__block-title">运行入参</div>
          <pre class="wf-run-detail__pre">{{ formatStepIo(detail.inputs) }}</pre>
        </div>

        <div v-if="finalOutputText" class="wf-run-detail__block">
          <div class="wf-run-detail__block-title">最终输出</div>
          <pre class="wf-run-detail__pre wf-run-detail__pre--output">{{ finalOutputText }}</pre>
        </div>

        <div class="wf-run-detail__block">
          <div class="wf-run-detail__block-head">
            <div class="wf-run-detail__block-title">执行追踪</div>
            <el-button
              v-if="detail.steps?.length"
              link
              size="small"
              type="primary"
              @click="toggleExpandAll"
            >
              {{ allExpanded ? '全部收起' : '全部展开' }}
            </el-button>
          </div>

          <div v-if="detail.steps?.length" class="wf-run-detail__timeline">
            <RunStepCard
              v-for="(step, idx) in detail.steps"
              :key="step.stepId || `${step.nodeId}_${step.orderNo}_${idx}`"
              :step="step"
              :expanded="isExpanded(step, idx)"
              :is-last="idx === detail.steps.length - 1"
              :node-title="resolveStepTitle(step)"
              @toggle="toggleStep(step, idx)"
            />
          </div>
          <el-empty v-else description="暂无步骤记录" :image-size="72" />
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import RunStepCard from '../../design/components/RunStepCard.vue'
import { extractRunOutputText } from '../../design/composables/useWorkflowRun'
import { getNodeLabel } from '../../design/nodeMeta'
import {
  computeRunStats,
  formatDurationMs,
  formatRunStatusLabel,
  formatStepIo,
  runStatusTagType,
  traceStepKey
} from '../../design/utils/runTraceUtils'

defineOptions({ name: 'RunDetailDrawer' })

const props = defineProps({
  visible: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  detail: { type: Object, default: null }
})

defineEmits(['update:visible'])

const expandedKeys = ref(new Set())
const allExpanded = ref(false)

const runStats = computed(() =>
  computeRunStats(props.detail?.steps || [], {
    status: props.detail?.status,
    durationMs: props.detail?.durationMs
  })
)

const finalOutputText = computed(() => {
  if (!props.detail) return ''
  return extractRunOutputText(props.detail)
})

watch(
  () => props.detail?.runId,
  () => {
    expandedKeys.value = new Set()
    allExpanded.value = false
    if (props.detail?.steps?.length) {
      expandAllSteps()
    }
  }
)

/**
 * @param {object} step
 * @returns {string}
 */
function resolveStepTitle(step) {
  const typeLabel = getNodeLabel(step?.nodeType) || step?.nodeType || '节点'
  return `${typeLabel} · ${step?.nodeId || ''}`
}

/**
 * @param {object} step
 * @param {number} idx
 */
function isExpanded(step, idx) {
  return expandedKeys.value.has(traceStepKey(step, idx))
}

function toggleStep(step, idx) {
  const key = traceStepKey(step, idx)
  const next = new Set(expandedKeys.value)
  if (next.has(key)) {
    next.delete(key)
  } else {
    next.add(key)
  }
  expandedKeys.value = next
  allExpanded.value = next.size === (props.detail?.steps?.length || 0)
}

function expandAllSteps() {
  const steps = props.detail?.steps || []
  expandedKeys.value = new Set(steps.map((step, idx) => traceStepKey(step, idx)))
  allExpanded.value = true
}

function toggleExpandAll() {
  if (allExpanded.value) {
    expandedKeys.value = new Set()
    allExpanded.value = false
  } else {
    expandAllSteps()
  }
}
</script>

<style scoped lang="scss">
.wf-run-detail {
  padding: 0 4px 16px;
}

.wf-run-detail__summary {
  display: flex;
  flex-wrap: wrap;
  gap: 14px 20px;
  padding: 12px 14px;
  margin-bottom: 14px;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
  border: 1px solid #ebeef5;
  border-radius: 10px;
}

.wf-run-detail__summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;

  strong {
    color: #0a2463;
    font-size: 14px;
  }
}

.wf-run-detail__summary-label {
  color: #909399;
}

.wf-run-detail__meta {
  margin-bottom: 14px;
}

.wf-run-detail__error {
  margin: 0 0 14px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef0f0;
  color: #f56c6c;
  font-size: 13px;
  line-height: 1.5;
}

.wf-run-detail__block {
  margin-bottom: 16px;
}

.wf-run-detail__block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.wf-run-detail__block-title {
  font-size: 14px;
  font-weight: 600;
  color: #0a2463;
}

.wf-run-detail__pre {
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 200px;
  overflow: auto;
  font-family: Consolas, Monaco, 'Courier New', monospace;

  &--output {
    max-height: 280px;
    background: #fff;
    border: 1px solid #ebeef5;
  }
}

.wf-run-detail__timeline {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>

<style lang="scss">
.wf-run-detail-drawer .el-drawer__body {
  padding-top: 8px;
}
</style>
