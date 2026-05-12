<template>
  <div class="app-container">
    <!-- 字典数据表格 -->
    <c7-json-table
      ref="tableRef"
      :init="false"
      :initParam="initParam"
      :listFunction="listDictData"
      :tableColumns="tableColumns"
      :searchColumns="searchColumns"
      :tableProps="tableProps"
      rowsKey="data.records"
      totalKey="data.total"
      @addBtnHandle="handleAdd"
      @editBtnHandle="handleEdit"
      :delete-function="delDictData"
    >
      <template #table-operate="scope">
        <C7ButtonGroup>
          <C7Button
            link
            type="primary"
            icon="Edit"
            @click="handleEdit(scope.row)"
            v-hasPermi="['system:dict:edit']"
          >
            编辑
          </C7Button>
          <C7Button
            link
            type="danger"
            icon="Delete"
            @click="tableRef.handleDelete(scope.row.id)"
            v-hasPermi="['system:dict:remove']"
          >
            删除
          </C7Button>
        </C7ButtonGroup>
      </template>
    </c7-json-table>

    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update
      :key="addKey"
      ref="addOrUpdateRef"
      @refreshDataList="tableRef.refreshData()"
    />
  </div>
</template>
<script setup>
import {C7JsonTable, C7JsonForm, C7Button, C7ButtonGroup} from "@c7-plus";
import { reactive, ref, toRefs, getCurrentInstance, nextTick } from "vue";
import AddOrUpdate from "./data-add-or-update.vue";
import { listData as listDictData, delData as delDictData } from '@/api/system/dict/data.js';

// 获取当前实例和字典数据
const { proxy } = getCurrentInstance();
const dictData = proxy.useDict("sys_normal_disable","role_data_scope");
const sys_normal_disable = dictData.sys_normal_disable;

// 表格引用
const tableRef = ref();
const addOrUpdateRef = ref();
const addKey = ref(0);

// 搜索字段配置
const searchColumns = ref([
  {
    label: "字典标签",
    prop: "dictLabel",
    type: "input",
    placeholder: "请输入字典标签"
  },
  {
    label: "字典键值",
    prop: "dictValue",
    type: "input",
    placeholder: "请输入字典键值"
  },
  {
    label: "状态",
    prop: "status",
    type: "select",
    dictType: "sys_normal_disable"
  }
]);

// 表格列配置
const tableColumns = ref([
  {
    label: "字典类型",
    prop: "dictType",
    showOverflowTooltip: true
  },
  {
    label: "字典标签",
    prop: "dictLabel",
    showOverflowTooltip: true
  },
  {
    label: "字典键值",
    prop: "dictValue",
    showOverflowTooltip: true
  },
  {
    label: "字典排序",
    prop: "dictSort",
    width: 100
  },
  {
    label: "状态",
    prop: "status",
    columnType: 'tag',
    dictList: sys_normal_disable
  },
  {
    label: "备注",
    prop: "remark",
    showOverflowTooltip: true
  },
  {
    label: "创建时间",
    prop: "createTime",
    width: 180
  },
  {
    label: "操作",
    prop: "table-operate",
    width: 260,
    fixed: "right"
  }
]);

// 表格配置
const tableProps = ref({
  selection: true,
  showAdd: true,
  showEdit: true,
  showDelete: true,
  showRefresh: true
});

// 事件处理函数
const handleAdd = () => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(initParam.value.dictType);
  });
};

const handleEdit = (row) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(row.dictType, row.id);
  });
};

const  initParam = ref({})
// 初始化函数
const init = (dictType) => {
  console.log('init called with dictType:', dictType);
  initParam.value = {
    dictType: dictType,
  }
  console.log('initParam set to:', initParam.value)
  // 不需要手动调用getDataList，initParam 变化会自动触发
};

defineExpose({
  init
})
</script>

<style scoped>


</style>
