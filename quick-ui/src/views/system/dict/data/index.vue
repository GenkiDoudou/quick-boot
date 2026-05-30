<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="dictCode"
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
      import-template-file-name="dict-data-template.xlsx"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #dictLabel="{ row }">
        <span v-if="isPlainDictStyle(row.listClass, row.cssClass)">{{ row.dictLabel }}</span>
        <el-tag v-else :type="resolveListClassTagType(row.listClass)" :class="row.cssClass">{{ row.dictLabel }}</el-tag>
      </template>

      <template #action="{ row }">
        <el-button link @click="openEdit(row)">修改</el-button>
        <c7-button btn-type="delete" link confirm :confirm-message="`确认删除${row.dictLabel}吗？`" :click-function="() => removeRow(row)" />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="form.dictCode ? '修改字典项' : '新增字典项'" :on-confirm="submit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="dict-dialog-form">
        <el-form-item label="字典类型" prop="dictType"><el-input v-model="form.dictType" disabled /></el-form-item>
        <el-form-item label="数据标签" prop="dictLabel"><el-input v-model="form.dictLabel" placeholder="请输入数据标签" /></el-form-item>
        <el-form-item label="数据键值" prop="dictValue"><el-input v-model="form.dictValue" placeholder="请输入数据键值" /></el-form-item>
        <el-form-item label="样式属性" prop="cssClass">
          <el-input v-model="form.cssClass" placeholder="可选，自定义 CSS 类名" />
        </el-form-item>
        <el-form-item label="显示排序" prop="dictSort"><el-input-number v-model="form.dictSort" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="回显样式" prop="listClass">
          <el-select v-model="form.listClass" placeholder="请选择回显样式" style="width: 100%">
            <el-option
              v-for="item in LIST_CLASS_OPTIONS"
              :key="item.value"
              :label="`${item.label}(${item.value})`"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useDict, LIST_CLASS_OPTIONS, isPlainDictStyle, resolveListClassTagType } from '@/utils/dict'
import useDictStore from '@/store/modules/dict'
import { addData, delData, exportData, importData, importDataTemplate, listData, updateData } from '@/api/system/dict/data'

const route = useRoute()
const { sys_normal_disable } = useDict('sys_normal_disable')
const tableRef = ref(null)
const visible = ref(false)
const formRef = ref(null)
const currentDictType = ref(route.params.dictType || '')

const defaultForm = () => ({
  dictCode: null,
  dictType: currentDictType.value,
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  cssClass: '',
  listClass: 'default',
  status: '0',
  remark: ''
})

const form = ref(defaultForm())

const defaultSearchParam = {
  dictType: currentDictType.value,
  dictLabel: '',
  status: ''
}

const searchColumns = computed(() => [
  { prop: 'dictType', label: '字典类型', type: 'input', span: 6, props: { disabled: true } },
  { prop: 'dictLabel', label: '数据标签', type: 'input', span: 6, props: { placeholder: '请输入数据标签', clearable: true } },
  { prop: 'status', label: '状态', type: 'select', span: 6, options: sys_normal_disable.value, props: { placeholder: '数据状态', clearable: true, style: 'width: 200px', popperClass: 'dict-status-popper' } }
])

const tableColumns = computed(() => [
  { prop: 'dictCode', label: '字典编码' },
  { prop: 'dictLabel', label: '数据标签', columnType: 'slot', slotName: 'dictLabel' },
  { prop: 'dictValue', label: '数据键值' },
  { prop: 'dictSort', label: '排序', width: 80 },
  { prop: 'status', label: '状态', columnType: 'tag', options: sys_normal_disable.value, width: 100 },
  { prop: 'remark', label: '备注' },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 180, fixed: 'right' }
])

const rules = {
  dictType: [{ required: true, message: '字典类型不能为空', trigger: 'blur' }],
  dictLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
  dictValue: [{ required: true, message: '数据键值不能为空', trigger: 'blur' }],
  dictSort: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }]
}

function invalidateDictCache() {
  if (currentDictType.value) {
    useDictStore().removeDict(currentDictType.value)
  }
}

function listFunction(params) {
  const req = { ...params, dictType: currentDictType.value }
  return listData(req).then((res) => {
    const records = res.data || []
    return { data: { records, total: records.length } }
  })
}

function openAdd() {
  form.value = defaultForm()
  visible.value = true
}

function openEdit(row) {
  if (!row) return
  form.value = {
    ...defaultForm(),
    ...row,
    listClass: row.listClass || 'default'
  }
  visible.value = true
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate((valid) => {
      if (!valid) return reject(new Error('校验失败'))
      const req = form.value.dictCode ? updateData(form.value) : addData(form.value)
      req.then(() => {
        invalidateDictCache()
        ElMessage.success('操作成功')
        visible.value = false
        tableRef.value?.refreshData()
        resolve()
      }).catch(reject)
    })
  })
}

function removeRow(row) {
  return delData(row.dictCode).then(() => {
    invalidateDictCache()
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

function batchDeleteFunction(ids) {
  return Promise.all((ids || []).map((id) => delData(id))).then(() => {
    invalidateDictCache()
  })
}

function exportFunction(searchParam) {
  return exportData({ ...searchParam, dictType: currentDictType.value })
}

function importFunction(file, strategy) {
  return importData(file, currentDictType.value, strategy === 'overwrite').then((res) => {
    invalidateDictCache()
    tableRef.value?.refreshData()
    return res?.data || res || { total: 0, successCount: 0, failCount: 0 }
  })
}

function importTemplateFunction() {
  return importDataTemplate()
}

watch(
  () => route.params.dictType,
  (v) => {
    currentDictType.value = v || ''
    form.value.dictType = currentDictType.value
    tableRef.value?.refreshData()
  }
)
</script>

<style>
.dict-status-popper {
  min-width: 180px !important;
}
</style>
