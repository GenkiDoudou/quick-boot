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
      <template #toolbar-left="{ refreshData }">
        <el-button
          type="warning"
          plain
          v-hasPermi="['monitor:clientTrack:remove']"
          @click="handleCleanAll(refreshData)"
        >
          全部删除
        </el-button>
      </template>
      <template #pageMenu="{ row }">
        <el-tooltip v-if="row.menuBreadcrumb || row.menuName" :content="row.pagePath || ''" placement="top">
          <span class="client-track-cell-text">{{ row.menuBreadcrumb || row.menuName }}</span>
        </el-tooltip>
        <span v-else class="client-track-cell-text client-track-cell-muted">{{ row.pagePath || '—' }}</span>
      </template>
      <template #reason="{ row }">
        <el-tag :type="reasonTagType(row.reason)" size="small">{{ reasonLabel(row.reason) }}</el-tag>
      </template>
      <template #triggerAction="{ row }">
        <span class="client-track-cell-text">{{ formatTriggerCell(row) }}</span>
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
        <el-descriptions-item label="触发操作">
          <el-tag v-if="detailTriggerLabel" type="primary" size="small">{{ detailTriggerLabel }}</el-tag>
          <span v-else>—</span>
          <span v-if="detailRow.triggerAction" class="client-track-trigger-raw">{{ detailRow.triggerAction }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="serverTraceId（oper_log）">{{ detailRow.traceId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailRow.userName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="上报原因">
          <el-tag :type="reasonTagType(detailRow.reason)" size="small">{{ reasonLabel(detailRow.reason) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属菜单">
          {{ detailRow.menuBreadcrumb || detailRow.menuName || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="页面路径" :span="2">{{ detailRow.pagePath || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户端 IP">{{ detailRow.clientIp || '—' }}</el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ formatTime(detailRow.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detailRow" class="client-track-timeline-section">
        <div class="client-track-timeline-header">
          <span class="client-track-timeline-title">事件链路</span>
          <span class="client-track-timeline-count">共 {{ parsedEvents.length }} 条</span>
          <el-button v-if="parsedEvents.length" link type="primary" size="small" @click="showRawJson = !showRawJson">
            {{ showRawJson ? '收起 JSON' : '查看 JSON' }}
          </el-button>
        </div>

        <el-empty v-if="!parsedEvents.length" description="暂无事件" :image-size="64" />

        <el-timeline v-else class="client-track-timeline">
          <el-timeline-item
            v-for="(ev, idx) in parsedEvents"
            :key="idx"
            :type="timelineNodeType(ev)"
            :color="timelineNodeColor(ev)"
            :hollow="ev.type === 'route_leave'"
            :timestamp="formatEventTs(ev, idx)"
            placement="top"
          >
            <div class="client-track-event-card" :class="'client-track-event-card--' + (ev.type || 'unknown')">
              <div class="client-track-event-card__head">
                <el-tag :type="eventTagType(ev.type)" size="small" effect="light">{{ eventTypeLabel(ev.type) }}</el-tag>
                <span class="client-track-event-card__title">{{ eventHeadline(ev) }}</span>
                <el-tag v-if="ev.cost != null" size="small" type="info" effect="plain">{{ ev.cost }}ms</el-tag>
              </div>
              <dl v-if="eventDetailItems(ev).length" class="client-track-event-card__details">
                <div v-for="(item, di) in eventDetailItems(ev)" :key="di" class="client-track-event-detail">
                  <dt>{{ item.label }}</dt>
                  <dd>
                    <span class="client-track-event-detail__value">{{ item.value }}</span>
                    <el-button
                      v-if="item.copyable"
                      link
                      type="primary"
                      size="small"
                      @click="copyText(String(item.value))"
                    >
                      复制
                    </el-button>
                  </dd>
                </div>
              </dl>
            </div>
          </el-timeline-item>
        </el-timeline>

        <pre v-if="showRawJson && formattedEventsJson !== '—'" class="client-track-pre">{{ formattedEventsJson }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { cleanClientTrack, listClientTrack, removeClientTrack } from '@/api/monitor/clientTrack'
import { formatTrackLabel, resolveBatchTriggerAction } from '@/monitor/trackLabel'

/**
 * 前端用户行为监控批次：按 traceId/用户/原因查询，查看事件 JSON 还原操作路径。
 */
defineOptions({ name: 'SysClientTrack' })

const tableRef = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)
const showRawJson = ref(false)

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
  { prop: 'triggerAction', label: '触发操作', columnType: 'slot', slotName: 'triggerAction', width: 120, showOverflowTooltip: true },
  { prop: 'operationId', label: 'operationId', minWidth: 160, showOverflowTooltip: true },
  { prop: 'traceId', label: 'serverTraceId', minWidth: 140, showOverflowTooltip: true },
  { prop: 'userName', label: '用户名', width: 120, showOverflowTooltip: true },
  { prop: 'reason', label: '上报原因', columnType: 'slot', slotName: 'reason', width: 100 },
  { prop: 'menuName', label: '所属菜单', columnType: 'slot', slotName: 'pageMenu', minWidth: 160, showOverflowTooltip: true },
  { prop: 'pagePath', label: '页面路径', minWidth: 160, showOverflowTooltip: true },
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

const detailTriggerLabel = computed(() => {
  const row = detailRow.value
  if (!row) {
    return ''
  }
  if (row.triggerAction) {
    return formatTrackLabel(String(row.triggerAction))
  }
  const { label } = resolveBatchTriggerAction(parsedEvents.value)
  return label
})

/**
 * @param {Record<string, unknown>} row
 * @returns {string}
 */
function formatTriggerCell(row) {
  const raw = row.triggerAction
  if (raw) {
    return formatTrackLabel(String(raw))
  }
  if (row.eventsJson) {
    try {
      const events = JSON.parse(String(row.eventsJson))
      return resolveBatchTriggerAction(events).label || '—'
    } catch {
      return '—'
    }
  }
  return '—'
}

/**
 * @param {string | undefined} type
 * @returns {string}
 */
function eventTypeLabel(type) {
  const map = {
    click: '点击',
    api_call: 'API',
    api_slow: '慢API',
    api_error: 'API错误',
    route_enter: '进入页面',
    route_leave: '离开页面',
    js_error: 'JS错误',
    promise_error: 'Promise错误'
  }
  return map[type] || type || '—'
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {string}
 */
function eventTriggerLabel(ev) {
  if (ev.trigger) {
    return formatTrackLabel(String(ev.trigger))
  }
  if (ev.type === 'click' && ev.target) {
    const t = String(ev.target)
    if (!/^(BUTTON|A|SPAN|DIV|INPUT|I|SVG)$/i.test(t)) {
      return formatTrackLabel(t)
    }
  }
  return ''
}

/**
 * 时间轴节点主标题（一眼可见的操作描述）。
 * @param {Record<string, unknown>} ev
 * @returns {string}
 */
function eventHeadline(ev) {
  const type = String(ev.type || '')
  if (type === 'click') {
    return eventTriggerLabel(ev) || (ev.target ? `点击「${ev.target}」` : '点击')
  }
  if (type === 'api_call' || type === 'api_slow' || type === 'api_error') {
    const method = ev.method ? String(ev.method).toUpperCase() : 'GET'
    const url = ev.url ? String(ev.url) : '—'
    if (type === 'api_error' && ev.msg) {
      return `${method} ${url} · ${ev.msg}`
    }
    return `${method} ${url}`
  }
  if (type === 'route_enter') {
    return ev.path ? `进入 ${ev.path}` : '进入页面'
  }
  if (type === 'route_leave') {
    const from = ev.from ? String(ev.from) : '—'
    const to = ev.to ? String(ev.to) : '—'
    return `${from} → ${to}`
  }
  if (type === 'js_error' || type === 'promise_error') {
    return ev.msg ? String(ev.msg) : '运行时错误'
  }
  return type || '—'
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {Array<{ label: string, value: string, copyable?: boolean }>}
 */
function eventDetailItems(ev) {
  /** @type {Array<{ label: string, value: string, copyable?: boolean }>} */
  const items = []
  const trigger = eventTriggerLabel(ev)
  if (trigger && ev.type !== 'click') {
    items.push({ label: '触发操作', value: trigger })
  }
  if (ev.type === 'click' && ev.target && !trigger) {
    items.push({ label: '点击目标', value: String(ev.target) })
  }
  if (ev.page) {
    items.push({ label: '页面', value: String(ev.page) })
  }
  if (ev.operationId) {
    items.push({ label: 'operationId', value: String(ev.operationId), copyable: true })
  }
  if (ev.serverTraceId) {
    items.push({ label: 'serverTraceId', value: String(ev.serverTraceId), copyable: true })
  } else if (ev.clientTraceId) {
    items.push({ label: 'clientTraceId', value: String(ev.clientTraceId), copyable: true })
  }
  if (ev.responseTraceId && ev.responseTraceId !== ev.serverTraceId) {
    items.push({ label: 'responseTraceId', value: String(ev.responseTraceId), copyable: true })
  }
  if (ev.httpStatus != null) {
    items.push({ label: 'HTTP', value: String(ev.httpStatus) })
  }
  if (ev.bizCode != null) {
    items.push({ label: '业务码', value: String(ev.bizCode) })
  }
  return items
}

/**
 * @param {Record<string, unknown>} ev
 * @param {number} idx
 * @returns {string}
 */
function formatEventTs(ev, idx) {
  const ts = ev.ts
  if (ts == null || ts === '') {
    return `#${idx + 1}`
  }
  const n = Number(ts)
  if (!Number.isFinite(n)) {
    return String(ts)
  }
  const d = new Date(n)
  const pad = (v) => String(v).padStart(2, '0')
  const ms = pad(d.getMilliseconds()).padStart(3, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${ms}`
}

/**
 * @param {string | undefined} type
 * @returns {'success'|'warning'|'danger'|'info'|'primary'|''}
 */
function eventTagType(type) {
  if (type === 'click') return 'primary'
  if (type === 'api_call') return 'success'
  if (type === 'api_slow') return 'warning'
  if (type === 'api_error' || type === 'js_error' || type === 'promise_error') return 'danger'
  if (type === 'route_enter' || type === 'route_leave') return 'info'
  return ''
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {'primary'|'success'|'warning'|'danger'|'info'|undefined}
 */
function timelineNodeType(ev) {
  const type = ev.type
  if (type === 'click') return 'primary'
  if (type === 'api_call') return 'success'
  if (type === 'api_slow') return 'warning'
  if (type === 'api_error' || type === 'js_error' || type === 'promise_error') return 'danger'
  return undefined
}

/**
 * @param {Record<string, unknown>} ev
 * @returns {string|undefined}
 */
function timelineNodeColor(ev) {
  if (ev.type === 'route_enter' || ev.type === 'route_leave') {
    return '#909399'
  }
  return undefined
}

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
 * 清空全部监控批次（二次确认，不可恢复）。
 * @param {(() => void) | undefined} refreshData
 */
function handleCleanAll(refreshData) {
  ElMessageBox.confirm('确认删除全部前端监控记录？此操作不可恢复。', '提示', { type: 'warning' })
    .then(() => cleanClientTrack())
    .then(() => {
      ElMessage.success('已全部删除')
      refreshData?.()
    })
    .catch(() => {})
}

/**
 * @param {Record<string, unknown>} row
 */
function openDetail(row) {
  detailRow.value = row ? { ...row } : null
  showRawJson.value = false
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
  margin: 12px 0 0;
  padding: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 280px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.client-track-timeline-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.client-track-timeline-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.client-track-timeline-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.client-track-timeline-count {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.client-track-timeline {
  padding-left: 4px;
  max-height: 460px;
  overflow-y: auto;
}

.client-track-event-card {
  padding: 10px 12px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  border-left: 3px solid var(--el-color-primary);
}

.client-track-event-card--api_call {
  border-left-color: var(--el-color-success);
}

.client-track-event-card--api_slow {
  border-left-color: var(--el-color-warning);
}

.client-track-event-card--api_error,
.client-track-event-card--js_error,
.client-track-event-card--promise_error {
  border-left-color: var(--el-color-danger);
}

.client-track-event-card--route_enter,
.client-track-event-card--route_leave {
  border-left-color: var(--el-color-info);
}

.client-track-event-card__head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.client-track-event-card__title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  word-break: break-all;
}

.client-track-event-card__details {
  margin: 10px 0 0;
  padding: 8px 10px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.client-track-event-detail {
  display: grid;
  grid-template-columns: 100px 1fr auto;
  gap: 4px 12px;
  align-items: start;
  font-size: 12px;
  line-height: 1.6;
}

.client-track-event-detail + .client-track-event-detail {
  margin-top: 4px;
  padding-top: 4px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.client-track-event-detail dt {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.client-track-event-detail dd {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.client-track-event-detail__value {
  word-break: break-all;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: var(--el-text-color-regular);
}

.client-track-cell-muted {
  color: var(--el-text-color-secondary);
}

.client-track-trigger-raw {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
</style>
