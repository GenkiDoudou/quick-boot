<template>
  <div class="app-container">
    <!-- 搜索栏 -->
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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" style="margin-bottom: 12px;">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">新增</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="userList" border stripe>
      <el-table-column label="用户账号" prop="userName" show-overflow-tooltip />
      <el-table-column label="用户昵称" prop="nickName" show-overflow-tooltip />
      <el-table-column label="部门" prop="deptName" />
      <el-table-column label="手机号码" prop="phonenumber" />
      <el-table-column label="角色" prop="roleNames" show-overflow-tooltip />
      <el-table-column label="帐号状态" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" />
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="scope">
          <el-button
            type="primary" link icon="Edit"
            @click="handleEdit(scope.row)"
            v-hasPermi="['system:user:edit']"
          >修改</el-button>
          <el-button
            type="danger" link icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:user:remove']"
          >删除</el-button>
          <el-button
            type="warning" link icon="Key"
            @click="handleResetPwd(scope.row)"
            v-hasPermi="['system:user:resetPwd']"
          >重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <add-or-update
      :key="addKey"
      ref="addOrUpdateRef"
      @refreshDataList="handleQuery"
    />
  </div>
</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddOrUpdate from './add-or-update.vue'
import { listUser, delUser, resetUserPwd, changeUserStatus } from '@/api/system/user'
import { useDict } from '@/utils/dict'

defineOptions({ name: 'User' })

const { sys_normal_disable } = useDict('sys_normal_disable')

const loading = ref(false)
const total = ref(0)
const userList = ref([])
const addKey = ref(0)
const addOrUpdateRef = ref(null)
const queryFormRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  nickName: '',
  phonenumber: '',
  status: ''
})

function handleQuery() {
  loading.value = true
  listUser(queryParams).then(res => {
    userList.value = res.data.records || res.data
    total.value = res.data.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function handleAdd() {
  addKey.value++
  nextTick(() => { addOrUpdateRef.value.init() })
}

function handleEdit(row) {
  addKey.value++
  nextTick(() => { addOrUpdateRef.value.init(row.id) })
}

function handleDelete(row) {
  ElMessageBox.confirm('确认要删除用户"' + row.userName + '"吗？', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    return delUser([row.id])
  }).then(() => {
    ElMessage.success('删除成功')
    handleQuery()
  }).catch(() => {})
}

function handleResetPwd(row) {
  ElMessageBox.confirm('是否确认重置用户"' + row.userName + '"的密码？', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    return resetUserPwd(row.id)
  }).then(() => {
    ElMessage.success('重置成功')
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  ElMessageBox.confirm('确认要' + text + '用户"' + row.userName + '"吗？', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(() => {
    return changeUserStatus(row.id, row.status)
  }).then(() => {
    ElMessage.success(text + '成功')
  }).catch(() => {
    row.status = row.status === '0' ? '1' : '0'
  })
}

handleQuery()
</script>
