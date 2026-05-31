<template>
  <el-dialog :model-value="modelValue" title="前端监控批次详情" width="920px" destroy-on-close @update:model-value="emit('update:modelValue', $event)">
    <el-descriptions v-if="row" :column="2" border size="small" class="client-track-detail">
        <el-descriptions-item label="批次编号">{{ row.batchId }}</el-descriptions-item>
        <el-descriptions-item label="browserVisitId">{{ row.browserVisitId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="sessionId">
        <span v-if="row.sessionId">{{ row.sessionId }}</span>
        <span v-else>—</span>
        <el-button v-if="row.sessionId" link type="primary" size="small" @click="emit('filter-session', row.sessionId)">
          查同会话
        </el-button>
        <el-button v-if="row.sessionId" link type="primary" size="small" @click="emit('view-event-chain', row)">
          事件链路
        </el-button>
        <el-button v-if="row.browserVisitId || row.sessionId" link type="success" size="small" @click="emit('view-timeline', row)">
          行为轨迹
        </el-button>
      </el-descriptions-item>
      <el-descriptions-item label="pageVisitId">{{ row.pageVisitId || '—' }}</el-descriptions-item>
      <el-descriptions-item label="operationId">{{ row.operationId || '—' }}</el-descriptions-item>
      <el-descriptions-item label="触发操作">
        <el-tag v-if="detailTriggerLabel" type="primary" size="small">{{ detailTriggerLabel }}</el-tag>
        <span v-else>—</span>
        <span v-if="row.triggerAction" class="client-track-trigger-raw">{{ row.triggerAction }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="serverTraceId（oper_log）">{{ row.traceId || '—' }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ row.userName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="上报原因">
        <el-tag :type="reasonTagType(row.reason)" size="small">{{ reasonLabel(row.reason) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="所属菜单">
        {{ row.menuBreadcrumb || row.menuName || '—' }}
      </el-descriptions-item>
      <el-descriptions-item label="页面路径" :span="2">{{ row.pagePath || '—' }}</el-descriptions-item>
      <el-descriptions-item label="客户端 IP">{{ row.clientIp || '—' }}</el-descriptions-item>
      <el-descriptions-item label="入库时间">{{ formatTime(row.createTime) }}</el-descriptions-item>
    </el-descriptions>

    <div v-if="row" class="client-track-timeline-section">
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
                  <el-button v-if="item.copyable" link type="primary" size="small" @click="copyText(String(item.value))">
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
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { formatTrackLabel, resolveBatchTriggerAction } from '@/monitor/trackLabel'
import {
  eventDetailItems,
  eventHeadline,
  eventTagType,
  eventTypeLabel,
  formatEventTs,
  parseEventsJson,
  reasonLabel,
  reasonTagType,
  timelineNodeColor,
  timelineNodeType
} from './clientTrackEvent'

/**
 * 前端监控批次详情弹窗（含事件时间轴）。
 */
defineOptions({ name: 'ClientTrackBatchDetailDialog' })

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'filter-session', 'view-event-chain', 'view-timeline'])

const showRawJson = ref(false)

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) showRawJson.value = false
  }
)

const formattedEventsJson = computed(() => {
  const raw = props.row?.eventsJson
  if (!raw) return '—'
  try {
    return JSON.stringify(JSON.parse(String(raw)), null, 2)
  } catch {
    return String(raw)
  }
})

const parsedEvents = computed(() => parseEventsJson(props.row?.eventsJson))

const detailTriggerLabel = computed(() => {
  const row = props.row
  if (!row) return ''
  if (row.triggerAction) return formatTrackLabel(String(row.triggerAction))
  return resolveBatchTriggerAction(parsedEvents.value).label
})

/**
 * @param {string|number|Date|null|undefined} value
 */
function formatTime(value) {
  if (value == null || value === '') return '—'
  return parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}') || String(value)
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
</script>

<style scoped>
.client-track-detail {
  -webkit-user-select: text;
  user-select: text;
}

.client-track-detail :deep(.el-descriptions__label) {
  width: 96px;
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

.client-track-trigger-raw {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
</style>
