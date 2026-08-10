<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="pageLogininfor"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="false"
      :show-add-button="false"
      row-key="infoId"
      :export-function="exportLogininfor"
      :export-button-permi="['monitor:logininfor:export']"
      export-default-file-name="logininfor.xlsx"
      :show-selection="true"
    >
      <template #toolbar-left="{ selectedRows }">
        <el-button type="warning" plain v-hasPermi="['monitor:logininfor:remove']" @click="handleClean">清空</el-button>
        <el-button
          type="primary"
          plain
          :disabled="!selectedRows?.length"
          v-hasPermi="['monitor:logininfor:unlock']"
          @click="handleUnlock(selectedRows)"
        >
          解锁选中
        </el-button>
      </template>
      <template #status="{ row }">
        <C7DictTag :model-value="row.status" :options="sys_login_status" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['monitor:logininfor:query']" @click="openDetail(row)">详情</el-button>
      </template>
    </C7JsonTable>

    <el-dialog v-model="detailVisible" title="登录日志详情" width="640px" destroy-on-close>
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="用户名">{{ detail.userName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户端ID">{{ detail.clientId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ipaddr || '—' }}</el-descriptions-item>
        <el-descriptions-item label="登录地点">{{ detail.loginLocation || '—' }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ detail.os || '—' }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ detail.browser || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <C7DictTag :model-value="detail.status" :options="sys_login_status" />
        </el-descriptions-item>
        <el-descriptions-item label="访问时间">{{ detail.loginTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.msg || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageLogininfor, cleanLogininfor, unlockLogininfor, exportLogininfor
} from '@/api/monitor/logininfor'

defineOptions({ name: 'SysLogininfor' })

const tableRef = ref(null)
const detailVisible = ref(false)
const detail = ref(null)
const { sys_login_status } = useDict('sys_login_status')

const defaultSearch = { ipaddr: '', userName: '', clientId: '', status: '' }
const searchColumns = computed(() => [
  { prop: 'ipaddr', label: '登录地址', type: 'input', span: 8 },
  { prop: 'userName', label: '用户名', type: 'input', span: 8 },
  { prop: 'clientId', label: '客户端ID', type: 'input', span: 8 },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: sys_login_status.value || []
  }
])
const tableColumns = [
  { prop: 'userName', label: '用户名', minWidth: 110 },
  { prop: 'clientId', label: '客户端', width: 100 },
  { prop: 'ipaddr', label: 'IP', width: 130 },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'msg', label: '描述', minWidth: 160, showOverflowTooltip: true },
  { prop: 'loginTime', label: '访问时间', width: 170 },
  { prop: 'action', label: '操作', width: 90, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

function openDetail(row) {
  detail.value = { ...row }
  detailVisible.value = true
}

function handleClean() {
  ElMessageBox.confirm('确认清空全部登录日志？', '提示', { type: 'warning' })
    .then(() => cleanLogininfor())
    .then(() => {
      ElMessage.success('已清空')
      tableRef.value?.refreshData?.()
    })
    .catch(() => {})
}

function handleUnlock(selectedRows) {
  const names = [...new Set((selectedRows || []).map((r) => r.userName).filter(Boolean))]
  if (!names.length) {
    ElMessage.warning('请先勾选要解锁的用户')
    return
  }
  ElMessageBox.confirm(`确认解锁用户：${names.join('、')} ？`, '提示', { type: 'warning' })
    .then(async () => {
      for (const n of names) {
        await unlockLogininfor(n)
      }
      ElMessage.success('解锁成功')
      tableRef.value?.refreshData?.()
    })
    .catch(() => {})
}
</script>
