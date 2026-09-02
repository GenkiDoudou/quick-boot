<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="page${className}"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="del${className}"
      :row-key="rowKey"
      :show-add-button="true"
      :add-button-permi="[permPrefix + ':add']"
      :delete-button-permi="[permPrefix + ':remove']"
      :on-add="openAdd"
      :export-function="export${className}"
      :export-button-permi="[permPrefix + ':export']"
      export-default-file-name="${businessName}.xlsx"
    >
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="[permPrefix + ':edit']" @click="openEdit(row)">修改</el-button>
        <el-button link type="danger" v-hasPermi="[permPrefix + ':remove']" @click="removeRow(row)">删除</el-button>
      </template>
    </C7JsonTable>

    <C7Dialog v-model="formVisible" :title="isAdd ? '新增' : '修改'" width="560px" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
<#list editColumns as col>
        <el-form-item label="${col.columnComment!}" prop="${col.javaField}">
          <el-input v-model="form.${col.javaField}" <#if col.htmlType?? && col.htmlType == "textarea">type="textarea" :rows="3"</#if> />
        </el-form-item>
</#list>
      </el-form>
    </C7Dialog>
  </div>
</template>

<script setup>
/**
 * ${tableComment!}：schema + useCrudPage 驱动的标准 CRUD 页（codegen）。
 */
import {
  page${className}, add${className}, update${className}, del${className}, export${className}, get${className}
} from '@/api/${moduleName}/${businessName}'
import { useCrudPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/${moduleName}/${businessName}.schema'

defineOptions({ name: '${className}' })

const rowKey = schema.rowKey
const permPrefix = schema.permPrefix
const defaultSearch = schema.defaultSearch
const searchColumns = schema.searchColumns
const tableColumns = schema.tableColumns

const {
  tableRef, formRef, formVisible, isAdd, form, formRules,
  openAdd, openEdit, submitForm, removeRow
} = useCrudPage({
  idField: schema.rowKey,
  formInitial: schema.formInitial,
  formRules: schema.formRules,
  api: { add: add${className}, update: update${className}, remove: del${className} },
  loadDetail: async (row) => (await get${className}(row[schema.rowKey])).data
})
</script>
