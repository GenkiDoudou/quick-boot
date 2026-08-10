<template>
  <el-descriptions v-if="row" :column="2" border size="small" class="operlog-detail">
    <el-descriptions-item label="日志编号">{{ row.operId }}</el-descriptions-item>
    <el-descriptions-item label="链路ID">{{ row.traceId || '—' }}</el-descriptions-item>
    <el-descriptions-item label="操作ID" :span="2">{{ row.clientOperationId || '—' }}</el-descriptions-item>
    <el-descriptions-item label="客户端ID" :span="2">{{ row.clientId || '—' }}</el-descriptions-item>
    <el-descriptions-item label="系统模块" :span="2">{{ row.title || '—' }}</el-descriptions-item>
    <el-descriptions-item label="业务类型">
      <C7DictTag :model-value="String(row.businessType ?? '')" :options="sys_oper_business_type" />
    </el-descriptions-item>
    <el-descriptions-item label="操作类别">
      <C7DictTag :model-value="String(row.operatorType ?? '')" :options="sys_oper_operator_type" />
    </el-descriptions-item>
    <el-descriptions-item label="方法" :span="2">{{ row.method || '—' }}</el-descriptions-item>
    <el-descriptions-item label="请求地址" :span="2">{{ row.operUrl || '—' }}</el-descriptions-item>
    <el-descriptions-item label="请求方式">{{ row.requestMethod || '—' }}</el-descriptions-item>
    <el-descriptions-item label="状态">
      <C7DictTag :model-value="row.status" :options="sys_oper_status" />
    </el-descriptions-item>
    <el-descriptions-item label="操作人员">{{ row.operName || '—' }}</el-descriptions-item>
    <el-descriptions-item label="部门">{{ row.deptName || '—' }}</el-descriptions-item>
    <el-descriptions-item label="IP">{{ row.operIp || '—' }}</el-descriptions-item>
    <el-descriptions-item label="地点">{{ row.operLocation || '—' }}</el-descriptions-item>
    <el-descriptions-item label="操作时间">{{ formatOperTime(row.operTime) }}</el-descriptions-item>
    <el-descriptions-item label="耗时(ms)">{{ row.costTime ?? '—' }}</el-descriptions-item>
    <el-descriptions-item label="请求参数" :span="2">
      <pre class="oper-pre">{{ row.operParam || '—' }}</pre>
    </el-descriptions-item>
    <el-descriptions-item label="返回参数" :span="2">
      <pre class="oper-pre">{{ row.jsonResult || '—' }}</pre>
    </el-descriptions-item>
    <el-descriptions-item v-if="row.errorMsg" label="异常信息" :span="2">
      <pre class="oper-pre oper-pre--error">{{ row.errorMsg }}</pre>
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup>
import { useDict } from '@/utils/dict'
import { formatOperTime } from './operLogFormat'

/**
 * 操作日志详情展示面板，供列表页与行为轨迹 / 全链路等场景复用。
 */
defineOptions({ name: 'OperLogDetailPanel' })

defineProps({
  /** @type {import('vue').PropType<Record<string, unknown>|null>} */
  row: {
    type: Object,
    default: null,
  },
})

const { sys_oper_status, sys_oper_business_type, sys_oper_operator_type } = useDict(
  'sys_oper_status',
  'sys_oper_business_type',
  'sys_oper_operator_type',
)
</script>

<style scoped>
.operlog-detail {
  -webkit-user-select: text;
  user-select: text;
}

.operlog-detail :deep(.el-descriptions__label) {
  width: 96px;
}

.oper-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
}

.oper-pre--error {
  color: var(--el-color-danger);
}
</style>
