<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="listFn"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="delData"
      row-key="dictCode"
      :show-add-button="true"
      :add-button-permi="['system:dictData:add']"
      :delete-button-permi="['system:dictData:remove']"
      :on-add="openAdd"
      :export-function="exportFn"
      :export-button-permi="['system:dictData:export']"
      export-default-file-name="dict-data.xlsx"
      :import-function="importData"
      :import-template-download-fn="downloadDataImportTemplate"
      :import-button-permi="['system:dictData:import']"
      import-template-file-name="dict-data-import-template.xlsx"
      :show-import-button="true"
    >
      <template #status="{ row }">
        <C7DictTag :model-value="row.status" :options="sys_normal_disable" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['system:dictData:edit']" @click="openEdit(row)">修改</el-button>
      </template>
    </C7JsonTable>

    <C7Dialog v-model="formVisible" :title="isAdd ? '新增字典项' : '修改字典项'" width="560px" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="字典类型" prop="dictType"><el-input v-model="form.dictType" disabled /></el-form-item>
        <el-form-item label="数据标签" prop="dictLabel"><el-input v-model="form.dictLabel" /></el-form-item>
        <el-form-item label="数据键值" prop="dictValue"><el-input v-model="form.dictValue" /></el-form-item>
        <el-form-item label="显示排序" prop="dictSort"><el-input-number v-model="form.dictSort" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="回显样式" prop="listClass"><el-input v-model="form.listClass" placeholder="primary/success/danger..." /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in (sys_normal_disable || [])" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
    </C7Dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageDictData, addData, updateData, delData, getData,
  exportData, importData, downloadDataImportTemplate
} from '@/api/system/dict/data'

defineOptions({ name: 'SysDictData' })

const { sys_normal_disable } = useDict('sys_normal_disable')

const route = useRoute()
const dictType = computed(() => String(route.params.dictType || route.query.dictType || ''))
const tableRef = ref(null)
const formRef = ref(null)
const formVisible = ref(false)
const isAdd = ref(true)
const form = reactive({
  dictCode: null, dictType: '', dictLabel: '', dictValue: '', dictSort: 0, listClass: 'default', status: '0', remark: ''
})
const rules = {
  dictLabel: [{ required: true, message: '必填', trigger: 'blur' }],
  dictValue: [{ required: true, message: '必填', trigger: 'blur' }]
}
const defaultSearch = computed(() => ({ dictType: dictType.value, dictLabel: '', status: '' }))
const searchColumns = computed(() => [
  { prop: 'dictLabel', label: '字典标签', type: 'input', span: 8 },
  { prop: 'status', label: '状态', type: 'select', span: 8, props: { options: sys_normal_disable.value || [] } }
])
const tableColumns = [
  { prop: 'dictLabel', label: '标签', minWidth: 120 },
  { prop: 'dictValue', label: '键值', minWidth: 120 },
  { prop: 'dictSort', label: '排序', width: 80 },
  { prop: 'status', label: '状态', width: 80, columnType: 'slot', slotName: 'status' },
  { prop: 'remark', label: '备注', minWidth: 120 },
  { prop: 'action', label: '操作', width: 100, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

function listFn(pageRequest) {
  const param = { ...(pageRequest?.param || {}), dictType: dictType.value }
  return pageDictData({ ...pageRequest, param })
}

function exportFn(snapshot) {
  return exportData({ ...(snapshot || {}), dictType: dictType.value })
}

function openAdd() {
  isAdd.value = true
  Object.assign(form, {
    dictCode: null, dictType: dictType.value, dictLabel: '', dictValue: '', dictSort: 0, listClass: 'default', status: '0', remark: ''
  })
  formVisible.value = true
}

async function openEdit(row) {
  isAdd.value = false
  const res = await getData(row.dictCode)
  Object.assign(form, res.data)
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  if (isAdd.value) {
    const { dictCode, ...payload } = form
    await addData(payload)
  } else {
    await updateData({ ...form })
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  tableRef.value?.refreshData?.()
}
</script>
