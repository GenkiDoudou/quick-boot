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
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageDictType, addType, updateType, removeType, refreshAllType,
  exportType, importType, downloadTypeImportTemplate, getType
} from '@/api/system/dict/type'
import useDictStore from '@/store/modules/dict'
import { useCrudPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/tier-a/dictType.schema'

defineOptions({ name: 'SysDictType' })

const { sys_normal_disable } = useDict('sys_normal_disable')
const router = useRouter()

const {
  tableRef, formRef, formVisible, isAdd, form, formRules,
  openAdd, openEdit, submitForm, removeRow
} = useCrudPage({
  idField: schema.rowKey,
  labelField: 'dictType',
  formInitial: schema.formInitial,
  formRules: schema.formRules,
  api: { add: addType, update: updateType, remove: removeType },
  loadDetail: async (row) => (await getType(row.dictId)).data
})

const defaultSearch = schema.defaultSearch
const searchColumns = computed(() => schema.buildSearchColumns(sys_normal_disable))
const tableColumns = schema.tableColumns

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
