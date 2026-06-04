<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" class="io-center-tabs">
      <el-tab-pane label="导入任务" name="import">
        <C7JsonTable
          ref="importTableRef"
          row-key="taskId"
          :show-index="false"
          :show-selection="false"
          :list-function="listImportFunction"
          :table-columns="importTableColumns"
          :search-columns="importSearchColumns"
          :default-search-param="defaultImportSearch"
          :show-add-button="false"
          :show-edit-button="false"
          :show-delete-button="false"
          :show-export-button="false"
          rows-key="data.records"
          total-key="data.total"
        >
          <template #status="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
          <template #action="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.failRows > 0 && row.errorFileId"
              link
              type="danger"
              @click="downloadError(row.errorFileId)"
            >失败明细</el-button>
            <span v-else-if="row.status === 'RUNNING' || row.status === 'PENDING'" class="io-center-action-hint">处理中</span>
          </template>
        </C7JsonTable>
      </el-tab-pane>
      <el-tab-pane label="导出记录" name="export">
        <C7JsonTable
          ref="exportTableRef"
          row-key="taskId"
          :show-index="false"
          :show-selection="false"
          :list-function="listExportFunction"
          :table-columns="exportTableColumns"
          :search-columns="exportSearchColumns"
          :default-search-param="defaultExportSearch"
          :show-add-button="false"
          :show-edit-button="false"
          :show-delete-button="false"
          :show-export-button="false"
          rows-key="data.records"
          total-key="data.total"
        >
          <template #status="{ row }">
            <el-tag :type="exportStatusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
          <template #action="{ row }">
            <el-button
              v-if="row.status === 'SUCCESS' && row.resultFileId"
              link
              type="primary"
              @click="downloadExportResult(row)"
            >下载</el-button>
            <span v-else-if="row.status === 'RUNNING' || row.status === 'PENDING'" class="io-center-action-hint">处理中</span>
            <el-tooltip v-else-if="row.status === 'FAILED' && row.errorMessage" :content="row.errorMessage">
              <span class="io-center-action-hint io-center-action-hint--danger">失败</span>
            </el-tooltip>
            <span v-else class="io-center-action-hint">—</span>
          </template>
        </C7JsonTable>
      </el-tab-pane>
    </el-tabs>

    <c7-dialog v-model="detailVisible" title="导入任务详情" :show-confirm="false" width="520px" @closed="stopPoll">
      <div v-if="detail" class="import-detail">
        <p><span class="label">任务ID：</span>{{ detail.taskId }}</p>
        <p><span class="label">文件名：</span>{{ detail.fileName || '—' }}</p>
        <p><span class="label">业务：</span>{{ BIZ_TYPE_LABELS[detail.bizType] || detail.bizType }}</p>
        <p><span class="label">模式：</span>{{ detail.importMode }}</p>
        <p><span class="label">状态：</span>{{ detail.status }}</p>
        <p v-if="detail.status === 'RUNNING' || detail.status === 'PENDING'">
          <span class="label">进度：</span>{{ detail.processedRows }} / {{ detail.totalRows }}
        </p>
        <p><span class="label">成功：</span>{{ detail.successRows }}，<span class="label">失败：</span>{{ detail.failRows }}</p>
        <p v-if="detail.errorMessage"><span class="label">系统错误：</span>{{ detail.errorMessage }}</p>
      </div>
    </c7-dialog>
  </div>
</template>

<script setup>
import { onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadImportErrorFile, getImportTask, listImportTasks } from '@/api/import/task'
import { listExportTasks } from '@/api/export/task'
import { saveAs } from 'file-saver'
import { downloadFile } from '@/api/system/file'

const activeTab = ref('import')
const importTableRef = ref(null)
const exportTableRef = ref(null)
const detailVisible = ref(false)
const detail = ref(null)
let pollTimer = null

const defaultImportSearch = { pageNum: 1, pageSize: 10 }

const importSearchColumns = [
  { prop: 'bizType', label: '业务编码', type: 'input' },
  { prop: 'status', label: '状态', type: 'input' }
]

const BIZ_TYPE_LABELS = {
  'system:user': '用户',
  'system:role': '角色',
  'system:dict:type': '字典类型',
  'system:dict:data': '字典数据',
  'monitor:logininfor': '登录日志',
  'monitor:operlog': '操作日志',
}

const importTableColumns = [
  { prop: 'taskId', label: '任务ID', minWidth: 150, showOverflowTooltip: true },
  { prop: 'fileName', label: '文件名', minWidth: 160, showOverflowTooltip: true, formatter: (row) => row.fileName || '—' },
  { prop: 'bizType', label: '业务', minWidth: 120, showOverflowTooltip: true, formatter: (row) => BIZ_TYPE_LABELS[row.bizType] || row.bizType },
  { prop: 'importMode', label: '模式', width: 72, align: 'center' },
  { prop: 'status', label: '状态', width: 96, align: 'center', columnType: 'slot', slotName: 'status' },
  { prop: 'totalRows', label: '总数', width: 72, align: 'center' },
  { prop: 'successRows', label: '成功', width: 72, align: 'center' },
  { prop: 'failRows', label: '失败', width: 72, align: 'center' },
  { prop: 'createTime', label: '创建时间', width: 168 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 140, fixed: 'right', align: 'center' },
]

function listImportFunction(params) {
  return listImportTasks(params)
}

const defaultExportSearch = { pageNum: 1, pageSize: 10 }

const exportSearchColumns = [
  { prop: 'bizType', label: '业务编码', type: 'input' },
  { prop: 'status', label: '状态', type: 'input' },
]

const exportTableColumns = [
  { prop: 'taskId', label: '任务ID', minWidth: 150, showOverflowTooltip: true },
  { prop: 'fileName', label: '文件名', minWidth: 160, showOverflowTooltip: true, formatter: (row) => row.fileName || '—' },
  { prop: 'bizType', label: '业务', minWidth: 120, showOverflowTooltip: true, formatter: (row) => BIZ_TYPE_LABELS[row.bizType] || row.bizType },
  { prop: 'exportMode', label: '模式', width: 72, align: 'center' },
  { prop: 'status', label: '状态', width: 96, align: 'center', columnType: 'slot', slotName: 'status' },
  { prop: 'totalRows', label: '行数', width: 72, align: 'center' },
  { prop: 'createTime', label: '创建时间', width: 168 },
  { prop: 'finishTime', label: '完成时间', width: 168 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 120, fixed: 'right', align: 'center' },
]

function listExportFunction(params) {
  return listExportTasks(params)
}

function exportStatusTagType(status) {
  return statusTagType(status)
}

/**
 * 下载导出结果文件。
 * @param {object} row 导出行
 */
function downloadExportResult(row) {
  const fileId = row?.resultFileId
  if (fileId == null) {
    ElMessage.warning('暂无可下载文件')
    return
  }
  const name = row.fileName || (BIZ_TYPE_LABELS[row.bizType] || row.bizType || 'export').replace(/:/g, '-') + `-${fileId}.xlsx`
  downloadFile(fileId).then(({ data }) => {
    saveAs(data, name)
  }).catch(() => ElMessage.error('下载失败'))
}

function statusTagType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'RUNNING') return 'warning'
  return 'info'
}

function openDetail(row) {
  detail.value = { ...row }
  detailVisible.value = true
  startPoll(row.taskId)
}

function startPoll(taskId) {
  stopPoll()
  const tick = () => {
    getImportTask(taskId).then((res) => {
      const d = res?.data ?? res
      detail.value = d
      if (d?.status === 'SUCCESS' || d?.status === 'FAILED') {
        stopPoll()
        importTableRef.value?.refreshData()
      }
    }).catch(() => stopPoll())
  }
  tick()
  pollTimer = setInterval(tick, 3000)
}

function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function downloadError(fileId) {
  const id = fileId != null ? String(fileId) : ''
  downloadImportErrorFile(id).then(({ data }) => {
    saveAs(data, `import-error-${id}.xlsx`)
  }).catch(() => ElMessage.error('下载失败'))
}

onUnmounted(() => stopPoll())
</script>

<style scoped>
.io-center-tabs { margin-top: 4px; }
.import-detail p { margin: 8px 0; font-size: 14px; color: #303133; }
.import-detail .label { color: #606266; }
.io-center-action-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.io-center-action-hint--danger {
  color: var(--el-color-danger);
  cursor: help;
}
</style>
