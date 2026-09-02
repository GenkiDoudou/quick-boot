<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="jobLogId"
      export-default-file-name="job-log-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :export-function="exportJobLogWrapped"
      :export-button-permi="['monitor:job:export']"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="true"
      :show-export-button="true"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left="{ refreshData }">
        <el-button type="warning" plain v-hasPermi="['monitor:job:remove']" @click="handleClean(refreshData)">
          清空
        </el-button>
        <el-button plain @click="goJob">返回任务</el-button>
      </template>
      <template #status="{ row }">
        <c7-dict-tag :model-value="row.status" :options="sys_job_log_status" />
      </template>
      <template #actions="scope">
        <el-button link type="primary" v-hasPermi="['monitor:job:query']" @click="openDetail(scope.row)">
          详情
        </el-button>
      </template>
    </C7JsonTable>
    <el-dialog v-model="detailVisible" title="调度日志详情" width="720px" destroy-on-close>
      <el-descriptions v-if="detailRow" :column="1" border size="small">
        <el-descriptions-item label="日志编号">{{ detailRow.jobLogId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detailRow.jobName }}</el-descriptions-item>
        <el-descriptions-item label="任务组名">{{ detailRow.jobGroup }}</el-descriptions-item>
        <el-descriptions-item label="调用目标">{{ detailRow.invokeTarget }}</el-descriptions-item>
        <el-descriptions-item label="日志信息">{{ detailRow.jobMessage || '—' }}</el-descriptions-item>
        <el-descriptions-item label="执行状态">
          <c7-dict-tag :model-value="detailRow.status" :options="sys_job_log_status" />
        </el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ detailRow.createTime || '—' }}</el-descriptions-item>
        <el-descriptions-item v-if="detailRow.exceptionInfo" label="异常信息">
          <pre class="job-log-pre">{{ detailRow.exceptionInfo }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDict } from '@/utils/dict'
import { cleanJobLog, exportJobLog, getJobLog, listJobLog, removeJobLog } from '@/api/monitor/jobLog'
import { confirmCleanList, useCrudListPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/tier-a/jobLog.schema'

/** 调度日志：查询、详情、删除、清空、导出；支持从任务页带入筛选。 */
defineOptions({ name: 'SysJobLog' })

const route = useRoute()
const router = useRouter()
const defaultSearchParam = ref({ ...schema.defaultSearch })
const { sys_job_group, sys_job_log_status } = useDict('sys_job_group', 'sys_job_log_status')
const { tableRef, detailVisible, detail: detailRow, openDetailFromApi } = useCrudListPage()

onMounted(() => {
  if (route.query.jobName) defaultSearchParam.value.jobName = String(route.query.jobName)
  if (route.query.jobGroup) defaultSearchParam.value.jobGroup = String(route.query.jobGroup)
})

const searchColumns = computed(() => schema.buildSearchColumns({
  sys_job_group,
  sys_job_log_status
}))
const tableColumns = computed(() => schema.buildTableColumns({
  sys_job_group,
  sys_job_log_status
}))

const listFunction = (pageReq) => listJobLog(schema.toJobLogQuery(pageReq))
const exportJobLogWrapped = (snapshot) => exportJobLog(schema.toJobLogQuery(snapshot || {}))
const batchDeleteFunction = (ids) => removeJobLog(ids || [])

function handleClean(refreshData) {
  confirmCleanList('确认清空全部调度日志？', cleanJobLog, refreshData)
}

function openDetail(row) {
  openDetailFromApi(row, getJobLog, schema.rowKey)
}

function goJob() {
  router.push({ name: 'SysJob' })
}
</script>

<style scoped>
.job-log-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}
</style>
