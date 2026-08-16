<template>
  <div class="app-container">
    <el-form :inline="true" class="mb8" @submit.prevent>
      <el-form-item label="时间">
        <el-date-picker
          v-model="range"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始"
          end-placeholder="结束"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="sources" multiple clearable placeholder="页面/接口/SQL" style="width: 260px">
          <el-option label="页面" value="page" />
          <el-option label="接口" value="api" />
          <el-option label="SQL" value="sql" />
          <el-option label="操作日志" value="oper" />
          <el-option label="登录日志" value="login" />
        </el-select>
      </el-form-item>
      <el-form-item label="页面地址">
        <el-input v-model="pagePath" clearable placeholder="/monitor/..." style="width: 160px" />
      </el-form-item>
      <el-form-item label="sessionId">
        <el-input v-model="sessionId" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="接口地址">
        <el-input v-model="apiUrl" clearable placeholder="/system/..." style="width: 160px" />
      </el-form-item>
      <el-form-item label="用户">
        <el-input v-model="actor" clearable placeholder="uin / 操作人" style="width: 120px" />
      </el-form-item>
      <el-form-item label="客户端ID">
        <el-input v-model="clientId" clearable placeholder="精确匹配，如 quick-ui" style="width: 160px" />
      </el-form-item>
      <el-form-item label="关键字">
        <el-input v-model="keyword" clearable placeholder="SQL / mapper / 其它" style="width: 140px" />
      </el-form-item>
      <el-form-item label="结果">
        <el-select v-model="okFlag" clearable placeholder="全部" style="width: 100px">
          <el-option label="成功" value="1" />
          <el-option label="失败" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="traceId">
        <el-input v-model="traceId" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
      </el-form-item>
    </el-form>
    <el-alert
      v-if="approximate"
      type="warning"
      :closable="false"
      show-icon
      title="一期为近似合并分页。可用页面地址 / sessionId / 接口地址独立筛选。"
      class="mb8"
    />
    <el-table v-loading="loading" :data="rows" border size="small" @row-click="openRow">
      <el-table-column prop="occurredAt" label="时间" width="170" />
      <el-table-column prop="source" label="来源" width="72">
        <template #default="{ row }">{{ sourceLabel(row.source) }}</template>
      </el-table-column>
      <el-table-column prop="pagePath" label="页面地址" min-width="140" show-overflow-tooltip />
      <el-table-column prop="sessionId" label="sessionId" min-width="140" show-overflow-tooltip />
      <el-table-column label="接口地址" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.source === 'api'">{{ (row.method || '').toUpperCase() }} {{ row.url || '—' }}</span>
          <span v-else-if="row.source === 'sql'">{{ row.url || row.mapperId || '—' }}</span>
          <span v-else>{{ row.url || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="refId" label="ID" width="120" show-overflow-tooltip />
      <el-table-column prop="actor" label="用户" width="100" show-overflow-tooltip />
      <el-table-column prop="clientId" label="客户端ID" width="110" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="72">
        <template #default="{ row }">
          <el-tag :type="row.status === 'fail' ? 'danger' : 'success'" size="small">{{ row.status === 'fail' ? '失败' : '成功' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="traceId" label="traceId" width="130" show-overflow-tooltip />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="openRow(row)">打开</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawer" title="日志详情" size="480px">
      <template v-if="current">
        <div class="kv"><label>来源</label><span>{{ sourceLabel(current.source) }}</span></div>
        <div class="kv"><label>时间</label><span>{{ current.occurredAt }}</span></div>
        <div class="kv"><label>用户</label><span>{{ current.actor || '—' }}</span></div>
        <div class="kv"><label>客户端ID</label><span>{{ current.clientId || '—' }}</span></div>
        <div class="kv"><label>ID</label><span>{{ current.refId || '—' }}</span></div>
        <div class="kv"><label>页面地址</label><span>{{ current.pagePath || '—' }}</span></div>
        <div class="kv"><label>来自</label><span>{{ current.fromPage || '—' }}</span></div>
        <div class="kv"><label>sessionId</label><span>{{ current.sessionId || '—' }}</span></div>
        <div class="kv"><label>接口地址</label><span>{{ (current.method || '').toUpperCase() }} {{ current.url || '—' }}</span></div>
        <div class="kv"><label>traceId</label><span>{{ current.traceId || '—' }}</span></div>
        <div v-if="current.mapperId" class="kv"><label>Mapper</label><span>{{ current.mapperId }}</span></div>
        <div v-if="current.sqlText || current.detail" class="kv">
          <label>{{ current.source === 'sql' ? '完整 SQL' : '详情' }}</label>
          <pre>{{ current.sqlText || current.detail }}</pre>
        </div>
        <div class="acts">
          <el-button v-if="current.traceId" type="primary" @click="goTrace(current.traceId)">请求链路</el-button>
          <el-button v-if="current.source === 'sql'" @click="goSlow(current)">慢 SQL</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listLogHub } from '@/api/monitor/logHub'

/**
 * 日志中心：多来源（页面/接口/SQL/操作/登录）统一检索，支持跳转链路/SQL 详情。
 */
defineOptions({ name: 'MonitorLogHub' })

const router = useRouter()
const range = ref([])
const sources = ref(['page', 'api', 'sql'])
const pagePath = ref('')
const sessionId = ref('')
const apiUrl = ref('')
const actor = ref('')
const clientId = ref('')
const keyword = ref('')
const okFlag = ref('')
const traceId = ref('')
const rows = ref([])
const loading = ref(false)
const approximate = ref(true)
const drawer = ref(false)
const current = ref(null)

/** 默认查询最近 24 小时 */
function defaultRange() {
  const end = new Date()
  const start = new Date(end.getTime() - 24 * 3600 * 1000)
  const fmt = (d) => {
    const p = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
  }
  return [fmt(start), fmt(end)]
}

function sourceLabel(s) {
  if (s === 'page') return '页面'
  if (s === 'api') return '接口'
  if (s === 'sql' || s === 'slow_sql') return 'SQL'
  if (s === 'oper') return '操作'
  if (s === 'login') return '登录'
  return s
}

/** 按筛选条件拉取日志中心列表 */
async function load() {
  loading.value = true
  try {
    const res = await listLogHub({
      beginTime: range.value?.[0],
      endTime: range.value?.[1],
      sources: sources.value.length ? sources.value : undefined,
      pagePath: pagePath.value.trim() || undefined,
      sessionId: sessionId.value.trim() || undefined,
      apiUrl: apiUrl.value.trim() || undefined,
      actor: actor.value.trim() || undefined,
      clientId: clientId.value.trim() || undefined,
      keyword: keyword.value.trim() || undefined,
      okFlag: okFlag.value || undefined,
      traceId: traceId.value.trim() || undefined,
      pageSize: 50
    })
    const data = (res && res.data) || {}
    rows.value = data.rows || []
    approximate.value = data.approximate !== false
  } finally {
    loading.value = false
  }
}

function openRow(row) {
  if (!row) return
  current.value = row
  drawer.value = true
}

/** 携带 traceId 跳转到请求链路页 */
function goTrace(id) {
  router.push({ path: '/monitor/liteTrace', query: { q: `traceId:${id}` } })
}

/** 跳转到慢 SQL 页（优先 traceId，否则 slowId） */
function goSlow(row) {
  router.push({
    path: '/monitor/slowSql',
    query: row.traceId ? { traceId: row.traceId } : (row.refId ? { slowId: row.refId } : {})
  })
}

onMounted(() => {
  range.value = defaultRange()
  load()
})
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.kv { margin-bottom: 12px; font-size: 13px; }
.kv label { display: block; color: var(--el-text-color-secondary); margin-bottom: 4px; font-size: 12px; }
.kv pre { white-space: pre-wrap; word-break: break-all; background: var(--el-fill-color-light); padding: 8px; border-radius: 4px; margin: 0; max-height: 360px; overflow: auto; }
.acts { margin-top: 16px; display: flex; gap: 8px; }
</style>
