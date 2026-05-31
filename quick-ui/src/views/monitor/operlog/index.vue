<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="operId"
      export-default-file-name="operlog-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :export-function="exportFunction"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="true"
      :show-export-button="true"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left="{ refreshData }">
        <el-button
          type="warning"
          plain
          v-hasPermi="['monitor:operlog:remove']"
          @click="handleClean(refreshData)"
        >
          清空
        </el-button>
      </template>
      <template #status="{ row }">
        <c7-dict-tag :model-value="row.status" :options="sys_oper_status" />
      </template>
      <template #businessType="{ row }">
        <c7-dict-tag :model-value="String(row.businessType)" :options="sys_oper_business_type" />
      </template>
      <template #operatorType="{ row }">
        <c7-dict-tag :model-value="String(row.operatorType)" :options="sys_oper_operator_type" />
      </template>
      <template #operTime="{ row }">
        <span class="operlog-cell-text">{{ formatOperTimeCell(row.operTime) }}</span>
      </template>
      <template #actions="scope">
        <el-button link type="primary" v-hasPermi="['monitor:operlog:query']" @click="openDetail(scope.row)">
          详情
        </el-button>
      </template>
    </C7JsonTable>
    <el-dialog v-model="detailVisible" title="操作日志详情" width="880px" destroy-on-close>
      <OperLogDetailPanel :row="detailRow" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { cleanOperlog, exportOperlog, getOperlog, listOperlog, removeOperlog } from '@/api/monitor/operlog'
import OperLogDetailPanel from './OperLogDetailPanel.vue'
import { formatOperTime } from './operLogFormat'

/**
 * 操作日志：查询、导出、批量删除、清空、详情。
 */
defineOptions({ name: 'SysOperLog' })

const tableRef = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)
const { sys_oper_status, sys_oper_business_type, sys_oper_operator_type } = useDict(
  'sys_oper_status',
  'sys_oper_business_type',
  'sys_oper_operator_type',
)

const defaultSearchParam = {
  operUrl: '',
  title: '',
  operName: '',
  businessType: '',
  status: '',
  traceId: '',
  clientOperationId: '',
  clientId: '',
  operTimeRange: [],
}

const searchColumns = computed(() => [
  { prop: 'operUrl', label: '操作地址', type: 'input', span: 8, props: { placeholder: 'URI 片段', clearable: true } },
  { prop: 'title', label: '系统模块', type: 'input', span: 8, props: { placeholder: '模块/标题', clearable: true } },
  { prop: 'operName', label: '操作人员', type: 'input', span: 8, props: { placeholder: '用户名', clearable: true } },
  {
    prop: 'businessType',
    label: '业务类型',
    type: 'select',
    span: 8,
    options: sys_oper_business_type.value,
    props: { placeholder: '业务类型', clearable: true, style: 'width: 240px' },
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: sys_oper_status.value,
    props: { placeholder: '状态', clearable: true, style: 'width: 240px' },
  },
  { prop: 'traceId', label: '链路ID', type: 'input', span: 8, props: { placeholder: 'traceId 精确匹配', clearable: true } },
  { prop: 'clientOperationId', label: '操作ID', type: 'input', span: 8, props: { placeholder: 'operationId 精确匹配', clearable: true } },
  { prop: 'clientId', label: '客户端ID', type: 'input', span: 8, props: { placeholder: 'clientId 精确匹配', clearable: true } },
  {
    prop: 'operTimeRange',
    label: '操作时间',
    type: 'daterange',
    span: 16,
    props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' },
  },
])

const tableColumns = computed(() => [
  { prop: 'operId', label: '日志编号', width: 120 },
  { prop: 'title', label: '系统模块', minWidth: 140, showOverflowTooltip: true },
  { prop: 'businessType', label: '业务类型', columnType: 'slot', slotName: 'businessType', width: 110 },
  { prop: 'method', label: '操作方法', minWidth: 160, showOverflowTooltip: true },
  { prop: 'operName', label: '操作人员', width: 120, sortable: 'custom' },
  { prop: 'operIp', label: 'IP', width: 130, showOverflowTooltip: true },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 90 },
  { prop: 'operTime', label: '操作日期', columnType: 'slot', slotName: 'operTime', width: 180, sortable: 'custom' },
  { prop: 'costTime', label: '耗时(ms)', width: 110, sortable: 'custom' },
  { prop: 'traceId', label: '链路ID', minWidth: 140, showOverflowTooltip: true },
  { prop: 'clientOperationId', label: '操作ID', minWidth: 140, showOverflowTooltip: true },
  { prop: 'clientId', label: '客户端ID', minWidth: 120, showOverflowTooltip: true },
  { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 100, fixed: 'right' },
])

/**
 * @param {string|number|Date|null|undefined} value
 * @returns {string}
 */
function formatOperTimeCell(value) {
  return formatOperTime(value)
}

function normalizeListParams(raw) {
  const p = { ...raw }
  const range = p.operTimeRange
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p.beginTime = range[0]
    p.endTime = range[1]
  }
  delete p.operTimeRange
  if (p.status === '' || p.status === null || p.status === undefined) delete p.status
  if (p.businessType === '' || p.businessType === null || p.businessType === undefined) delete p.businessType
  if (p.operUrl === '') delete p.operUrl
  if (p.title === '') delete p.title
  if (p.operName === '') delete p.operName
  if (p.traceId === '') delete p.traceId
  if (p.clientOperationId === '') delete p.clientOperationId
  if (p.clientId === '') delete p.clientId
  return p
}

function listFunction(params) {
  return listOperlog(normalizeListParams(params))
}

function batchDeleteFunction(ids) {
  return removeOperlog(ids || [])
}

/**
 * 导出由 C7ExcelDownload 根据返回值触发下载，须返回 Blob 或 { data, headers }。
 */
function exportFunction(searchParam) {
  const req = normalizeListParams({ ...searchParam })
  delete req.pageNum
  delete req.pageSize
  return exportOperlog(req)
}

function handleClean(refreshData) {
  ElMessageBox.confirm('确认清空全部操作日志？', '提示', { type: 'warning' })
    .then(() => cleanOperlog())
    .then(() => {
      ElMessage.success('已清空')
      refreshData?.()
    })
    .catch(() => {})
}

async function openDetail(row) {
  if (!row?.operId) {
    return
  }
  const res = await getOperlog(row.operId)
  detailRow.value = res?.data ?? res
  detailVisible.value = true
}
</script>

<style scoped>
.operlog-cell-text {
  -webkit-user-select: text;
  user-select: text;
}
</style>
