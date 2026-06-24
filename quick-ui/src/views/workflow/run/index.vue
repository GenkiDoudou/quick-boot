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
        <el-tag :type="runStatusTagType(row.status)">{{ formatRunStatusLabel(row.status) }}</el-tag>
      </template>

      <template #durationMs="{ row }">
        <span>{{ formatDurationMs(row.durationMs) }}</span>
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="openDetail(row)" v-hasPermi="['workflow:query']">详情</el-button>
      </template>
    </C7JsonTable>

    <RunDetailDrawer
      v-model:visible="drawerVisible"
      :loading="detailLoading"
      :detail="detail"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { getRunInfo, listRuns } from '@/api/workflow'
import RunDetailDrawer from './components/RunDetailDrawer.vue'
import { formatDurationMs, formatRunStatusLabel, runStatusTagType } from '../design/utils/runTraceUtils'

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
