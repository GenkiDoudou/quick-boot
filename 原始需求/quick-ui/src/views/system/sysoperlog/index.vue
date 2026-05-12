<template>
  <div class="app-container">
    <!-- 操作日志表格 -->
    <C7JsonTable
      ref="tableRef"
      :listFunction="listOperlog"
      :tableColumns="tableColumns"
      :searchColumns="searchColumns"
      :tableProps="tableProps"
      rowsKey="data.records"
      totalKey="data.total"
      @addBtnHandle="handleView"
      @refreshDataList="refreshData"
    >
      <template #status="scope">
        <span :style="scope.row.status != 200 ? 'color: red':''">{{ scope.row.status }}</span>
      </template>
      <template #table-operate="scope">
        <C7Button
          type="primary"
          link
          icon="View"
          @click="handleView(scope.row)"
          v-hasPermi="['system:operlog:query']"
        >
          查看
        </C7Button>
      </template>
    </C7JsonTable>

    <!-- 查看详情弹窗 -->
    <add-or-update 
      :key="addKey" 
      ref="addOrUpdateRef" 
      @refreshDataList="refreshData"
    />
  </div>
</template>


<script setup lang="ts">
import { reactive, ref, nextTick } from "vue";
import { C7JsonTable, C7Button } from '@c7-plus';
import AddOrUpdate from "./add-or-update.vue";
import { listOperlog } from '@/api/system/operlog.js';
import {useDict} from '@/composables/useDict';
import {useUtils} from '@/composables/useUtils';

// 使用 composables 替代 getCurrentInstance
const utils = useUtils();

// 表格引用
const tableRef = ref();
const addOrUpdateRef = ref();
const addKey = ref(0);

// 搜索字段配置
const searchColumns = ref([
  {
    label: "方法名称",
    prop: "method",
    type: "input",
    placeholder: "请输入方法名称"
  },
  {
    label: "操作人员",
    prop: "operName",
    type: "input",
    placeholder: "请输入操作人员"
  },
  {
    label: "请求URL",
    prop: "operUrl",
    type: "input",
    placeholder: "请输入请求URL"
  },
  {
    label: "主机地址",
    prop: "operIp",
    type: "input",
    placeholder: "请输入主机地址"
  },
  {
    label: "状态",
    prop: "status",
    type: "select",
    dictType: "sys_common_status"
  },
  {
    label: "操作时间",
    prop: "operTimes",
    type: "daterange",
    placeholder: "请选择操作时间",
    rangeSeparator: "至",
    startPlaceholder: "开始日期",
    endPlaceholder: "结束日期",
    valueFormat: "YYYY-MM-DD"
  }
]);

// 表格列配置
const tableColumns = ref([

  {
    label: "方法名称",
    prop: "method",
    showOverflowTooltip: true
  },
  {
    label: "请求方式",
    prop: "requestMethod",
    width: 100
  },
  {
    label: "操作人员",
    prop: "operName",
    width: 120
  },
  {
    label: "主机地址",
    prop: "operIp",
    width: 130
  },

  {
    label: "操作地点",
    prop: "operLocation",
    width: 130
  },
  {
    label: "状态码",
    prop: "status",
    width: 80
  },
  {
    label: "操作时间",
    prop: "operTime",
    width: 170
  },
  {
    label: "耗时",
    prop: "costTime",
    width: 60
  },
  {
    label: "操作",
    prop: "table-operate",
    width: 100,
    fixed: "right"
  }
]);

// 表格配置
const tableProps = ref({
  selection: false,
  showAdd: false,
  showEdit: false,
  showDelete: false,
  showRefresh: true,
  showExport: proxy.checkPermission('system:operlog:export'),
  showImport: false,
  border: true,
  stripe: true,
  height: 'auto'
});

// 事件处理函数
const handleView = (row) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(row.id);
  });
};

const refreshData = () => {
  tableRef.value?.getDataList();
};

</script>

<style scoped>


</style>
