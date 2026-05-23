<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="${pkField!"id"}"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :show-add-button="true"
      :show-edit-button="true"
      :show-delete-button="true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #action="{ row }">
        <el-button link @click="openEdit(row)">修改</el-button>
        <c7-button
          btn-type="delete"
          link
          confirm
          :confirm-message="'确认删除该记录吗？'"
          :click-function="() => removeRow(row)"
        />
      </template>
    </C7JsonTable>
  </div>
</template>

<script setup>
/**
 * ${tableComment!} 列表（代码生成；结构对齐 views/system/config/index.vue）。
 */
import { ref } from 'vue'
import { list${className}, del${className} } from '@/api/${moduleName}/${businessName}'

defineOptions({ name: '${className}' })

const tableRef = ref(null)

const defaultSearchParam = {
<#list queryColumns as col>
  ${col.javaField}: '',
</#list>
}

const searchColumns = ref([
<#list queryColumns as col>
  { prop: '${col.javaField}', label: '${col.columnComment!}', type: 'input', span: 8, props: { placeholder: '请输入${col.columnComment!}', clearable: true } },
</#list>
])

const tableColumns = ref([
<#list listColumns as col>
  { prop: '${col.javaField}', label: '${col.columnComment!}', minWidth: 120, showOverflowTooltip: true },
</#list>
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 160, fixed: 'right' }
])

function listFunction(params) {
  return list${className}(params)
}

function batchDeleteFunction(ids) {
  return del${className}(ids || [])
}

function openEdit(row) {
  // TODO: 对接编辑弹窗（参照 system/config/index.vue）
}

function removeRow(row) {
  return del${className}([row.${pkField!"id"}]).then(() => tableRef.value?.refreshData?.())
}
</script>
