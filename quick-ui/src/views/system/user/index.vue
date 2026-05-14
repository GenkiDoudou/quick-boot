<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="用户账号" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户账号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户昵称" prop="nickName">
        <el-input v-model="queryParams.nickName" placeholder="请输入用户昵称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号码" prop="phonenumber">
        <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="部门" prop="deptId">
        <el-tree-select
          v-model="queryParams.deptId"
          :data="deptTree"
          :props="{ value: 'id', label: 'label', children: 'children' }"
          value-key="id"
          placeholder="请选择部门"
          clearable
          check-strictly
          style="width: 220px"
        />
      </el-form-item>
      <el-form-item label="帐号状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择帐号状态" clearable>
          <el-option
            v-for="dict in sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTimeRange">
        <el-date-picker
          v-model="queryParams.createTimeRange"
          type="daterange"
          range-separator="-"
          start-placeholder="开始"
          end-placeholder="结束"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <c7-button
          btn-type="query"
          :validate="true"
          :validate-ref="queryFormRef"
          :click-function="fetchUserList"
        />
        <c7-button
          btn-type="refresh"
          style="margin-left: 8px"
          :click-function="resetAndQuery"
        />
      </el-form-item>
    </el-form>

    <el-row :gutter="10" style="margin-bottom: 12px;">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="Delete"
          :disabled="multipleSelection.length === 0"
          @click="handleBatchDelete"
          v-hasPermi="['system:user:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" icon="Download" @click="handleExport" v-hasPermi="['system:user:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-upload
          v-hasPermi="['system:user:import']"
          :show-file-list="false"
          :http-request="handleImport"
          accept=".xlsx,.xls"
        >
          <el-button type="success" icon="Upload">导入</el-button>
        </el-upload>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" icon="Document" @click="handleImportTemplate" v-hasPermi="['system:user:import']">模板</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="userList" border stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="用户编号" prop="userId" width="100" />
      <el-table-column label="用户账号" prop="userName" show-overflow-tooltip />
      <el-table-column label="用户昵称" prop="nickName" show-overflow-tooltip />
      <el-table-column label="部门" prop="deptName" />
      <el-table-column label="手机号码" prop="phonenumber" />
      <el-table-column label="角色" prop="roleNames" show-overflow-tooltip />
      <el-table-column label="帐号状态" align="center" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="scope">
          <el-button
            type="primary" link icon="Edit"
            @click="handleEdit(scope.row)"
            v-hasPermi="['system:user:edit']"
          >修改</el-button>
          <el-button
            type="primary" link icon="User"
            @click="handleAuthRole(scope.row)"
            v-hasPermi="['system:user:edit']"
          >分配角色</el-button>
          <c7-button
            btn-type="delete"
            link
            confirm
            :confirm-message="'确认要删除用户「' + scope.row.userName + '」吗？'"
            :click-function="() => deleteUser(scope.row)"
            success-message="删除成功"
            v-hasPermi="['system:user:remove']"
          />
          <el-button
            type="warning" link icon="Key"
            @click="handleResetPwd(scope.row)"
            v-hasPermi="['system:user:resetPwd']"
          >重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleQuery"
      @current-change="handleQuery"
      style="margin-top: 16px;"
    />

    <add-or-update
      :key="addKey"
      ref="addOrUpdateRef"
      @refreshDataList="handleQuery"
    />
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { saveAs } from 'file-saver'
import AddOrUpdate from './add-or-update.vue'
import {
  listUser,
  delUser,
  resetUserPwd,
  changeUserStatus,
  exportUser,
  importUser,
  importTemplate,
  importError
} from '@/api/system/user'
import { listTreeDept } from '@/api/system/dept'
import { useDict } from '@/utils/dict'

defineOptions({ name: 'User' })

const router = useRouter()
const { sys_normal_disable } = useDict('sys_normal_disable')

const loading = ref(false)
const total = ref(0)
const userList = ref([])
const addKey = ref(0)
const addOrUpdateRef = ref(null)
const queryFormRef = ref(null)
const deptTree = ref([])
const multipleSelection = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  nickName: '',
  phonenumber: '',
  status: '',
  deptId: undefined,
  createTimeRange: []
})

onMounted(() => {
  listTreeDept().then((res) => {
    deptTree.value = res.data || res || []
  })
})

function handleSelectionChange(rows) {
  multipleSelection.value = rows || []
}

function handleQuery() {
  loading.value = true
  const params = { ...queryParams }
  const range = params.createTimeRange
  if (range && range.length === 2) {
    params.beginTime = range[0]
    params.endTime = range[1]
  }
  delete params.createTimeRange
  return listUser(params).then((res) => {
    const page = res.data || res
    userList.value = page.records || []
    total.value = page.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
    return Promise.reject(new Error('查询失败'))
  })
}

/** @returns {Promise<void>} */
function fetchUserList() {
  return handleQuery()
}

/** @returns {Promise<void>} */
function resetAndQuery() {
  queryFormRef.value?.resetFields()
  queryParams.createTimeRange = []
  queryParams.deptId = undefined
  return handleQuery()
}

function handleAdd() {
  addKey.value++
  nextTick(() => { addOrUpdateRef.value.init() })
}

function handleEdit(row) {
  addKey.value++
  nextTick(() => { addOrUpdateRef.value.init(row.userId) })
}

function handleAuthRole(row) {
  router.push({ path: '/system/user/auth-role', query: { userId: row.userId } })
}

function handleBatchDelete() {
  const ids = multipleSelection.value.map((r) => r.userId)
  if (ids.length === 0) {
    return
  }
  if (ids.includes(1)) {
    ElMessage.error('选中行包含内置超级管理员，无法删除')
    return
  }
  ElMessageBox.confirm('确认删除选中的用户吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => delUser(ids).then(() => {
    ElMessage.success('删除成功')
    handleQuery()
  })).catch(() => {})
}

function handleExport() {
  const params = { ...queryParams }
  const range = params.createTimeRange
  if (range && range.length === 2) {
    params.beginTime = range[0]
    params.endTime = range[1]
  }
  delete params.createTimeRange
  delete params.pageNum
  delete params.pageSize
  return exportUser(params).then(({ data, headers }) => {
    const cd = headers['content-disposition'] || headers['Content-Disposition']
    let filename = 'user-export.xlsx'
    if (cd) {
      const m = /filename\*=UTF-8''([^;]+)|filename="([^"]+)"/i.exec(cd)
      const raw = decodeURIComponent(m?.[1] || m?.[2] || '')
      if (raw) filename = raw
    }
    saveAs(data, filename)
  })
}

function handleImportTemplate() {
  importTemplate().then((blob) => {
    saveAs(blob, 'user-import-template.xlsx')
  })
}

/**
 * @param {{ file: File }} opt
 */
function handleImport(opt) {
  ElMessageBox.confirm('已存在同名用户时是否更新记录？', '导入用户', {
    distinguishCancelAndClose: true,
    confirmButtonText: '更新已存在',
    cancelButtonText: '仅新增'
  }).then(() => doImport(opt.file, true))
    .catch((action) => {
      if (action === 'cancel') {
        return doImport(opt.file, false)
      }
      return Promise.resolve()
    })
}

function doImport(file, updateSupport) {
  return importUser(file, updateSupport).then((res) => {
    const r = res.data || res
    ElMessage.success(`导入完成：成功 ${r.success}，失败 ${r.failure}，共 ${r.total}`)
    if (r.errorKey) {
      ElMessageBox.confirm('存在失败行，是否下载失败明细？', '提示', { type: 'info' })
        .then(() => importError(r.errorKey).then((blob) => saveAs(blob, 'user-import-error.xlsx')))
        .catch(() => {})
    }
    handleQuery()
  })
}

/**
 * @param row 当前行
 * @returns {Promise<void>}
 */
async function deleteUser(row) {
  await delUser([row.userId])
  await handleQuery()
}

function handleResetPwd(row) {
  ElMessageBox.prompt('请输入用户「' + row.userName + '」的新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'password',
    inputPattern: /.{6,}/,
    inputErrorMessage: '密码至少 6 位'
  }).then(({ value }) => {
    return resetUserPwd({ userId: row.userId, newPassword: value })
  }).then(() => {
    ElMessage.success('重置成功')
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  ElMessageBox.confirm('确认要' + text + '用户"' + row.userName + '"吗？', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    return changeUserStatus({ userId: row.userId, status: row.status })
  }).then(() => {
    ElMessage.success(text + '成功')
  }).catch(() => {
    row.status = row.status === '0' ? '1' : '0'
  })
}

handleQuery()
</script>
