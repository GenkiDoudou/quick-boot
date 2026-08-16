<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="mb8">
      <el-form-item label="部门名称">
        <el-input v-model="query.deptName" clearable @keyup.enter="loadData" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable style="width: 140px">
          <el-option v-for="d in (sys_normal_disable || [])" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" v-hasPermi="['system:dept:list']" @click="loadData">搜索</el-button>
        <el-button v-hasPermi="['system:dept:add']" type="primary" plain @click="openAdd()">新增</el-button>
        <C7ExcelDownload type="warning" plain :download-fn="handleExport" default-file-name="dept.xlsx" v-hasPermi="['system:dept:export']">导出</C7ExcelDownload>
        <el-button type="info" plain v-hasPermi="['system:dept:import']" @click="importVisible = true">导入</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" row-key="deptId" border default-expand-all :tree-props="{ children: 'children' }">
      <el-table-column prop="deptName" label="部门名称" min-width="180" />
      <el-table-column prop="orderNum" label="排序" width="80" align="center" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <C7DictTag :model-value="row.status" :options="sys_normal_disable" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" v-hasPermi="['system:dept:add']" @click="openAdd(row)">新增</el-button>
          <el-button link type="primary" v-hasPermi="['system:dept:edit']" @click="openEdit(row)">修改</el-button>
          <el-button link type="danger" v-hasPermi="['system:dept:remove']" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <C7Dialog v-model="formVisible" :title="isAdd ? '新增部门' : '修改部门'" width="560px" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
            value-key="deptId"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" maxlength="64" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in (sys_normal_disable || [])" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
    </C7Dialog>

    <el-dialog v-model="importVisible" title="导入部门" width="520px" destroy-on-close>
      <C7ExcelUpload
        :upload-fn="importDept"
        :template-download-fn="downloadDeptImportTemplate"
        template-file-name="dept-import-template.xlsx"
        @success="onImportSuccess"
        @cancel="importVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 部门管理：树形表格 CRUD、子部门新增、Excel 导入导出。
 * 上级部门通过 treeselect 选择，parentId=0 表示根部门。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  listDept, addDept, updateDept, delDept, exportDept, importDept, downloadDeptImportTemplate
} from '@/api/system/dept'

defineOptions({ name: 'SysDept' })

const { sys_normal_disable } = useDict('sys_normal_disable')

const loading = ref(false)
const list = ref([])
const treeOptions = ref([{ deptId: 0, deptName: '根部门', children: [] }])
const query = reactive({ deptName: '', status: '' })
const formVisible = ref(false)
const importVisible = ref(false)
const isAdd = ref(true)
const formRef = ref(null)
const form = reactive({
  deptId: null, parentId: 0, deptName: '', orderNum: 0, leader: '', phone: '', email: '', status: '0', remark: ''
})
const rules = {
  deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }]
}

function loadData() {
  loading.value = true
  listDept(query).then((res) => {
    list.value = res.data || []
    treeOptions.value = [{ deptId: 0, deptName: '根部门', children: res.data || [] }]
  }).finally(() => { loading.value = false })
}

function resetForm() {
  Object.assign(form, {
    deptId: null, parentId: 0, deptName: '', orderNum: 0, leader: '', phone: '', email: '', status: '0', remark: ''
  })
}

function openAdd(row) {
  isAdd.value = true
  resetForm()
  form.parentId = row?.deptId ?? 0
  formVisible.value = true
}

function openEdit(row) {
  isAdd.value = false
  Object.assign(form, {
    deptId: row.deptId,
    parentId: row.parentId ?? 0,
    deptName: row.deptName,
    orderNum: row.orderNum ?? 0,
    leader: row.leader,
    phone: row.phone,
    email: row.email,
    status: row.status ?? '0',
    remark: row.remark
  })
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  const payload = { ...form }
  if (isAdd.value) {
    delete payload.deptId
    await addDept(payload)
  } else {
    await updateDept(payload)
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  loadData()
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除部门「${row.deptName}」？`, '提示', { type: 'warning' })
    .then(() => delDept(row.deptId))
    .then(() => { ElMessage.success('删除成功'); loadData() })
    .catch(() => {})
}

function handleExport() {
  return exportDept({ deptName: query.deptName || undefined, status: query.status || undefined })
}

function onImportSuccess(result) {
  if (!(Number(result?.failCount) > 0)) importVisible.value = false
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
