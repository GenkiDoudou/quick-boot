<template>
  <div class="app-container ub-page">
    <div class="ub-filters">
      <el-input v-model="uin" clearable placeholder="uin / 用户名" style="width: 150px" @keyup.enter="applyFilter" />
      <el-input v-model="sessionId" clearable placeholder="sessionId（可选）" style="width: 190px" @keyup.enter="applyFilter" />
      <div class="time-field">
        <span class="time-label">时间</span>
        <el-select v-model="timePreset" size="default" style="width: 120px" @change="onTimePresetChange">
          <el-option label="5分钟内" value="5m"></el-option>
          <el-option label="10分钟内" value="10m"></el-option>
          <el-option label="30分钟内" value="30m"></el-option>
          <el-option label="1小时内" value="1h"></el-option>
          <el-option label="6小时内" value="6h"></el-option>
          <el-option label="自定义" value="custom"></el-option>
        </el-select>
        <el-date-picker
          v-if="timePreset === 'custom'"
          v-model="range"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 340px"
        />
        <span v-else class="time-range-text mono" :title="timeRangeLabel">{{ timeRangeLabel }}</span>
      </div>
      <el-button type="primary" :loading="loading" @click="applyFilter">查询</el-button>
      <span v-if="err" class="ub-err">{{ err }}</span>
    </div>

    <div class="ub-shell">
      <div class="ub-sessions">
        <div class="ub-sec">会话</div>
        <div v-if="!sessions.length && !loading" class="ub-empty">输入用户或会话后查询</div>
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="ub-sess"
          :class="{ on: cur && cur.sessionId === s.sessionId }"
          @click="openSession(s)"
        >
          <div class="mono">{{ shortId(s.sessionId) }}</div>
          <div class="meta">{{ s.uin || '—' }} · {{ s.eventCount || 0 }} 事件</div>
          <div class="meta">{{ shortPath(s.firstPage) }} → {{ shortPath(s.lastPage) }}</div>
        </div>
      </div>

      <div class="ub-main">
        <template v-if="!cur">
          <div class="ub-empty pad">选择左侧会话后查看行为轨迹</div>
        </template>
        <template v-else>
          <div class="path-banner">
            <span class="path-banner-label">路径</span>
            <span class="mono">{{ pathSummary }}</span>
          </div>
          <div v-if="timelineLoading" class="ub-empty compact pad-inline">加载轨迹…</div>
          <div v-else-if="!pages.length" class="ub-empty compact pad-inline">该会话暂无页面访问（需有 PV 事件）</div>
          <template v-else>
            <div class="trail-wrap" @wheel.prevent="onTrailWheel">
              <div class="trail-toolbar">
                <span class="h trail-title">行为轨迹</span>
                <span class="zoom-hint" title="鼠标悬停此区域时滚轮缩放">滚轮缩放 {{ trailZoom }}%</span>
              </div>
              <div class="trail-h" :style="{ '--ub-s': trailZoom / 100 }">
                <template v-for="(pg, i) in pages" :key="pg.key">
                  <div class="node" :class="{ on: pageKey === pg.key }" @click="selectPage(pg)">
                    <div class="ord">{{ i + 1 }}</div>
                    <div class="t">{{ pageTitle(pg.pagePath) }}</div>
                    <div class="p mono">{{ pg.pagePath || '—' }}</div>
                    <div class="cnt">{{ pg.opCount }} 个操作</div>
                  </div>
                  <div v-if="i < pages.length - 1" class="arrow">
                    →
                    <span>进入</span>
                  </div>
                </template>
              </div>
            </div>

            <div class="ub-split">
              <div class="ub-acts">
                <div class="h">本页操作 · {{ pageTitle(currentPage?.pagePath) }}</div>
                <div class="sub mono">{{ currentPage?.pagePath || '—' }}</div>
                <el-radio-group v-model="opFilter" size="small" class="op-filter">
                  <el-radio-button label="all">全部</el-radio-button>
                  <el-radio-button label="button">按钮</el-radio-button>
                  <el-radio-button label="api">接口</el-radio-button>
                </el-radio-group>
                <div v-if="!currentPage" class="ub-empty compact">请先点选轨迹上的页面</div>
                <div v-else-if="!filteredOpItems.length" class="ub-empty compact">当前筛选下暂无数据</div>
                <template v-for="item in filteredOpItems" :key="item.key">
                  <div
                    v-if="item.kind === 'action'"
                    class="act"
                    :class="{ on: actionId === item.node.eventId }"
                    @click="selectOpItem(item)"
                  >
                    <div class="name">
                      <span class="badge" :class="actionBadgeClass(item.node)">{{ actionBadge(item.node) }}</span>
                      {{ actionTitle(item.node) }}
                    </div>
                    <div class="meta">
                      {{ actionMeta(item.node) }}
                      <template v-if="item.apis.length"> · 触发 {{ item.apis.length }} 个接口</template>
                    </div>
                  </div>
                  <div
                    v-for="api in item.kind === 'action' ? item.apis : []"
                    :key="api.eventId"
                    class="act act-child"
                    :class="{ on: actionId === api.eventId }"
                    @click.stop="selectApiNode(api, item.node)"
                  >
                    <div class="name">
                      <span class="badge api">接口</span>
                      {{ actionTitle(api) }}
                    </div>
                    <div class="meta">{{ actionMeta(api) }}</div>
                  </div>
                  <div
                    v-if="item.kind === 'api'"
                    class="act"
                    :class="{ on: actionId === item.node.eventId }"
                    @click="selectOpItem(item)"
                  >
                    <div class="name">
                      <span class="badge api">接口</span>
                      {{ actionTitle(item.node) }}
                    </div>
                    <div class="meta">{{ actionMeta(item.node) }} · 未关联操作</div>
                  </div>
                </template>
              </div>

              <div class="ub-wf">
                <template v-if="currentAction">
                  <div class="h">操作触发的链路（对齐请求链路瀑布）</div>
                  <div class="sub">
                    「{{ actionTitle(currentAction) }}」· {{ currentAction.eventTime || '—' }}
                    <template v-if="resolvedTraceId"> · traceId {{ resolvedTraceId }}</template>
                  </div>
                  <div v-if="relatedApis.length" class="related">
                    <div class="related-title">关联接口</div>
                    <div
                      v-for="api in relatedApis"
                      :key="api.eventId"
                      class="related-row"
                      :class="{ on: actionId === api.eventId }"
                      @click="selectApiNode(api, currentRootAction || currentAction)"
                    >
                      <span class="mono">{{ actionTitle(api) }}</span>
                      <span class="meta">{{ api.durationMs != null ? `${api.durationMs}ms` : '' }}</span>
                    </div>
                  </div>
                  <div v-if="drillLoading" class="ub-empty compact">加载 Span…</div>
                  <div v-else-if="drillError" class="ub-err">{{ drillError }}</div>
                  <div v-else-if="!displaySpans.length" class="ub-empty compact">
                    未关联到链路（需有 traceId / operationId，且接口已采集）
                  </div>
                  <div v-else class="wf-layout">
                    <div class="wf-col">
                      <div class="sec">瀑布</div>
                      <div
                        v-for="s in displaySpans"
                        :key="s.id"
                        class="wf-row"
                        :class="{ on: selSpan && selSpan.id === s.id }"
                        @click="selSpan = s"
                      >
                        <div>
                          <div class="mono">{{ s.name }}</div>
                          <div class="kind">{{ s.kindLabel }}</div>
                        </div>
                        <div class="track">
                          <div class="bar" :class="s.bar" :style="spanBarStyle(s)" />
                        </div>
                        <div class="ms">{{ s.durationMs || 0 }}ms</div>
                      </div>
                      <el-button
                        v-if="resolvedTraceId"
                        type="primary"
                        link
                        size="small"
                        style="margin-top: 10px"
                        @click="goTrace(resolvedTraceId)"
                      >打开完整请求链路</el-button>
                    </div>
                    <div class="wf-detail">
                      <div class="sec">{{ selSpan ? `${selSpan.kindLabel || 'Span'}详情` : '详情' }}</div>
                      <LiteTraceSpanDetail :sel="selSpan" :trace="drillTraceCtx" />
                    </div>
                  </div>
                </template>
                <div v-else class="ub-empty pad">请选择左侧某个操作或接口</div>
              </div>
            </div>
          </template>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listLiteTraceIndex, listLiteTraceSpans } from '@/api/monitor/liteTrace'
import { getUserBehaviorTimeline, listUserBehaviorSessions } from '@/api/monitor/userBehavior'
import LiteTraceSpanDetail from '@/views/monitor/components/LiteTraceSpanDetail.vue'
import { mergeSpansForWaterfall, spanBarStyle } from '@/views/monitor/components/spanWaterfall'

/**
 * 用户行为轨迹：按用户/会话查询 PV 路径、操作分组，下钻 Lite Trace 瀑布图。
 */
defineOptions({ name: 'MonitorUserBehavior' })

const route = useRoute()
const router = useRouter()

const uin = ref('')
const sessionId = ref('')
const timePreset = ref('10m')
const range = ref([])
const sessions = ref([])
const cur = ref(null)
const nodes = ref([])
const loading = ref(false)
const timelineLoading = ref(false)
const err = ref('')

const pageKey = ref('')
const actionId = ref('')
const currentAction = ref(null)
const currentRootAction = ref(null)
const relatedApis = ref([])
const resolvedTraceId = ref('')
const drillLoading = ref(false)
const drillError = ref('')
const rawSpans = ref([])
const selSpan = ref(null)
const trailZoom = ref(100)
const opFilter = ref('all')

const pages = computed(() => buildPages(nodes.value))
const pathSummary = computed(() => pages.value.map((p) => p.pagePath || '—').join(' → ') || '—')
const currentPage = computed(() => pages.value.find((p) => p.key === pageKey.value) || null)
const pageOpGroups = computed(() => buildOpGroups(currentPage.value?.actions || []))
const filteredOpItems = computed(() => {
  const groups = pageOpGroups.value
  if (opFilter.value === 'button') {
    return groups.filter((g) => g.kind === 'action')
  }
  if (opFilter.value === 'api') {
    const apis = []
    for (const g of groups) {
      if (g.kind === 'api') {
        apis.push(g)
      } else {
        for (const api of g.apis) {
          apis.push({ key: `api-${api.eventId}`, kind: 'api', node: api, apis: [] })
        }
      }
    }
    return apis
  }
  return groups
})

const timeRangeLabel = computed(() => {
  const a = range.value?.[0]
  const b = range.value?.[1]
  if (!a || !b) return '—'
  return `${a} ~ ${b}`
})

const displaySpans = computed(() => mergeSpansForWaterfall(rawSpans.value))

const drillTraceCtx = computed(() => ({
  traceId: resolvedTraceId.value || '',
  operationId: currentAction.value?.operationId || currentRootAction.value?.operationId || '',
  pagePath: currentPage.value?.pagePath || currentAction.value?.pagePath || ''
}))

watch(
  () => pages.value.length,
  (n) => {
    trailZoom.value = autoTrailZoom(n)
  },
  { immediate: true }
)

function autoTrailZoom(n) {
  if (n <= 3) return 100
  if (n <= 5) return 90
  if (n <= 8) return 80
  if (n <= 12) return 70
  return 60
}

function onTrailWheel(ev) {
  const step = ev.deltaY > 0 ? -5 : 5
  const next = Math.min(150, Math.max(50, trailZoom.value + step))
  trailZoom.value = next
}

function formatNowRange(start, end) {
  const fmt = (d) => {
    const p = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
  }
  return [fmt(start), fmt(end)]
}

function rangeByPreset(preset) {
  const end = new Date()
  const start = new Date(end.getTime())
  switch (preset) {
    case '5m':
      start.setTime(end.getTime() - 5 * 60 * 1000)
      break
    case '10m':
      start.setTime(end.getTime() - 10 * 60 * 1000)
      break
    case '30m':
      start.setTime(end.getTime() - 30 * 60 * 1000)
      break
    case '1h':
      start.setTime(end.getTime() - 60 * 60 * 1000)
      break
    case '6h':
      start.setTime(end.getTime() - 6 * 60 * 60 * 1000)
      break
    default:
      start.setTime(end.getTime() - 10 * 60 * 1000)
  }
  return formatNowRange(start, end)
}

function defaultRange() {
  return rangeByPreset('10m')
}

function onTimePresetChange(val) {
  if (val === 'custom') return
  range.value = rangeByPreset(val)
  applyFilter()
}

function applyFilter() {
  if (timePreset.value !== 'custom') {
    range.value = rangeByPreset(timePreset.value)
  }
  loadSessions()
}

function shortPath(p) {
  if (!p) return '—'
  const parts = String(p).split('/').filter(Boolean)
  return parts.length ? parts[parts.length - 1] : String(p)
}

function pageTitle(p) {
  return shortPath(p)
}

function shortId(id) {
  if (!id) return '—'
  const s = String(id)
  return s.length > 16 ? `${s.slice(0, 8)}…${s.slice(-4)}` : s
}

function actionBadge(a) {
  if (!a) return '操作'
  if (a.eventType === 'api') return '接口'
  if (a.actionName === '页面加载') return '加载'
  if (!a.actionName) return '浏览'
  const n = String(a.actionName)
  if (/列表|list|load|加载/i.test(n)) return '列表'
  if (/新增|创建|add|create/i.test(n)) return '点击'
  if (/修改|编辑|edit|update/i.test(n)) return '点击'
  if (/删除|delete|remove/i.test(n)) return '点击'
  return '点击'
}

function actionBadgeClass(a) {
  if (!a) return {}
  if (a.eventType === 'api') return { api: true }
  if (a.actionName === '页面加载') return { load: true }
  if (!a.actionName) return { view: true }
  return {}
}

function actionTitle(a) {
  if (!a) return '—'
  if (a.eventType === 'api') {
    const method = a.apiMethod || 'GET'
    const url = a.apiUrl || a.actionName || '—'
    return `${method} ${url}`
  }
  if (!a.actionName) return '停留浏览'
  if (a.actionName === '页面加载') return '页面加载'
  const n = String(a.actionName)
  if (/^点击/.test(n) || /按钮/.test(n)) return n
  return `点击「${n}」`
}

function actionMeta(a) {
  if (!a) return '—'
  const time = a.eventTime || '—'
  if (a.eventType === 'api') {
    const parts = [time]
    if (a.statusCode != null && a.statusCode !== '') parts.push(`HTTP ${a.statusCode}`)
    if (a.durationMs != null) parts.push(`${a.durationMs}ms`)
    if (a.okFlag === '0') parts.push('失败')
    return parts.join(' · ')
  }
  return time
}

function buildOpGroups(list) {
  const groups = []
  /** @type {Map<string, any>} */
  const byOp = new Map()
  for (const n of list || []) {
    if (n.eventType === 'action') {
      const g = { key: `a-${n.eventId}`, kind: 'action', node: n, apis: [] }
      groups.push(g)
      if (n.operationId) byOp.set(String(n.operationId), g)
    } else if (n.eventType === 'api') {
      const opId = n.operationId ? String(n.operationId) : ''
      const g = opId ? byOp.get(opId) : null
      if (g) {
        g.apis.push(n)
      } else {
        groups.push({ key: `api-${n.eventId}`, kind: 'api', node: n, apis: [] })
      }
    }
  }
  return groups
}

function buildPages(list) {
  const out = []
  let curPage = null
  for (const n of list || []) {
    if (n.eventType === 'pv') {
      curPage = {
        key: `pv-${n.eventId}`,
        pagePath: n.pagePath || '未知页',
        fromPage: n.fromPage,
        time: n.eventTime,
        eventId: n.eventId,
        traceId: n.traceId,
        actions: []
      }
      out.push(curPage)
    } else if (n.eventType === 'action' || n.eventType === 'api') {
      if (!curPage) {
        curPage = {
          key: `orphan-${n.eventId}`,
          pagePath: n.pagePath || '未知页',
          fromPage: n.fromPage,
          time: n.eventTime,
          eventId: n.eventId,
          traceId: n.traceId,
          actions: []
        }
        out.push(curPage)
      }
      curPage.actions.push(n)
    }
  }
  for (const pg of out) {
    pg.opCount = buildOpGroups(pg.actions).length
  }
  return out
}

function clearDrill() {
  currentAction.value = null
  currentRootAction.value = null
  relatedApis.value = []
  actionId.value = ''
  resolvedTraceId.value = ''
  rawSpans.value = []
  selSpan.value = null
  drillError.value = ''
}

function selectPage(pg) {
  pageKey.value = pg.key
  clearDrill()
  const groups = buildOpGroups(pg.actions || [])
  const first = groups[0]
  if (first) selectOpItem(first)
}

function selectOpItem(item) {
  if (!item) return
  if (item.kind === 'action') {
    currentRootAction.value = item.node
    relatedApis.value = item.apis || []
    currentAction.value = item.node
    actionId.value = item.node.eventId
    const prefer = (item.apis || []).find((a) => a.traceId) || item.apis?.[0]
    if (prefer) {
      loadDrill(prefer)
    } else {
      loadDrill(item.node)
    }
  } else {
    currentRootAction.value = null
    relatedApis.value = []
    selectApiNode(item.node, null)
  }
}

function selectApiNode(api, rootAction) {
  currentAction.value = api
  actionId.value = api.eventId
  if (rootAction) {
    currentRootAction.value = rootAction
    const g = pageOpGroups.value.find((x) => x.kind === 'action' && x.node.eventId === rootAction.eventId)
    relatedApis.value = g ? g.apis : []
  } else if (api.operationId) {
    const g = pageOpGroups.value.find((x) => x.kind === 'action' && x.node.operationId === api.operationId)
    currentRootAction.value = g ? g.node : null
    relatedApis.value = g ? g.apis : []
  } else {
    currentRootAction.value = null
    relatedApis.value = []
  }
  loadDrill(api)
}

async function loadDrill(node) {
  resolvedTraceId.value = ''
  rawSpans.value = []
  selSpan.value = null
  drillError.value = ''
  drillLoading.value = true
  try {
    let traceId = node.traceId || ''
    if (!traceId && node.operationId) {
      const res = await listLiteTraceIndex({
        operationId: node.operationId,
        beginTime: range.value?.[0],
        endTime: range.value?.[1],
        pageNum: 1,
        pageSize: 20,
        sortKey: 'time',
        sortDir: 'desc'
      })
      const records = (res && res.data && res.data.records) || []
      if (node.eventType === 'api' && node.traceId) {
        traceId = node.traceId
      } else if (records.length) {
        const hit = records.find(
          (r) =>
            r.traceId &&
            (!node.apiUrl || String(r.entryName || '').includes(String(node.apiUrl).slice(0, 24)))
        )
        traceId = (hit || records[0]).traceId
      }
    }
    resolvedTraceId.value = traceId || ''
    if (!traceId) return
    const spanRes = await listLiteTraceSpans(traceId)
    rawSpans.value = (spanRes && spanRes.data) || []
    const merged = mergeSpansForWaterfall(rawSpans.value)
    selSpan.value = merged[0] || null
  } catch (e) {
    drillError.value = (e && e.message) || '加载链路失败'
  } finally {
    drillLoading.value = false
  }
}

async function loadSessions() {
  err.value = ''
  loading.value = true
  try {
    const res = await listUserBehaviorSessions({
      uin: uin.value.trim() || undefined,
      userName: uin.value.trim() || undefined,
      sessionId: sessionId.value.trim() || undefined,
      beginTime: range.value?.[0],
      endTime: range.value?.[1],
      limit: 50
    })
    sessions.value = (res && res.data) || []
    cur.value = null
    nodes.value = []
    pageKey.value = ''
    clearDrill()
  } catch (e) {
    err.value = (e && e.message) || '加载失败'
    sessions.value = []
  } finally {
    loading.value = false
  }
}

async function openSession(s) {
  cur.value = s
  nodes.value = []
  pageKey.value = ''
  clearDrill()
  timelineLoading.value = true
  try {
    const res = await getUserBehaviorTimeline({
      sessionId: s.sessionId,
      beginTime: range.value?.[0],
      endTime: range.value?.[1]
    })
    nodes.value = (res && res.data) || []
    if (pages.value.length) {
      selectPage(pages.value[0])
    }
  } catch (e) {
    err.value = (e && e.message) || '时间线加载失败'
  } finally {
    timelineLoading.value = false
  }
}

function goTrace(traceId) {
  if (!traceId) return
  router.push({ path: '/monitor/liteTrace', query: { traceId } })
}

onMounted(() => {
  range.value = defaultRange()
  if (route.query.uin) uin.value = String(route.query.uin)
  if (route.query.sessionId) sessionId.value = String(route.query.sessionId)
  if (uin.value || sessionId.value) applyFilter()
})
</script>

<style scoped>
.ub-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 84px);
  height: auto;
  overflow: visible;
  padding-bottom: 24px;
}
.ub-filters {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.time-field {
  display: flex;
  align-items: center;
  gap: 6px;
}
.time-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.time-range-text {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ub-err {
  color: #f56c6c;
  font-size: 12px;
}
.ub-shell {
  display: grid;
  grid-template-columns: 200px 1fr;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: visible;
  background: #fff;
  align-items: start;
}
.ub-sessions {
  position: sticky;
  top: 0;
  max-height: calc(100vh - 100px);
  overflow: auto;
  border-right: 1px solid var(--el-border-color);
  background: #fafafa;
  align-self: stretch;
}
.ub-sec {
  font-size: 12px;
  font-weight: 600;
  padding: 10px 12px 6px;
  color: var(--el-text-color-secondary);
}
.ub-sess {
  padding: 10px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  font-size: 12px;
}
.ub-sess.on,
.ub-sess:hover {
  background: #eff6ff;
}
.ub-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
  overflow: visible;
}
.path-banner {
  background: #fff;
  border-bottom: 1px solid var(--el-border-color);
  padding: 10px 14px;
  font-size: 13px;
  word-break: break-all;
}
.path-banner-label {
  display: inline-block;
  margin-right: 8px;
  padding: 1px 8px;
  border-radius: 3px;
  background: #2563eb;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.ub-split {
  display: grid;
  grid-template-columns: 340px 1fr;
  min-height: 360px;
}
.trail-wrap {
  background: #fff;
  border-bottom: 1px solid var(--el-border-color);
  padding: 12px 14px 14px;
  overscroll-behavior: contain;
}
.trail-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.trail-title {
  margin: 0;
}
.zoom-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.trail-h {
  display: flex;
  flex-wrap: wrap;
  align-items: stretch;
  gap: 0;
  --ub-s: 1;
}
.node {
  flex: 0 0 auto;
  width: calc(168px * var(--ub-s));
  border: 2px solid var(--el-border-color);
  border-radius: calc(10px * var(--ub-s));
  padding: calc(10px * var(--ub-s)) calc(12px * var(--ub-s));
  background: #fff;
  cursor: pointer;
  box-sizing: border-box;
}
.node:hover {
  border-color: #93c5fd;
}
.node.on {
  border-color: #2563eb;
  background: #eff6ff;
}
.arrow {
  flex: 0 0 calc(48px * var(--ub-s));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #2563eb;
  font-weight: 700;
  font-size: calc(18px * var(--ub-s));
  min-height: calc(72px * var(--ub-s));
}
.arrow span {
  font-size: calc(10px * var(--ub-s));
  color: var(--el-text-color-secondary);
  font-weight: 500;
  line-height: 1.2;
}
.ub-acts {
  background: #fff;
  border-right: 1px solid var(--el-border-color);
  padding: 14px;
}
.ub-wf {
  padding: 14px 16px;
  background: #fafafa;
  min-width: 0;
}
.wf-layout {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(280px, 1.1fr);
  gap: 12px;
  align-items: start;
}
.wf-col,
.wf-detail {
  min-width: 0;
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 10px 12px;
}
.sec {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 8px;
}
.op-filter {
  margin-bottom: 12px;
}
.h {
  font-size: 13px;
  font-weight: 700;
  margin: 0 0 8px;
}
.sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 10px;
  word-break: break-all;
}
.ord {
  width: calc(22px * var(--ub-s, 1));
  height: calc(22px * var(--ub-s, 1));
  border-radius: 50%;
  background: #2563eb;
  color: #fff;
  font-size: calc(12px * var(--ub-s, 1));
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-bottom: calc(6px * var(--ub-s, 1));
}
.t {
  font-weight: 700;
  font-size: calc(14px * var(--ub-s, 1));
}
.p {
  font-size: calc(11px * var(--ub-s, 1));
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}
.cnt {
  font-size: calc(11px * var(--ub-s, 1));
  color: #2563eb;
  margin-top: calc(6px * var(--ub-s, 1));
}
.act {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 8px;
  cursor: pointer;
  background: #fff;
}
.act-child {
  margin-left: 16px;
  padding: 8px 10px;
  background: #fafafa;
}
.act:hover,
.act.on {
  border-color: #2563eb;
  background: #eff6ff;
}
.act .name {
  font-weight: 650;
  font-size: 13px;
  word-break: break-all;
}
.act .meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.badge {
  display: inline-block;
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #fef3c7;
  color: #b45309;
  margin-right: 6px;
  vertical-align: middle;
}
.badge.view {
  background: #e0e7ff;
  color: #3730a3;
}
.badge.api {
  background: #ede9fe;
  color: #6d28d9;
}
.badge.load {
  background: #dcfce7;
  color: #15803d;
}
.related {
  margin-bottom: 12px;
  padding: 8px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: #fff;
}
.related-title {
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 6px;
}
.related-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 0;
  font-size: 12px;
  cursor: pointer;
}
.related-row:hover,
.related-row.on {
  color: #2563eb;
}
.wf-row {
  display: grid;
  grid-template-columns: minmax(100px, 40%) 1fr 56px;
  gap: 8px;
  align-items: center;
  padding: 6px 4px;
  font-size: 12px;
  border-radius: 4px;
  margin-bottom: 4px;
  cursor: pointer;
}
.wf-row:hover {
  background: #f3f4f6;
}
.wf-row.on {
  background: #eff6ff;
  outline: 1px solid #93c5fd;
}
.track {
  height: 16px;
  background: #e5e7eb;
  border-radius: 3px;
  position: relative;
}
.bar {
  position: absolute;
  top: 2px;
  height: 12px;
  border-radius: 2px;
  min-width: 3px;
}
.bar.api {
  background: #8b5cf6;
}
.bar.sql {
  background: #f59e0b;
}
.bar.page {
  background: #06b6d4;
}
.ms {
  text-align: right;
  color: var(--el-text-color-secondary);
  font-size: 11px;
}
.kind {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
.mono {
  font-family: ui-monospace, Consolas, monospace;
  word-break: break-all;
}
.meta {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.ub-empty {
  color: var(--el-text-color-secondary);
  padding: 16px;
  font-size: 13px;
}
.ub-empty.pad {
  padding: 40px;
}
.ub-empty.compact {
  padding: 8px 0;
  font-size: 12px;
}
.ub-empty.pad-inline {
  padding: 16px;
}
</style>
