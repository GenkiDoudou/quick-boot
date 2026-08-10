<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="pageConfig"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="removeConfig"
      row-key="configId"
      :show-add-button="true"
      :add-button-permi="['system:config:add']"
      :delete-button-permi="['system:config:remove']"
      :on-add="openAdd"
      :export-function="exportConfig"
      :export-button-permi="['system:config:export']"
      export-default-file-name="config.xlsx"
      :import-function="importConfig"
      :import-template-download-fn="downloadConfigImportTemplate"
      :import-button-permi="['system:config:import']"
      import-template-file-name="config-import-template.xlsx"
      :show-import-button="true"
    >
      <template #toolbar-left>
        <el-button type="danger" plain v-hasPermi="['system:config:query']" @click="handleRefresh">刷新缓存</el-button>
      </template>
      <template #configType="{ row }">
        <C7DictTag :model-value="row.configType" :options="sys_yes_no" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['system:config:edit']" @click="openEdit(row)">修改</el-button>
        <el-button link type="danger" v-hasPermi="['system:config:remove']" @click="removeRow(row)">删除</el-button>
      </template>
    </C7JsonTable>

    <C7Dialog v-model="formVisible" :title="isAdd ? '新增参数' : '修改参数'" width="560px" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="参数名称" prop="configName"><el-input v-model="form.configName" /></el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" :disabled="isBuiltinEdit" placeholder="如 qc.login.fail-lock-enabled" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-radio-group v-model="form.configType" :disabled="isBuiltinEdit">
            <el-radio v-for="d in (sys_yes_no || [])" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
    </C7Dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageConfig, addConfig, updateConfig, removeConfig, refreshConfigCache,
  exportConfig, importConfig, downloadConfigImportTemplate, getConfig
} from '@/api/system/config'

defineOptions({ name: 'SysConfig' })

const { sys_yes_no } = useDict('sys_yes_no')

const tableRef = ref(null)
const formRef = ref(null)
const formVisible = ref(false)
const isAdd = ref(true)
const form = reactive({
  configId: null, configName: '', configKey: '', configValue: '', configType: '0', remark: ''
})
const isBuiltinEdit = computed(() => !isAdd.value && form.configType === '1')
const rules = {
  configName: [{ required: true, message: '必填', trigger: 'blur' }],
  configKey: [{ required: true, message: '必填', trigger: 'blur' }],
  configValue: [{ required: true, message: '必填', trigger: 'blur' }]
}
const defaultSearch = { configName: '', configKey: '', configType: '' }
const searchColumns = computed(() => [
  { prop: 'configName', label: '参数名称', type: 'input', span: 8 },
  { prop: 'configKey', label: '参数键名', type: 'input', span: 8 },
  {
    prop: 'configType',
    label: '系统内置',
    type: 'select',
    span: 8,
    props: { options: sys_yes_no.value || [] }
  }
])
const tableColumns = [
  { prop: 'configName', label: '参数名称', minWidth: 140 },
  { prop: 'configKey', label: '参数键名', minWidth: 200 },
  { prop: 'configValue', label: '参数键值', minWidth: 180 },
  { prop: 'configType', label: '系统内置', width: 100, columnType: 'slot', slotName: 'configType' },
  { prop: 'remark', label: '备注', minWidth: 140 },
  { prop: 'action', label: '操作', width: 160, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

function openAdd() {
  isAdd.value = true
  Object.assign(form, { configId: null, configName: '', configKey: '', configValue: '', configType: '0', remark: '' })
  formVisible.value = true
}

async function openEdit(row) {
  isAdd.value = false
  const res = await getConfig(row.configId)
  Object.assign(form, res.data)
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  if (isAdd.value) {
    const { configId, ...payload } = form
    await addConfig(payload)
  } else {
    await updateConfig({ ...form })
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  tableRef.value?.refreshData?.()
}

function removeRow(row) {
  if (row.configType === '1') {
    ElMessage.warning('系统内置参数不允许删除')
    return
  }
  ElMessageBox.confirm(`确认删除「${row.configName}」？`, '提示', { type: 'warning' })
    .then(() => removeConfig([row.configId]))
    .then(() => { ElMessage.success('删除成功'); tableRef.value?.refreshData?.() })
    .catch(() => {})
}

function handleRefresh() {
  refreshConfigCache().then(() => ElMessage.success('缓存已刷新'))
}
</script>
