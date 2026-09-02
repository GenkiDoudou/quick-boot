<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="slowId"
      export-default-file-name="slowsql-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      export-biz-type="monitor:slowSql"
      :export-query-normalizer="normalizeListParams"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="true"
      :show-export-button="true"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left="{ refreshData }">
        <el-button
          type="warning"
          plain
          v-hasPermi="['monitor:slowSql:remove']"
          @click="handleClean(refreshData)"
        >
          清空
        </el-button>
      </template>
      <template #sqlSource="{ row }">
        <el-tag :type="sourceTagType(row.sqlSource)" size="small">{{ row.sqlSource || '—' }}</el-tag>
      </template>
      <template #sqlType="{ row }">
        <el-tag :type="sqlTypeTagType(row.sqlType)" size="small" effect="plain">{{ row.sqlType || 'OTHER' }}</el-tag>
      </template>
      <template #sqlText="{ row }">
        <span class="slowsql-sql-cell" :title="row.sqlText">{{ sqlPreview(row.sqlText) }}</span>
      </template>
      <template #createTime="{ row }">
        <span class="slowsql-cell-text">{{ formatTime(row.createTime) }}</span>
      </template>
      <template #actions="scope">
        <el-button link type="primary" v-hasPermi="['monitor:slowSql:query']" @click="openDetail(scope.row)">
          详情
        </el-button>
      </template>
    </C7JsonTable>
    <el-dialog v-model="detailVisible" title="慢 SQL 详情" width="920px" destroy-on-close>
      <el-descriptions v-if="detailRow" :column="1" border>
        <el-descriptions-item label="来源">{{ detailRow.sqlSource }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ detailRow.sqlType || 'OTHER' }}</el-descriptions-item>
        <el-descriptions-item label="traceId">{{ detailRow.traceId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="Mapper">{{ detailRow.mapperId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ detailRow.costTime }}</el-descriptions-item>
        <el-descriptions-item label="请求">{{ detailRow.requestMethod }} {{ detailRow.requestUri || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作ID">{{ detailRow.clientOperationId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="记录时间">{{ formatTime(detailRow.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="SQL">
          <pre class="slowsql-sql-pre">{{ detailRow.sqlText }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { cleanSlowSql, getSlowSql, listSlowSql, removeSlowSql } from '@/api/monitor/slowSql'
import { formatTime } from '@/utils/formatTime'
import { confirmCleanList, useCrudListPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/tier-a/slowSql.schema'

/** 慢 SQL 监控：分页列表、详情、批量删除、清空、导出。 */
defineOptions({ name: 'SysSlowSql' })

const { tableRef, detailVisible, detail: detailRow, openDetailFromApi } = useCrudListPage()

const defaultSearchParam = schema.defaultSearch
const searchColumns = schema.searchColumns
const tableColumns = schema.tableColumns
const sourceTagType = schema.sourceTagType
const sqlTypeTagType = schema.sqlTypeTagType
const sqlPreview = schema.sqlPreview
const normalizeListParams = schema.normalizeListParams

const listFunction = (params) => listSlowSql(normalizeListParams(params))
const batchDeleteFunction = (ids) => removeSlowSql(ids || [])

function handleClean(refreshData) {
  confirmCleanList('确认清空全部慢 SQL 记录？', cleanSlowSql, refreshData)
}

function openDetail(row) {
  if (!row?.slowId) return
  openDetailFromApi(row, getSlowSql, schema.rowKey)
}
</script>

<style scoped>
.slowsql-cell-text {
  user-select: text;
}
.slowsql-sql-cell {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 12px;
  color: var(--el-text-color-regular);
  user-select: text;
}
.slowsql-sql-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  max-height: 480px;
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
}
</style>
