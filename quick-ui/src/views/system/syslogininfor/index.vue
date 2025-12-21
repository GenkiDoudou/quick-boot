<template>
  <div class="app-container">
    <!-- 登录日志表格 -->
    <C7JsonTable
      ref="tableRef"
      :listFunction="listLogininfor"
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
          v-hasPermi="['system:logininfor:query']"
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
import { reactive, ref, nextTick, getCurrentInstance } from "vue";
import { C7JsonTable, C7Button } from '@/components/c7';
import AddOrUpdate from "./add-or-update.vue";
import { listLogininfor } from '@/api/system/logininfor.js';

// 获取当前实例和权限检查函数
const { proxy } = getCurrentInstance();
const dictData = proxy.useDict("sys_normal_disable");
const sys_normal_disable = dictData.sys_normal_disable;
// 表格引用
const tableRef = ref();
const addOrUpdateRef = ref();
const addKey = ref(0);

// 搜索字段配置
const searchColumns = ref([
  {
    label: "用户账号",
    prop: "userName",
    type: "input",
    placeholder: "请输入用户账号"
  },
  {
    label: "登录IP地址",
    prop: "ipaddr",
    type: "input",
    placeholder: "请输入登录IP地址"
  },
  {
    label: "登录时间",
    prop: "loginTimes",
    type: "daterange",
    placeholder: "请选择登录时间",
    rangeSeparator: "至",
    startPlaceholder: "开始日期",
    endPlaceholder: "结束日期",
    valueFormat: "YYYY-MM-DD"
  }
]);

// 表格列配置
const tableColumns = ref([

  {
    label: "用户账号",
    prop: "userName",
    showOverflowTooltip: true
  },
  {
    label: "类型",
    prop: "loginType",
    showOverflowTooltip: true
  },
  {
    label: "结果",
    prop: "loginResult",
    showOverflowTooltip: true,
    columnType: 'tag',
    dictList: sys_normal_disable,
  },
  {
    label: "登录IP地址",
    prop: "ipaddr",
    showOverflowTooltip: true
  },
  {
    label: "客户端",
    prop: "clientId",
    showOverflowTooltip: true
  },
  {
    label: "登录地点",
    prop: "loginLocation",
    showOverflowTooltip: true
  },
  {
    label: "浏览器类型",
    prop: "browser",
    showOverflowTooltip: true
  },
  {
    label: "操作系统",
    prop: "os",
    showOverflowTooltip: true
  },
  {
    label: "登录时间",
    prop: "loginTime",
    width: 180
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
  showDelete: proxy.checkPermission('system:logininfor:remove'),
  showRefresh: true,
  showExport: proxy.checkPermission('system:logininfor:export'),
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

