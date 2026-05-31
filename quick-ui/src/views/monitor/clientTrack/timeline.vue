<template>
  <div class="app-container client-track-timeline-page">
    <el-card shadow="never" class="client-track-timeline-search">
      <el-form :model="searchForm" inline label-width="120px" @submit.prevent="handleSearch">
        <el-form-item label="browserVisitId">
          <el-input v-model="searchForm.browserVisitId" clearable placeholder="精确匹配" style="width: 220px" />
        </el-form-item>
        <el-form-item label="sessionId">
          <el-input v-model="searchForm.sessionId" clearable placeholder="精确匹配" style="width: 220px" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.userName" clearable placeholder="模糊，合并时间范围内批次" style="width: 220px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.createTimeRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" v-hasPermi="['monitor:clientTrack:list']" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div v-if="summaryText" class="client-track-timeline-summary">
        <el-alert :title="summaryText" :type="summaryAlertType" show-icon :closable="false" />
      </div>
    </el-card>

    <el-empty v-if="searched && !loading && !hasData" description="未找到匹配的监控批次" />

    <template v-else-if="hasData">
      <el-card shadow="never" class="client-track-timeline-chart-card">
        <template #header>
          <div class="client-track-timeline-card-head">
            <span>访问顺序</span>
            <el-button v-if="selectedPageId" link type="primary" size="small" @click="clearPageSelection">查看全部页面</el-button>
          </div>
        </template>
        <el-steps :active="activeStepIndex" align-center finish-status="success" class="client-track-flow-steps">
          <el-step
            v-for="(step, idx) in flowSteps"
            :key="step.id"
            :title="step.title"
            :description="step.description"
            class="client-track-flow-step"
            :class="{ 'is-selected': step.id === selectedPageId }"
            @click="selectPage(step.id, idx)"
          />
        </el-steps>
        <div class="client-track-timeline-legend">
          <span class="client-track-legend-item client-track-legend-item--page">页面</span>
          <span class="client-track-legend-item client-track-legend-item--visit">访问批</span>
          <span class="client-track-legend-item client-track-legend-item--action">操作批</span>
          <span class="client-track-legend-item client-track-legend-item--api">API/点击</span>
        </div>
        <div ref="graphRef" class="client-track-timeline-chart client-track-timeline-chart--graph" />
      </el-card>
      <el-card shadow="never" class="client-track-timeline-chart-card">
        <div ref="treeRef" class="client-track-timeline-chart client-track-timeline-chart--tree" />
      </el-card>
    </template>

    <el-drawer v-model="drawerVisible" title="事件明细" size="420px" destroy-on-close>
      <div v-if="selectedEvent" class="client-track-event-drawer">
        <div class="client-track-event-card__head">
          <el-tag :type="eventTagType(selectedEvent.type)" size="small" effect="light">
            {{ eventTypeLabel(selectedEvent.type) }}
          </el-tag>
          <span class="client-track-event-card__title">{{ eventHeadline(selectedEvent) }}</span>
          <el-tag v-if="selectedEvent.cost != null" size="small" type="info" effect="plain">
            {{ selectedEvent.cost }}ms
          </el-tag>
        </div>
        <dl v-if="eventDetailItems(selectedEvent).length" class="client-track-event-card__details">
          <div v-for="(item, di) in eventDetailItems(selectedEvent)" :key="di" class="client-track-event-detail">
            <dt>{{ item.label }}</dt>
            <dd>
              <span class="client-track-event-detail__value">{{ item.value }}</span>
              <el-button v-if="item.copyable" link type="primary" size="small" @click="copyText(String(item.value))">
                复制
              </el-button>
            </dd>
          </div>
        </dl>
        <pre v-if="selectedEventRaw" class="client-track-pre">{{ selectedEventRaw }}</pre>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getClientTrackTimeline } from '@/api/monitor/clientTrack'
import { buildTimelineModel } from './buildTimelineModel'
import { createGraphOption, createTreeOption, echarts } from './timelineChart'
import {
  eventDetailItems,
  eventHeadline,
  eventTagType,
  eventTypeLabel,
  normalizeListParams
} from './clientTrackEvent'

/**
 * 行为轨迹可视化：页面跳转关系图 + 页面内行为树，点击叶子查看事件明细。
 */
defineOptions({ name: 'SysClientTrackTimeline' })

const route = useRoute()
const loading = ref(false)
const searched = ref(false)
const drawerVisible = ref(false)
const selectedEvent = ref(null)
const selectedPageId = ref('')
/** @type {import('vue').Ref<ReturnType<typeof buildTimelineModel>|null>} */
const timelineModel = ref(null)

const graphRef = ref(null)
const treeRef = ref(null)
/** @type {import('echarts/core').EChartsType|null} */
let graphChart = null
/** @type {import('echarts/core').EChartsType|null} */
let treeChart = null

const searchForm = reactive({
  browserVisitId: '',
  sessionId: '',
  userName: '',
  createTimeRange: []
})

const hasData = computed(() => {
  const nodes = timelineModel.value?.graph?.nodes
  return Array.isArray(nodes) && nodes.length > 0
})

const summaryText = computed(() => {
  const s = timelineModel.value?.summary
  if (!s || !searched.value) return ''
  const parts = []
  if (s.userName) parts.push(`用户 ${s.userName}`)
  if (s.sessionId) parts.push(`sessionId ${s.sessionId}`)
  if (s.browserVisitId) parts.push(`browserVisitId ${s.browserVisitId}`)
  parts.push(`共 ${s.totalBatches ?? 0} 批`)
  if (s.truncated) parts.push('（已截断，请缩小时间范围）')
  return parts.join(' · ')
})

const summaryAlertType = computed(() => (timelineModel.value?.summary?.truncated ? 'warning' : 'info'))

const flowSteps = computed(() => timelineModel.value?.flowSteps || [])

const activeStepIndex = computed(() => {
  if (!selectedPageId.value || !flowSteps.value.length) {
    return flowSteps.value.length ? flowSteps.value.length - 1 : 0
  }
  const idx = flowSteps.value.findIndex((s) => s.id === selectedPageId.value)
  return idx >= 0 ? idx : flowSteps.value.length - 1
})

const selectedEventRaw = computed(() => {
  if (!selectedEvent.value) return ''
  try {
    return JSON.stringify(selectedEvent.value, null, 2)
  } catch {
    return String(selectedEvent.value)
  }
})

function applyRouteQuery() {
  const q = route.query
  ;['browserVisitId', 'sessionId', 'userName'].forEach((key) => {
    const val = q[key]
    if (val != null && String(val).trim() !== '') {
      searchForm[key] = String(val)
    }
  })
}

function buildQueryParams() {
  return normalizeListParams({
    browserVisitId: searchForm.browserVisitId,
    sessionId: searchForm.sessionId,
    userName: searchForm.userName,
    createTimeRange: searchForm.createTimeRange
  })
}

function validateSearch() {
  if (!searchForm.browserVisitId?.trim() && !searchForm.sessionId?.trim() && !searchForm.userName?.trim()) {
    ElMessage.warning('browserVisitId、sessionId、用户名至少填写一项')
    return false
  }
  return true
}

function handleSearch() {
  if (!validateSearch()) return
  loading.value = true
  searched.value = true
  getClientTrackTimeline(buildQueryParams())
    .then((res) => {
      const vo = res.data || {}
      timelineModel.value = buildTimelineModel(vo)
      selectedPageId.value = ''
      selectedEvent.value = null
      drawerVisible.value = false
      nextTick(() => renderCharts())
    })
    .finally(() => {
      loading.value = false
    })
}

function handleReset() {
  searchForm.browserVisitId = ''
  searchForm.sessionId = ''
  searchForm.userName = ''
  searchForm.createTimeRange = []
}

function renderCharts() {
  if (!timelineModel.value) return
  disposeCharts()
  if (graphRef.value) {
    graphChart = echarts.init(graphRef.value)
    graphChart.setOption(createGraphOption(timelineModel.value, { selectedPageId: selectedPageId.value }))
    graphChart.on('click', onGraphClick)
  }
  if (treeRef.value) {
    treeChart = echarts.init(treeRef.value)
    treeChart.setOption(createTreeOption(timelineModel.value, { selectedPageId: selectedPageId.value, expandDepth: 2 }))
    treeChart.on('click', onTreeClick)
  }
}

/**
 * @param {Record<string, unknown>} params
 */
function onGraphClick(params) {
  if (params.dataType !== 'node' || !params.data?.id) return
  selectedPageId.value = String(params.data.id)
  refreshChartHighlight()
}

/**
 * @param {string} pageId
 * @param {number} [_idx]
 */
function selectPage(pageId, _idx) {
  selectedPageId.value = pageId
  refreshChartHighlight()
}

function clearPageSelection() {
  selectedPageId.value = ''
  refreshChartHighlight()
}

/**
 * @param {Record<string, unknown>} params
 */
function onTreeClick(params) {
  const data = params.data || {}
  if (data.pageNodeId) {
    selectedPageId.value = String(data.pageNodeId)
    refreshChartHighlight()
    return
  }
  if (data.isEventLeaf && data.nodeId) {
    const ev = timelineModel.value?.eventMap?.[String(data.nodeId)]
    if (ev) {
      selectedEvent.value = ev
      drawerVisible.value = true
    }
  }
}

function refreshChartHighlight() {
  if (graphChart && timelineModel.value) {
    graphChart.setOption(createGraphOption(timelineModel.value, { selectedPageId: selectedPageId.value }), true)
  }
  if (treeChart && timelineModel.value) {
    treeChart.setOption(
      createTreeOption(timelineModel.value, {
        selectedPageId: selectedPageId.value,
        expandDepth: selectedPageId.value ? 4 : 2
      }),
      true
    )
  }
}

function disposeCharts() {
  graphChart?.off('click', onGraphClick)
  treeChart?.off('click', onTreeClick)
  graphChart?.dispose()
  treeChart?.dispose()
  graphChart = null
  treeChart = null
}

function handleResize() {
  graphChart?.resize()
  treeChart?.resize()
}

/**
 * @param {string} text
 */
function copyText(text) {
  if (!text) return
  navigator.clipboard?.writeText(text).then(() => {
    ElMessage.success('已复制')
  })
}

onMounted(() => {
  applyRouteQuery()
  window.addEventListener('resize', handleResize)
  if (searchForm.browserVisitId || searchForm.sessionId || searchForm.userName) {
    handleSearch()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})

watch(
  () => route.query,
  () => {
    applyRouteQuery()
  }
)
</script>

<style scoped>
.client-track-timeline-search {
  margin-bottom: 16px;
}

.client-track-timeline-summary {
  margin-top: 8px;
}

.client-track-timeline-chart-card {
  margin-bottom: 16px;
}

.client-track-timeline-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.client-track-flow-steps {
  margin-bottom: 12px;
}

.client-track-flow-step {
  cursor: pointer;
}

.client-track-flow-step.is-selected :deep(.el-step__title),
.client-track-flow-step.is-selected :deep(.el-step__description) {
  color: var(--el-color-warning);
  font-weight: 600;
}

.client-track-timeline-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.client-track-legend-item {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid transparent;
}

.client-track-legend-item--page {
  background: #ecf5ff;
  border-color: #409eff;
  color: #337ecc;
}

.client-track-legend-item--visit {
  background: #ecf5ff;
  border-color: #79bbff;
  color: #337ecc;
}

.client-track-legend-item--action {
  background: #fdf6ec;
  border-color: #e6a23c;
  color: #b88230;
}

.client-track-legend-item--api {
  background: #f0f9eb;
  border-color: #67c23a;
  color: #529b2e;
}

.client-track-timeline-chart {
  width: 100%;
}

.client-track-timeline-chart--graph {
  height: 380px;
}

.client-track-timeline-chart--tree {
  height: 560px;
}

.client-track-event-drawer .client-track-event-card__head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.client-track-event-card__title {
  flex: 1;
  min-width: 0;
  word-break: break-all;
}

.client-track-event-card__details {
  margin: 0;
}

.client-track-event-detail {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 4px 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.client-track-event-detail dt {
  margin: 0;
  color: var(--el-text-color-secondary);
}

.client-track-event-detail dd {
  margin: 0;
}

.client-track-event-detail__value {
  word-break: break-all;
  -webkit-user-select: text;
  user-select: text;
}

.client-track-pre {
  margin-top: 12px;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  font-size: 12px;
  overflow: auto;
  max-height: 280px;
  -webkit-user-select: text;
  user-select: text;
}
</style>
