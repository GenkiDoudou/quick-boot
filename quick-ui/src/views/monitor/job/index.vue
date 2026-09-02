<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="jobId"
      export-default-file-name="job-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :export-function="exportJob"
      :export-button-permi="['monitor:job:export']"
      :show-add-button="true"
      :show-edit-button="true"
      :show-delete-button="true"
      :show-export-button="true"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
      :on-add="openAdd"
      :on-edit="onToolbarEdit"
    >
      <template #toolbar-left>
        <el-button type="primary" plain v-hasPermi="['monitor:job:query']" @click="goJobLog()">调度日志</el-button>
      </template>
      <template #status="{ row }">
        <el-switch
          v-model="row.status"
          active-value="0"
          inactive-value="1"
          v-hasPermi="['monitor:job:changeStatus']"
          :before-change="() => beforeStatusChange(row)"
        />
      </template>
      <template #actions="scope">
        <el-button link type="primary" v-hasPermi="['monitor:job:query']" @click="openForm(scope.row.jobId, true)">
          详情
        </el-button>
        <el-button link type="primary" v-hasPermi="['monitor:job:edit']" @click="openForm(scope.row.jobId)">修改</el-button>
        <el-button link type="primary" v-hasPermi="['monitor:job:changeStatus']" @click="handleRun(scope.row)">
          执行
        </el-button>
        <el-button link type="primary" v-hasPermi="['monitor:job:query']" @click="goJobLog(scope.row)">日志</el-button>
      </template>
    </C7JsonTable>
    <JobFormDialog v-model="formOpen" :job-id="editJobId" :read-only="formReadOnly" @success="refresh" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  changeJobStatus,
  exportJob,
  listJob,
  removeJob,
  runJob,
} from '@/api/monitor/job'
import JobFormDialog from '@/components/monitor/JobFormDialog.vue'

/**
 * 定时任务：CRUD、启停、立即执行、跳转调度日志。
 */
defineOptions({ name: 'SysJob' })

const router = useRouter()
const tableRef = ref(null)
const formOpen = ref(false)
const editJobId = ref(null)
const formReadOnly = ref(false)

const { sys_job_group, sys_job_status, sys_job_concurrent, sys_job_type } = useDict(
  'sys_job_group',
  'sys_job_status',
  'sys_job_concurrent',
  'sys_job_type'
)

const defaultSearchParam = { jobName: '', jobGroup: '', status: '' }

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
    label: '状态',
    type: 'select',
    span: 8,
    options: sys_job_status.value,
    props: { clearable: true, style: 'width: 240px' },
  },
])

const tableColumns = computed(() => [
  { prop: 'jobId', label: '任务编号', width: 100 },
  { prop: 'jobName', label: '任务名称', minWidth: 120, showOverflowTooltip: true },
  {
    prop: 'jobGroup',
    label: '任务组名',
    width: 100,
    columnType: 'tag',
    dictList: sys_job_group.value,
  },
  {
    prop: 'jobType',
    label: '任务类型',
    width: 100,
    columnType: 'tag',
    dictList: sys_job_type.value,
  },
  { prop: 'invokeTarget', label: '调用目标', minWidth: 120, showOverflowTooltip: true },
  { prop: 'cronExpression', label: 'Cron', width: 140, showOverflowTooltip: true },
  {
    prop: 'concurrent',
    label: '并发',
    width: 80,
    columnType: 'tag',
    dictList: sys_job_concurrent.value,
  },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 90 },
  { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 220, fixed: 'right' },
])

function listFunction(pageReq) {
  const raw = pageReq && typeof pageReq === 'object' ? pageReq : {}
  const nested =
    raw.param && typeof raw.param === 'object' && !Array.isArray(raw.param)
      ? { ...raw.param }
      : { ...raw }
  delete nested.current
  delete nested.size
  delete nested.param
  const p = {
    ...nested,
    pageNum: raw.current ?? raw.pageNum ?? 1,
    pageSize: raw.size ?? raw.pageSize ?? 10
  }
  if (p.status === '' || p.status == null) delete p.status
  if (p.jobGroup === '') delete p.jobGroup
  if (p.jobName === '') delete p.jobName
  return listJob(p)
}

function batchDeleteFunction(ids) {
  return removeJob(ids || [])
}

function refresh() {
  tableRef.value?.refreshData?.()
}

/** 打开新增弹窗（C7JsonTable 内置「新增」回调） */
function openAdd() {
  openForm(null)
}

function openForm(jobId, readOnly = false) {
  editJobId.value = jobId
  formReadOnly.value = readOnly
  formOpen.value = true
}

function onToolbarEdit(selectedRows) {
  const row = selectedRows?.[0]
  if (!row?.jobId) {
    ElMessage.warning('请选择一条任务')
    return
  }
  openForm(row.jobId)
}

/**
 * 阻止表格初次渲染时 el-switch 误触发 change（会 POST changeStatus 且 jobId 为空）。
 * @param {object} row 行数据
 * @returns {boolean}
 */
function beforeStatusChange(row) {
  if (row?.jobId == null || row?.jobId === '') {
    return false
  }
  const newStatus = row.status === '0' ? '1' : '0'
  const prev = row.status
  changeJobStatus({ jobId: row.jobId, status: newStatus })
    .then(() => {
      row.status = newStatus
      ElMessage.success(newStatus === '0' ? '已启用' : '已暂停')
    })
    .catch(() => {
      row.status = prev
    })
  return false
}

function handleRun(row) {
  ElMessageBox.confirm(`确认立即执行任务「${row.jobName}」？`, '提示', { type: 'warning' })
    .then(() => runJob({ jobId: row.jobId }))
    .then((res) => {
      ElMessage.success(res.msg || '执行成功')
    })
    .catch(() => {})
}

function goJobLog(row) {
  const query = {}
  if (row?.jobName) query.jobName = row.jobName
  if (row?.jobGroup) query.jobGroup = row.jobGroup
  router.push({ name: 'SysJobLog', query })
}
</script>
