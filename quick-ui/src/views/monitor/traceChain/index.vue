<template>
  <div class="app-container trace-chain-page">
    <el-card shadow="never" class="trace-chain-search">
      <el-form :model="searchForm" inline label-width="100px" @submit.prevent="handleSearch">
        <el-form-item label="operationId">
          <el-input v-model="searchForm.operationId" clearable placeholder="一次用户操作" style="width: 200px" />
        </el-form-item>
        <el-form-item label="traceId">
          <el-input v-model="searchForm.traceId" clearable placeholder="单次 HTTP" style="width: 180px" />
        </el-form-item>
        <el-form-item label="batchId">
          <el-input v-model="searchForm.batchId" clearable placeholder="批次 ID" style="width: 120px" />
        </el-form-item>
        <el-form-item label="pageVisitId">
          <el-input v-model="searchForm.pageVisitId" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="sessionId">
          <el-input v-model="searchForm.sessionId" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="browserVisitId">
          <el-input v-model="searchForm.browserVisitId" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.userName" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.createTimeRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" v-hasPermi="['monitor:traceChain:query']" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-if="summaryText" class="trace-chain-summary">
      <el-alert :title="summaryText" :type="summaryAlertType" show-icon :closable="false" />
    </div>

    <template v-if="graph">
      <dl class="trace-chain-ctx">
        <dt>用户</dt>
        <dd>{{ meta.userName || '—' }}</dd>
        <dt>页面</dt>
        <dd>{{ meta.pagePath || '—' }}</dd>
        <dt>菜单</dt>
        <dd>{{ meta.menuBreadcrumb || '—' }}</dd>
        <dt>操作</dt>
        <dd>{{ meta.triggerAction || '—' }}</dd>
        <dt>总耗时</dt>
        <dd>{{ meta.totalMs }}ms</dd>
        <dt>跳转</dt>
        <dd>{{ meta.jumpCount }} 步</dd>
        <dt>行为</dt>
        <dd>{{ meta.behaviorCount }} 条</dd>
        <dt>HTTP</dt>
        <dd>{{ meta.apiCount }} 个</dd>
      </dl>

      <el-card v-if="filters.includes('nav') && pageJumps.length" shadow="never" class="trace-chain-jump-card">
        <template #header><span class="trace-chain-section-title">页面跳转</span></template>
        <el-table :data="pageJumps" size="small" border stripe @row-click="onJumpClick">
          <el-table-column prop="step" label="步" width="48" align="center" />
          <el-table-column prop="fromLabel" label="来源" min-width="100" show-overflow-tooltip />
          <el-table-column prop="toLabel" label="目标" min-width="100" show-overflow-tooltip />
          <el-table-column prop="jumpLabel" label="说明" min-width="120" show-overflow-tooltip />
          <el-table-column prop="pageVisitId" label="pageVisitId" min-width="110" show-overflow-tooltip />
          <el-table-column prop="atLabel" label="时间" width="88" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="trace-chain-network-card">
        <div class="trace-chain-network-toolbar">
          <span class="trace-chain-section-title">资源时间线</span>
          <el-checkbox-group v-model="filters" size="small" @change="rebuildRows">
            <el-checkbox label="nav">跳转</el-checkbox>
            <el-checkbox label="behavior">行为</el-checkbox>
            <el-checkbox label="xhr">HTTP</el-checkbox>
            <el-checkbox label="log">日志</el-checkbox>
            <el-checkbox label="sql">慢SQL</el-checkbox>
            <el-checkbox label="slow">仅慢</el-checkbox>
          </el-checkbox-group>
          <div class="trace-chain-scale">
            <span>0ms</span>
            <div class="trace-chain-scale-ticks">
              <span>{{ tickLabel(0.25) }}</span>
              <span>{{ tickLabel(0.5) }}</span>
              <span>{{ tickLabel(0.75) }}</span>
              <span>{{ tickLabel(1) }}</span>
            </div>
          </div>
          <el-button size="small" @click="toggleExpandAll">{{ expandAll ? '全部折叠' : '全部展开' }}</el-button>
        </div>

        <div class="trace-chain-network-wrap">
          <div class="trace-chain-network-head">
            <span></span>
            <span>名称</span>
            <span>类型</span>
            <span>状态</span>
            <span>耗时</span>
            <span>瀑布</span>
            <span>操作</span>
          </div>
          <div v-if="showSectionNav" class="trace-chain-section-label">页面跳转 / 路由</div>
          <div v-if="showSectionBehavior" class="trace-chain-section-label">行为明细</div>
          <div v-if="showSectionBackend" class="trace-chain-section-label">后端落库</div>

          <div
            v-for="row in visibleRows"
            :key="row.key"
            class="trace-chain-network-row"
            :class="rowClass(row)"
            @click="onRowClick(row)"
          >
            <span class="trace-chain-expand-cell">
              <el-icon v-if="row.expandable">
                <ArrowDown v-if="isApiExpanded(row)" />
                <ArrowRight v-else />
              </el-icon>
            </span>
            <div class="trace-chain-name-cell">
              <span class="trace-chain-name">{{ row.name }}</span>
              <span v-if="row.sub" class="trace-chain-name-sub">{{ row.sub }}</span>
            </div>
            <span>
              <el-tag size="small" effect="plain" :type="row.typeTag">{{ row.typeText }}</el-tag>
            </span>
            <span>
              <el-tag v-if="row.statusText && row.statusText !== '—'" size="small" :type="row.statusTag" effect="light">
                {{ row.statusText }}
              </el-tag>
              <span v-else>—</span>
            </span>
            <span class="trace-chain-duration">{{ row.durationMs }}ms</span>
            <div class="trace-chain-waterfall">
              <div
                v-if="row.waterfall"
                class="trace-chain-waterfall-bar"
                :class="'trace-chain-waterfall-bar--' + row.wfKind"
                :style="{ left: row.waterfall.leftPct + '%', width: row.waterfall.widthPct + '%' }"
                :title="'+' + row.waterfall.startMs + 'ms'"
              />
            </div>
            <span>
              <el-button link type="primary" size="small" @click.stop="openDetail(row)">详情</el-button>
            </span>
          </div>
        </div>
      </el-card>
    </template>

    <el-empty v-else-if="!loading && searched" description="未找到链路数据" class="trace-chain-empty" />

    <el-drawer v-model="drawerVisible" title="节点详情" size="460px" destroy-on-close>
      <template v-if="detailRow">
        <el-tag size="small" :type="detailRow.statusTag">{{ detailRow.typeText }}</el-tag>
        <div class="trace-chain-drawer-title">{{ detailRow.name }}</div>
        <dl class="trace-chain-detail-dl">
          <template v-for="(item, idx) in detailFields" :key="idx">
            <dt>{{ item.label }}</dt>
            <dd>
              {{ item.value }}
              <el-button v-if="item.copy" link type="primary" size="small" @click="copyText(item.value)">复制</el-button>
            </dd>
          </template>
        </dl>
        <div class="trace-chain-drawer-actions">
          <el-button
            v-if="canOperLog(detailRow)"
            size="small"
            @click="openOperLog(detailRow)"
          >
            操作日志
          </el-button>
          <el-button
            v-if="canSlowSql(detailRow)"
            size="small"
            @click="openSlowSql(detailRow)"
          >
            慢 SQL
          </el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="operLogDetailVisible" title="操作日志详情" width="880px" destroy-on-close append-to-body>
      <OperLogDetailPanel v-if="operLogDetailRow" :row="operLogDetailRow" />
    </el-dialog>

    <el-dialog v-model="slowSqlVisible" title="慢 SQL 详情" width="720px" destroy-on-close append-to-body>
      <el-descriptions v-if="slowSqlDetailRow" :column="1" border size="small">
        <el-descriptions-item label="traceId">{{ slowSqlDetailRow.traceId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作ID">{{ slowSqlDetailRow.clientOperationId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ slowSqlDetailRow.sqlSource }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ slowSqlDetailRow.costTime }}</el-descriptions-item>
        <el-descriptions-item label="Mapper">{{ slowSqlDetailRow.mapperId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="SQL">
          <pre class="trace-chain-sql-pre">{{ slowSqlDetailRow.sqlText }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import { getTraceChainGraph } from '@/api/monitor/traceChain'
import { getSlowSql } from '@/api/monitor/slowSql'
import OperLogDetailPanel from '@/views/monitor/operlog/OperLogDetailPanel.vue'
import { useOperLogDetail } from '@/views/monitor/operlog/useOperLogDetail'
import { buildNetworkRows, filterVisibleRows } from './useTraceChainNetwork'

/**
 * 全链路监控（Network 视图，对齐原型 v3）。
 */
defineOptions({ name: 'SysTraceChain' })

const route = useRoute()
const loading = ref(false)
const searched = ref(false)
/** @type {import('vue').Ref<Record<string, unknown>|null>} */
const graph = ref(null)
const expandAll = ref(true)
/** @type {import('vue').Ref<Record<string, boolean>>} */
const expandedApis = ref({})
const selectedKey = ref('')
/** @type {import('vue').Ref<Record<string, unknown>|null>} */
const detailRow = ref(null)
const drawerVisible = ref(false)
const slowSqlVisible = ref(false)
/** @type {import('vue').Ref<Record<string, unknown>|null>} */
const slowSqlDetailRow = ref(null)

const filters = ref(['nav', 'behavior', 'xhr', 'log', 'sql'])
const flatRows = ref([])
const visibleRows = ref([])

const {
  operLogDetailVisible,
  operLogDetailRow,
  operLogDetailLoading,
  openOperLogByTraceId,
  openOperLogByOperId,
} = useOperLogDetail()

const searchForm = reactive({
  operationId: '',
  traceId: '',
  batchId: '',
  pageVisitId: '',
  sessionId: '',
  browserVisitId: '',
  userName: '',
  createTimeRange: [],
})

const summary = computed(() => graph.value?.summary || null)

const summaryText = computed(() => {
  const s = summary.value
  if (!s) return ''
  const st = s.status === 'ok' ? '正常' : s.status === 'error' ? '异常' : '含慢路径'
  return `${s.userName || '—'} · ${s.menuBreadcrumb || s.pagePath || '—'} · ${st} · operationId: ${s.operationId || '—'}`
})

const summaryAlertType = computed(() => {
  const st = summary.value?.status
  if (st === 'error') return 'error'
  if (st === 'warn') return 'warning'
  return 'success'
})

const meta = computed(() => ({
  userName: summary.value?.userName,
  pagePath: summary.value?.pagePath,
  menuBreadcrumb: summary.value?.menuBreadcrumb,
  triggerAction: summary.value?.triggerAction,
  totalMs: graph.value?.timelineMaxMs ?? 0,
  apiCount: summary.value?.apiCount ?? 0,
  jumpCount: summary.value?.pageJumpCount ?? 0,
  behaviorCount: summary.value?.behaviorEventCount ?? 0,
}))

const pageJumps = computed(() => graph.value?.pageJumps || [])

const showSectionNav = computed(() => visibleRows.value.some((r) => r.section === 'nav'))
const showSectionBehavior = computed(() => visibleRows.value.some((r) => r.section === 'behavior'))
const showSectionBackend = computed(() => visibleRows.value.some((r) => r.section === 'backend'))

const detailFields = computed(() => {
  const row = detailRow.value
  const node = row?.node
  if (!node) return []
  const items = [
    { label: '相对时间', value: `+${node.startMs ?? 0}ms ~ +${node.endMs ?? 0}ms` },
    { label: '耗时', value: `${(node.endMs ?? 0) - (node.startMs ?? 0)}ms` },
  ]
  if (node.pageVisitId) items.push({ label: 'pageVisitId', value: node.pageVisitId, copy: true })
  if (node.operationId) items.push({ label: 'operationId', value: node.operationId, copy: true })
  if (node.traceId) items.push({ label: 'traceId', value: node.traceId, copy: true })
  if (node.batchId) items.push({ label: 'batchId', value: String(node.batchId) })
  if (node.passive) items.push({ label: '备注', value: '被动操作' })
  return items
})

/**
 * @param {number} ratio
 */
function tickLabel(ratio) {
  const max = graph.value?.timelineMaxMs ?? 0
  return '+' + Math.round(max * ratio) + 'ms'
}

function rebuildRows() {
  flatRows.value = buildNetworkRows(graph.value, filters.value)
  visibleRows.value = filterVisibleRows(flatRows.value, expandAll.value, expandedApis.value)
}

/**
 * @param {Record<string, unknown>} row
 */
function rowClass(row) {
  return {
    'trace-chain-network-row--selected': selectedKey.value === row.key,
    'trace-chain-network-row--child': row.isChild,
    'trace-chain-network-row--jump': row.kind === 'nav',
    'trace-chain-network-row--behavior': row.kind === 'behavior',
  }
}

/**
 * @param {Record<string, unknown>} row
 */
function isApiExpanded(row) {
  if (expandAll.value) return true
  const apiId = row.id
  return !!expandedApis.value[apiId]
}

/**
 * @param {Record<string, unknown>} row
 */
function onRowClick(row) {
  if (row.expandable && !expandAll.value) {
    const id = String(row.id)
    expandedApis.value = { ...expandedApis.value, [id]: !expandedApis.value[id] }
    rebuildRows()
    return
  }
  selectedKey.value = row.key
  detailRow.value = row
}

/**
 * @param {Record<string, unknown>} row
 */
function openDetail(row) {
  detailRow.value = row
  drawerVisible.value = true
}

/**
 * @param {Record<string, unknown>} jump
 */
function onJumpClick(jump) {
  if (jump.pageVisitId) {
    searchForm.pageVisitId = String(jump.pageVisitId)
    handleSearch()
  }
}

function toggleExpandAll() {
  expandAll.value = !expandAll.value
  rebuildRows()
}

/**
 * @param {Record<string, unknown>} row
 */
function canOperLog(row) {
  const t = row.node?.type
  return t === 'api' || t === 'api_call' || t === 'oper_log' || row.kind === 'api' || row.kind === 'log'
}

/**
 * @param {Record<string, unknown>} row
 */
function canSlowSql(row) {
  const t = row.node?.type
  return t === 'api' || t === 'api_call' || t === 'slow_sql' || row.kind === 'api' || row.kind === 'sql'
}

/**
 * @param {Record<string, unknown>} row
 */
async function openOperLog(row) {
  const node = row.node || {}
  if (node.operId) {
    operLogDetailLoading.value = true
    try {
      await openOperLogByOperId(node.operId)
    } finally {
      operLogDetailLoading.value = false
    }
    return
  }
  if (node.traceId) {
    await openOperLogByTraceId(String(node.traceId))
  }
}

/**
 * @param {Record<string, unknown>} row
 */
async function openSlowSql(row) {
  const node = row.node || {}
  if (node.slowId) {
    try {
      const res = await getSlowSql(node.slowId)
      slowSqlDetailRow.value = res?.data ?? res
      slowSqlVisible.value = true
    } catch {
      ElMessage.error('加载慢 SQL 失败')
    }
    return
  }
  ElMessage.info('请从慢 SQL 列表按 traceId 查看')
}

/**
 * @param {string} text
 */
function copyText(text) {
  navigator.clipboard.writeText(String(text)).then(() => ElMessage.success('已复制'))
}

/**
 * @returns {Record<string, unknown>}
 */
function buildQueryParams() {
  /** @type {Record<string, unknown>} */
  const p = {}
  if (searchForm.operationId) p.operationId = searchForm.operationId.trim()
  if (searchForm.traceId) p.traceId = searchForm.traceId.trim()
  if (searchForm.batchId) p.batchId = Number(searchForm.batchId)
  if (searchForm.pageVisitId) p.pageVisitId = searchForm.pageVisitId.trim()
  if (searchForm.sessionId) p.sessionId = searchForm.sessionId.trim()
  if (searchForm.browserVisitId) p.browserVisitId = searchForm.browserVisitId.trim()
  if (searchForm.userName) p.userName = searchForm.userName.trim()
  const range = searchForm.createTimeRange
  if (Array.isArray(range) && range.length === 2) {
    p.beginDate = range[0]
    p.endDate = range[1]
  }
  return p
}

async function handleSearch() {
  const p = buildQueryParams()
  if (
    !p.operationId &&
    !p.traceId &&
    !p.batchId &&
    !p.pageVisitId &&
    !p.browserVisitId &&
    !p.sessionId &&
    !p.userName
  ) {
    ElMessage.warning('请至少填写 operationId、traceId、batchId、pageVisitId 或会话维度条件')
    return
  }
  loading.value = true
  searched.value = true
  try {
    const res = await getTraceChainGraph(p)
    graph.value = res?.data ?? res
    expandAll.value = true
    expandedApis.value = {}
    rebuildRows()
    if (graph.value?.truncated) {
      ElMessage.warning((graph.value.warnings || []).join('；') || '数据已截断')
    }
  } catch {
    graph.value = null
    flatRows.value = []
    visibleRows.value = []
  } finally {
    loading.value = false
  }
}

function handleReset() {
  Object.assign(searchForm, {
    operationId: '',
    traceId: '',
    batchId: '',
    pageVisitId: '',
    sessionId: '',
    browserVisitId: '',
    userName: '',
    createTimeRange: [],
  })
  graph.value = null
  flatRows.value = []
  visibleRows.value = []
  searched.value = false
}

function applyRouteQuery() {
  const q = route.query
  if (q.operationId) searchForm.operationId = String(q.operationId)
  if (q.traceId) searchForm.traceId = String(q.traceId)
  if (q.batchId) searchForm.batchId = String(q.batchId)
  if (q.pageVisitId) searchForm.pageVisitId = String(q.pageVisitId)
  if (q.clientOperationId) searchForm.operationId = String(q.clientOperationId)
  if (q.operationId || q.traceId || q.batchId || q.pageVisitId || q.clientOperationId) {
    handleSearch()
  }
}

onMounted(() => {
  applyRouteQuery()
})
</script>

<style scoped lang="scss">
.trace-chain-page {
  .trace-chain-search {
    margin-bottom: 12px;
  }
  .trace-chain-summary {
    margin-bottom: 12px;
  }
  .trace-chain-ctx {
    display: flex;
    flex-wrap: wrap;
    gap: 12px 20px;
    padding: 10px 14px;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
    margin-bottom: 12px;
    font-size: 12px;
    dt {
      color: var(--el-text-color-secondary);
      display: inline;
    }
    dd {
      display: inline;
      margin: 0 8px 0 4px;
      font-weight: 500;
    }
  }
  .trace-chain-section-title {
    font-size: 13px;
    font-weight: 600;
  }
  .trace-chain-jump-card {
    margin-bottom: 12px;
  }
  .trace-chain-network-toolbar {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 10px;
  }
  .trace-chain-scale {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 11px;
    color: var(--el-text-color-secondary);
    margin-left: auto;
  }
  .trace-chain-scale-ticks {
    display: flex;
    width: 200px;
    justify-content: space-between;
    border-bottom: 1px solid var(--el-border-color);
  }
  .trace-chain-network-wrap {
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 4px;
    overflow: hidden;
  }
  .trace-chain-network-head,
  .trace-chain-network-row {
    display: grid;
    grid-template-columns: 28px minmax(180px, 1.4fr) 72px 56px 72px minmax(160px, 1fr) 64px;
    gap: 8px;
    align-items: center;
    padding: 8px 12px;
    font-size: 13px;
  }
  .trace-chain-network-head {
    background: var(--el-fill-color-light);
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-secondary);
  }
  .trace-chain-network-row {
    border-top: 1px solid var(--el-border-color-extra-light);
    cursor: pointer;
    &:hover {
      background: var(--el-color-primary-light-9);
    }
    &--selected {
      background: var(--el-color-primary-light-8);
    }
    &--child {
      background: var(--el-fill-color-lighter);
      padding-left: 20px;
    }
    &--jump {
      background: #f9f0ff;
    }
    &--behavior {
      background: #fff;
    }
  }
  .trace-chain-section-label {
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-secondary);
    background: var(--el-fill-color-light);
    border-top: 1px solid var(--el-border-color-extra-light);
  }
  .trace-chain-name-cell {
    min-width: 0;
  }
  .trace-chain-name {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-weight: 500;
  }
  .trace-chain-name-sub {
    font-size: 11px;
    color: var(--el-text-color-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .trace-chain-duration {
    font-variant-numeric: tabular-nums;
  }
  .trace-chain-waterfall {
    position: relative;
    height: 18px;
    background: var(--el-fill-color);
    border-radius: 2px;
    overflow: hidden;
  }
  .trace-chain-waterfall-bar {
    position: absolute;
    top: 2px;
    height: 14px;
    border-radius: 2px;
    min-width: 3px;
    &--ok {
      background: linear-gradient(90deg, #79bbff, #409eff);
    }
    &--warn {
      background: linear-gradient(90deg, #f3d19e, #e6a23c);
    }
    &--err {
      background: linear-gradient(90deg, #fab6b6, #f56c6c);
    }
    &--log {
      background: linear-gradient(90deg, #b3e19d, #67c23a);
    }
    &--sql {
      background: linear-gradient(90deg, #d3adf7, #9254de);
    }
    &--nav {
      background: linear-gradient(90deg, #d3d4d6, #909399);
    }
    &--behavior {
      background: linear-gradient(90deg, #b3e5fc, #03a9f4);
    }
  }
  .trace-chain-detail-dl {
    dt {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-top: 10px;
    }
    dd {
      margin: 4px 0 0;
      word-break: break-all;
    }
  }
  .trace-chain-drawer-title {
    font-weight: 600;
    margin: 8px 0 12px;
  }
  .trace-chain-drawer-actions {
    margin-top: 12px;
    display: flex;
    gap: 8px;
  }
  .trace-chain-sql-pre {
    margin: 0;
    max-height: 200px;
    overflow: auto;
    font-size: 12px;
    white-space: pre-wrap;
  }
  .trace-chain-empty {
    margin-top: 48px;
  }
}
</style>
