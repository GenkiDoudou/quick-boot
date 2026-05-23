<template>
  <C7Dialog :visible="visibleRef" mode="dialog" title="日志查看"
             @close="visibleRef = false" :footer="false" :modalProps="{
               width:'80%'
             }">
    <div class="app-container">
      <!-- 表格 -->
      <C7JsonTable
        ref="tableRef"
        :listFunction="getDataList"
        :searchColumns="searchColumns"
        :tableColumns="tableColumns"
        :tableProps="tableProps"
        :initParam="initParam"
        rowsKey="data.records"
        totalKey="data.total"
        @selection-change="handleSelectionChange"
      >
        <template #operate>
          <C7Button
            type="danger"
            icon="Delete"
            @click="clearHandler()"
            v-hasPermi="['quartz:sysjoblog:remove']"
          >
            清空
          </C7Button>
        </template>
        
        <template #rowOperate="{ row }">
          <C7Button
            link
            type="primary"
            icon="View"
            @click="addOrUpdateHandle(row.id)"
            v-hasPermi="['quartz:sysjoblog:query']"
          >
            查看
          </C7Button>
        </template>
      </C7JsonTable>
      
      <!-- 弹窗, 新增 / 修改 -->
      <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="refreshDataList"></add-or-update>
    </div>
  </C7Dialog>
</template>

<script setup>
import {C7Dialog, C7JsonTable, C7Button, C7Select, C7DictTag} from "@c7-plus";
import {reactive, ref, toRefs, nextTick, getCurrentInstance, computed} from "vue";
import AddOrUpdate from "./add-or-update.vue";
import baseService from "@/service/baseService.js";

const {proxy} = getCurrentInstance();

// 获取字典数据
const dictData = proxy.useDict("sys_job_group", "sys_common_status");

// 数据相关
const tableRef = ref();

// 搜索列配置
const searchColumns = ref([
  { label: "任务名称", prop: "jobName", type: "input", placeholder: "请输入任务名称" },
  { label: "任务组名", prop: "jobGroup", type: "select", dictType: "sys_job_group", placeholder: "请选择任务组名" },
  { label: "调用对象", prop: "invokeTarget", type: "input", placeholder: "请输入调用对象" },
  { label: "执行状态", prop: "status", type: "select", dictType: "sys_common_status", placeholder: "请选择执行状态" }
]);

// 表格列配置 - 使用计算属性确保字典数据正确传递
const tableColumns = computed(() => [
  { label: "任务名称", prop: "jobName", width: 150 },
  { label: "任务组名", prop: "jobGroup", columnType: "tag", dictList: dictData.sys_job_group?.value || [], width: 120 },
  { label: "调用对象", prop: "invokeTarget", width: 200, showOverflowTooltip: true },
  { label: "参数", prop: "params", width: 150, showOverflowTooltip: true },
  { label: "执行状态", prop: "status", columnType: "tag", dictList: dictData.sys_common_status?.value || [], width: 100 },
  { label: "耗时", prop: "timeConsuming", width: 100 },
  { label: "开始时间", prop: "startTime", width: 160 },
  { label: "结束时间", prop: "endTime", width: 160 },
  { label: "操作", prop: "operate", slotName: "rowOperate", width: 100, fixed: "right" }
]);

// 表格属性配置
const tableProps = ref({
  selection: false,
  showAdd: false,
  showEdit: false,
  showDelete: proxy.checkPermission('quartz:sysjoblog:remove'),
  showRefresh: true,
  showExport: proxy.checkPermission('quartz:sysjoblog:export'),
  showImport: false,
  border: true,
  stripe: true,
  height: 'auto'
});

// 获取任务日志列表
const getDataList = async (params) => {
  try {
    const response = await baseService.get("/quartz/sysjoblog/list", params);
    return response;
  } catch (error) {
    console.error('获取任务日志列表失败:', error);
    return { rows: [], total: 0 };
  }
};

// 选择变化处理
const handleSelectionChange = (selection) => {
  // 日志查看不需要选择功能
};

// 刷新数据列表
const refreshDataList = () => {
  if (tableRef.value) {
    tableRef.value.fetchData();
  }
};

// 弹窗相关
const addKey = ref(0);
const addOrUpdateRef = ref();
const jobIdRef = ref('');
const visibleRef = ref(false);
const initParam = ref({
  jobId: ''
});

// 新增/修改处理
const addOrUpdateHandle = (id) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(id);
  });
};

// 初始化方法
const init = (jobId) => {
  console.log("jobId", jobId);
  visibleRef.value = true;
  if (jobId) {
    jobIdRef.value = jobId;
    initParam.value.jobId = jobId;
  } else {
    initParam.value.jobId = '';
    jobIdRef.value = '';
  }
  console.log("initParam", initParam.value);

  nextTick(() => {
    getDataList();
  });
};

// 清空所有日志
const clearHandler = () => {
  proxy.$confirm('确定要清空所有的日志吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    baseService.delete("/quartz/sysjoblog/clean?jobId=" + jobIdRef.value).then(() => {
      proxy.$message.success('清空成功!');
      refreshDataList();
    });
  }).catch(() => {
    proxy.$message({});
  });
};

defineExpose({
  init
});
</script>
