<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="deptId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :show-edit-button="true"
      :show-delete-button="true"
      :show-export-button="false"
      :show-import-button="false"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-right>
        <el-tooltip content="部门树暂不支持导出" placement="top">
          <span class="dept-toolbar-export-wrap">
            <el-button type="warning" plain disabled>导出</el-button>
          </span>
        </el-tooltip>
      </template>
      <template #status="{ row }">
        <c7-dict-tag :model-value="row.status" :options="sys_normal_disable" dict-type="success" />
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="handleView(row)" v-hasPermi="['system:dept:query']">查看</el-button>
        <el-button link type="primary" @click="openAdd(row)" v-hasPermi="['system:dept:add']">新增</el-button>
        <el-button link type="primary" @click="openEdit(row)" v-hasPermi="['system:dept:edit']">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除部门「${row.deptName}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['system:dept:remove']"
        />
      </template>
    </C7JsonTable>

    <add-or-update ref="formRef" @success="tableRef?.refreshData?.()" />

    <c7-dialog v-model="detailVisible" title="部门详情" width="600px" :footer="false">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="部门ID">{{ detail.deptId }}</el-descriptions-item>
        <el-descriptions-item label="上级部门">{{ detailParentName }}</el-descriptions-item>
        <el-descriptions-item label="部门名称">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detail.leader }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useDict } from '@/utils/dict'
import { delDept, getDept, listDept } from '@/api/system/dept'
import AddOrUpdate from './add-or-update.vue'

defineOptions({ name: 'Dept' })

const ROOT_PARENT_ID = -1

const { sys_normal_disable } = useDict('sys_normal_disable')
const tableRef = ref(null)
const formRef = ref(null)
const detailVisible = ref(false)
const detail = ref({})
const deptRows = ref([])

const defaultSearchParam = {
  deptName: '',
  leader: '',
  status: '',
}

const searchColumns = computed(() => [
  { prop: 'deptName', label: '部门名称', type: 'input', span: 8, props: { placeholder: '请输入部门名称', clearable: true } },
  { prop: 'leader', label: '负责人', type: 'input', span: 8, props: { placeholder: '请输入负责人', clearable: true } },
  { prop: 'status', label: '状态', type: 'select', span: 8, options: sys_normal_disable.value, props: { placeholder: '请选择状态', clearable: true } },
])

const tableColumns = computed(() => [
  { prop: 'deptName', label: '部门名称', minWidth: 180 },
  { prop: 'orderNum', label: '排序', width: 90 },
  { prop: 'leader', label: '负责人', width: 120 },
  { prop: 'phone', label: '联系电话', width: 150 },
  { prop: 'status', label: '状态', columnType: 'slot', slotName: 'status', width: 100 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 280, fixed: 'right' },
])

const deptNameMap = computed(() => {
  const map = new Map()
  const walk = (rows) => {
    for (const row of rows || []) {
      if (!row) continue
      map.set(row.deptId, row.deptName || '')
      if (Array.isArray(row.children) && row.children.length > 0) {
        walk(row.children)
      }
    }
  }
  walk(deptRows.value || [])
  return map
})

const detailParentName = computed(() => {
  const parentId = detail.value?.parentId
  if (parentId === ROOT_PARENT_ID || parentId == null) {
    return '顶级部门'
  }
  return deptNameMap.value.get(parentId) || String(parentId)
})

function listFunction(params) {
  return listDept(params).then((res) => {
    const records = res.data || []
    deptRows.value = records
    return { data: { records, total: records.length } }
  })
}

function openAdd(row) {
  formRef.value?.open({ parentId: row?.deptId ?? ROOT_PARENT_ID })
}

function openEdit(row) {
  if (!row) return
  formRef.value?.open({ deptId: row.deptId })
}

function handleView(row) {
  getDept(row.deptId).then((res) => {
    detail.value = res.data || {}
    detailVisible.value = true
  })
}

function removeRow(row) {
  return delDept(row.deptId).then(() => tableRef.value?.refreshData?.())
}

function batchDeleteFunction(ids) {
  const list = ids || []
  return list.reduce(
    (p, id) => p.then(() => delDept(id)),
    Promise.resolve()
  )
}
</script>

<style scoped>
.dept-toolbar-export-wrap {
  display: inline-block;
  margin-right: 8px;
  vertical-align: middle;
}
</style>