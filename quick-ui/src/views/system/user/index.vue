<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="userId"
      export-default-file-name="user-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      export-biz-type="system:user"
      :export-query-normalizer="normalizeExportParams"
      :show-add-button="true"
      :show-edit-button="true"
      :show-delete-button="true"
      :show-export-button="true"
      :show-import-button="true"
      import-biz-type="system:user"
      :import-template-function="importTemplateFunction"
      import-template-file-name="user-import-template.xlsx"
      import-error-file-name="user-import-error.xlsx"
      :on-add="openAdd"
      :on-edit="openEdit"
      :before-delete="beforeBatchDelete"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #deptId="{ formData }">
        <el-tree-select
          v-model="formData.deptId"
          :data="deptTree"
          :props="{ value: 'id', label: 'label', children: 'children' }"
          value-key="id"
          placeholder="请选择部门"
          clearable
          check-strictly
          style="width: 100%"
        />
      </template>

      <template #status="{ row }">
        <el-switch
          :model-value="String(row.status ?? '0')"
          active-value="0"
          inactive-value="1"
          :disabled="row.userId === 1"
          @update:model-value="(v) => handleUserStatusInput(row, v)"
        />
      </template>

      <template #action="{ row }">
        <el-button
          link
          type="primary"
          @click="openEdit(row)"
          v-hasPermi="['system:user:edit']"
        >修改</el-button>
        <el-button link type="primary" @click="handleAuthRole(row)" v-hasPermi="['system:user:edit']">分配角色</el-button>
        <c7-button
          v-if="row.userId !== 1"
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认要删除用户「${row.userName}」吗？`"
          :click-function="() => deleteOne(row)"
          v-hasPermi="['system:user:remove']"
        />
        <el-button
          v-if="row.userId !== 1"
          link
          type="warning"
          @click="handleResetPwd(row)"
          v-hasPermi="['system:user:resetPwd']"
        >
          重置密码
        </el-button>
      </template>
    </C7JsonTable>

    <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="onFormSuccess" />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddOrUpdate from './add-or-update.vue'
import {
  listUser,
  delUser,
  resetUserPwd,
  changeUserStatus,
  importTemplate,
} from '@/api/system/user'
import { listTreeDept } from '@/api/system/dept'
import { useDict } from '@/utils/dict'

defineOptions({ name: 'User' })

const router = useRouter()
const { sys_normal_disable } = useDict('sys_normal_disable')

const tableRef = ref(null)
const addKey = ref(0)
const addOrUpdateRef = ref(null)
const deptTree = ref([])

const defaultSearchParam = {
  userName: '',
  nickName: '',
  phonenumber: '',
  status: '',
  deptId: undefined,
  createTimeRange: [],
}

const searchColumns = computed(() => [
  { prop: 'userName', label: '用户账号', type: 'input', span: 8, props: { placeholder: '请输入', clearable: true } },
  { prop: 'nickName', label: '用户昵称', type: 'input', span: 8, props: { placeholder: '请输入', clearable: true } },
  { prop: 'phonenumber', label: '手机号码', type: 'input', span: 8, props: { placeholder: '请输入', clearable: true } },
  { prop: 'deptId', label: '部门', type: 'slot', span: 8 },
  {
    prop: 'status',
    label: '帐号状态',
    type: 'select',
    span: 8,
    options: sys_normal_disable.value,
    props: { placeholder: '请选择', clearable: true },
  },
  {
    prop: 'createTimeRange',
    label: '创建时间',
    type: 'daterange',
    span: 8,
    props: { 'value-format': 'YYYY-MM-DD', 'range-separator': '-', 'start-placeholder': '开始', 'end-placeholder': '结束' },
  },
])

const tableColumns = computed(() => [
  { prop: 'userId', label: '用户编号', width: 100 },
  { prop: 'userName', label: '用户账号', minWidth: 120 },
  { prop: 'nickName', label: '用户昵称', minWidth: 120 },
  { prop: 'deptName', label: '部门', width: 120 },
  { prop: 'phonenumber', label: '手机号码', width: 120 },
  { prop: 'roleNames', label: '角色', minWidth: 140 },
  { prop: 'status', label: '帐号状态', width: 110, columnType: 'slot', slotName: 'status' },
  { prop: 'createTime', label: '创建时间', width: 170 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 280, fixed: 'right' },
])

onMounted(() => {
  listTreeDept({}).then((res) => {
    deptTree.value = res.data || res || []
  })
})

function listFunction(params) {
  const req = { ...params }
  const [beginTime, endTime] = req.createTimeRange || []
  if (beginTime && endTime) {
    req.beginTime = beginTime
    req.endTime = endTime
  }
  delete req.createTimeRange
  // 避免 tree-select 清空后传 '' 导致 Long 等类型绑定失败；去掉 JsonTable 自带的排序参数（用户接口未声明）
  if (req.deptId === '' || req.deptId === null) delete req.deptId
  if (req.status === '' || req.status === null) delete req.status
  delete req.orderByColumn
  delete req.isAsc

  return listUser(req).then((res) => {
    const page = res?.data
    if (page && Array.isArray(page.records)) {
      for (const row of page.records) {
        row.status = row.status == null || row.status === '' ? '0' : String(row.status)
      }
    }
    return res
  })
}

function openAdd() {
  addKey.value += 1
  nextTick(() => {
    addOrUpdateRef.value?.init()
  })
}

/**
 * 工具栏「修改」传入选中行；表格行「修改」传入当前行。
 * @param {Record<string, any>} [row]
 */
function openEdit(row) {
  if (!row?.userId) return
  addKey.value += 1
  nextTick(() => {
    addOrUpdateRef.value?.init(row.userId)
  })
}

function onFormSuccess() {
  tableRef.value?.refreshData()
}

function handleAuthRole(row) {
  router.push({ path: '/system/user/auth-role', query: { userId: row.userId } })
}

/**
 * 批量删除前校验（禁止包含内置管理员 userId=1）。
 * @param {Array<number|string>} ids
 * @returns {Promise<boolean>}
 */
async function beforeBatchDelete(ids) {
  if ((ids || []).includes(1)) {
    ElMessage.error('选中行包含内置超级管理员，无法删除')
    return false
  }
  return true
}

function batchDeleteFunction(ids) {
  return delUser(ids || [])
}

function deleteOne(row) {
  return delUser([row.userId]).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

/** 导出 query 与列表筛选对齐（日期范围、空字段清理）。 */
function normalizeExportParams(raw) {
  const req = { ...raw }
  const [beginTime, endTime] = req.createTimeRange || []
  if (beginTime && endTime) {
    req.beginTime = beginTime
    req.endTime = endTime
  }
  delete req.createTimeRange
  if (req.deptId === '' || req.deptId === null) delete req.deptId
  if (req.status === '' || req.status === null) delete req.status
  return req
}

function importTemplateFunction() {
  return importTemplate()
}

function handleResetPwd(row) {
  ElMessageBox.prompt('请输入用户「' + row.userName + '」的新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPattern: /.{6,}/,
    inputErrorMessage: '密码至少 6 位',
  })
    .then(({ value }) => resetUserPwd({ userId: row.userId, newPassword: value }))
    .then(() => {
      ElMessage.success('重置成功')
    })
    .catch(() => {})
}

/**
 * 仅响应用户操作切换状态（受控开关），避免 v-model+@change 在 status 未对齐时挂载即触发确认框。
 * @param {Record<string, any>} row
 * @param {string} newStatus
 */
function handleUserStatusInput(row, newStatus) {
  const next = String(newStatus)
  const prev = String(row.status ?? '0')
  if (prev === next) return
  const text = next === '0' ? '启用' : '停用'
  const name = row.userName ?? row.nickName ?? String(row.userId ?? '')
  ElMessageBox.confirm('确认要' + text + '用户「' + name + '」吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => changeUserStatus({ userId: row.userId, status: next }))
    .then(() => {
      row.status = next
      ElMessage.success(text + '成功')
    })
    .catch(() => {})
}
</script>
