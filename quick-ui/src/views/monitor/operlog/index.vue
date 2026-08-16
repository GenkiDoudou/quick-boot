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
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageOperlog, getOperlog, removeOperlog, cleanOperlog, exportOperlog
} from '@/api/monitor/operlog'

/**
 * 操作日志：分页查询、详情、批量删除、清空、导出。
 */
defineOptions({ name: 'SysOperlog' })

const tableRef = ref(null)
const detailVisible = ref(false)
const detail = ref(null)
const { sys_oper_status, sys_oper_business_type } = useDict('sys_oper_status', 'sys_oper_business_type')

const defaultSearch = {
  title: '', operName: '', businessType: '', status: '', traceId: '', clientId: '',
  costTimeMin: undefined, costTimeMax: undefined
}
const searchColumns = computed(() => [
  { prop: 'title', label: '系统模块', type: 'input', span: 8 },
  { prop: 'operName', label: '操作人员', type: 'input', span: 8 },
  {
    prop: 'businessType',
    label: '业务类型',
    type: 'select',
    span: 8,
    options: sys_oper_business_type.value || []
  },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: sys_oper_status.value || []
  },
  { prop: 'traceId', label: '链路ID', type: 'input', span: 8 },
  { prop: 'clientId', label: '客户端ID', type: 'input', span: 8 },
  {
    prop: 'costTimeMin',
    label: '耗时≥(ms)',
    type: 'input',
    span: 8,
    props: { type: 'number', clearable: true, placeholder: '最小耗时' }
  },
  {
    prop: 'costTimeMax',
    label: '耗时≤(ms)',
    type: 'input',
    span: 8,
    props: { type: 'number', clearable: true, placeholder: '最大耗时' }
  }
])
const tableColumns = [
  { prop: 'title', label: '系统模块', minWidth: 160, showOverflowTooltip: true },
  { prop: 'businessType', label: '业务类型', width: 110, columnType: 'slot', slotName: 'businessType' },
  { prop: 'operName', label: '操作人员', width: 110 },
  { prop: 'clientId', label: '客户端', width: 110, showOverflowTooltip: true },
  { prop: 'operIp', label: 'IP', width: 130 },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'operTime', label: '操作时间', width: 170 },
  { prop: 'costTime', label: '耗时(ms)', width: 100 },
  { prop: 'traceId', label: '链路ID', minWidth: 140, showOverflowTooltip: true },
  { prop: 'action', label: '操作', width: 90, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

function toLongOrUndef(v) {
  if (v === '' || v == null) return undefined
  const n = Number(v)
  return Number.isFinite(n) ? n : undefined
}

/** 规范化耗时区间等筛选参数，空字符串转为 undefined */
function normalizeOperParam(param) {
  const p = { ...(param || {}) }
  p.costTimeMin = toLongOrUndef(p.costTimeMin)
  p.costTimeMax = toLongOrUndef(p.costTimeMax)
  if (p.businessType === '') delete p.businessType
  if (p.status === '') delete p.status
  return p
}

function listOperlog(pageReq) {
  return pageOperlog({
    ...pageReq,
    param: normalizeOperParam(pageReq?.param)
  })
}

function exportOperlogWrapped(snapshot) {
  return exportOperlog(normalizeOperParam(snapshot))
}

async function openDetail(row) {
  const id = row?.operId != null ? String(row.operId) : ''
  if (!id) {
    ElMessage.warning('日志主键无效')
    return
  }
  const res = await getOperlog(id)
  detail.value = res.data
  detailVisible.value = true
}

function handleClean() {
  ElMessageBox.confirm('确认清空全部操作日志？', '提示', { type: 'warning' })
    .then(() => cleanOperlog())
    .then(() => {
      ElMessage.success('已清空')
      tableRef.value?.refreshData?.()
    })
    .catch(() => {})
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
