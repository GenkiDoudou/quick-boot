<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="listOperlog"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="removeOperlog"
      :show-add-button="false"
      row-key="operId"
      :delete-button-permi="['monitor:operlog:remove']"
      :export-function="exportOperlogWrapped"
      :export-button-permi="['monitor:operlog:export']"
      export-default-file-name="operlog.xlsx"
    >
      <template #toolbar-left>
        <el-button type="warning" plain v-hasPermi="['monitor:operlog:remove']" @click="handleClean">清空</el-button>
      </template>
      <template #businessType="{ row }">
        <C7DictTag :model-value="String(row.businessType ?? '')" :options="sys_oper_business_type" />
      </template>
      <template #status="{ row }">
        <C7DictTag :model-value="String(row.status ?? '')" :options="sys_oper_status" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['monitor:operlog:query']" @click="openDetail(row)">详情</el-button>
      </template>
    </C7JsonTable>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="880px" destroy-on-close>
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="链路ID">{{ detail.traceId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户端ID">{{ detail.clientId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="系统模块" :span="2">{{ detail.title || '—' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">
          <C7DictTag :model-value="String(detail.businessType ?? '')" :options="sys_oper_business_type" />
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <C7DictTag :model-value="String(detail.status ?? '')" :options="sys_oper_status" />
        </el-descriptions-item>
        <el-descriptions-item label="方法" :span="2">{{ detail.method || '—' }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ detail.operUrl || '—' }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detail.operName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.operIp || '—' }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ detail.costTime ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent" :span="2">{{ detail.userAgent || '—' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="oper-pre">{{ detail.operParam || '—' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回参数" :span="2">
          <pre class="oper-pre">{{ detail.jsonResult || '—' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="异常信息" :span="2">
          <pre class="oper-pre oper-pre--error">{{ detail.errorMsg }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useDict } from '@/utils/dict'
import {
  pageOperlog, getOperlog, removeOperlog, cleanOperlog, exportOperlog
} from '@/api/monitor/operlog'
import { confirmCleanList, useCrudListPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/tier-a/operlog.schema'

/** 操作日志：分页查询、详情、批量删除、清空、导出。 */
defineOptions({ name: 'SysOperlog' })

const { sys_oper_status, sys_oper_business_type } = useDict('sys_oper_status', 'sys_oper_business_type')
const { tableRef, detailVisible, detail, refreshTable, openDetailFromApi } = useCrudListPage()

const defaultSearch = schema.defaultSearch
const searchColumns = computed(() => schema.buildSearchColumns({
  sys_oper_status,
  sys_oper_business_type
}))
const tableColumns = schema.tableColumns

function listOperlog(pageReq) {
  return pageOperlog({
    ...pageReq,
    param: schema.normalizeOperParam(pageReq?.param)
  })
}

function exportOperlogWrapped(snapshot) {
  return exportOperlog(schema.normalizeOperParam(snapshot))
}

function openDetail(row) {
  openDetailFromApi(row, getOperlog, schema.rowKey)
}

function handleClean() {
  confirmCleanList('确认清空全部操作日志？', cleanOperlog, refreshTable)
}
</script>

<style scoped>
.oper-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 180px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
}
.oper-pre--error {
  color: #f56c6c;
}
</style>
