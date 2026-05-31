<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="infoId"
      export-default-file-name="logininfor-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :export-function="exportFunction"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="true"
      :show-export-button="true"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left="{ selectedRows, refreshData }">
        <el-button
          type="warning"
          plain
          v-hasPermi="['monitor:logininfor:remove']"
          @click="handleClean(refreshData)"
        >
          清空
        </el-button>
        <el-button
          type="primary"
          plain
          :disabled="!selectedRows?.length"
          v-hasPermi="['monitor:logininfor:unlock']"
          @click="handleUnlock(selectedRows, refreshData)"
        >
          解锁选中
        </el-button>
      </template>
      <template #status="{ row }">
        <c7-dict-tag :model-value="row.status" :options="sys_login_status" />
      </template>
    </C7JsonTable>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { cleanLogininfor, exportLogininfor, listLogininfor, removeLogininfor, unlockLogininfor } from '@/api/monitor/logininfor'
import { saveAs } from 'file-saver'

/**
 * 登录日志：查询、导出、批量删除、清空、按选中用户解锁。
 */
defineOptions({ name: 'SysLogininfor' })

const tableRef = ref(null)
const { sys_login_status } = useDict('sys_login_status')

const defaultSearchParam = {
  ipaddr: '',
  userName: '',
  status: '',
  clientId: '',
  loginTimeRange: [],
}

const searchColumns = computed(() => [
  { prop: 'ipaddr', label: '登录地址', type: 'input', span: 8, props: { placeholder: 'IP', clearable: true } },
  { prop: 'userName', label: '用户名', type: 'input', span: 8, props: { placeholder: '用户名', clearable: true } },
  { prop: 'clientId', label: '客户端ID', type: 'input', span: 8, props: { placeholder: 'clientId 精确匹配', clearable: true } },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: sys_login_status.value,
    props: { placeholder: '状态', clearable: true, style: 'width: 240px' },
  },
  {
    prop: 'loginTimeRange',
    label: '访问时间',
    type: 'daterange',
    span: 16,
    props: { valueFormat: 'YYYY-MM-DD', startPlaceholder: '开始', endPlaceholder: '结束' },
  },
])

const tableColumns = computed(() => [
  { prop: 'infoId', label: '访问编号', width: 120 },
  { prop: 'userName', label: '用户名', minWidth: 120, showOverflowTooltip: true },
  { prop: 'clientId', label: '客户端ID', minWidth: 120, showOverflowTooltip: true },
  { prop: 'ipaddr', label: 'IP', width: 140, showOverflowTooltip: true },
  { prop: 'loginLocation', label: '登录地点', minWidth: 120, showOverflowTooltip: true },
  { prop: 'os', label: '操作系统', width: 120, showOverflowTooltip: true },
  { prop: 'browser', label: '浏览器', width: 120, showOverflowTooltip: true },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 100 },
  { prop: 'msg', label: '描述', minWidth: 160, showOverflowTooltip: true },
  { prop: 'loginTime', label: '访问时间', width: 180 },
])

function normalizeListParams(raw) {
  const p = { ...raw }
  const range = p.loginTimeRange
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p.beginTime = range[0]
    p.endTime = range[1]
  }
  delete p.loginTimeRange
  if (p.status === '' || p.status === null) delete p.status
  if (p.ipaddr === '') delete p.ipaddr
  if (p.userName === '') delete p.userName
  if (p.clientId === '') delete p.clientId
  return p
}

function listFunction(params) {
  return listLogininfor(normalizeListParams(params))
}

function batchDeleteFunction(ids) {
  return removeLogininfor(ids || [])
}

function exportFunction(searchParam) {
  const req = normalizeListParams({ ...searchParam })
  delete req.pageNum
  delete req.pageSize
  return exportLogininfor(req).then(({ data, headers }) => {
    const cd = headers['content-disposition'] || headers['Content-Disposition']
    let filename = 'logininfor-export.xlsx'
    if (cd) {
      const m = /filename\*=UTF-8''([^;]+)|filename="([^"]+)"/i.exec(cd)
      const raw = decodeURIComponent(m?.[1] || m?.[2] || '')
      if (raw) filename = raw
    }
    saveAs(data, filename)
  })
}

function handleClean(refreshData) {
  ElMessageBox.confirm('确认清空全部登录日志？', '提示', { type: 'warning' })
    .then(() => cleanLogininfor())
    .then(() => {
      ElMessage.success('已清空')
      refreshData?.()
    })
    .catch(() => {})
}

function handleUnlock(selectedRows, refreshData) {
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
      refreshData?.()
    })
    .catch(() => {})
}
</script>
