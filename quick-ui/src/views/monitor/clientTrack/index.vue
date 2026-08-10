<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="batchId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="batchSearchColumns"
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
        <el-button type="primary" plain v-hasPermi="['monitor:clientTrack:list']" @click="goEventChainPage()">
          事件链路
        </el-button>
        <el-button type="success" plain v-hasPermi="['monitor:clientTrack:list']" @click="goTimelinePage()">
          行为轨迹
        </el-button>
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
        <el-button link type="primary" v-hasPermi="['monitor:clientTrack:list']" @click="goEventChainPage(scope.row)">
          事件
        </el-button>
        <el-button link type="success" v-hasPermi="['monitor:clientTrack:list']" @click="goTimelinePage(scope.row)">
          轨迹
        </el-button>
      </template>
    </C7JsonTable>

    <BatchDetailDialog
      v-model="detailVisible"
      :row="detailRow"
      @filter-session="filterBySession"
      @view-event-chain="goEventChainPage"
      @view-timeline="goTimelinePage"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseTime } from '@/utils/ruoyi'
import { cleanClientTrack, listClientTrack, removeClientTrack } from '@/api/monitor/clientTrack'
import { formatTrackLabel, resolveBatchTriggerAction } from '@/monitor/display'
import BatchDetailDialog from './BatchDetailDialog.vue'
import { defaultSearchParam, normalizeListParams, reasonLabel, reasonTagType, searchColumns } from './clientTrackEvent'

/**
 * 前端用户行为监控批次列表，支持 sessionId/菜单/各 ID 筛选；事件明细见「事件链路」菜单页。
 */
defineOptions({ name: 'SysClientTrack' })

const router = useRouter()
const tableRef = ref(null)
const detailVisible = ref(false)
const detailRow = ref(null)

const batchSearchColumns = computed(() => searchColumns)

const tableColumns = computed(() => [
  { prop: 'batchId', label: '批次编号', width: 120 },
  { prop: 'triggerAction', label: '触发操作', columnType: 'slot', slotName: 'triggerAction', width: 120, showOverflowTooltip: true },
  { prop: 'sessionId', label: 'sessionId', minWidth: 140, showOverflowTooltip: true },
  { prop: 'browserVisitId', label: 'browserVisitId', minWidth: 140, showOverflowTooltip: true },
  { prop: 'pageVisitId', label: 'pageVisitId', minWidth: 140, showOverflowTooltip: true },
  { prop: 'operationId', label: 'operationId', minWidth: 140, showOverflowTooltip: true },
  { prop: 'traceId', label: 'serverTraceId', minWidth: 140, showOverflowTooltip: true },
  { prop: 'userName', label: '用户名', width: 120, showOverflowTooltip: true },
  { prop: 'reason', label: '上报原因', columnType: 'slot', slotName: 'reason', width: 100 },
  { prop: 'menuName', label: '所属菜单', columnType: 'slot', slotName: 'pageMenu', minWidth: 160, showOverflowTooltip: true },
  { prop: 'pagePath', label: '页面路径', minWidth: 160, showOverflowTooltip: true },
  { prop: 'clientIp', label: 'IP', width: 130, showOverflowTooltip: true },
  { prop: 'createTime', label: '入库时间', columnType: 'slot', slotName: 'createTime', width: 180 },
  { prop: 'actions', label: '操作', columnType: 'slot', slotName: 'actions', width: 130, fixed: 'right' }
])

/**
 * @param {Record<string, unknown>} row
 */
function formatTriggerCell(row) {
  const raw = row.triggerAction
  if (raw) return formatTrackLabel(String(raw))
  if (row.eventsJson) {
    try {
      return resolveBatchTriggerAction(JSON.parse(String(row.eventsJson))).label || '—'
    } catch {
      return '—'
    }
  }
  return '—'
}

/**
 * @param {string|number|Date|null|undefined} value
 */
function formatTime(value) {
  if (value == null || value === '') return '—'
  return parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}') || String(value)
}

function listFunction(params) {
  return listClientTrack(normalizeListParams(params))
}

function batchDeleteFunction(ids) {
  return removeClientTrack(ids || [])
}

/**
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
 * @param {string} sessionId
 */
function filterBySession(sessionId) {
  if (!sessionId || !tableRef.value) return
  detailVisible.value = false
  tableRef.value.searchParam.sessionId = sessionId
  tableRef.value.searchParam.pageVisitId = ''
  tableRef.value.refreshData()
}

/**
 * @param {Record<string, unknown>} row
 */
function openDetail(row) {
  detailRow.value = row ? { ...row } : null
  detailVisible.value = true
}

/**
 * 跳转事件链路页，可携带 sessionId / pageVisitId 等 query。
 * @param {Record<string, unknown>|undefined} row
 */
function goEventChainPage(row) {
  /** @type {Record<string, string>} */
  const query = {}
  if (row?.sessionId) query.sessionId = String(row.sessionId)
  if (row?.pageVisitId) query.pageVisitId = String(row.pageVisitId)
  if (row?.operationId) query.operationId = String(row.operationId)
  if (row?.userName) query.userName = String(row.userName)
  router.push({ path: '/system/clientTrackEvents', query })
}

/**
 * 跳转行为轨迹页，可携带 browserVisitId / sessionId / userName 等 query。
 * @param {Record<string, unknown>|undefined} row
 */
function goTimelinePage(row) {
  /** @type {Record<string, string>} */
  const query = {}
  if (row?.browserVisitId) query.browserVisitId = String(row.browserVisitId)
  if (row?.sessionId) query.sessionId = String(row.sessionId)
  if (row?.userName) query.userName = String(row.userName)
  router.push({ path: '/system/clientTrackTimeline', query })
}
</script>

<style scoped>
.client-track-cell-text {
  -webkit-user-select: text;
  user-select: text;
}

.client-track-cell-muted {
  color: var(--el-text-color-secondary);
}
</style>
