<template>
  <div class="app-container wf-run-page">
    <C7JsonTable
      ref="tableRef"
      row-key="runId"
      :show-index="false"
      :show-selection="false"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="false"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #status="{ row }">
        <el-tag :type="runStatusTag(row.status)">{{ runStatusLabel(row.status) }}</el-tag>
      </template>

      <template #durationMs="{ row }">
        <span>{{ formatDuration(row.durationMs) }}</span>
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="openDetail(row)" v-hasPermi="['workflow:query']">详情</el-button>
      </template>
    </C7JsonTable>

    <el-drawer v-model="drawerVisible" title="运行详情" size="520px" destroy-on-close>
      <div v-loading="detailLoading" class="wf-run-detail">
        <el-descriptions v-if="detail" :column="1" border size="small" class="wf-run-detail__meta">
          <el-descriptions-item label="运行 ID">{{ detail.runId }}</el-descriptions-item>
          <el-descriptions-item label="工作流 ID">{{ detail.workflowId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="runStatusTag(detail.status)">{{ runStatusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">{{ formatDuration(detail.durationMs) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detail.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ detail.endTime || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.errorMsg" label="失败原因">
            <span class="wf-run-detail__error">{{ detail.errorMsg }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="wf-run-detail__section">
          <div class="wf-run-detail__section-title">步骤 Trace</div>
          <el-timeline v-if="detail?.steps?.length">
            <el-timeline-item
              v-for="step in detail.steps"
              :key="step.stepId || step.orderNo"
              :type="stepTimelineType(step.status)"
              :timestamp="`#${step.orderNo ?? '-'} · ${step.durationMs ?? 0} ms`"
            >
              <div class="wf-run-step">
                <div class="wf-run-step__head">
                  <strong>{{ step.nodeId }}</strong>
                  <el-tag size="small" effect="plain">{{ step.nodeType }}</el-tag>
                  <el-tag size="small" :type="runStatusTag(step.status)">{{ step.status }}</el-tag>
                </div>
                <div v-if="step.errorMsg" class="wf-run-step__error">{{ step.errorMsg }}</div>
                <el-collapse v-if="step.outputs && Object.keys(step.outputs).length" class="wf-run-step__collapse">
                  <el-collapse-item title="输出摘要" name="outputs">
                    <pre class="wf-run-step__json">{{ formatJson(step.outputs) }}</pre>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无步骤记录" :image-size="80" />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { getRunInfo, listRuns } from '@/api/workflow'

defineOptions({ name: 'WfRunList' })

const tableRef = ref(null)
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const defaultSearchParam = {
  workflowId: '',
  status: ''
}

const searchColumns = computed(() => [
  { prop: 'workflowId', label: '工作流 ID', type: 'input', span: 8, props: { placeholder: '工作流 ID', clearable: true } },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: [
      { label: '排队中', value: 'QUEUED' },
      { label: '运行中', value: 'RUNNING' },
      { label: '成功', value: 'SUCCESS' },
      { label: '失败', value: 'FAILED' }
    ],
    props: { placeholder: '运行状态', clearable: true, style: 'width: 240px' }
  }
])

const tableColumns = computed(() => [
  { prop: 'runId', label: '运行 ID', width: 100 },
  { prop: 'workflowId', label: '工作流 ID', width: 110 },
  { prop: 'runMode', label: '模式', width: 90 },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 100 },
  { prop: 'durationMs', label: '耗时', columnType: 'slot', slotName: 'durationMs', width: 100 },
  { prop: 'startTime', label: '开始时间', width: 180 },
  { prop: 'createBy', label: '操作人', width: 120 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 90, fixed: 'right' }
])

function listFunction(params) {
  const req = { ...params }
  if (req.workflowId === '') delete req.workflowId
  return listRuns(req)
}

/** @param {string} status */
function runStatusLabel(status) {
  const map = {
    QUEUED: '排队中',
    RUNNING: '运行中',
    SUCCESS: '成功',
    FAILED: '失败',
    SKIPPED: '跳过'
  }
  return map[status] || status || '-'
}

/** @param {string} status */
function runStatusTag(status) {
  const map = {
    QUEUED: 'info',
    RUNNING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    SKIPPED: 'info'
  }
  return map[status] || 'info'
}

/** @param {string} status */
function stepTimelineType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  return 'primary'
}

/** @param {number|null|undefined} ms */
function formatDuration(ms) {
  if (ms == null) return '-'
  if (ms < 1000) return `${ms} ms`
  return `${(ms / 1000).toFixed(2)} s`
}

/** @param {unknown} obj */
function formatJson(obj) {
  try {
    return JSON.stringify(obj, null, 2)
  } catch {
    return String(obj)
  }
}

/** @param {{ runId: number|string }} row */
function openDetail(row) {
  drawerVisible.value = true
  detail.value = null
  detailLoading.value = true
  getRunInfo(row.runId)
    .then((res) => {
      detail.value = res.data
    })
    .finally(() => {
      detailLoading.value = false
    })
}
</script>

<style scoped lang="scss">
.wf-run-detail__meta {
  margin-bottom: 20px;
}

.wf-run-detail__section-title {
  font-size: 14px;
  font-weight: 600;
  color: #0a2463;
  margin-bottom: 12px;
}

.wf-run-detail__error,
.wf-run-step__error {
  color: #f56c6c;
  font-size: 13px;
}

.wf-run-step__head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.wf-run-step__json {
  margin: 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  overflow: auto;
  max-height: 200px;
}

.wf-run-step__collapse {
  margin-top: 6px;
}
</style>
