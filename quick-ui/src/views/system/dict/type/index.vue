<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="dictId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :export-function="exportFunction"
      :show-add-button="true"
      :show-edit-button="true"
      :show-delete-button="true"
      :show-export-button="true"
      :show-import-button="true"
      :on-add="openAdd"
      :on-edit="openEdit"
      :import-function="importFunction"
      :import-template-function="importTemplateFunction"
      import-template-file-name="dict-type-template.xlsx"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left>
        <el-button type="danger" plain @click="refreshAll" v-hasPermi="['system:dict:refresh']">刷新缓存</el-button>
      </template>

      <template #dictType="{ row }">
        <el-button link type="primary" @click="goData(row)">{{ row.dictType }}</el-button>
      </template>

      <template #action="{ row }">
        <el-button link @click="openEdit(row)" v-hasPermi="['system:dict:edit']">修改</el-button>
        <c7-button btn-type="delete" link confirm :confirm-message="`确认删除${row.dictName}吗？`" :click-function="() => removeRow(row)" v-hasPermi="['system:dict:remove']" />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="form.dictId ? '修改字典' : '新增字典'" :on-confirm="submit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="dict-dialog-form">
        <el-form-item label="字典名称" prop="dictName"><el-input v-model="form.dictName" /></el-form-item>
        <el-form-item label="字典类型" prop="dictType"><el-input v-model="form.dictType" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio v-for="d in sys_normal_disable" :key="d.value" :label="d.value">{{ d.label }}</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useDict } from '@/utils/dict'
import { addType, exportType, importType, importTypeTemplate, listType, refreshAllType, removeType, updateType } from '@/api/system/dict/type'

defineOptions({ name: 'DictType' })

const router = useRouter()
const { sys_normal_disable } = useDict('sys_normal_disable')
const tableRef = ref(null)
const visible = ref(false)
const formRef = ref(null)

const form = ref({ dictId: null, dictName: '', dictType: '', status: '0', remark: '' })

const defaultSearchParam = {
  dictName: '',
  dictType: '',
  status: '',
  createTimeRange: []
}

const searchColumns = computed(() => [
  { prop: 'dictName', label: '字典名称', type: 'input', span: 8, props: { placeholder: '请输入字典名称', clearable: true } },
  { prop: 'dictType', label: '字典类型', type: 'input', span: 8, props: { placeholder: '请输入字典类型', clearable: true } },
  { prop: 'status', label: '状态', type: 'select', span: 8, options: sys_normal_disable.value, props: { placeholder: '字典状态', clearable: true, style: 'width: 240px', popperClass: 'dict-status-popper' } },
  { prop: 'createTimeRange', label: '创建时间', type: 'daterange', span: 8, props: { 'value-format': 'YYYY-MM-DD', 'range-separator': '-', 'start-placeholder': '开始日期', 'end-placeholder': '结束日期' } }
])

const tableColumns = computed(() => [
  // { prop: 'dictId', label: '字典编号', width: 130 },
  { prop: 'dictName', label: '字典名称' },
  { prop: 'dictType', label: '字典类型', columnType: 'slot', slotName: 'dictType' },
  { prop: 'status', label: '状态', columnType: 'tag', options: sys_normal_disable.value, width: 100 },
  { prop: 'remark', label: '备注' },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 180, fixed: 'right' }
])

const rules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function listFunction(params) {
  const [beginTime, endTime] = params.createTimeRange || []
  const req = { ...params, params: { beginTime, endTime } }
  return listType(req).then((res) => {
    const records = res.data || []
    return { data: { records, total: records.length } }
  })
}

function openAdd() {
  form.value = { dictId: null, dictName: '', dictType: '', status: '0', remark: '' }
  visible.value = true
}

function openEdit(row) {
  if (!row) return
  form.value = { ...row }
  visible.value = true
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('校验失败'))
      const req = form.value.dictId ? updateType(form.value) : addType(form.value)
      req.then(() => {
        ElMessage.success('操作成功')
        visible.value = false
        tableRef.value?.refreshData()
        resolve()
      }).catch(reject)
    })
  })
}

function removeRow(row) {
  return removeType(row.dictId).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

function batchDeleteFunction(ids) {
  return Promise.all((ids || []).map((id) => removeType(id)))
}

function exportFunction(searchParam) {
  return exportType(searchParam)
}

function importFunction(file, strategy) {
  return importType(file, strategy === 'overwrite').then((res) => {
    tableRef.value?.refreshData()
    return res?.data || res || { total: 0, successCount: 0, failCount: 0 }
  })
}

function importTemplateFunction() {
  return importTypeTemplate()
}

function refreshAll() {
  refreshAllType().then(() => {
    ElMessage.success('刷新成功')
    tableRef.value?.refreshData()
  })
}

function goData(row) {
  router.push(`/system/dict/data/${row.dictType}`)
}
</script>

<style>
.dict-status-popper {
  min-width: 180px !important;
}
</style>
