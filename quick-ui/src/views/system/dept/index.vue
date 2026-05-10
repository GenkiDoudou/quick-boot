<template>
  <div class="app-container dept-page">
    <section class="dept-search-card">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="dept-query-form">
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="queryParams.deptName" clearable placeholder="请输入部门名称" />
      </el-form-item>
      <el-form-item label="负责人" prop="leader">
        <el-input v-model="queryParams.leader" clearable placeholder="请输入负责人" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
          <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <c7-button btn-type="query" :click-function="queryAction" />
        <c7-button btn-type="refresh" style="margin-left: 8px" :click-function="resetAction" />
      </el-form-item>
    </el-form>
    </section>

    <el-row :gutter="10" class="dept-toolbar">
      <el-col :span="1.5">
        <c7-button btn-type="add" :click-function="addAction" v-hasPermi="['system:dept:add']" />
      </el-col>
    </el-row>

    <el-table :data="deptList" v-loading="loading" row-key="deptId" :tree-props="{ children: 'children' }" border class="dept-table">
      <el-table-column prop="deptName" label="部门名称" min-width="180" />
      <el-table-column prop="orderNum" label="排序" width="80" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <c7-dict-tag class="dept-status-tag" :model-value="scope.row.status" :options="sys_normal_disable" dict-type="success" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)" v-hasPermi="['system:dept:query']">查看</el-button>
          <el-button link type="primary" @click="handleAdd(scope.row)" v-hasPermi="['system:dept:add']">新增</el-button>
          <el-button link type="primary" @click="handleUpdate(scope.row)" v-hasPermi="['system:dept:edit']">修改</el-button>
          <c7-button
            btn-type="delete"
            link
            confirm
            :confirm-message="`确认删除部门「${scope.row.deptName}」吗？`"
            :click-function="() => deleteAction(scope.row)"
            success-message="删除成功"
            v-hasPermi="['system:dept:remove']"
          />
        </template>
      </el-table-column>
    </el-table>

    <add-or-update ref="formRef" @success="getList" />

    <c7-dialog v-model="detailVisible" title="部门详情" width="600px" :footer="false">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="部门ID">{{ detail.deptId }}</el-descriptions-item>
        <el-descriptions-item label="上级部门">{{ detail.parentId }}</el-descriptions-item>
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
import { reactive, ref } from 'vue'
import { useDict } from '@/utils/dict'
import { delDept, getDept, listDept } from '@/api/system/dept'
import AddOrUpdate from './add-or-update.vue'

defineOptions({ name: 'Dept' })

const { sys_normal_disable } = useDict('sys_normal_disable')
const loading = ref(false)
const deptList = ref([])
const detailVisible = ref(false)
const formRef = ref(null)
const queryFormRef = ref(null)
const detail = ref({})

const queryParams = reactive({ deptName: '', leader: '', status: '' })

function getList() {
  loading.value = true
  return listDept(queryParams).then(res => {
    deptList.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  getList()
}

/** @returns {Promise<void>} */
function queryAction() {
  return getList()
}

/** @returns {Promise<void>} */
function resetAction() {
  queryFormRef.value?.resetFields()
  return getList()
}

/** @returns {Promise<void>} */
function addAction() {
  handleAdd()
  return Promise.resolve()
}

function handleAdd(row) {
  formRef.value?.open({ parentId: row?.deptId ?? -1 })
}

function handleUpdate(row) {
  formRef.value?.open({ deptId: row.deptId })
}

function handleView(row) {
  getDept(row.deptId).then(res => {
    detail.value = res.data || {}
    detailVisible.value = true
  })
}

function deleteAction(row) {
  return delDept(row.deptId).then(() => getList())
}

getList()
</script>

<style scoped>

</style>
