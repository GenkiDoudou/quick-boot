<template>
  <div class="app-container">
    <div class="client-track-events-toolbar">
      <el-button type="success" plain v-hasPermi="['monitor:clientTrack:list']" @click="goTimelinePage()">
        行为轨迹
      </el-button>
    </div>
    <EventChainPanel ref="eventChainRef" :initial-search="routeInitialSearch" @view-batch="openDetailByBatchId" />
    <BatchDetailDialog
      v-model="detailVisible"
      :row="detailRow"
      @filter-session="filterBySession"
      @view-event-chain="applyRowSearch"
      @view-timeline="goTimelinePage"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listClientTrack } from '@/api/monitor/clientTrack'
import EventChainPanel from './EventChainPanel.vue'
import BatchDetailDialog from './BatchDetailDialog.vue'
import { defaultSearchParam } from './clientTrackEvent'

/**
 * 前端监控事件链路明细页：扁平展示所有 click / API / 路由事件，支持多维度搜索。
 */
defineOptions({ name: 'SysClientTrackEvents' })

const route = useRoute()
const router = useRouter()
const eventChainRef = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

/** 从路由 query 带入初始搜索（批次页跳转时可带 sessionId 等） */
const routeInitialSearch = computed(() => {
  const q = route.query
  /** @type {Record<string, string>} */
  const partial = { ...defaultSearchParam }
  ;['sessionId', 'pageVisitId', 'operationId', 'traceId', 'userName', 'menuName', 'pagePath', 'triggerAction', 'reason', 'eventType', 'browserVisitId'].forEach((key) => {
    const val = q[key]
    if (val != null && String(val).trim() !== '') {
      partial[key] = String(val)
    }
  })
  return partial
})

/**
 * @param {number|string} batchId
 */
function openDetailByBatchId(batchId) {
  listClientTrack({ pageNum: 1, pageSize: 1, batchId })
    .then((res) => {
      const row = res.data?.records?.[0]
      if (row) {
        detailRow.value = row
        detailVisible.value = true
      } else {
        ElMessage.warning('未找到对应批次')
      }
    })
}

/**
 * @param {string} sessionId
 */
function filterBySession(sessionId) {
  if (!sessionId) return
  detailVisible.value = false
  eventChainRef.value?.applySearch?.({ sessionId, pageVisitId: '' })
}

/**
 * @param {Record<string, unknown>} row
 */
function applyRowSearch(row) {
  if (!row) return
  detailVisible.value = false
  eventChainRef.value?.applySearch?.({
    sessionId: row.sessionId || '',
    pageVisitId: row.pageVisitId || ''
  })
}

/**
 * 跳转行为轨迹页；可从详情行带入 browserVisitId / sessionId / userName。
 * @param {Record<string, unknown>|undefined} row
 */
function goTimelinePage(row) {
  /** @type {Record<string, string>} */
  const query = {}
  const src = row || eventChainRef.value?.searchParam || routeInitialSearch.value
  if (src?.browserVisitId) query.browserVisitId = String(src.browserVisitId)
  if (src?.sessionId) query.sessionId = String(src.sessionId)
  if (src?.userName) query.userName = String(src.userName)
  detailVisible.value = false
  router.push({ path: '/system/clientTrackTimeline', query })
}
</script>

<style scoped>
.client-track-events-toolbar {
  margin-bottom: 12px;
}
</style>
