<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="slowId"
      export-default-file-name="slowsql-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      export-biz-type="monitor:slowSql"
      :export-query-normalizer="normalizeListParams"
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
          v-hasPermi="['monitor:slowSql:remove']"
          @click="handleClean(refreshData)"
        >
          清空
        </el-button>
      </template>
      <template #sqlSource="{ row }">
        <el-tag :type="sourceTagType(row.sqlSource)" size="small">{{ row.sqlSource || '—' }}</el-tag>
      </template>
      <template #sqlType="{ row }">
        <el-tag :type="sqlTypeTagType(row.sqlType)" size="small" effect="plain">{{ row.sqlType || 'OTHER' }}</el-tag>
      </template>
      <template #sqlText="{ row }">
        <span class="slowsql-sql-cell" :title="row.sqlText">{{ sqlPreview(row.sqlText) }}</span>
      </template>
      <template #createTime="{ row }">
        <span class="slowsql-cell-text">{{ formatTime(row.createTime) }}</span>
      </template>
      <template #actions="scope">
        <el-button link type="primary" v-hasPermi="['monitor:slowSql:query']" @click="openDetail(scope.row)">
          详情
        </el-button>
      </template>
    </C7JsonTable>
    <el-dialog v-model="detailVisible" title="慢 SQL 详情" width="920px" destroy-on-close>
      <el-descriptions v-if="detailRow" :column="1" border>
        <el-descriptions-item label="来源">{{ detailRow.sqlSource }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detailRow.sqlType || 'OTHER' }}</el-descriptions-item>
        <el-descriptions-item label="traceId">{{ detailRow.traceId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Mapper">{{ detailRow.mapperId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ detailRow.costTime }}</el-descriptions-item>
        <el-descriptions-item label="请求">{{ detailRow.requestMethod }} {{ detailRow.requestUri || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作ID">{{ detailRow.clientOperationId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{ formatTime(detailRow.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="SQL">
          <pre class="slowsql-sql-pre">{{ detailRow.sqlText }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cleanSlowSql, getSlowSql, listSlowSql, removeSlowSql } from '@/api/monitor/slowSql'
import { parseTime } from '@/utils/ruoyi'

defineOptions({ name: 'SysSlowSql' })

const SQL_SOURCE_OPTIONS = [
  { label: '业务', value: 'BUSINESS' },
  { label: '积木', value: 'JIMU' },
  { label: '系统', value: 'SYSTEM' },
]

/** 与后端 SlowSqlType 对齐，列表筛选常用 DML */
const SQL_TYPE_OPTIONS = [
  { label: 'SELECT', value: 'SELECT' },
  { label: 'INSERT', value: 'INSERT' },
  { label: 'UPDATE', value: 'UPDATE' },
  { label: 'DELETE', value: 'DELETE' },
  { label: 'MERGE', value: 'MERGE' },
  { label: 'EXEC', value: 'EXEC' },
  { label: 'CREATE', value: 'CREATE' },
  { label: 'ALTER', value: 'ALTER' },
  { label: 'DROP', value: 'DROP' },
  { label: 'TRUNCATE', value: 'TRUNCATE' },
  { label: 'OTHER', value: 'OTHER' },
]

const tableRef = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

const defaultSearchParam = {
  sqlSource: '',
  sqlType: '',
  mapperId: '',
  sqlText: '',
  requestUri: '',
  traceId: '',
  minCostTime: '',
  createTimeRange: [],
}

const searchColumns = computed(() => [
  {
    prop: 'sqlSource',
    label: '来源',
    type: 'select',
    span: 8,
    options: SQL_SOURCE_OPTIONS,
    props: { placeholder: '来源', clearable: true, style: 'width: 240px' },
  },
  {
    prop: 'sqlType',
    label: '操作类型',
    type: 'select',
    span: 8,
    options: SQL_TYPE_OPTIONS,
    props: { placeholder: '操作类型', clearable: true, style: 'width: 240px' },
  },
  { prop: 'traceId', label: '链路ID', type: 'input', span: 8, props: { placeholder: 'traceId', clearable: true } },
  { prop: 'mapperId', label: 'Mapper', type: 'input', span: 8, props: { placeholder: 'Mapper 片段', clearable: true } },
  { prop: 'sqlText', label: 'SQL', type: 'input', span: 8, props: { placeholder: 'SQL 片段', clearable: true } },
  { prop: 'requestUri', label: '请求URI', type: 'input', span: 8, props: { placeholder: 'URI 片段', clearable: true } },
  { prop: 'minCostTime', label: '最小耗时(ms)', type: 'input', span: 8, props: { clearable: true } },
  {
    prop: 'createTimeRange',
    label: '记录时间',
    type: 'daterange',
    span: 16,
    props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' },
  },
])

const tableColumns = computed(() => [
  { prop: 'slowId', label: '编号', width: 110 },
  { prop: 'sqlSource', label: '来源', columnType: 'slot', slotName: 'sqlSource', width: 90 },
  { prop: 'sqlType', label: '操作类型', columnType: 'slot', slotName: 'sqlType', width: 100 },
  { prop: 'costTime', label: '耗时(ms)', width: 100, sortable: 'custom' },
  { prop: 'sqlText', label: 'SQL', columnType: 'slot', slotName: 'sqlText', minWidth: 280 },
  { prop: 'traceId', label: '链路ID', minWidth: 120, showOverflowTooltip: true },
  { prop: 'mapperId', label: 'Mapper', minWidth: 160, showOverflowTooltip: true },
  { prop: 'requestUri', label: '请求URI', minWidth: 140, showOverflowTooltip: true },
  { prop: 'createTime', label: '记录时间', columnType: 'slot', slotName: 'createTime', width: 170, sortable: 'custom' },
  { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 90, fixed: 'right' },
])

/**
 * @param {string} source
 * @returns {string}
 */
function sourceTagType(source) {
  if (source === 'JIMU') return 'warning'
  if (source === 'SYSTEM') return 'info'
  return 'primary'
}

/**
 * @param {string|undefined|null} sqlType
 * @returns {string}
 */
function sqlTypeTagType(sqlType) {
  switch (sqlType) {
    case 'SELECT':
      return 'success'
    case 'INSERT':
      return ''
    case 'UPDATE':
      return 'warning'
    case 'DELETE':
      return 'danger'
    case 'MERGE':
    case 'EXEC':
      return 'primary'
    default:
      return 'info'
  }
}

function formatTime(value) {
  return parseTime(value) || ''
}

/**
 * 列表 SQL 预览：单行截断，悬停 title 展示完整格式化 SQL。
 * @param {string|undefined|null} text
 * @param {number} [maxLen=160]
 */
function sqlPreview(text, maxLen = 160) {
  if (!text) return '—'
  const oneLine = String(text).replace(/\s+/g, ' ').trim()
  return oneLine.length > maxLen ? `${oneLine.slice(0, maxLen)}…` : oneLine
}

function normalizeListParams(raw) {
  const p = { ...raw }
  const range = p.createTimeRange
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p.beginTime = range[0]
    p.endTime = range[1]
  }
  delete p.createTimeRange
  Object.keys(p).forEach((k) => {
    if (p[k] === '') delete p[k]
  })
  if (p.minCostTime != null && p.minCostTime !== '') {
    p.minCostTime = Number(p.minCostTime)
  }
  return p
}

function listFunction(params) {
  return listSlowSql(normalizeListParams(params))
}

function batchDeleteFunction(ids) {
  return removeSlowSql(ids || [])
}

function handleClean(refreshData) {
  ElMessageBox.confirm('确认清空全部慢 SQL 记录？', '提示', { type: 'warning' })
    .then(() => cleanSlowSql())
    .then(() => {
      ElMessage.success('已清空')
      refreshData?.()
    })
    .catch(() => {})
}

async function openDetail(row) {
  if (!row?.slowId) return
  const res = await getSlowSql(row.slowId)
  detailRow.value = res?.data ?? res
  detailVisible.value = true
}
</script>

<style scoped>
.slowsql-cell-text {
  user-select: text;
}
.slowsql-sql-cell {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 12px;
  color: var(--el-text-color-regular);
  user-select: text;
}
.slowsql-sql-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  max-height: 480px;
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}
</style>
