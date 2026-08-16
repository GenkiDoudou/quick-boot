<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="pageDictType"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="removeType"
      row-key="dictId"
      :show-add-button="true"
      :add-button-permi="['system:dict:add']"
      :delete-button-permi="['system:dict:remove']"
      :on-add="openAdd"
      :export-function="exportType"
      :export-button-permi="['system:dict:export']"
      export-default-file-name="dict-type.xlsx"
      :import-function="importType"
      :import-template-download-fn="downloadTypeImportTemplate"
      :import-button-permi="['system:dict:import']"
      import-template-file-name="dict-type-import-template.xlsx"
      :show-import-button="true"
    >
      <template #toolbar-left>
        <el-button type="danger" plain v-hasPermi="['system:dict:refresh']" @click="handleRefresh">刷新缓存</el-button>
      </template>
      <template #dictType="{ row }">
        <el-button link type="primary" @click="goData(row)">{{ row.dictType }}</el-button>
      </template>
      <template #status="{ row }">
        <C7DictTag :model-value="row.status" :options="sys_normal_disable" />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['system:dict:edit']" @click="openEdit(row)">修改</el-button>
        <el-button link type="danger" v-hasPermi="['system:dict:remove']" @click="removeRow(row)">删除</el-button>
      </template>
    </C7JsonTable>

    <C7Dialog v-model="formVisible" :title="isAdd ? '新增字典类型' : '修改字典类型'" width="520px" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="字典名称" prop="dictName"><el-input v-model="form.dictName" /></el-form-item>
        <el-form-item label="字典类型" prop="dictType"><el-input v-model="form.dictType" :disabled="!isAdd" /></el-form-item>
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
/**
 * 字典类型管理：分页 CRUD、跳转字典数据页、刷新后端与前端字典缓存。
 */
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageDictType, addType, updateType, removeType, refreshAllType,
  exportType, importType, downloadTypeImportTemplate, getType
} from '@/api/system/dict/type'
import useDictStore from '@/store/modules/dict'

defineOptions({ name: 'SysDictType' })

const { sys_normal_disable } = useDict('sys_normal_disable')

const router = useRouter()
const tableRef = ref(null)
const formRef = ref(null)
const formVisible = ref(false)
const isAdd = ref(true)
const form = reactive({ dictId: null, dictName: '', dictType: '', status: '0', remark: '' })
const rules = {
  dictName: [{ required: true, message: '必填', trigger: 'blur' }],
  dictType: [{ required: true, message: '必填', trigger: 'blur' }]
}
const defaultSearch = { dictName: '', dictType: '', status: '' }
const searchColumns = computed(() => [
  { prop: 'dictName', label: '字典名称', type: 'input', span: 8 },
  { prop: 'dictType', label: '字典类型', type: 'input', span: 8 },
  { prop: 'status', label: '状态', type: 'select', span: 8, props: { options: sys_normal_disable.value || [] } }
])
const tableColumns = [
  { prop: 'dictName', label: '字典名称', minWidth: 140 },
  { prop: 'dictType', label: '字典类型', minWidth: 160, columnType: 'slot', slotName: 'dictType' },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'remark', label: '备注', minWidth: 140 },
  { prop: 'action', label: '操作', width: 160, fixed: 'right', columnType: 'slot', slotName: 'action' }
]

function openAdd() {
  isAdd.value = true
  Object.assign(form, { dictId: null, dictName: '', dictType: '', status: '0', remark: '' })
  formVisible.value = true
}

async function openEdit(row) {
  isAdd.value = false
  const res = await getType(row.dictId)
  Object.assign(form, res.data)
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  if (isAdd.value) {
    const { dictId, ...payload } = form
    await addType(payload)
  } else {
    await updateType({ ...form })
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  tableRef.value?.refreshData?.()
}

function removeRow(row) {
  ElMessageBox.confirm(`确认删除字典类型「${row.dictType}」？`, '提示', { type: 'warning' })
    .then(() => removeType([row.dictId]))
    .then(() => { ElMessage.success('删除成功'); tableRef.value?.refreshData?.() })
    .catch(() => {})
}

/** 刷新后端字典缓存并清空前端 Pinia 字典，避免旧选项残留 */
function handleRefresh() {
  refreshAllType().then(() => {
    useDictStore().cleanDict?.()
    ElMessage.success('缓存已刷新')
  })
}

function goData(row) {
  router.push({ path: '/system/dict-data/index/' + encodeURIComponent(row.dictType) })
}
</script>
