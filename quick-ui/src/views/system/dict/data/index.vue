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
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
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
/**
 * 字典数据项管理：路由 dictType 限定当前类型下的分页 CRUD 与导入导出。
 */
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useDict } from '@/utils/dict'
import {
  pageDictData, addData, updateData, delData, getData,
  exportData, importData, downloadDataImportTemplate
} from '@/api/system/dict/data'
import { useCrudPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/tier-a/dictData.schema'

defineOptions({ name: 'SysDictData' })

const { sys_normal_disable } = useDict('sys_normal_disable')
const route = useRoute()
const dictType = computed(() => String(route.params.dictType || route.query.dictType || ''))

const {
  tableRef, formRef, formVisible, isAdd, form, formRules,
  openAdd: baseOpenAdd, openEdit, submitForm
} = useCrudPage({
  idField: schema.rowKey,
  formInitial: () => schema.formInitial(dictType.value),
  formRules: schema.formRules,
  api: { add: addData, update: updateData, remove: delData },
  loadDetail: async (row) => (await getData(row.dictCode)).data
})

watch(dictType, () => {
  form.dictType = dictType.value
})

const defaultSearch = computed(() => ({ dictType: dictType.value, ...schema.defaultSearch }))
const searchColumns = computed(() => schema.buildSearchColumns(sys_normal_disable))
const tableColumns = schema.tableColumns

function listFn(pageRequest) {
  const param = { ...(pageRequest?.param || {}), dictType: dictType.value }
  return pageDictData({ ...pageRequest, param })
}

function exportFn(snapshot) {
  return exportData({ ...(snapshot || {}), dictType: dictType.value })
}

function openAdd() {
  baseOpenAdd()
  form.dictType = dictType.value
  form.listClass = form.listClass || 'default'
}
</script>
