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
 * 发布记录：Jenkins 成功入库后的查询页（schema + useCrudListPage）。
 */
import { getDeployRecord, pageDeployRecord } from '@/api/monitor/deployRecord'
import { useCrudListPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/tier-a/deployRecord.schema'

defineOptions({ name: 'MonitorDeployRecord' })

const { tableRef, detailVisible, detail, openDetailFromApi } = useCrudListPage()

const defaultSearch = schema.defaultSearch
const searchColumns = schema.searchColumns
const tableColumns = schema.tableColumns

function openDetail(row) {
  openDetailFromApi(row, getDeployRecord, schema.rowKey)
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
