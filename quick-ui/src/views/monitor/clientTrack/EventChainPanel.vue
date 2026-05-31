<template>
  <div class="client-track-events">
    <el-form :model="searchParam" inline class="client-track-events__search" @submit.prevent="handleSearch">
      <el-row :gutter="12" class="client-track-events__search-row">
        <el-col v-for="col in eventSearchColumns" :key="col.prop" :span="col.span || 8">
          <el-form-item :label="col.label" class="client-track-events__search-item">
            <el-input
              v-if="col.type === 'input'"
              v-model="searchParam[col.prop]"
              v-bind="col.props"
            />
            <el-select
              v-else-if="col.type === 'select'"
              v-model="searchParam[col.prop]"
              v-bind="col.props"
            >
              <el-option v-for="opt in col.options" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-date-picker
              v-else-if="col.type === 'daterange'"
              v-model="searchParam[col.prop]"
              type="daterange"
              v-bind="col.props"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="client-track-events__search-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-form>

    <div class="client-track-events__summary">
      本页 {{ batchTotal }} 个批次，展开 {{ filteredEvents.length }} 条事件
      <span v-if="searchParam.eventType" class="client-track-events__summary-filter">
        （已筛事件类型：{{ eventTypeLabel(searchParam.eventType) }}）
      </span>
    </div>

    <el-table
      v-loading="loading"
      :data="pagedEvents"
      row-key="rowKey"
      stripe
      border
      size="small"
      class="client-track-events__table"
      empty-text="暂无事件，请调整搜索条件"
    >
      <el-table-column label="#" width="52" align="center">
        <template #default="{ $index }">{{ (pageNum - 1) * pageSize + $index + 1 }}</template>
      </el-table-column>
      <el-table-column label="事件" min-width="220">
        <template #default="{ row }">
          <div class="client-track-events__event-head">
            <el-tag :type="row.eventTagType" size="small" effect="light">{{ eventTypeLabel(row.eventType) }}</el-tag>
            <span class="client-track-events__event-title">{{ row.eventHeadline }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="事件序" prop="eventIndex" width="64" align="center" />
      <el-table-column label="browserVisitId" prop="browserVisitId" min-width="130" show-overflow-tooltip />
      <el-table-column label="sessionId" prop="sessionId" min-width="130" show-overflow-tooltip />
      <el-table-column label="pageVisitId" prop="pageVisitId" min-width="130" show-overflow-tooltip />
      <el-table-column label="operationId" prop="operationId" min-width="130" show-overflow-tooltip />
      <el-table-column label="用户名" prop="userName" width="100" show-overflow-tooltip />
      <el-table-column label="所属菜单" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.menuBreadcrumb || row.menuName || '—' }}
        </template>
      </el-table-column>
      <el-table-column label="批次触发" min-width="100" show-overflow-tooltip>
        <template #default="{ row }">
          {{ formatTrigger(row.batchTriggerAction) }}
        </template>
      </el-table-column>
      <el-table-column label="批次原因" width="96" align="center">
        <template #default="{ row }">
          <el-tag :type="reasonTagType(row.batchReason)" size="small">{{ reasonLabel(row.batchReason) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="批次编号" prop="batchId" width="96" align="center" />
      <el-table-column label="入库时间" width="160">
        <template #default="{ row }">{{ formatTime(row.batchCreateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="88" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="emitViewBatch(row.batchId)">批次</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="client-track-events__pager">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="filteredEvents.length"
        :page-sizes="[20, 50, 100, 200]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="handleEventPageChange"
        @size-change="handleEventSizeChange"
      />
      <div class="client-track-events__batch-pager">
        <span class="client-track-events__batch-pager-label">批次页</span>
        <el-pagination
          v-model:current-page="batchPageNum"
          :page-size="batchPageSize"
          :total="batchTotal"
          layout="prev, pager, next"
          small
          background
          @current-change="loadBatches"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { parseTime } from '@/utils/ruoyi'
import { listClientTrack } from '@/api/monitor/clientTrack'
import { formatTrackLabel } from '@/monitor/trackLabel'
import {
  defaultSearchParam,
  eventSearchColumns,
  eventTypeLabel,
  flattenBatchEvents,
  normalizeListParams,
  reasonLabel,
  reasonTagType
} from './clientTrackEvent'

/**
 * 事件链路明细：按搜索条件拉取批次并展开 eventsJson 为扁平表格。
 */
defineOptions({ name: 'ClientTrackEventChain' })

const props = defineProps({
  /** 外部注入初始 sessionId 等（如从批次详情跳转） */
  initialSearch: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['view-batch'])

const loading = ref(false)
const searchParam = reactive({ ...defaultSearchParam, ...props.initialSearch })
const batches = ref([])
const batchPageNum = ref(1)
const batchPageSize = ref(20)
const batchTotal = ref(0)
const pageNum = ref(1)
const pageSize = ref(50)

const filteredEvents = computed(() => {
  let rows = flattenBatchEvents(batches.value)
  if (searchParam.eventType) {
    rows = rows.filter((r) => r.eventType === searchParam.eventType)
  }
  return rows
})

const pagedEvents = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredEvents.value.slice(start, start + pageSize.value)
})

watch(
  () => props.initialSearch,
  (val) => {
    if (val && typeof val === 'object') {
      Object.assign(searchParam, defaultSearchParam, val)
      handleSearch()
    }
  },
  { deep: true }
)

onMounted(() => {
  loadBatches()
})

/**
 * @param {string|number|Date|null|undefined} value
 */
function formatTime(value) {
  if (value == null || value === '') return '—'
  return parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}') || String(value)
}

/**
 * @param {string|undefined|null} raw
 */
function formatTrigger(raw) {
  if (!raw) return '—'
  return formatTrackLabel(String(raw))
}

function handleSearch() {
  batchPageNum.value = 1
  pageNum.value = 1
  loadBatches()
}

function handleReset() {
  Object.assign(searchParam, defaultSearchParam)
  batchPageNum.value = 1
  pageNum.value = 1
  loadBatches()
}

function handleEventPageChange() {
  /* computed 自动更新 */
}

function handleEventSizeChange() {
  pageNum.value = 1
}

function loadBatches() {
  loading.value = true
  const query = normalizeListParams({
    ...searchParam,
    pageNum: batchPageNum.value,
    pageSize: batchPageSize.value
  })
  listClientTrack(query)
    .then((res) => {
      batches.value = res.data?.records || []
      batchTotal.value = Number(res.data?.total || 0)
      pageNum.value = 1
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * @param {number|string} batchId
 */
function emitViewBatch(batchId) {
  emit('view-batch', batchId)
}

/** 供父组件按 sessionId 等筛选 */
function applySearch(partial) {
  Object.assign(searchParam, partial)
  handleSearch()
}

defineExpose({ applySearch, loadBatches })
</script>

<style scoped>
.client-track-events__search {
  margin-bottom: 12px;
  padding: 12px 12px 4px;
  background: var(--el-fill-color-blank);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}

.client-track-events__search-row {
  width: 100%;
}

.client-track-events__search-item {
  width: 100%;
  margin-bottom: 8px;
}

.client-track-events__search-item :deep(.el-form-item__content) {
  width: 100%;
}

.client-track-events__search-item :deep(.el-input),
.client-track-events__search-item :deep(.el-select),
.client-track-events__search-item :deep(.el-date-editor) {
  width: 100%;
}

.client-track-events__search-actions {
  margin-bottom: 8px;
}

.client-track-events__summary {
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.client-track-events__summary-filter {
  color: var(--el-color-primary);
}

.client-track-events__event-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.client-track-events__event-title {
  flex: 1;
  min-width: 0;
  word-break: break-all;
  font-size: 13px;
}

.client-track-events__pager {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}

.client-track-events__batch-pager {
  display: flex;
  align-items: center;
  gap: 8px;
}

.client-track-events__batch-pager-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
</style>
