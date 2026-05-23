<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="configId"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :export-function="exportFunction"
      :show-add-button="true"
      :add-button-permi="['system:config:add']"
      :show-edit-button="true"
      :edit-button-permi="['system:config:edit']"
      :show-delete-button="true"
      :delete-button-permi="['system:config:remove']"
      :show-export-button="true"
      :export-button-permi="['system:config:export']"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #toolbar-left>
        <el-button type="danger" plain @click="handleRefreshCache" v-hasPermi="['system:config:query']">刷新缓存</el-button>
      </template>

      <template #configType="{ row }">
        <el-tag :type="row.configType === '1' ? 'warning' : 'info'">
          {{ row.configType === '1' ? '是' : '否' }}
        </el-tag>
      </template>

      <template #action="{ row }">
        <el-button link @click="openEdit(row)" v-hasPermi="['system:config:edit']">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除${row.configName}吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['system:config:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="visible" :title="form.configId ? '修改参数' : '新增参数'" :on-confirm="submit">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="config-dialog-form">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如 qc.login.fail-lock-enabled" :disabled="isBuiltinEdit" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-radio-group v-model="form.configType" :disabled="isBuiltinEdit">
            <el-radio label="0">否</el-radio>
            <el-radio label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addConfig, exportConfig, listConfig, refreshConfigCache, removeConfig, updateConfig } from '@/api/system/config'

defineOptions({ name: 'SysConfig' })

const tableRef = ref(null)
const visible = ref(false)
const formRef = ref(null)

const form = ref({
  configId: null,
  configName: '',
  configKey: '',
  configValue: '',
  configType: '0',
  remark: ''
})

const defaultSearchParam = {
  configName: '',
  configKey: '',
  configType: '',
  createTimeRange: []
}

const searchColumns = computed(() => [
  { prop: 'configName', label: '参数名称', type: 'input', span: 8, props: { placeholder: '请输入参数名称', clearable: true } },
  { prop: 'configKey', label: '参数键名', type: 'input', span: 8, props: { placeholder: '请输入参数键名', clearable: true } },
  {
    prop: 'configType',
    label: '系统内置',
    type: 'select',
    span: 8,
    options: [
      { label: '是', value: '1' },
      { label: '否', value: '0' }
    ],
    props: { placeholder: '请选择系统内置', clearable: true, style: 'width: 240px' }
  },
  {
    prop: 'createTimeRange',
    label: '创建时间',
    type: 'daterange',
    span: 8,
    props: { 'value-format': 'YYYY-MM-DD', 'range-separator': '-', 'start-placeholder': '开始日期', 'end-placeholder': '结束日期' }
  }
])

const tableColumns = computed(() => [
  { prop: 'configId', label: '参数主键', width: 160 },
  { prop: 'configName', label: '参数名称', minWidth: 160 },
  { prop: 'configKey', label: '参数键名', minWidth: 220 },
  { prop: 'configValue', label: '参数键值', minWidth: 260, showOverflowTooltip: true },
  { prop: 'configType', label: '系统内置', columnType: 'slot', slotName: 'configType', width: 100 },
  { prop: 'remark', label: '备注', minWidth: 160, showOverflowTooltip: true },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 170, fixed: 'right' }
])

const isBuiltinEdit = computed(() => !!form.value.configId && form.value.configType === '1')

/** 与后端 SysConfigBo 一致：点分段，段内可用连字符（如 qc.login.fail-lock-enabled） */
const CONFIG_KEY_PATTERN = /^[a-z0-9]+(-[a-z0-9]+)*(\.[a-z0-9]+(-[a-z0-9]+)*)*$/

const rules = computed(() => ({
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [
    { required: true, message: '请输入参数键名', trigger: 'blur' },
    ...(isBuiltinEdit.value
      ? []
      : [{
          pattern: CONFIG_KEY_PATTERN,
          message: '参数键名仅支持小写字母、数字、点号与连字符',
          trigger: 'blur'
        }])
  ],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }]
}))

function listFunction(params) {
  const [beginTime, endTime] = params.createTimeRange || []
  const req = { ...params, beginTime, endTime }
  delete req.createTimeRange
  return listConfig(req).then((res) => {
    const records = res.data || []
    return { data: { records, total: records.length } }
  })
}

function openAdd() {
  form.value = { configId: null, configName: '', configKey: '', configValue: '', configType: '0', remark: '' }
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
      const req = form.value.configId ? updateConfig(form.value) : addConfig(form.value)
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
  return removeConfig([row.configId]).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

function batchDeleteFunction(ids) {
  return removeConfig(ids || []).then(() => {
    ElMessage.success('删除成功')
  })
}

function exportFunction(searchParam) {
  const req = { ...searchParam }
  const [beginTime, endTime] = req.createTimeRange || []
  req.beginTime = beginTime
  req.endTime = endTime
  delete req.createTimeRange
  return exportConfig(req)
}

function handleRefreshCache() {
  refreshConfigCache().then(() => {
    ElMessage.success('刷新缓存成功')
    tableRef.value?.refreshData()
  })
}
</script>
