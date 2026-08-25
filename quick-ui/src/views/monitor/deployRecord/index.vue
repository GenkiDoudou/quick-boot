<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="pageDeployRecord"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="false"
      :show-add-button="false"
      row-key="recordId"
    >
      <template #status="{ row }">
        <el-tag v-if="row.status === '0'" type="success" size="small">成功</el-tag>
        <el-tag v-else type="info" size="small">{{ row.status || '—' }}</el-tag>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['monitor:deployRecord:query']" @click="openDetail(row)">
          详情
        </el-button>
      </template>
    </C7JsonTable>

    <el-dialog v-model="detailVisible" title="发布记录详情" width="820px" destroy-on-close>
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="应用">{{ detail.appName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="环境">{{ detail.env || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ detail.operate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detail.status === '0'" type="success" size="small">成功</el-tag>
          <span v-else>{{ detail.status || '—' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="分支">{{ detail.branch || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Commit">{{ detail.gitCommit || '—' }}</el-descriptions-item>
        <el-descriptions-item label="主机" :span="2">{{ detail.hosts || '—' }}</el-descriptions-item>
        <el-descriptions-item label="构建号">{{ detail.buildNumber || '—' }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ detail.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="构建链接" :span="2">
          <a v-if="detail.buildUrl" :href="detail.buildUrl" target="_blank" rel="noopener">{{ detail.buildUrl }}</a>
          <span v-else>—</span>
        </el-descriptions-item>
        <el-descriptions-item label="发版说明" :span="2">
          <pre class="release-notes">{{ detail.releaseNotes || '—' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 发布记录：Jenkins 成功入库后的查询页。
 */
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDeployRecord, pageDeployRecord } from '@/api/monitor/deployRecord'

defineOptions({ name: 'MonitorDeployRecord' })

const tableRef = ref(null)
const detailVisible = ref(false)
const detail = ref(null)

const defaultSearch = { appName: '', env: '', operate: '' }
const searchColumns = computed(() => [
  { prop: 'appName', label: '应用', type: 'input', span: 8 },
  { prop: 'env', label: '环境', type: 'input', span: 8 },
  { prop: 'operate', label: '操作', type: 'input', span: 8 }
])

const tableColumns = [
  { prop: 'appName', label: '应用', minWidth: 100 },
  { prop: 'env', label: '环境', width: 90 },
  { prop: 'operate', label: '操作', width: 100 },
  { prop: 'branch', label: '分支', minWidth: 120 },
  { prop: 'hosts', label: '主机', minWidth: 140, showOverflowTooltip: true },
  { prop: 'buildNumber', label: '构建号', width: 90 },
  { prop: 'gitCommit', label: 'Commit', width: 100 },
  { prop: 'status', label: '状态', width: 80, columnType: 'slot', slotName: 'status' },
  { prop: 'createTime', label: '时间', width: 170 },
  { prop: 'action', label: '操作', width: 90, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

async function openDetail(row) {
  const id = row?.recordId != null ? String(row.recordId) : ''
  if (!id) {
    ElMessage.warning('记录主键无效')
    return
  }
  const res = await getDeployRecord(id)
  detail.value = res.data
  detailVisible.value = true
}
</script>

<style scoped>
.release-notes {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.5;
  max-height: 360px;
  overflow: auto;
}
</style>
