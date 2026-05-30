<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="batchId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="true"
      :delete-button-permi="['monitor:clientTrack:remove']"
      :show-export-button="false"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #reason="{ row }">
        <el-tag :type="reasonTagType(row.reason)" size="small">{{ reasonLabel(row.reason) }}</el-tag>
      </template>
      <template #createTime="{ row }">
        <span class="client-track-cell-text">{{ formatTime(row.createTime) }}</span>
      </template>
      <template #actions="scope">
        <el-button link type="primary" v-hasPermi="['monitor:clientTrack:list']" @click="openDetail(scope.row)">
          详情
        </el-button>
      </template>
    </C7JsonTable>

    <el-dialog v-model="detailVisible" title="前端监控批次详情" width="920px" destroy-on-close>
      <el-descriptions v-if="detailRow" :column="2" border size="small" class="client-track-detail">
        <el-descriptions-item label="批次编号">{{ detailRow.batchId }}</el-descriptions-item>
        <el-descriptions-item label="operationId">{{ detailRow.operationId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="serverTraceId（oper_log）">{{ detailRow.traceId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailRow.userName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="上报原因">
          <el-tag :type="reasonTagType(detailRow.reason)" size="small">{{ reasonLabel(detailRow.reason) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="页面路径" :span="2">{{ detailRow.pagePath || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户端 IP">{{ detailRow.clientIp || '—' }}</el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ formatTime(detailRow.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="事件链路" :span="2">
          <div class="client-track-events">
            <div v-for="(ev, idx) in parsedEvents" :key="idx" class="client-track-event-row">
              <span class="client-track-event-type">{{ ev.type }}</span>
              <span v-if="ev.type === 'click' && ev.target" class="client-track-event-meta">按钮: {{ ev.target }}</span>
              <span v-if="ev.trigger" class="client-track-event-meta">trigger: {{ ev.trigger }}</span>
              <span v-if="ev.operationId" class="client-track-event-meta">op: {{ ev.operationId }}</span>
              <span v-if="ev.clientTraceId" class="client-track-event-meta">client: {{ ev.clientTraceId }}</span>
              <span v-if="ev.responseTraceId" class="client-track-event-meta">response: {{ ev.responseTraceId }}</span>
              <span v-if="ev.serverTraceId" class="client-track-event-meta">
                server:
                <el-button link type="primary" size="small" @click="copyText(ev.serverTraceId)">
                  {{ ev.serverTraceId }}
                </el-button>
              </span>
              <span v-if="ev.url" class="client-track-event-meta">{{ ev.url }}</span>
            </div>
            <pre v-if="!parsedEvents.length" class="client-track-pre">{{ formattedEventsJson }}</pre>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { listClientTrack, removeClientTrack } from '@/api/monitor/clientTrack'

/**
 * 前端用户行为监控批次：按 traceId/用户/原因查询，查看事件 JSON 还原操作路径。
 */
defineOptions({ name: 'SysClientTrack' })

const tableRef = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

/** 上报原因静态选项（与后端 reason 字段一致） */
const reasonOptions = [
  { label: '普通', value: 'normal' },
  { label: '错误', value: 'error' },
  { label: '离开', value: 'leave' },
  { label: '定时', value: 'timer' },
  { label: '操作结束', value: 'operation_end' }
]

const defaultSearchParam = {
  operationId: '',
  traceId: '',
  userName: '',
  reason: '',
  createTimeRange: []
}

const searchColumns = computed(() => [
  { prop: 'operationId', label: 'operationId', type: 'input', span: 8, props: { placeholder: '精确匹配', clearable: true } },
  { prop: 'traceId', label: 'serverTraceId', type: 'input', span: 8, props: { placeholder: '精确匹配', clearable: true } },
  { prop: 'userName', label: '用户名', type: 'input', span: 8, props: { placeholder: '模糊匹配', clearable: true } },
  {
    prop: 'reason',
    label: '上报原因',
    type: 'select',
    span: 8,
    options: reasonOptions,
    props: { placeholder: '全部', clearable: true, style: 'width: 240px' }
  },
  {
    prop: 'createTimeRange',
    label: '入库时间',
    type: 'daterange',
    span: 16,
    props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' }
  }
])

const tableColumns = computed(() => [
  { prop: 'batchId', label: '批次编号', width: 120 },
  { prop: 'operationId', label: 'operationId', minWidth: 160, showOverflowTooltip: true },
  { prop: 'traceId', label: 'serverTraceId', minWidth: 140, showOverflowTooltip: true },
  { prop: 'userName', label: '用户名', width: 120, showOverflowTooltip: true },
  { prop: 'reason', label: '上报原因', columnType: 'slot', slotName: 'reason', width: 100 },
  { prop: 'pagePath', label: '页面路径', minWidth: 180, showOverflowTooltip: true },
  { prop: 'clientIp', label: 'IP', width: 130, showOverflowTooltip: true },
  { prop: 'createTime', label: '入库时间', columnType: 'slot', slotName: 'createTime', width: 180 },
  { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 100, fixed: 'right' }
])

const formattedEventsJson = computed(() => {
  const raw = detailRow.value?.eventsJson
  if (!raw) {
    return '—'
  }
  try {
    return JSON.stringify(JSON.parse(raw), null, 2)
  } catch {
    return raw
  }
})

const parsedEvents = computed(() => {
  const raw = detailRow.value?.eventsJson
  if (!raw) {
    return []
  }
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

/**
 * @param {string} text
 */
async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败')
  }
}

/**
 * @param {string|undefined} reason
 * @returns {string}
 */
function reasonLabel(reason) {
  const hit = reasonOptions.find((o) => o.value === reason)
  return hit ? hit.label : reason || '—'
}

/**
 * @param {string|undefined} reason
 * @returns {'success'|'danger'|'info'|'warning'|''}
 */
function reasonTagType(reason) {
  if (reason === 'error') return 'danger'
  if (reason === 'leave') return 'info'
  if (reason === 'timer') return 'warning'
  if (reason === 'operation_end') return 'success'
  if (reason === 'normal') return 'success'
  return ''
}

/**
 * @param {string|number|Date|null|undefined} value
 * @returns {string}
 */
function formatTime(value) {
  if (value == null || value === '') {
    return '—'
  }
  const formatted = parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}')
  return formatted || String(value)
}

/**
 * 将日期范围转为后端 beginDate/endDate。
 * @param {Record<string, unknown>} raw
 */
function normalizeListParams(raw) {
  const p = { ...raw }
  const range = p.createTimeRange
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p.beginDate = range[0]
    p.endDate = range[1]
  }
  delete p.createTimeRange
  if (p.reason === '' || p.reason == null) delete p.reason
  if (p.operationId === '') delete p.operationId
  if (p.traceId === '') delete p.traceId
  if (p.userName === '') delete p.userName
  return p
}

function listFunction(params) {
  return listClientTrack(normalizeListParams(params))
}

function batchDeleteFunction(ids) {
  return removeClientTrack(ids || [])
}

/**
 * @param {Record<string, unknown>} row
 */
function openDetail(row) {
  detailRow.value = row ? { ...row } : null
  detailVisible.value = true
}
</script>

<style scoped>
.client-track-detail {
  -webkit-user-select: text;
  user-select: text;
}

.client-track-detail :deep(.el-descriptions__label) {
  width: 96px;
}

.client-track-cell-text {
  -webkit-user-select: text;
  user-select: text;
}

.client-track-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 420px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
}

.client-track-events {
  max-height: 420px;
  overflow: auto;
}

.client-track-event-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-size: 12px;
}

.client-track-event-type {
  font-weight: 600;
  min-width: 80px;
}

.client-track-event-meta {
  color: var(--el-text-color-secondary);
}
</style>
