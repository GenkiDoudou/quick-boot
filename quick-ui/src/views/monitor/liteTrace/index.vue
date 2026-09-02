<template>
  <div class="app-container lt-page" :class="{ 'is-fs': isFullscreen }">
    <div ref="ltRootRef" class="lt-root">
      <div class="lt-bar">
        <div class="lt-bar-row">
          <strong class="lt-title">请求链路</strong>
          <div class="search-wrap">
            <el-select v-model="searchType" size="small" class="search-type">
              <el-option label="全部" value="all" />
              <el-option label="页面路径" value="page" />
              <el-option label="接口 URL" value="url" />
              <el-option label="traceId" value="traceId" />
              <el-option label="sessionId" value="sessionId" />
              <el-option label="pageVisitId" value="pageVisitId" />
              <el-option label="关键字" value="keyword" />
            </el-select>
            <el-input
              v-model="keyword"
              size="small"
              clearable
              class="search-input"
              :placeholder="searchPlaceholder"
              @keyup.enter="applyFilter"
            />
          </div>
          <el-button size="small" type="primary" :loading="loading" @click="applyFilter">查询</el-button>
          <el-button size="small" @click="resetFilter">重置</el-button>
          <el-button size="small" class="fs-btn" @click="toggleFullscreen">
            {{ isFullscreen ? '退出全屏' : '全屏' }}
          </el-button>
          <span v-if="loadError" class="lt-err">{{ loadError }}</span>
        </div>
        <div class="lt-bar-row">
          <div class="field time-field">
            <label>时间</label>
            <el-select v-model="timePreset" size="small" style="width: 120px" @change="onTimePresetChange">
              <el-option label="5分钟内" value="5m" />
              <el-option label="10分钟内" value="10m" />
              <el-option label="30分钟内" value="30m" />
              <el-option label="1小时内" value="1h" />
              <el-option label="6小时内" value="6h" />
              <el-option label="今日" value="today" />
              <el-option label="自定义" value="custom" />
            </el-select>
            <el-date-picker
              v-if="timePreset === 'custom'"
              v-model="timeRange"
              type="datetimerange"
              size="small"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 340px"
            />
            <span v-else class="time-range-text mono" :title="timeRangeLabel">{{ timeRangeLabel }}</span>
          </div>
          <div class="field">
            <label>用户名</label>
            <el-input
              v-model="uin"
              size="small"
              clearable
              placeholder="uin / 用户名"
              style="width: 130px"
              @keyup.enter="applyFilter"
            />
          </div>
          <div class="field">
            <label>客户端</label>
            <el-input
              v-model="callerName"
              size="small"
              clearable
              placeholder="精确匹配，如 quick-ui"
              style="width: 150px"
              @keyup.enter="applyFilter"
            />
          </div>
          <div class="field">
            <label>结果</label>
            <el-select v-model="okFlag" size="small" clearable placeholder="全部" style="width: 100px">
              <el-option label="成功" value="1" />
              <el-option label="失败" value="0" />
            </el-select>
          </div>
        </div>
      </div>

      <div class="lt-hint">
        搜索类型下拉限定字段；左侧「页面访问」只显示页面信息（不含接口 URL）。
        点页面 → <b>先页面介绍 + traceId 列表</b>，再点某条 trace 看瀑布。
      </div>

      <div class="lt-body">
        <div class="lt-left">
          <div class="lt-filters">
            <span class="lt-chip" :class="{ on: scope === 'all' }" @click="scope = 'all'">全部</span>
            <span class="lt-chip" :class="{ on: scope === 'page' }" @click="scope = 'page'">页面访问</span>
            <span class="lt-chip" :class="{ on: scope === 'api' }" @click="scope = 'api'">接口</span>
            <span class="lt-chip" :class="{ on: scope === 'job' }" @click="scope = 'job'">定时任务</span>
          </div>
          <div class="sort-row">
            <span class="sort-label">排序</span>
            <el-select v-model="sortKey" size="small" style="width: 120px">
              <el-option label="时间" value="time" />
              <el-option label="耗时" value="duration" />
              <el-option label="名称" value="name" />
            </el-select>
            <el-select v-model="sortDir" size="small" style="width: 100px">
              <el-option label="降序" value="desc" />
              <el-option label="升序" value="asc" />
            </el-select>
          </div>
          <div class="count">
            页 {{ showPages ? sortedVisits.length : 0 }} ·
            接口 {{ showApis ? sortedApis.length : 0 }} ·
            任务 {{ showJobs ? sortedJobs.length : 0 }}
          </div>

          <template v-if="showPages">
            <div class="sec-h">页面访问</div>
            <div v-if="loading && !visits.length" class="lt-empty compact">加载中…</div>
            <div v-else-if="!sortedVisits.length" class="lt-empty compact">无匹配页面访问</div>
            <div
              v-for="pv in sortedVisits"
              :key="'pv-' + pv.pageVisitId"
              class="lt-item"
              :class="{ active: mode === 'page' && curVisit && curVisit.pageVisitId === pv.pageVisitId }"
              @click="pickVisit(pv)"
            >
              <div>
                <span class="copy-field">
                  <span class="mono">{{ pv.pagePath }}</span>
                  <c7-copy v-if="canCopy(pv.pagePath)" mode="icon" size="small" class="hover-copy" :text="pv.pagePath" @click.stop />
                </span>
                <span class="badge page">页</span>
                <span class="badge" :class="pv.okFlag === '1' ? 'ok' : 'bad'">{{ pv.okFlag === '1' ? 'OK' : 'ERR' }}</span>
              </div>
              <div v-if="pv.fromPage" class="meta">
                来自
                <span class="copy-field">
                  <span class="mono">{{ pv.fromPage }}</span>
                  <c7-copy v-if="canCopy(pv.fromPage)" mode="icon" size="small" class="hover-copy" :text="pv.fromPage" @click.stop />
                </span>
              </div>
              <div class="meta">{{ pv.traceCount || 0 }} 条链路 · {{ pv.durationMs || 0 }}ms</div>
              <div class="meta">
                用户
                <span class="copy-field">
                  <span>{{ pv.uin || '—' }}</span>
                  <c7-copy v-if="canCopy(pv.uin)" mode="icon" size="small" class="hover-copy" :text="pv.uin" @click.stop />
                </span>
                · 客户端 {{ pv.callerName || '—' }}
                · {{ formatDateTime(pv.endedAt || pv.startedAt) }}
              </div>
              <div class="meta">
                session
                <span class="copy-field">
                  <span class="mono" :title="pv.sessionId">{{ shortId(pv.sessionId) }}</span>
                  <c7-copy v-if="canCopy(pv.sessionId)" mode="icon" size="small" class="hover-copy" :text="pv.sessionId" @click.stop />
                </span>
              </div>
            </div>
          </template>

          <template v-if="showApis">
            <div class="sec-h">接口（页面内 + 纯 API）</div>
            <div v-if="loading && !apiItems.length" class="lt-empty compact">加载中…</div>
            <div v-else-if="!sortedApis.length" class="lt-empty compact">无匹配接口</div>
            <div
              v-for="t in sortedApis"
              :key="'api-' + t.traceId"
              class="lt-item"
              :class="{ active: mode === 'api' && curTrace && curTrace.traceId === t.traceId }"
              @click="pickApi(t)"
            >
              <div>
                <span class="copy-field">
                  <span class="mono">{{ t.entryName }}</span>
                  <c7-copy v-if="canCopy(t.entryName)" mode="icon" size="small" class="hover-copy" :text="t.entryName" @click.stop />
                </span>
                <span class="badge api">{{ t.fromPageVisit ? '页内' : '纯API' }}</span>
                <span class="badge" :class="t.okFlag === '1' ? 'ok' : 'bad'">{{ t.okFlag === '1' ? 'OK' : 'ERR' }}</span>
              </div>
              <div v-if="t.pagePath" class="meta">
                页
                <span class="copy-field">
                  <span class="mono">{{ t.pagePath }}</span>
                  <c7-copy v-if="canCopy(t.pagePath)" mode="icon" size="small" class="hover-copy" :text="t.pagePath" @click.stop />
                </span>
              </div>
              <div class="meta">
                用户
                <span class="copy-field">
                  <span>{{ t.uin || '—' }}</span>
                  <c7-copy v-if="canCopy(t.uin)" mode="icon" size="small" class="hover-copy" :text="t.uin" @click.stop />
                </span>
                · 客户端 {{ t.callerName || '—' }}
                · {{ formatDateTime(t.startedAt) }} · {{ t.durationMs || 0 }}ms
              </div>
            </div>
          </template>

          <template v-if="showJobs">
            <div class="sec-h">定时任务</div>
            <div v-if="loading && !jobItems.length" class="lt-empty compact">加载中…</div>
            <div v-else-if="!sortedJobs.length" class="lt-empty compact">无匹配任务</div>
            <div
              v-for="t in sortedJobs"
              :key="'job-' + t.traceId"
              class="lt-item"
              :class="{ active: mode === 'job' && curTrace && curTrace.traceId === t.traceId }"
              @click="pickJob(t)"
            >
              <div>
                <span class="mono">{{ t.entryName }}</span>
                <span class="badge job">任务</span>
                <span class="badge" :class="t.okFlag === '1' ? 'ok' : 'bad'">{{ t.okFlag === '1' ? 'OK' : 'ERR' }}</span>
              </div>
              <div class="meta">
                用户 {{ t.uin || '—' }} · 客户端 {{ t.callerName || 'quartz' }}
                · {{ formatDateTime(t.startedAt) }} · {{ t.durationMs || 0 }}ms
              </div>
            </div>
          </template>
        </div>

        <!-- 页面：概览 + trace 列表 -->
        <div v-if="mode === 'page' && curVisit && pageView === 'overview'" class="lt-right">
          <div class="lt-head">
            <span>页面访问</span>
            <span class="mono">{{ curVisit.pagePath }}</span>
            <span :class="curVisit.okFlag === '1' ? 'ok' : 'bad'">{{ curVisit.okFlag === '1' ? 'OK' : 'ERR' }}</span>
          </div>
          <div class="page-body">
            <div class="intro">
              <h2 class="copy-field">
                <span class="mono">{{ curVisit.pagePath }}</span>
                <c7-copy v-if="canCopy(curVisit.pagePath)" mode="icon" size="small" class="hover-copy" :text="curVisit.pagePath" />
              </h2>
              <div class="intro-grid">
                <div>
                  <label>来自</label>
                  <span class="copy-field">
                    <span class="mono">{{ curVisit.fromPage || '—' }}</span>
                    <c7-copy v-if="canCopy(curVisit.fromPage)" mode="icon" size="small" class="hover-copy" :text="curVisit.fromPage" />
                  </span>
                </div>
                <div>
                  <label>sessionId</label>
                  <span class="copy-field">
                    <span class="mono">{{ curVisit.sessionId || '—' }}</span>
                    <c7-copy v-if="canCopy(curVisit.sessionId)" mode="icon" size="small" class="hover-copy" :text="curVisit.sessionId" />
                  </span>
                </div>
                <div>
                  <label>pageVisitId</label>
                  <span class="copy-field">
                    <span class="mono">{{ curVisit.pageVisitId }}</span>
                    <c7-copy v-if="canCopy(curVisit.pageVisitId)" mode="icon" size="small" class="hover-copy" :text="curVisit.pageVisitId" />
                  </span>
                </div>
                <div>
                  <label>uin</label>
                  <span class="copy-field">
                    <span>{{ curVisit.uin || '—' }}</span>
                    <c7-copy v-if="canCopy(curVisit.uin)" mode="icon" size="small" class="hover-copy" :text="curVisit.uin" />
                  </span>
                </div>
                <div>
                  <label>客户端</label>
                  <span>{{ curVisit.callerName || '—' }}</span>
                </div>
                <div><label>开始</label>{{ formatDateTime(curVisit.startedAt) }}</div>
                <div><label>结束</label>{{ formatDateTime(curVisit.endedAt) }}</div>
              </div>
              <div class="stats">
                <div class="stat"><b>{{ visitTraces.length || curVisit.traceCount || 0 }}</b> 条 Trace</div>
                <div class="stat"><b>{{ curVisit.durationMs || 0 }}</b> ms 合计</div>
                <div class="stat"><b>{{ okTraceCount }}</b> 成功</div>
                <div class="stat"><b>{{ failTraceCount }}</b> 失败</div>
              </div>
            </div>
            <div class="sec">本页 Trace 列表 · 点击一行查看瀑布</div>
            <div v-if="tracesLoading" class="lt-empty">加载 Trace 列表…</div>
            <table v-else class="trace-table">
              <thead>
                <tr>
                  <th style="width: 40%">traceId</th>
                  <th>接口</th>
                  <th style="width: 70px">状态</th>
                  <th style="width: 70px">耗时</th>
                  <th style="width: 90px">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="t in visitTraces" :key="t.traceId" @click="openPageTrace(t)">
                  <td>
                    <span class="copy-field">
                      <span class="mono">{{ t.traceId }}</span>
                      <c7-copy v-if="canCopy(t.traceId)" mode="icon" size="small" class="hover-copy" :text="t.traceId" @click.stop />
                    </span>
                  </td>
                  <td>
                    <span class="copy-field">
                      <span>{{ t.entryName }}</span>
                      <c7-copy v-if="canCopy(t.entryName)" mode="icon" size="small" class="hover-copy" :text="t.entryName" @click.stop />
                    </span>
                  </td>
                  <td>
                    <span class="badge" :class="t.okFlag === '1' ? 'ok' : 'bad'">
                      {{ t.statusCode || (t.okFlag === '1' ? 'OK' : 'ERR') }}
                    </span>
                  </td>
                  <td>{{ t.durationMs || 0 }}ms</td>
                  <td>
                    <el-button link type="primary" size="small" @click.stop="openPageTrace(t)">查看链路</el-button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="!tracesLoading && !visitTraces.length" class="lt-empty">暂无 Trace</div>
          </div>
        </div>

        <!-- 页面下某条 trace 瀑布 -->
        <div v-else-if="mode === 'page' && curVisit && pageView === 'trace' && curTrace" class="lt-right">
          <div class="lt-head">
            <el-button size="small" @click="backToPageOverview">← 返回页面</el-button>
            <span>
              页
              <span class="copy-field">
                <span class="mono">{{ curVisit.pagePath }}</span>
                <c7-copy v-if="canCopy(curVisit.pagePath)" mode="icon" size="small" class="hover-copy" :text="curVisit.pagePath" />
              </span>
            </span>
            <span>
              traceId
              <span class="copy-field">
                <span class="mono">{{ curTrace.traceId }}</span>
                <c7-copy v-if="canCopy(curTrace.traceId)" mode="icon" size="small" class="hover-copy" :text="curTrace.traceId" />
              </span>
            </span>
            <span class="copy-field">
              <span class="mono">{{ curTrace.entryName }}</span>
              <c7-copy v-if="canCopy(curTrace.entryName)" mode="icon" size="small" class="hover-copy" :text="curTrace.entryName" />
            </span>
            <span>{{ traceDetail?.durationMs || curTrace.durationMs || 0 }}ms</span>
          </div>
          <div class="lt-main single">
            <div class="col">
              <div class="sec">瀑布（接口已去重）</div>
              <div class="call">fe_api + service → 1 条接口</div>
              <div v-if="detailLoading" class="lt-empty">加载 Span…</div>
              <template v-else>
                <div
                  v-for="s in displaySpans"
                  :key="s.id"
                  class="wf-row"
                  :class="{ on: sel && sel.id === s.id }"
                  @click="sel = s"
                >
                  <div class="wf-name" :title="s.name">
                    <span class="copy-field">
                      <span class="wf-name-text">{{ spanDisplayName(s) }}</span>
                      <c7-copy v-if="canCopy(s.name)" mode="icon" size="small" class="hover-copy" :text="s.name" @click.stop />
                    </span>
                    <div class="meta">{{ s.kindLabel }}</div>
                  </div>
                  <div class="track">
                    <div class="bar2" :class="s.bar" :style="barStyle(s)" />
                  </div>
                  <div class="ms">{{ s.durationMs || 0 }}ms</div>
                </div>
                <div v-if="!displaySpans.length" class="lt-empty">暂无 Span</div>
                <pre v-if="traceDetail?.errorSummary" class="lt-err-box">{{ traceDetail.errorSummary }}</pre>
              </template>
            </div>
            <div class="col detail-col">
              <div class="sec">{{ detailTitle }}</div>
              <LiteTraceDetailPanel :sel="sel" :visit="curVisit" :trace="curTrace" />
            </div>
          </div>
        </div>

        <!-- 接口 / 任务 单链 -->
        <div v-else-if="(mode === 'api' || mode === 'job') && curTrace" class="lt-right">
          <div class="lt-head">
            <span>
              traceId
              <span class="copy-field">
                <span class="mono">{{ curTrace.traceId }}</span>
                <c7-copy v-if="canCopy(curTrace.traceId)" mode="icon" size="small" class="hover-copy" :text="curTrace.traceId" />
              </span>
            </span>
            <span>{{ mode === 'job' ? '定时任务' : '接口' }}</span>
            <span v-if="curTrace.fromPageVisit" class="badge page">来自页面</span>
            <span v-else-if="mode === 'api'" class="badge api">纯 API</span>
            <span class="copy-field">
              <span class="mono">{{ curTrace.entryName }}</span>
              <c7-copy v-if="canCopy(curTrace.entryName)" mode="icon" size="small" class="hover-copy" :text="curTrace.entryName" />
            </span>
            <span>客户端 {{ curTrace.callerName || '—' }}</span>
            <span>
              uin
              <span class="copy-field">
                <span>{{ curTrace.uin || '—' }}</span>
                <c7-copy v-if="canCopy(curTrace.uin)" mode="icon" size="small" class="hover-copy" :text="curTrace.uin" />
              </span>
            </span>
            <span v-if="curTrace.pagePath">
              页
              <span class="copy-field">
                <span class="mono">{{ curTrace.pagePath }}</span>
                <c7-copy v-if="canCopy(curTrace.pagePath)" mode="icon" size="small" class="hover-copy" :text="curTrace.pagePath" />
              </span>
            </span>
            <span v-if="curTrace.sessionId">
              session
              <span class="copy-field">
                <span class="mono" :title="curTrace.sessionId">{{ shortId(curTrace.sessionId) }}</span>
                <c7-copy mode="icon" size="small" class="hover-copy" :text="curTrace.sessionId" />
              </span>
            </span>
            <span v-if="curTrace.pageVisitId">
              pageVisit
              <span class="copy-field">
                <span class="mono" :title="curTrace.pageVisitId">{{ shortId(curTrace.pageVisitId) }}</span>
                <c7-copy mode="icon" size="small" class="hover-copy" :text="curTrace.pageVisitId" />
              </span>
            </span>
            <span :class="curTrace.okFlag === '1' ? 'ok' : 'bad'">{{ curTrace.okFlag === '1' ? 'OK' : 'ERR' }}</span>
            <span>{{ traceDetail?.durationMs || curTrace.durationMs || 0 }}ms</span>
          </div>
          <div class="lt-main single">
            <div class="col">
              <div class="sec">瀑布</div>
              <div v-if="detailLoading" class="lt-empty">加载 Span…</div>
              <template v-else>
                <div
                  v-for="s in displaySpans"
                  :key="s.id"
                  class="wf-row"
                  :class="{ on: sel && sel.id === s.id }"
                  @click="sel = s"
                >
                  <div class="wf-name" :title="s.name">
                    <span class="copy-field">
                      <span class="wf-name-text">{{ spanDisplayName(s) }}</span>
                      <c7-copy v-if="canCopy(s.name)" mode="icon" size="small" class="hover-copy" :text="s.name" @click.stop />
                    </span>
                    <div class="meta">{{ s.kindLabel }}</div>
                  </div>
                  <div class="track">
                    <div class="bar2" :class="s.bar" :style="barStyle(s)" />
                  </div>
                  <div class="ms">{{ s.durationMs || 0 }}ms</div>
                </div>
                <div v-if="!displaySpans.length" class="lt-empty">暂无 Span</div>
                <el-button
                  v-if="curTrace.pageVisitId"
                  size="small"
                  style="margin-top: 10px"
                  @click="jumpVisit(curTrace.pageVisitId)"
                >查看所属页面访问</el-button>
                <pre v-if="traceDetail?.errorSummary" class="lt-err-box">{{ traceDetail.errorSummary }}</pre>
              </template>
            </div>
            <div class="col detail-col">
              <div class="sec">{{ detailTitle }}</div>
              <LiteTraceDetailPanel :sel="sel" :visit="null" :trace="curTrace" />
            </div>
          </div>
        </div>

        <div v-else class="lt-right">
          <div class="lt-empty pad">从左侧选择条目</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { useFullscreen } from '@vueuse/core'
import { useRoute } from 'vue-router'
import {
  getLiteTraceIndex,
  listLiteTraceByPageVisit,
  listLiteTraceIndex,
  listLiteTracePageVisits,
  listLiteTraceSpans
} from '@/api/monitor/liteTrace'
import C7Copy from '@/packages/C7Copy/index.vue'
import { formatTime } from '@/utils/formatTime'

/**
 * 请求链路（Lite Trace）：页面访问 / 接口 / 定时任务索引，瀑布图与 Span 详情。
 */
defineOptions({ name: 'MonitorLiteTrace' })

const route = useRoute()
const ltRootRef = ref(null)
const { isFullscreen, toggle: toggleFullscreen } = useFullscreen(ltRootRef)

const searchType = ref('all')
const keyword = ref('')
const appliedKw = ref('')
const appliedType = ref('all')
const timePreset = ref('10m')
const timeRange = ref([])
const uin = ref('')
const callerName = ref('')
const okFlag = ref('')
const scope = ref('all')
const sortKey = ref('time')
const sortDir = ref('desc')

const mode = ref('')
const pageView = ref('overview')
const curVisit = ref(null)
const curTrace = ref(null)
const sel = ref(null)
const traceDetail = ref(null)
const rawSpans = ref([])

const visits = ref([])
const apiItems = ref([])
const jobItems = ref([])
const visitTraces = ref([])

const loading = ref(false)
const tracesLoading = ref(false)
const detailLoading = ref(false)
const loadError = ref('')

const searchPlaceholder = computed(() => {
  const m = {
    all: '页面路径 / 接口 URL / ID…',
    page: '例如 /system/role',
    url: '例如 /sys/role/page',
    traceId: '粘贴完整或前缀 traceId',
    sessionId: '粘贴 sessionId',
    pageVisitId: '粘贴 pageVisitId',
    keyword: '任意关键字'
  }
  return m[searchType.value] || '输入查询内容'
})

const showPages = computed(() => {
  // 接口 URL / traceId 搜索不应混入「页面访问」列表
  if (appliedType.value === 'url' || appliedType.value === 'traceId') return false
  return scope.value === 'all' || scope.value === 'page'
})
const showApis = computed(() => {
  if (appliedType.value === 'url') return true
  if (appliedType.value === 'page') return false
  return scope.value === 'all' || scope.value === 'api'
})
const showJobs = computed(() => {
  if (appliedType.value === 'url' || appliedType.value === 'page') return false
  return scope.value === 'all' || scope.value === 'job'
})

const okTraceCount = computed(() => visitTraces.value.filter((t) => t.okFlag === '1').length)
const failTraceCount = computed(() => visitTraces.value.filter((t) => t.okFlag !== '1').length)

const detailTitle = computed(() => (sel.value ? `${sel.value.kindLabel || 'Span'}详情` : '详情'))

const displaySpans = computed(() => {
  if (!curTrace.value) return []
  const totalMs = Math.max(Number(traceDetail.value?.durationMs || curTrace.value.durationMs) || 1, 1)
  let list = mergeSpansFromRaw(rawSpans.value, totalMs, traceDetail.value)
  const hidePage =
    mode.value === 'job' ||
    (mode.value === 'api' && curTrace.value && !curTrace.value.fromPageVisit)
  if (hidePage) {
    list = list.filter((s) => s.kind !== 'page')
  }
  return list
})

function sortList(list, nameFn, timeFn, durFn) {
  const dir = sortDir.value === 'asc' ? 1 : -1
  return [...list].sort((a, b) => {
    if (sortKey.value === 'duration') return (durFn(a) - durFn(b)) * dir
    if (sortKey.value === 'name') return String(nameFn(a)).localeCompare(String(nameFn(b)), 'zh') * dir
    return (timeFn(a) - timeFn(b)) * dir
  })
}

const sortedVisits = computed(() =>
  sortList(
    visits.value,
    (x) => x.pagePath,
    (x) => parseTs(x.endedAt || x.startedAt),
    (x) => Number(x.durationMs) || 0
  )
)

const sortedApis = computed(() =>
  sortList(
    apiItems.value,
    (x) => x.entryName,
    (x) => parseTs(x.startedAt),
    (x) => Number(x.durationMs) || 0
  )
)

const sortedJobs = computed(() =>
  sortList(
    jobItems.value,
    (x) => x.entryName,
    (x) => parseTs(x.startedAt),
    (x) => Number(x.durationMs) || 0
  )
)

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
    case 'today':
      start.setHours(0, 0, 0, 0)
      break
    default:
      start.setTime(end.getTime() - 10 * 60 * 1000)
  }
  return formatNowRange(start, end)
}

function defaultRange() {
  return rangeByPreset('10m')
}

const timeRangeLabel = computed(() => {
  const a = timeRange.value?.[0]
  const b = timeRange.value?.[1]
  if (!a || !b) return '—'
  return `${a} ~ ${b}`
})

function onTimePresetChange(val) {
  if (val === 'custom') return
  timeRange.value = rangeByPreset(val)
  applyFilter()
}

function parseTs(v) {
  if (!v) return 0
  if (typeof v === 'string') return new Date(v.replace(' ', 'T')).getTime() || 0
  if (Array.isArray(v) && v.length >= 6) {
    const [y, mo, d, h, mi, s] = v
    return new Date(y, mo - 1, d, h, mi, s || 0).getTime()
  }
  return 0
}

function formatDateTime(v) {
  if (!v) return '—'
  if (typeof v === 'string') {
    const s = v.replace('T', ' ').slice(0, 19)
    return formatTime(s, '{y}-{m}-{d} {h}:{i}:{s}') || s
  }
  if (Array.isArray(v) && v.length >= 6) {
    const [y, mo, d, h, mi, s] = v
    const p = (n) => String(n).padStart(2, '0')
    return `${y}-${p(mo)}-${p(d)} ${p(h)}:${p(mi)}:${p(s ?? 0)}`
  }
  return String(v)
}

function shortId(id) {
  if (!id) return '—'
  const s = String(id)
  return s.length > 14 ? `${s.slice(0, 8)}…${s.slice(-4)}` : s
}

function spanDisplayName(s) {
  if (!s) return '—'
  if (s.kind === 'sql') return s.mapperId || s.name || '—'
  return s.name || '—'
}

function canCopy(v) {
  if (v == null) return false
  const s = String(v).trim()
  return s !== '' && s !== '—'
}

function buildQuery(extra = {}) {
  const q = {
    beginTime: timeRange.value?.[0],
    endTime: timeRange.value?.[1],
    uin: uin.value.trim() || undefined,
    callerName: callerName.value || undefined,
    okFlag: okFlag.value || undefined,
    searchType: appliedType.value || 'all',
    keyword: appliedKw.value.trim() || undefined,
    sortKey: sortKey.value,
    sortDir: sortDir.value,
    pageNum: 1,
    pageSize: 50,
    ...extra
  }
  return q
}

function mapApiRow(t) {
  return {
    ...t,
    fromPageVisit: t.rootSource === 'browser' && !!t.pageVisitId
  }
}

async function loadList() {
  loading.value = true
  loadError.value = ''
  try {
    const base = buildQuery()
    const needPages = showPages.value
    const needApis = showApis.value
    const needJobs = showJobs.value
    const [pvRes, apiRes, jobRes] = await Promise.all([
      needPages ? listLiteTracePageVisits(base) : Promise.resolve({ data: [] }),
      needApis ? listLiteTraceIndex({ ...base, listMode: 'api' }) : Promise.resolve({ data: { records: [] } }),
      needJobs ? listLiteTraceIndex({ ...base, listMode: 'job' }) : Promise.resolve({ data: { records: [] } })
    ])
    visits.value = needPages ? (pvRes && pvRes.data) || [] : []
    apiItems.value = needApis
      ? ((apiRes && apiRes.data && apiRes.data.records) || []).map(mapApiRow)
      : []
    jobItems.value = needJobs
      ? ((jobRes && jobRes.data && jobRes.data.records) || []).map((t) => ({
          ...t,
          fromPageVisit: false
        }))
      : []
  } catch (e) {
    loadError.value = (e && e.message) || '加载失败'
    visits.value = []
    apiItems.value = []
    jobItems.value = []
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  if (timePreset.value !== 'custom') {
    timeRange.value = rangeByPreset(timePreset.value)
  }
  appliedKw.value = keyword.value
  appliedType.value = searchType.value
  loadList()
}

function resetFilter() {
  searchType.value = 'all'
  keyword.value = ''
  appliedKw.value = ''
  appliedType.value = 'all'
  timePreset.value = '10m'
  timeRange.value = defaultRange()
  uin.value = ''
  callerName.value = ''
  okFlag.value = ''
  scope.value = 'all'
  sortKey.value = 'time'
  sortDir.value = 'desc'
  mode.value = ''
  pageView.value = 'overview'
  curVisit.value = null
  curTrace.value = null
  sel.value = null
  traceDetail.value = null
  rawSpans.value = []
  visitTraces.value = []
  loadList()
}

async function loadVisitTraces(pageVisitId) {
  tracesLoading.value = true
  try {
    const res = await listLiteTraceByPageVisit(pageVisitId, {
      beginTime: timeRange.value?.[0],
      endTime: timeRange.value?.[1]
    })
    visitTraces.value = (res && res.data) || []
  } catch (e) {
    loadError.value = (e && e.message) || 'Trace 列表加载失败'
    visitTraces.value = []
  } finally {
    tracesLoading.value = false
  }
}

async function pickVisit(pv) {
  mode.value = 'page'
  pageView.value = 'overview'
  curVisit.value = pv
  curTrace.value = null
  sel.value = null
  traceDetail.value = null
  rawSpans.value = []
  await loadVisitTraces(pv.pageVisitId)
}

function backToPageOverview() {
  pageView.value = 'overview'
  curTrace.value = null
  sel.value = null
  traceDetail.value = null
  rawSpans.value = []
}

async function openPageTrace(t) {
  pageView.value = 'trace'
  await pickTrace({ ...t, fromPageVisit: true })
}

async function pickApi(t) {
  mode.value = 'api'
  pageView.value = 'overview'
  curVisit.value = null
  await pickTrace(t)
}

async function pickJob(t) {
  mode.value = 'job'
  curVisit.value = null
  await pickTrace(t)
}

async function pickTrace(t) {
  curTrace.value = t
  sel.value = null
  traceDetail.value = null
  rawSpans.value = []
  detailLoading.value = true
  loadError.value = ''
  try {
    const [d, s] = await Promise.all([
      getLiteTraceIndex(t.traceId),
      listLiteTraceSpans(t.traceId)
    ])
    traceDetail.value = (d && d.data) || t
    rawSpans.value = (s && s.data) || []
    const list = displaySpans.value
    sel.value = list.find((x) => x.kind === 'api') || list[0] || null
  } catch (e) {
    loadError.value = (e && e.message) || '详情加载失败'
  } finally {
    detailLoading.value = false
  }
}

function jumpVisit(pageVisitId) {
  const pv = visits.value.find((v) => v.pageVisitId === pageVisitId)
  if (pv) {
    pickVisit(pv)
    return
  }
  pickVisit({
    pageVisitId,
    pagePath: curTrace.value?.pagePath || '—',
    sessionId: curTrace.value?.sessionId,
    uin: curTrace.value?.uin,
    fromPage: curTrace.value?.fromPage,
    okFlag: curTrace.value?.okFlag,
    traceCount: 0,
    durationMs: curTrace.value?.durationMs,
    startedAt: curTrace.value?.startedAt,
    endedAt: curTrace.value?.endedAt
  })
}

function barStyle(s) {
  const total = Math.max(s.totalMs || 1, 1)
  const start = Number(s.start || 0)
  const dur = Math.max(Number(s.durationMs || 0), 1)
  return {
    left: `${(start / total) * 100}%`,
    width: `${Math.max((dur / total) * 100, 1)}%`
  }
}

function parseAttrs(span) {
  if (!span || !span.attrsJson) return {}
  try {
    const o = typeof span.attrsJson === 'string' ? JSON.parse(span.attrsJson) : span.attrsJson
    return o && typeof o === 'object' ? o : {}
  } catch {
    return { raw: span.attrsJson }
  }
}

function resolveSpanKind(span, attrs) {
  if (!span) return ''
  const kind = attrs && attrs.kind
  if (kind === 'api' || kind === 'sql' || kind === 'page' || kind === 'action') return kind
  const t = span.sourceType || ''
  if (t === 'sql') return 'sql'
  if (t === 'fe_api' || t === 'service') return 'api'
  if (t === 'fe_pv') return 'page'
  if (t === 'fe_action') return 'action'
  return ''
}

function spanApiKey(span, attrs) {
  const method = attrs.method || ''
  const url = attrs.url || span.spanName || ''
  const name = span.spanName || `${method} ${url}`.trim()
  return normalizeUrlKey(name)
}

function normalizeUrlKey(name) {
  return String(name || '')
    .trim()
    .toUpperCase()
    .replace(/\s+/g, ' ')
}

function mergeSpansFromRaw(rawSpansList, totalMs, indexDetail) {
  const list = rawSpansList || []
  const apis = list.filter((s) => {
    const attrs = parseAttrs(s)
    const kind = resolveSpanKind(s, attrs)
    return kind === 'api' || s.sourceType === 'fe_api' || s.sourceType === 'service'
  })
  const others = list.filter((s) => {
    const attrs = parseAttrs(s)
    const kind = resolveSpanKind(s, attrs)
    return kind !== 'api' && s.sourceType !== 'fe_api' && s.sourceType !== 'service'
  })

  const byKey = new Map()
  for (const s of apis) {
    const attrs = parseAttrs(s)
    const key = spanApiKey(s, attrs)
    const isFe = s.sourceType === 'fe_api'
    const cur = byKey.get(key) || {
      id: `api-${key}`,
      kind: 'api',
      kindLabel: '接口',
      name: s.spanName || attrs.url || key,
      bar: 'be',
      durationMs: 0,
      start: Number(s.startOffsetMs || 0),
      requestParams: '',
      requestBody: '',
      responsePreview: '',
      bizCode: null,
      bizMsg: '',
      feMs: null,
      beMs: null,
      method: attrs.method || '',
      url: attrs.url || s.spanName || '',
      query: attrs.query || '',
      status: s.statusCode || attrs.status || '—',
      totalMs
    }
    if (isFe) {
      cur.requestParams = attrs.requestParams || cur.requestParams
      cur.requestBody = attrs.requestBody || cur.requestBody
      cur.responsePreview = attrs.responsePreview || cur.responsePreview
      cur.bizCode = attrs.bizCode != null ? attrs.bizCode : cur.bizCode
      cur.bizMsg = attrs.bizMsg || cur.bizMsg
      cur.feMs = Number(s.durationMs || 0)
      cur.method = attrs.method || cur.method
      cur.url = attrs.url || cur.url
      cur.query = attrs.query || cur.query
      cur.status = s.statusCode || attrs.status || cur.status
      if (!cur.durationMs) cur.durationMs = Number(s.durationMs || 0)
      cur.start = Math.min(cur.start, Number(s.startOffsetMs || 0))
    } else {
      cur.beMs = Number(s.durationMs || 0)
      cur.durationMs = Number(s.durationMs || 0)
      cur.start = Number(s.startOffsetMs || 0)
      cur.status = s.statusCode || attrs.status || cur.status
      cur.bar = 'be'
    }
    byKey.set(key, cur)
  }

  for (const cur of byKey.values()) {
    if (!cur.durationMs) {
      cur.durationMs = cur.beMs != null ? cur.beMs : cur.feMs || 0
    }
    cur.totalMs = totalMs
  }

  const mappedOthers = others.map((s) => {
    const attrs = parseAttrs(s)
    const kind = resolveSpanKind(s, attrs)
    return {
      id: String(s.spanId || s.sourceType + s.spanName),
      kind: kind || 'other',
      kindLabel:
        kind === 'page' ? '页面' : kind === 'sql' ? 'SQL' : kind === 'action' ? '操作' : s.sourceType || 'Span',
      name: s.spanName || '—',
      bar: kind === 'page' ? 'pg' : kind === 'sql' ? 'sql' : kind === 'action' ? 'fe' : 'fe',
      durationMs: Number(s.durationMs || 0),
      start: Number(s.startOffsetMs || 0),
      sql: attrs.sql || '',
      mapperId: attrs.mapperId || s.spanName || '',
      page: attrs.page || '',
      fromPage: attrs.fromPage || '',
      sessionId: attrs.sessionId || '',
      title: attrs.title || '',
      fullPath: attrs.fullPath || '',
      virtual: !!attrs.virtual,
      action: attrs.action || s.spanName || '',
      operationId: attrs.operationId || '',
      totalMs
    }
  })

  let pageSpans = mappedOthers.filter((x) => x.kind === 'page')
  const nonPage = mappedOthers.filter((x) => x.kind !== 'page')

  if (
    indexDetail &&
    indexDetail.rootSource === 'browser' &&
    (indexDetail.pagePath || indexDetail.fromPage) &&
    !pageSpans.length
  ) {
    pageSpans = [
      {
        id: '__virtual_page__',
        kind: 'page',
        kindLabel: '页面',
        name: indexDetail.pagePath || 'page',
        bar: 'pg',
        durationMs: 0,
        start: 0,
        page: indexDetail.pagePath || '',
        fromPage: indexDetail.fromPage || '',
        sessionId: indexDetail.sessionId || '',
        virtual: true,
        totalMs
      }
    ]
  }

  return [...pageSpans, ...byKey.values(), ...nonPage]
}

function applyRouteQuery() {
  const q = route.query.q || route.query.traceId || route.query.pageVisitId || route.query.sessionId
  if (!q) return
  const raw = Array.isArray(q) ? q[0] : q
  if (!raw) return
  if (route.query.traceId) {
    searchType.value = 'traceId'
    keyword.value = String(raw)
  } else if (route.query.pageVisitId) {
    searchType.value = 'pageVisitId'
    keyword.value = String(raw)
  } else if (route.query.sessionId) {
    searchType.value = 'sessionId'
    keyword.value = String(raw)
  } else if (route.query.q) {
    keyword.value = String(raw)
  }
}

const LiteTraceDetailPanel = defineComponent({
  name: 'LiteTraceDetailPanel',
  props: {
    sel: { type: Object, default: null },
    visit: { type: Object, default: null },
    trace: { type: Object, default: null }
  },
  setup(props) {
    const copyIcon = (text) =>
      h(C7Copy, {
        mode: 'icon',
        size: 'small',
        class: 'hover-copy',
        text: String(text)
      })

    const kv = (label, value, opts = {}) => {
      const { mono, pre, copyable } = opts
      const str = value == null || value === '' ? '—' : String(value)
      const children = [h('label', label)]
      if (pre) {
        children.push(h('pre', { class: opts.preClass || '' }, str))
        if (copyable && canCopy(str)) {
          children.push(
            h(C7Copy, {
              mode: 'button',
              size: 'small',
              class: 'copy-btn-wrap',
              text: str,
              buttonText: '复制'
            })
          )
        }
      } else {
        children.push(
          h('span', { class: 'copy-field' }, [
            h('span', { class: mono ? 'mono' : undefined }, str),
            copyable && canCopy(str) ? copyIcon(str) : null
          ])
        )
      }
      return h('div', { class: ['kv', pre ? 'kv-pre' : ''].filter(Boolean).join(' ') }, children)
    }

    return () => {
      const s = props.sel
      const visit = props.visit
      const trace = props.trace
      if (s && s.kind === 'api') {
        const netHint =
          s.feMs != null && s.beMs != null
            ? Math.max(0, Number(s.feMs) - Number(s.beMs))
            : null
        return h('div', { class: 'detail-body' }, [
          kv('URL', s.url || s.name || '—', { mono: true, copyable: true }),
          kv('Method', (s.method || '—').toString().toUpperCase()),
          kv('HTTP 状态', s.status ?? '—'),
          kv('业务码', s.bizCode != null ? String(s.bizCode) : '—'),
          kv('业务消息', s.bizMsg || '—'),
          h('div', { class: 'kv' }, [
            h('label', '耗时说明'),
            h('div', { class: 'dur-box' }, [
              h('div', { class: 'dur-row' }, [
                h('span', { class: 'dur-k' }, '前端观测'),
                h('span', { class: 'dur-v' }, s.feMs != null ? `${s.feMs} ms` : '—'),
                h('span', { class: 'dur-hint' }, '浏览器侧整段请求（含网络）')
              ]),
              h('div', { class: 'dur-row' }, [
                h('span', { class: 'dur-k' }, '后端处理'),
                h('span', { class: 'dur-v' }, s.beMs != null ? `${s.beMs} ms` : '—'),
                h('span', { class: 'dur-hint' }, '服务端处理，瀑布条按此绘制')
              ]),
              netHint != null
                ? h('div', { class: 'dur-row' }, [
                    h('span', { class: 'dur-k' }, '大致差额'),
                    h('span', { class: 'dur-v' }, `${netHint} ms`),
                    h('span', { class: 'dur-hint' }, '多含网络 / 排队 / 序列化')
                  ])
                : null
            ])
          ]),
          kv('Query', s.query || '—', { pre: true, preClass: 'body-pre', copyable: true }),
          kv('Params', s.requestParams || '—', { pre: true, preClass: 'body-pre', copyable: true }),
          kv('Body', s.requestBody || '—', { pre: true, preClass: 'body-pre', copyable: true }),
          kv('响应预览', s.responsePreview || '—', { pre: true, preClass: 'body-pre', copyable: true }),
          trace
            ? h('div', { class: 'kv-group' }, [
                h('div', { class: 'kv-group-title' }, '链路上下文'),
                kv('traceId', trace.traceId || '—', { mono: true, copyable: true }),
                kv('sessionId', trace.sessionId || '—', { mono: true, copyable: true }),
                kv('pageVisitId', trace.pageVisitId || '—', { mono: true, copyable: true }),
                kv('uin', trace.uin || '—', { copyable: true }),
                kv('页面路径', trace.pagePath || '—', { mono: true, copyable: true }),
                kv('客户端', trace.callerName || '—')
              ])
            : null
        ])
      }
      if (s && s.kind === 'page' && visit) {
        return h('div', { class: 'detail-body' }, [
          kv('当前页', visit.pagePath || '—', { mono: true, copyable: true }),
          kv('来自', visit.fromPage || '—', { mono: true, copyable: true }),
          kv('sessionId', visit.sessionId || '—', { mono: true, copyable: true }),
          kv('pageVisitId', visit.pageVisitId || '—', { mono: true, copyable: true }),
          kv('uin', visit.uin || '—', { copyable: true }),
          kv('客户端', visit.callerName || '—')
        ])
      }
      if (s && s.kind === 'page') {
        return h('div', { class: 'detail-body' }, [
          kv('当前页', s.page || s.name || '—', { mono: true, copyable: true }),
          kv('来自', s.fromPage || '—', { mono: true, copyable: true }),
          kv('sessionId', s.sessionId || '—', { mono: true, copyable: true }),
          s.virtual ? h('div', { class: 'meta' }, '（由链路索引合成的页面节点）') : null
        ])
      }
      if (s && s.kind === 'sql') {
        return h('div', { class: 'detail-body' }, [
          kv('Mapper', s.mapperId || s.name || '—', { mono: true, copyable: true }),
          kv('耗时', `${s.durationMs ?? 0} ms`),
          kv('SQL', s.sql || '—', { pre: true, preClass: 'sql-pre', copyable: true })
        ])
      }
      if (s && s.kind === 'action') {
        return h('div', { class: 'detail-body' }, [
          kv('操作', s.action || s.name || '—'),
          kv('operationId', s.operationId || '—', { mono: true, copyable: true })
        ])
      }
      return h('div', { class: 'meta' }, '点选 Span')
    }
  }
})

onMounted(() => {
  timeRange.value = defaultRange()
  applyRouteQuery()
  appliedKw.value = keyword.value
  appliedType.value = searchType.value
  loadList()
})
</script>

<style scoped>
.lt-page {
  height: calc(100vh - 84px);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.lt-page.is-fs {
  height: 100%;
}
.lt-root {
  --bg: #0b0f14;
  --panel: #121821;
  --panel2: #182130;
  --line: #243044;
  --text: #e8eef7;
  --muted: #8b9bb4;
  --accent: #3b82f6;
  --ok: #22c55e;
  --bad: #ef4444;
  --warn: #f59e0b;
  flex: 1;
  min-height: 0;
  min-width: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg);
  color: var(--text);
  border: 1px solid var(--line);
  border-radius: 4px;
  overflow: hidden;
}
.lt-root:fullscreen,
.lt-root:-webkit-full-screen {
  width: 100%;
  height: 100%;
  border-radius: 0;
  border: 0;
}
.lt-bar {
  border-bottom: 1px solid var(--line);
  background: var(--panel);
  padding: 8px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}
.lt-bar-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.lt-title {
  font-weight: 600;
  white-space: nowrap;
}
.fs-btn {
  margin-left: auto;
}
.time-field {
  flex-wrap: wrap;
}
.time-range-text {
  font-size: 11px;
  color: var(--muted);
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.search-wrap {
  flex: 1;
  min-width: 280px;
  display: flex;
  align-items: stretch;
  border: 1px solid var(--line);
  border-radius: 4px;
  overflow: hidden;
  background: #0f1620;
}
.search-type {
  width: 130px;
  flex-shrink: 0;
}
.search-type :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: #121821;
  border-radius: 0;
  border-right: 1px solid var(--line);
}
.search-input {
  flex: 1;
  min-width: 0;
}
.search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  border-radius: 0;
}
.search-input :deep(.el-input__inner) {
  color: var(--text);
  font-family: ui-monospace, Consolas, monospace;
  font-size: 12px;
}
.field {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--muted);
}
.field label {
  white-space: nowrap;
}
.lt-hint {
  padding: 6px 14px;
  font-size: 11px;
  color: var(--muted);
  border-bottom: 1px solid var(--line);
  background: #0e141c;
  line-height: 1.55;
  flex-shrink: 0;
}
.lt-hint b {
  color: #93c5fd;
}
.lt-err {
  color: #f87171;
  font-size: 12px;
  white-space: nowrap;
}
.lt-body {
  flex: 1;
  min-height: 0;
  min-width: 0;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
}
.lt-left {
  border-right: 1px solid var(--line);
  background: var(--panel);
  overflow: auto;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.lt-filters {
  padding: 10px 12px;
  border-bottom: 1px solid var(--line);
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.lt-chip {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 3px;
  border: 1px solid var(--line);
  color: var(--muted);
  cursor: pointer;
  user-select: none;
}
.lt-chip.on {
  border-color: var(--accent);
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.12);
}
.sort-row {
  padding: 8px 12px;
  border-bottom: 1px solid var(--line);
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
.sort-label {
  font-size: 11px;
  color: var(--muted);
}
.count {
  padding: 6px 12px;
  font-size: 11px;
  color: var(--muted);
  border-bottom: 1px solid var(--line);
}
.sec-h {
  padding: 8px 12px 4px;
  font-size: 10px;
  color: var(--muted);
  letter-spacing: 0.04em;
}
.lt-item {
  padding: 10px 12px;
  border-bottom: 1px solid var(--line);
  cursor: pointer;
  font-size: 12px;
}
.lt-item:hover,
.lt-item.active {
  background: var(--panel2);
}
.mono {
  font-family: ui-monospace, Consolas, monospace;
  color: #93c5fd;
  word-break: break-all;
}
.meta {
  color: var(--muted);
  margin-top: 3px;
}
.badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 2px;
  margin-left: 6px;
}
.badge.page {
  background: rgba(6, 182, 212, 0.2);
  color: #67e8f9;
}
.badge.api {
  background: rgba(139, 92, 246, 0.2);
  color: #c4b5fd;
}
.badge.job {
  background: rgba(245, 158, 11, 0.2);
  color: #fcd34d;
}
.badge.ok {
  background: rgba(34, 197, 94, 0.15);
  color: var(--ok);
}
.badge.bad {
  background: rgba(239, 68, 68, 0.15);
  color: var(--bad);
}
.lt-right {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}
.lt-head {
  padding: 10px 14px;
  border-bottom: 1px solid var(--line);
  background: var(--panel);
  font-size: 12px;
  color: var(--muted);
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  flex-shrink: 0;
}
.lt-head .ok {
  color: var(--ok);
}
.lt-head .bad {
  color: var(--bad);
}
.page-body {
  flex: 1;
  overflow: auto;
  padding: 16px 20px;
  min-width: 0;
}
.intro {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 16px 18px;
  margin-bottom: 16px;
}
.intro h2 {
  margin: 0 0 10px;
  font-size: 18px;
  color: var(--text);
}
.intro-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px 16px;
  font-size: 12px;
}
.intro-grid label {
  display: block;
  color: var(--muted);
  margin-bottom: 2px;
  font-size: 11px;
}
.stats {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 12px;
}
.stat {
  background: #0f1620;
  border: 1px solid var(--line);
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 12px;
}
.stat b {
  color: #93c5fd;
  font-size: 16px;
  margin-right: 4px;
}
.trace-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
}
.trace-table th,
.trace-table td {
  border: 1px solid var(--line);
  padding: 8px 10px;
  text-align: left;
}
.trace-table th {
  background: #0e141c;
  color: var(--muted);
  font-weight: 500;
}
.trace-table tr {
  cursor: pointer;
}
.trace-table tr:hover td {
  background: var(--panel2);
}
.sec {
  font-size: 12px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--text);
}
.lt-main {
  flex: 1;
  min-height: 0;
  display: grid;
}
.lt-main.single {
  grid-template-columns: minmax(0, 1fr) minmax(300px, 360px);
}
.col {
  overflow: auto;
  padding: 12px;
  border-right: 1px solid var(--line);
  min-width: 0;
}
.detail-col {
  border-right: 0;
  background: #0e141c;
}
.wf-row {
  display: grid;
  grid-template-columns: minmax(160px, 46%) minmax(60px, 1fr) 52px;
  gap: 8px;
  align-items: start;
  padding: 6px 4px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 11px;
  margin-bottom: 4px;
}
.wf-row.on {
  background: rgba(59, 130, 246, 0.1);
}
.wf-name {
  min-width: 0;
  overflow: visible;
}
.wf-name-text {
  display: block;
  color: var(--text);
  word-break: break-all;
  white-space: normal;
  line-height: 1.35;
}
.track {
  height: 18px;
  background: #0f1620;
  border: 1px solid var(--line);
  border-radius: 3px;
  position: relative;
  margin-top: 2px;
}
.bar2 {
  position: absolute;
  top: 2px;
  height: 12px;
  border-radius: 2px;
  min-width: 3px;
}
.bar2.fe {
  background: #3b82f6;
}
.bar2.be {
  background: #8b5cf6;
}
.bar2.sql {
  background: var(--warn);
}
.bar2.pg {
  background: #06b6d4;
}
.ms {
  text-align: right;
  color: var(--muted);
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}
.call {
  font-size: 11px;
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.25);
  padding: 6px 8px;
  border-radius: 4px;
  margin-bottom: 8px;
  line-height: 1.4;
}
.lt-empty {
  color: var(--muted);
  font-size: 13px;
  padding: 24px;
}
.lt-empty.compact {
  padding: 12px;
  font-size: 12px;
}
.lt-empty.pad {
  padding: 40px;
}
.lt-err-box {
  margin-top: 16px;
  background: #0f1620;
  border: 1px solid var(--line);
  padding: 12px;
  border-radius: 4px;
  color: #fecaca;
  white-space: pre-wrap;
  font-size: 12px;
}
.kv {
  margin-bottom: 10px;
  font-size: 11px;
}
.kv label {
  display: block;
  color: var(--muted);
  margin-bottom: 2px;
}
.kv pre {
  margin: 0;
  padding: 6px;
  background: #0b0f14;
  border: 1px solid var(--line);
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 140px;
  overflow: auto;
  color: var(--text);
}
.kv pre.sql-pre {
  max-height: 360px;
}
.kv pre.body-pre {
  max-height: 280px;
}
.kv span {
  color: var(--text);
  word-break: break-all;
}
.kv-pre {
  position: relative;
}
.copy-btn-wrap {
  margin-top: 4px;
}
.copy-field {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  max-width: 100%;
  vertical-align: middle;
  min-width: 0;
}
.copy-field .hover-copy {
  opacity: 0;
  flex-shrink: 0;
  transition: opacity 0.12s ease;
}
.copy-field:hover .hover-copy,
.copy-field:focus-within .hover-copy {
  opacity: 1;
}
@media (hover: none) {
  .copy-field .hover-copy {
    opacity: 0.65;
  }
}
.dur-box {
  background: #0b0f14;
  border: 1px solid var(--line);
  border-radius: 4px;
  padding: 8px;
}
.dur-row {
  display: grid;
  grid-template-columns: 72px 72px 1fr;
  gap: 6px;
  align-items: baseline;
  margin-bottom: 6px;
  font-size: 11px;
}
.dur-row:last-child {
  margin-bottom: 0;
}
.dur-k {
  color: var(--muted);
}
.dur-v {
  color: #93c5fd;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}
.dur-hint {
  color: #64748b;
  font-size: 10px;
}
.kv-group {
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px solid var(--line);
}
.kv-group-title {
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 8px;
}
.detail-body {
  min-width: 0;
}
</style>

<!-- render 函数子节点拿不到 scoped，补一份全局落在本页根下 -->
<style>
.lt-page .kv label {
  display: block;
  color: #94a3b8;
  margin-bottom: 2px;
}
.lt-page .kv span {
  color: #e2e8f0;
  word-break: break-all;
}
.lt-page .kv pre {
  margin: 0;
  padding: 6px;
  background: #0b0f14;
  border: 1px solid #1e293b;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 280px;
  overflow: auto;
  color: #e2e8f0;
  font-size: 11px;
}
.lt-page .kv pre.sql-pre {
  max-height: 360px;
}
.lt-page .copy-field {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  max-width: 100%;
  vertical-align: middle;
  min-width: 0;
}
.lt-page .copy-field .hover-copy {
  opacity: 0;
  flex-shrink: 0;
  transition: opacity 0.12s ease;
}
.lt-page .copy-field:hover .hover-copy,
.lt-page .copy-field:focus-within .hover-copy {
  opacity: 1;
}
@media (hover: none) {
  .lt-page .copy-field .hover-copy {
    opacity: 0.65;
  }
}
.lt-page .hover-copy.el-button,
.lt-page .hover-copy .el-button {
  color: #93c5fd;
  padding: 0 2px;
  height: auto;
  min-height: 0;
}
.lt-page .copy-btn-wrap {
  margin-top: 4px;
}
.lt-page .dur-box {
  background: #0b0f14;
  border: 1px solid #1e293b;
  border-radius: 4px;
  padding: 8px;
}
.lt-page .dur-row {
  display: grid;
  grid-template-columns: 72px 72px 1fr;
  gap: 6px;
  align-items: baseline;
  margin-bottom: 6px;
  font-size: 11px;
}
.lt-page .dur-k {
  color: #94a3b8;
}
.lt-page .dur-v {
  color: #93c5fd;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}
.lt-page .dur-hint {
  color: #64748b;
  font-size: 10px;
}
.lt-page .kv-group {
  margin-top: 14px;
  padding-top: 10px;
  border-top: 1px solid #1e293b;
}
.lt-page .kv-group-title {
  font-size: 11px;
  color: #94a3b8;
  margin-bottom: 8px;
}
</style>
