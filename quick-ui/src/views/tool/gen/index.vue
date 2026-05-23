<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="tableId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="false"
      rows-key="data.records"
      total-key="data.total"
      @selection-change="onSelectionChange"
    >
      <template #toolbar-left>
        <el-button type="primary" plain v-hasPermi="['tool:gen:code']" :disabled="!selectedTableNames.length" @click="handleBatchGen">
          生成
        </el-button>
        <el-button type="primary" plain v-hasPermi="['tool:gen:create']" @click="createVisible = true">创建表</el-button>
        <el-button type="info" plain v-hasPermi="['tool:gen:import']" @click="importVisible = true">导入</el-button>
      </template>
      <template #actions="{ row }">
        <el-button link type="primary" v-hasPermi="['tool:gen:preview']" @click="openPreview(row.tableId)">预览</el-button>
        <el-button link type="primary" v-hasPermi="['tool:gen:edit']" @click="goEdit(row.tableId)">编辑</el-button>
        <el-button link type="warning" v-hasPermi="['tool:gen:edit']" @click="handleSync(row)">同步</el-button>
        <el-button link type="danger" v-hasPermi="['tool:gen:remove']" @click="handleRemove(row)">删除</el-button>
        <el-button link type="success" v-hasPermi="['tool:gen:code']" @click="handleGenOne(row)">生成</el-button>
      </template>
    </C7JsonTable>

    <ImportTableDialog v-model="importVisible" @success="refresh" />
    <CreateTableDialog v-model="createVisible" @success="refresh" />
    <PreviewCodeDialog v-model="previewVisible" :table-id="previewTableId" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { saveAs } from 'file-saver'
import {
  listGenTable,
  delGenTable,
  synchDb,
  batchGenCode,
  genCodeToPath,
  getGenTable
} from '@/api/tool/gen'
import ImportTableDialog from './components/ImportTableDialog.vue'
import CreateTableDialog from './components/CreateTableDialog.vue'
import PreviewCodeDialog from './components/PreviewCodeDialog.vue'

/** 代码生成列表页（菜单动态路由 component: tool/gen/index） */
defineOptions({ name: 'ToolGen' })

const router = useRouter()
const tableRef = ref()
const importVisible = ref(false)
const createVisible = ref(false)
const previewVisible = ref(false)
const previewTableId = ref(null)
const selectedTableNames = ref([])

const defaultSearchParam = { tableName: '', tableComment: '', createTimeRange: [] }

const searchColumns = ref([
  { label: '表名称', prop: 'tableName', type: 'input', span: 8 },
  { label: '表描述', prop: 'tableComment', type: 'input', span: 8 },
  {
    label: '创建时间',
    prop: 'createTimeRange',
    type: 'daterange',
    span: 16,
    props: { valueFormat: 'YYYY-MM-DD HH:mm:ss', startPlaceholder: '开始', endPlaceholder: '结束' },
  },
])

const tableColumns = ref([
  { label: '表名称', prop: 'tableName', minWidth: 140 },
  { label: '表描述', prop: 'tableComment', minWidth: 160, showOverflowTooltip: true },
  { label: '实体名', prop: 'className', minWidth: 120 },
  { label: '创建时间', prop: 'createTime', minWidth: 170 },
  { label: '更新时间', prop: 'updateTime', minWidth: 170 },
  { label: '操作', prop: 'actions', columnType: 'slot', slotName: 'actions', width: 280, fixed: 'right' }
])

function listFunction(params) {
  const p = { ...params }
  const range = p.createTimeRange
  if (Array.isArray(range) && range.length === 2 && range[0] && range[1]) {
    p.beginTime = range[0]
    p.endTime = range[1]
  }
  delete p.createTimeRange
  if (p.tableName === '') delete p.tableName
  if (p.tableComment === '') delete p.tableComment
  return listGenTable(p)
}

function refresh() {
  tableRef.value?.refreshData?.()
}

function onSelectionChange(rows) {
  selectedTableNames.value = (rows || []).map((r) => r.tableName)
}

function goEdit(tableId) {
  router.push({ name: 'ToolGenEdit', query: { tableId } })
}

function openPreview(tableId) {
  previewTableId.value = tableId
  previewVisible.value = true
}

async function handleRemove(row) {
  await ElMessageBox.confirm(`确认删除表「${row.tableName}」的生成配置吗？`, '提示', { type: 'warning' })
  await delGenTable(row.tableId)
  ElMessage.success('删除成功')
  refresh()
}

async function handleSync(row) {
  await ElMessageBox.confirm(`确认从数据库同步表「${row.tableName}」的字段吗？`, '提示', { type: 'warning' })
  await synchDb(row.tableName)
  ElMessage.success('同步成功')
  refresh()
}

async function downloadZip(tables) {
  const blob = await batchGenCode(tables)
  saveAs(blob, 'quickboot.zip')
  ElMessage.success('下载成功')
}

async function handleGenOne(row) {
  try {
    const detail = await getGenTable(row.tableId)
    const info = detail.data?.info || {}
    if (info.genType === '1') {
      const res = await genCodeToPath(row.tableName)
      ElMessage.success(`已生成到：${res.data || res.msg || info.genPath}`)
    } else {
      await downloadZip(row.tableName)
    }
  } catch {
    ElMessage.error('生成失败')
  }
}

function handleBatchGen() {
  if (!selectedTableNames.value.length) {
    ElMessage.warning('请选择要生成的表')
    return
  }
  downloadZip(selectedTableNames.value.join(',')).catch(() => ElMessage.error('生成失败'))
}
</script>
