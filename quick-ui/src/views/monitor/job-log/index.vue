<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="jobLogId"
      export-default-file-name="job-log-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      export-biz-type="monitor:jobLog"
      :export-query-normalizer="normalizeParams"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="true"
      :show-export-button="true"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left="{ refreshData }">
        <el-button type="warning" plain v-hasPermi="['monitor:job:remove']" @click="handleClean(refreshData)">
          清空
        </el-button>
        <el-button plain @click="goJob">返回任务</el-button>
      </template>
      <template #status="{ row }">
        <c7-dict-tag :model-value="row.status" :options="sys_job_log_status" />
      </template>
      <template #actions="scope">
        <el-button link type="primary" v-hasPermi="['monitor:job:query']" @click="openDetail(scope.row)">
          详情
        </el-button>
      </template>
    </C7JsonTable>
    <el-dialog v-model="detailVisible" title="调度日志详情" width="720px" destroy-on-close>
      <el-descriptions v-if="detailRow" :column="1" border size="small">
        <el-descriptions-item label="日志编号">{{ detailRow.jobLogId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detailRow.jobName }}</el-descriptions-item>
        <el-descriptions-item label="任务组名">{{ detailRow.jobGroup }}</el-descriptions-item>
        <el-descriptions-item label="调用目标">{{ detailRow.invokeTarget }}</el-descriptions-item>
        <el-descriptions-item label="日志信息">{{ detailRow.jobMessage || '—' }}</el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <c7-dict-tag :model-value="detailRow.status" :options="sys_job_log_status" />
        </el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ detailRow.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.exceptionInfo" label="异常信息">
          <pre class="job-log-pre">{{ detailRow.exceptionInfo }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { cleanJobLog, getJobLog, listJobLog, removeJobLog } from '@/api/monitor/jobLog'

/**
 * 调度日志：查询、详情、删除、清空、导出；支持从任务页带入筛选。
 */
defineOptions({ name: 'SysJobLog' })

const route = useRoute()
const router = useRouter()
const tableRef = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

const { sys_job_group, sys_job_log_status } = useDict('sys_job_group', 'sys_job_log_status')

const defaultSearchParam = ref({
  jobName: '',
  jobGroup: '',
  status: '',
  timeRange: [],
})

onMounted(() => {
  if (route.query.jobName) defaultSearchParam.value.jobName = String(route.query.jobName)
  if (route.query.jobGroup) defaultSearchParam.value.jobGroup = String(route.query.jobGroup)
})

const searchColumns = computed(() => [
  { prop: 'jobName', label: '任务名称', type: 'input', span: 8, props: { clearable: true } },
  {
    prop: 'jobGroup',
    label: '任务组名',
    type: 'select',
    span: 8,
    options: sys_job_group.value,
    props: { clearable: true, style: 'width: 240px' },
  },
  {
    prop: 'status',
    label: '执行状态',
    type: 'select',
    span: 8,
    options: sys_job_log_status.value,
    props: { clearable: true, style: 'width: 240px' },
  },
  {
    prop: 'timeRange',
    label: '执行时间',
    type: 'daterange',
    span: 16,
    props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' },
  },
])

const tableColumns = computed(() => [
  { prop: 'jobLogId', label: '日志编号', width: 100 },
  { prop: 'jobName', label: '任务名称', minWidth: 120, showOverflowTooltip: true },
  {
    prop: 'jobGroup',
    label: '任务组名',
    width: 100,
    columnType: 'tag',
    dictList: sys_job_group.value,
  },
  { prop: 'invokeTarget', label: '调用目标', minWidth: 120, showOverflowTooltip: true },
  { prop: 'jobMessage', label: '日志信息', minWidth: 120, showOverflowTooltip: true },
  { prop: 'status', label: '执行状态', columnType: 'slot', slotName: 'status', width: 100 },
  { prop: 'createTime', label: '执行时间', width: 180 },
  { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 90, fixed: 'right' },
])

function normalizeParams(raw) {
  const p = { ...raw }
  const range = p.timeRange
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p.beginTime = range[0]
    p.endTime = range[1]
  }
  delete p.timeRange
  if (p.status === '') delete p.status
  if (p.jobGroup === '') delete p.jobGroup
  if (p.jobName === '') delete p.jobName
  return p
}

function listFunction(params) {
  return listJobLog(normalizeParams(params))
}

function batchDeleteFunction(ids) {
  return removeJobLog(ids || [])
}

function handleClean(refreshData) {
  ElMessageBox.confirm('确认清空全部调度日志？', '提示', { type: 'warning' })
    .then(() => cleanJobLog())
    .then(() => {
      ElMessage.success('已清空')
      refreshData?.()
    })
    .catch(() => {})
}

async function openDetail(row) {
  const res = await getJobLog(row.jobLogId)
  detailRow.value = res.data || res
  detailVisible.value = true
}

function goJob() {
  router.push({ name: 'SysJob' })
}
</script>

<style scoped>
.job-log-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}
</style>
