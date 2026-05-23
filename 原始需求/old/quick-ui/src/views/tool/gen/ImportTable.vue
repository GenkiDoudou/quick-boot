<!--导入表-->
<template>
  <C7Dialog 
    :visible="visibleRef" 
    mode="dialog" 
    title="导入表" 
    @close="visibleRef = false"
    :footer="true" 
    @submit="batchImport">
    <div class="app-container">
      <C7JsonTable
        ref="tableRef"
        :listFunction="getDataList"
        :searchColumns="searchColumns"
        :tableColumns="tableColumns"
        :tableProps="tableProps"
        row-key="tableName"
        @selection-change="handleSelectionChange"
      >
        <template #operate>
          <C7Button type="primary" @click="batchImport">
            导入
          </C7Button>
        </template>
      </C7JsonTable>
    </div>
  </C7Dialog>
</template>

<script setup>
import {C7Dialog, C7Button, C7JsonTable} from "@c7-plus"
import {reactive, ref, toRefs, nextTick, getCurrentInstance} from "vue";
import baseService from "@/service/baseService.js";

const {proxy} = getCurrentInstance();
const emit = defineEmits(["refreshDataList"]);
const visibleRef = ref(false);

// 数据相关
const tableRef = ref();
const selectedIds = ref([]);

// 搜索字段配置
const searchColumns = ref([
  { label: "表名称", prop: "tableName", type: "input", placeholder: "请输入表名称" },
  { label: "表注释", prop: "tableComment", type: "input", placeholder: "请输入表注释" }
]);

// 表格列配置
const tableColumns = ref([
  { label: "表名称", prop: "tableName", width: 200 },
  { label: "表注释", prop: "tableComment", width: 250, showOverflowTooltip: true }
]);

// 表格属性配置
const tableProps = ref({
  selection: true,
  showAdd: false,
  showEdit: false,
  showDelete: false,
  showRefresh: true,
  showExport: false,
  showImport: false
});

// 获取数据列表
const getDataList = async (params) => {
  try {
    const response = await baseService.get("/generator/gentable/dbTables", params);
    return response;
  } catch (error) {
    console.error('获取数据列表失败:', error);
    return { rows: [], total: 0 };
  }
};

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.tableName);
};
const init = () => {
  visibleRef.value = true;
  nextTick(() => {
    if (tableRef.value) {
      tableRef.value.getDataList();
    }
  })
}

const batchImport = () => {
  if (selectedIds.value.length == 0) {
    proxy.$modal.msgError(`请先选中!`);
    return false;
  }
  baseService.post("/generator/gentable/importTable", selectedIds.value).then(res => {
    proxy.$modal.msgSuccess("操作成功");
    visibleRef.value = false;
    emit("refreshDataList");
  })
}

defineExpose({
  init
})
</script>
<style scoped></style>
