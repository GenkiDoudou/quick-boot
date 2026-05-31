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
      <div v-if="volumeHint" class="client-track-timeline-summary">
        <el-alert :title="volumeHint" type="info" show-icon :closable="false" />
      </div>
    </el-card>

    <el-empty v-if="searched && !loading && !hasData" description="未找到匹配的监控批次" />

    <template v-else-if="hasData">
      <el-card v-if="multiSession" shadow="never" class="client-track-timeline-session-card">
        <template #header>
          <div class="client-track-timeline-card-head">
            <span>登录会话（共 {{ sessionCount }} 次）</span>
            <span class="client-track-session-hint">每次登录独立展示；点击页面查看操作明细</span>
          </div>
        </template>
        <el-select
          v-if="useSessionSelect"
          v-model="activeSessionKey"
          class="client-track-session-select"
          placeholder="选择登录会话"
          @change="onSessionChange"
        >
          <el-option v-for="session in sessionList" :key="session.key" :label="session.label" :value="session.key" />
        </el-select>
        <el-tabs
          v-else
          v-model="activeSessionKey"
          type="border-card"
          class="client-track-session-tabs"
          @tab-change="onSessionChange"
        >
          <el-tab-pane v-for="session in sessionList" :key="session.key" :label="session.label" :name="session.key" />
        </el-tabs>
      </el-card>

      <el-card shadow="never" class="client-track-timeline-chart-card">
        <template #header>
          <div class="client-track-timeline-card-head">
            <span>{{ multiSession ? '当前登录 · 页面跳转' : '页面跳转' }}</span>
            <el-radio-group v-model="graphViewMode" size="small" class="client-track-view-toggle" @change="onGraphViewModeChange">
              <el-radio-button label="chart">图表</el-radio-button>
              <el-radio-button label="table">表格</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div v-if="graphViewMode === 'chart'" class="client-track-graph-scroll">
          <div ref="graphRef" class="client-track-timeline-chart client-track-timeline-chart--graph" :style="graphChartStyle" />
        </div>
        <el-table
          v-else
          :data="flowEdgeTableRows"
          stripe
          border
          size="small"
          class="client-track-timeline-table"
          :row-class-name="flowTableRowClassName"
          @row-click="onFlowTableRowClick"
        >
          <el-table-column prop="step" label="步骤" width="72" align="center" />
          <el-table-column prop="fromLabel" label="来源页面" min-width="140" show-overflow-tooltip />
          <el-table-column prop="toLabel" label="目标页面" min-width="140" show-overflow-tooltip />
          <el-table-column prop="toPagePath" label="路径" min-width="160" show-overflow-tooltip />
          <el-table-column prop="jumpLabel" label="跳转说明" min-width="120" show-overflow-tooltip />
          <el-table-column label="操作" width="88" align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.targetPageId" link type="primary" size="small" @click.stop="selectPage(row.targetPageId)">
                查看明细
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="client-track-timeline-chart-card">
        <template #header>
          <div class="client-track-timeline-card-head">
            <span>页面导航</span>
            <div class="client-track-timeline-card-head__actions">
              <span v-if="useCompactNav && navViewMode === 'chart'" class="client-track-session-hint">页面较多，已启用精简导航</span>
              <el-radio-group v-model="navViewMode" size="small" class="client-track-view-toggle">
                <el-radio-button label="chart">图表</el-radio-button>
                <el-radio-button label="table">表格</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <template v-if="navViewMode === 'chart'">
          <el-steps
            v-if="!useCompactNav"
            :active="activeStepIndex"
            align-center
            finish-status="success"
            class="client-track-flow-steps"
          >
            <el-step
              v-for="(step, idx) in flowSteps"
              :key="step.id"
              :title="step.title"
              :description="stepNavDescription(step)"
              class="client-track-flow-step"
              :class="{ 'is-selected': step.id === selectedPageId }"
              @click="selectPage(step.id, idx)"
            />
          </el-steps>
          <div v-else class="client-track-page-chips">
            <button
              v-for="(item, idx) in pageIndex"
              :key="item.id"
              type="button"
              class="client-track-page-chip"
              :class="{ 'is-selected': item.id === selectedPageId }"
              @click="selectPage(item.id, idx)"
            >
              <span class="client-track-page-chip__step">{{ item.step }}</span>
              <span class="client-track-page-chip__label">{{ item.label }}</span>
              <span class="client-track-page-chip__meta">{{ item.actionCount }} 操作 · {{ item.eventCount }} 事件</span>
            </button>
          </div>
        </template>
        <el-table
          v-else
          :data="pageNavTableRows"
          stripe
          border
          size="small"
          class="client-track-timeline-table"
          :row-class-name="pageNavTableRowClassName"
          @row-click="onPageNavTableRowClick"
        >
          <el-table-column prop="step" label="序号" width="72" align="center" />
          <el-table-column prop="label" label="页面" min-width="160" show-overflow-tooltip />
          <el-table-column prop="pagePath" label="路径" min-width="180" show-overflow-tooltip />
          <el-table-column prop="actionCount" label="操作数" width="88" align="center" />
          <el-table-column prop="eventCount" label="事件数" width="88" align="center" />
          <el-table-column label="操作" width="88" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click.stop="selectPage(row.id)">查看明细</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="client-track-timeline-chart-card">
        <template #header>
          <div class="client-track-timeline-card-head">
            <span>当前页面行为明细</span>
            <div class="client-track-timeline-card-head__actions">
              <el-button v-if="selectedPageId" link type="primary" size="small" @click="clearPageSelection">取消选中</el-button>
              <el-radio-group
                v-if="selectedPageId && pageDetailModel"
                v-model="detailViewMode"
                size="small"
                class="client-track-view-toggle"
                @change="onDetailViewModeChange"
              >
                <el-radio-button label="chart">图表</el-radio-button>
                <el-radio-button label="table">表格</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <el-empty v-if="!selectedPageId" description="点击上方页面节点或导航项，查看该页操作与事件" />
        <template v-else-if="pageDetailModel">
          <template v-if="detailViewMode === 'chart'">
            <div v-if="pageDetailModel.useListFallback" class="client-track-detail-list">
              <el-alert
                title="事件较多，已使用列表模式展示（性能优化）"
                type="info"
                show-icon
                :closable="false"
                class="client-track-detail-list__hint"
              />
              <el-collapse accordion>
                <el-collapse-item
                  v-for="(group, gi) in pageDetailModel.batchGroups"
                  :key="group.batchId ?? gi"
                  :title="`${group.label}（${group.events?.length ?? 0} 事件）`"
                >
                  <el-timeline>
                    <el-timeline-item
                      v-for="ev in group.events"
                      :key="ev.nodeId"
                      :timestamp="ev.eventType"
                      placement="top"
                    >
                      <el-button link type="primary" @click="openEventDrawer(ev.nodeId)">{{ ev.name }}</el-button>
                    </el-timeline-item>
                  </el-timeline>
                </el-collapse-item>
              </el-collapse>
            </div>
            <div v-else ref="treeRef" class="client-track-timeline-chart client-track-timeline-chart--tree" />
          </template>
          <el-table
            v-else
            :data="detailEventTableRows"
            stripe
            border
            size="small"
            class="client-track-timeline-table"
            max-height="520"
            @row-click="onDetailTableRowClick"
          >
            <el-table-column prop="batchType" label="批次类型" width="88" align="center" />
            <el-table-column prop="batchLabel" label="操作批次" min-width="160" show-overflow-tooltip />
            <el-table-column prop="eventName" label="事件" min-width="200" show-overflow-tooltip />
            <el-table-column prop="eventType" label="类型" width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="88" align="center" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.nodeId" link type="primary" size="small" @click.stop="openEventDrawer(row.nodeId)">
                  明细
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
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
              <button
                v-if="item.linkOperLog"
                type="button"
                class="client-track-trace-link"
                :disabled="operLogDetailLoading"
                @click="openOperLogByTraceId(String(item.value))"
              >
                {{ item.value }}
              </button>
              <span v-else class="client-track-event-detail__value">{{ item.value }}</span>
              <el-button v-if="item.copyable" link type="primary" size="small" @click="copyText(String(item.value))">
                复制
              </el-button>
            </dd>
          </div>
        </dl>
        <pre v-if="selectedEventRaw" class="client-track-pre">{{ selectedEventRaw }}</pre>
      </div>
    </el-drawer>

    <el-dialog
      v-model="operLogDetailVisible"
      title="操作日志详情"
      width="880px"
      destroy-on-close
      append-to-body
    >
      <div v-loading="operLogDetailLoading">
        <OperLogDetailPanel :row="operLogDetailRow" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getClientTrackTimeline } from '@/api/monitor/clientTrack'
import { scheduleIdleTask } from '@/monitor/scheduleIdle'
import {
  buildPageDetailModel,
  buildTimelineOverview,
  findPageVo,
  findSessionModel,
  PAGE_NAV_STEPS_THRESHOLD,
  parseEventForDrawer,
  SESSION_TAB_SELECT_THRESHOLD
} from './buildTimelineModel'
import { createGraphOption, createTreeOption, echarts, patchGraphSelection } from './timelineChart'
import {
  buildDetailEventTableRows,
  buildFlowEdgeTableRows,
  buildPageNavTableRows
} from './timelineTableData'
import {
  eventDetailItems,
  eventHeadline,
  eventTagType,
  eventTypeLabel,
  normalizeListParams
} from './clientTrackEvent'
import OperLogDetailPanel from '@/views/monitor/operlog/OperLogDetailPanel.vue'
import { useOperLogDetail } from '@/views/monitor/operlog/useOperLogDetail'

/**
 * 行为轨迹可视化：页面优先展示跳转链，选中页后按需加载操作/事件明细。
 */
defineOptions({ name: 'SysClientTrackTimeline' })

const route = useRoute()
const loading = ref(false)
const searched = ref(false)
const drawerVisible = ref(false)
const selectedEvent = ref(null)
const {
  operLogDetailVisible,
  operLogDetailRow,
  operLogDetailLoading,
  openOperLogByTraceId,
} = useOperLogDetail()
const selectedPageId = ref('')
const activeSessionKey = ref('')
/** @type {import('vue').Ref<Record<string, unknown>|null>} */
const rawTimelineVo = ref(null)
/** @type {import('vue').Ref<ReturnType<typeof buildTimelineOverview>|null>} */
const overviewModel = ref(null)
/** @type {import('vue').Ref<ReturnType<typeof buildPageDetailModel>|null>} */
const pageDetailModel = ref(null)

/** @type {'chart'|'table'} */
const graphViewMode = ref('chart')
/** @type {'chart'|'table'} */
const navViewMode = ref('chart')
/** @type {'chart'|'table'} */
const detailViewMode = ref('chart')

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

const activeSessionModel = computed(() => {
  if (!overviewModel.value) return null
  return findSessionModel(overviewModel.value, activeSessionKey.value)
})

const sessionList = computed(() => overviewModel.value?.sessions || [])
const multiSession = computed(() => Boolean(overviewModel.value?.multiSession))
const sessionCount = computed(() => overviewModel.value?.summary?.sessionCount ?? sessionList.value.length)
const useSessionSelect = computed(() => sessionCount.value > SESSION_TAB_SELECT_THRESHOLD)

const hasData = computed(() => {
  const nodes = activeSessionModel.value?.graph?.nodes
  return Array.isArray(nodes) && nodes.length > 0
})

const pageIndex = computed(() => activeSessionModel.value?.pageIndex || [])
const flowSteps = computed(() => activeSessionModel.value?.flowSteps || [])
const useCompactNav = computed(() => pageIndex.value.length > PAGE_NAV_STEPS_THRESHOLD)

const flowEdgeTableRows = computed(() => buildFlowEdgeTableRows(activeSessionModel.value))
const pageNavTableRows = computed(() => buildPageNavTableRows(pageIndex.value))
const detailEventTableRows = computed(() => buildDetailEventTableRows(pageDetailModel.value))

const graphChartStyle = computed(() => {
  const count = activeSessionModel.value?.graph?.nodes?.length || 0
  if (count <= 1) return {}
  const minWidth = Math.max(720, 60 + count * 120)
  return { minWidth: `${minWidth}px` }
})

const summaryText = computed(() => {
  const s = overviewModel.value?.summary
  if (!s || !searched.value) return ''
  const parts = []
  if (s.userName) parts.push(`用户 ${s.userName}`)
  if (multiSession.value) {
    parts.push(`共 ${sessionCount.value} 次登录`)
  } else {
    if (s.sessionId) parts.push(`sessionId ${s.sessionId}`)
    if (s.browserVisitId) parts.push(`browserVisitId ${s.browserVisitId}`)
  }
  parts.push(`${s.totalPages ?? 0} 页 · ${s.totalBatches ?? 0} 批`)
  parts.push('点击页面查看操作明细')
  if (s.truncated) parts.push('（已截断，请缩小时间范围）')
  return parts.join(' · ')
})

const summaryAlertType = computed(() => {
  const s = overviewModel.value?.summary
  if (s?.truncated || (s?.totalBatches ?? 0) >= 400) return 'warning'
  return 'info'
})

const volumeHint = computed(() => {
  const s = overviewModel.value?.summary
  if (!s || !searched.value) return ''
  if ((s.totalPages ?? 0) > 50) {
    return '页面较多，已启用精简导航；建议缩小时间范围以获得更流畅的体验'
  }
  return ''
})

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

/**
 * @param {Record<string, unknown>} step
 */
function stepNavDescription(step) {
  const base = step.description ? String(step.description) : ''
  const meta = `${step.actionCount ?? 0} 操作 · ${step.eventCount ?? 0} 事件`
  return base ? `${base}\n${meta}` : meta
}

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
  disposePageDetailChart()
  pageDetailModel.value = null
  selectedPageId.value = ''
  selectedEvent.value = null
  drawerVisible.value = false

  getClientTrackTimeline(buildQueryParams())
    .then((res) => {
      const vo = res.data || {}
      rawTimelineVo.value = vo
      overviewModel.value = buildTimelineOverview(vo)
      activeSessionKey.value = overviewModel.value?.sessions?.[0]?.key || ''
      scheduleIdleTask(() => {
        nextTick(() => renderGraph())
      })
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

function renderGraph() {
  if (graphViewMode.value !== 'chart') return
  const model = activeSessionModel.value
  if (!model) return
  disposeGraphChart()
  if (!graphRef.value) return
  graphChart = echarts.init(graphRef.value)
  graphChart.setOption(createGraphOption(model, { selectedPageId: selectedPageId.value }))
  graphChart.on('click', onGraphClick)
}

function renderPageDetail() {
  if (detailViewMode.value !== 'chart') return
  disposePageDetailChart()
  if (!pageDetailModel.value || pageDetailModel.value.useListFallback) return
  nextTick(() => {
    if (!treeRef.value) return
    treeChart = echarts.init(treeRef.value)
    treeChart.setOption(createTreeOption(pageDetailModel.value, { expandDepth: 4 }))
    treeChart.on('click', onTreeClick)
  })
}

function onGraphViewModeChange(mode) {
  if (mode === 'chart') {
    nextTick(() => renderGraph())
  } else {
    disposeGraphChart()
  }
}

function onDetailViewModeChange(mode) {
  if (mode === 'chart') {
    nextTick(() => renderPageDetail())
  } else {
    disposePageDetailChart()
  }
}

/**
 * @param {{ row: Record<string, unknown> }} param
 */
function flowTableRowClassName({ row }) {
  return row.targetPageId && row.targetPageId === selectedPageId.value ? 'client-track-table-row--selected' : ''
}

/**
 * @param {{ row: Record<string, unknown> }} param
 */
function pageNavTableRowClassName({ row }) {
  return row.id === selectedPageId.value ? 'client-track-table-row--selected' : ''
}

/**
 * @param {Record<string, unknown>} row
 */
function onFlowTableRowClick(row) {
  if (row.targetPageId) {
    selectPage(String(row.targetPageId))
  }
}

/**
 * @param {Record<string, unknown>} row
 */
function onPageNavTableRowClick(row) {
  if (row.id) {
    selectPage(String(row.id))
  }
}

/**
 * @param {Record<string, unknown>} row
 */
function onDetailTableRowClick(row) {
  if (row.nodeId) {
    openEventDrawer(String(row.nodeId))
  }
}

/**
 * @param {string} pageId
 * @param {number} [_idx]
 */
function selectPage(pageId, _idx) {
  if (selectedPageId.value === pageId) return
  selectedPageId.value = pageId
  loadPageDetail(pageId)
  if (graphViewMode.value === 'chart') {
    patchGraphSelection(graphChart, activeSessionModel.value, selectedPageId.value)
  }
}

function loadPageDetail(pageId) {
  if (!rawTimelineVo.value || !activeSessionKey.value) return
  const hit = findPageVo(rawTimelineVo.value, activeSessionKey.value, pageId)
  if (!hit) {
    pageDetailModel.value = null
    return
  }
  pageDetailModel.value = buildPageDetailModel(hit.page, hit.sessionId, hit.pageIndex)
  selectedEvent.value = null
  drawerVisible.value = false
  scheduleIdleTask(() => {
    renderPageDetail()
  })
}

function onSessionChange() {
  selectedPageId.value = ''
  pageDetailModel.value = null
  selectedEvent.value = null
  drawerVisible.value = false
  disposePageDetailChart()
  nextTick(() => renderGraph())
}

/**
 * @param {Record<string, unknown>} params
 */
function onGraphClick(params) {
  if (params.dataType !== 'node' || !params.data?.id) return
  selectPage(String(params.data.id))
}

function clearPageSelection() {
  selectedPageId.value = ''
  pageDetailModel.value = null
  selectedEvent.value = null
  drawerVisible.value = false
  disposePageDetailChart()
  if (graphViewMode.value === 'chart') {
    patchGraphSelection(graphChart, activeSessionModel.value, '')
  }
}

/**
 * @param {Record<string, unknown>} params
 */
function onTreeClick(params) {
  const data = params.data || {}
  if (data.isEventLeaf && data.nodeId) {
    openEventDrawer(String(data.nodeId))
  }
}

/**
 * @param {string} nodeId
 */
function openEventDrawer(nodeId) {
  const ref = pageDetailModel.value?.eventMap?.[nodeId]
  if (!ref) return
  selectedEvent.value = parseEventForDrawer(ref)
  drawerVisible.value = true
}

function disposeGraphChart() {
  graphChart?.off('click', onGraphClick)
  graphChart?.dispose()
  graphChart = null
}

function disposePageDetailChart() {
  treeChart?.off('click', onTreeClick)
  treeChart?.dispose()
  treeChart = null
}

function disposeCharts() {
  disposeGraphChart()
  disposePageDetailChart()
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

.client-track-timeline-session-card {
  margin-bottom: 16px;
}

.client-track-session-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
}

.client-track-session-select {
  width: 100%;
  max-width: 520px;
}

.client-track-session-tabs :deep(.el-tabs__content) {
  display: none;
}

.client-track-timeline-chart-card {
  margin-bottom: 16px;
}

.client-track-timeline-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-weight: 600;
}

.client-track-timeline-card-head__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.client-track-view-toggle {
  flex-shrink: 0;
}

.client-track-timeline-table {
  width: 100%;
}

.client-track-timeline-table :deep(.client-track-table-row--selected) {
  background-color: #fdf6ec !important;
}

.client-track-timeline-table :deep(.el-table__row) {
  cursor: pointer;
}

.client-track-graph-scroll {
  overflow-x: auto;
  overflow-y: hidden;
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

.client-track-page-chips {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.client-track-page-chip {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  min-width: 140px;
  max-width: 220px;
  padding: 8px 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  cursor: pointer;
  text-align: left;
}

.client-track-page-chip:hover {
  border-color: var(--el-color-primary);
}

.client-track-page-chip.is-selected {
  border-color: var(--el-color-warning);
  background: #fdf6ec;
}

.client-track-page-chip__step {
  font-size: 11px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.client-track-page-chip.is-selected .client-track-page-chip__step {
  color: var(--el-color-warning);
}

.client-track-page-chip__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.client-track-page-chip__meta {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.client-track-detail-list__hint {
  margin-bottom: 12px;
}

.client-track-timeline-chart {
  width: 100%;
}

.client-track-timeline-chart--graph {
  height: 380px;
}

.client-track-timeline-chart--tree {
  height: 520px;
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

.client-track-trace-link {
  padding: 0;
  border: 0;
  background: none;
  color: var(--el-color-primary);
  cursor: pointer;
  font: inherit;
  text-align: left;
  word-break: break-all;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.client-track-trace-link:hover:not(:disabled) {
  color: var(--el-color-primary-light-3);
}

.client-track-trace-link:disabled {
  cursor: wait;
  opacity: 0.7;
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
