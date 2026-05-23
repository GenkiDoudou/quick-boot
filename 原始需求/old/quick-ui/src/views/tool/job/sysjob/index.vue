<template>
  <div class="app-container">
    <!-- 表格 -->
    <C7JsonTable
        ref="tableRef"
        :listFunction="getDataList"
        :searchColumns="searchColumns"
        :tableColumns="tableColumns"
        :tableProps="tableProps"
        rowsKey="data.records"
        totalKey="data.total"
        @selection-change="handleSelectionChange"
        @addBtnHandle="handleAdd"
        @editBtnHandle="handleEdit"
        @deleteBtnHandle="handleBatchDelete"
    >
      <template #operate>
        <C7Button
            type="primary"
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['quartz:sysjob:add']"
        >
          新增
        </C7Button>
        <C7Button
            type="success"
            icon="Edit"
            :disabled="selectedIds.length !== 1"
            @click="handleEdit"
            v-hasPermi="['quartz:sysjob:edit']"
        >
          修改
        </C7Button>
        <C7Button
            type="danger"
            icon="Delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
            v-hasPermi="['quartz:sysjob:remove']"
        >
          删除
        </C7Button>
        <C7Button
            type="primary"
            plain
            icon="View"
            @click="sysJobLogHandler()"
            v-hasPermi="['quartz:sysjob:list']"
        >
          调度日志
        </C7Button>
      </template>

      <template #status="{ row }">
        <el-switch
            v-model="row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(row)"
        />
      </template>

      <template #rowOperate="{ row }">
        <C7ButtonGroup mode='inline'>
          <C7Button
              link
              type="primary"
              icon="Edit"
              @click="addOrUpdateHandle(row.id)"
              v-hasPermi="['quartz:sysjob:edit']"
          >
            修改
          </C7Button>
          <C7Button
              link
              type="danger"
              icon="Delete"
              @click="handleSingleDelete(row.id)"
              v-hasPermi="['quartz:sysjob:remove']"
          >
            删除
          </C7Button>
          <C7Button
              link
              type="info"
              icon="View"
              @click="addOrUpdateHandle(row.id,'2')"
              v-hasPermi="['quartz:sysjob:query']"
          >
            任务详细
          </C7Button>
          <C7Button
              link
              type="success"
              icon="CaretRight"
              :clickFunction="() => runHandle(row.id)"
              :confirm="true"
              confirmMessage="确认要立即执行一次任务吗？"
              successMessage="立即执行成功"
              v-hasPermi="['quartz:sysjob:run']"
          >
            执行一次
          </C7Button>
          <C7Button
              link
              type="warning"
              icon="Operation"
              @click="sysJobLogHandler(row.id)"
              v-hasPermi="['quartz:sysjoblog:list']"
          >
            调度日志
          </C7Button>
        </C7ButtonGroup>
      </template>
    </C7JsonTable>

    <!-- 弹窗, 新增 / 修改 -->
    <add-or-update :key="addKey" ref="addOrUpdateRef" @refreshDataList="refreshDataList"></add-or-update>

    <!-- 日志-->
    <sys-job-log :key="addKey" ref="sysJobLogRef"></sys-job-log>
  </div>
</template>


<script setup>
import {getCurrentInstance, nextTick, reactive, ref, computed} from "vue";
import {
  C7JsonTable,
  C7Button,
  C7Select,
  C7DictTag,
  C7ButtonGroup
} from "@c7-plus";
import AddOrUpdate from "./add-or-update.vue";
import sysJobLog from '../sysjoblog/index.vue'
import baseService from "@/service/baseService.js";

const {proxy} = getCurrentInstance();

// 获取字典数据
const dictData = proxy.useDict("sys_job_group", "sys_job_status", "sys_yes_no");

// 数据相关
const tableRef = ref();
const selectedIds = ref([]);

// 搜索列配置 - 使用计算属性确保字典数据正确传递
const searchColumns = computed(() => [
  {label: "任务名称", prop: "jobName", type: "input", placeholder: "请输入任务名称"},
  {
    label: "任务组名",
    prop: "jobGroup",
    type: "select",
    dataList: dictData.sys_job_group?.value || [],
    placeholder: "请选择任务组名"
  },
  {
    label: "状态",
    prop: "status",
    type: "select",
    dataList: dictData.sys_job_status?.value || [],
    placeholder: "请选择状态"
  }
]);

// 表格列配置 - 使用计算属性确保字典数据正确传递
const tableColumns = computed(() => [
  {label: "任务名称", prop: "jobName"},
  {label: "任务组名", prop: "jobGroup", columnType: "tag", dictList: dictData.sys_job_group?.value || []},
  {label: "调用对象", prop: "invokeTarget", showOverflowTooltip: true},
  {label: "参数", prop: "params", showOverflowTooltip: true},
  {label: "cron执行表达式", prop: "cronExpression", width: 180, showOverflowTooltip: true},
  {
    label: "是否并发执行",
    prop: "concurrent",
    columnType: "tag",
    dictList: dictData.sys_yes_no?.value || [],
    width: 120
  },
  {label: "状态", prop: "status", slotName: "status", width: 100},
  {label: "操作", prop: "operate", slotName: "rowOperate", width: 250, fixed: "right"}
]);

// 表格属性配置
const tableProps = ref({
  selection: true,
  showAdd: proxy.checkPermission('quartz:sysjob:add'),
  showEdit: proxy.checkPermission('quartz:sysjob:edit'),
  showDelete: proxy.checkPermission('quartz:sysjob:remove'),
  showRefresh: true,
  showExport: proxy.checkPermission('quartz:sysjob:export'),
  showImport: proxy.checkPermission('quartz:sysjob:import'),
  border: true,
  stripe: true,
  height: 'auto'
});

// 获取任务列表
const getDataList = async (params) => {
  try {
    const response = await baseService.get("/quartz/sysjob/list", params);
    return response;
  } catch (error) {
    console.error('获取任务列表失败:', error);
    return {rows: [], total: 0};
  }
};
// 弹窗相关
const addKey = ref(0);
const addOrUpdateRef = ref();
const sysJobLogRef = ref();

// 新增/修改处理
const addOrUpdateHandle = (id, type) => {
  if (!type) {
    type = '1';
  }
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(id, type);
  });
};

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id);
};

// 刷新数据列表
const refreshDataList = () => {
  if (tableRef.value) {
    tableRef.value.fetchData();
  }
};

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    proxy.$message.warning('请选择要删除的数据');
    return;
  }

  proxy.$confirm('确认要删除选中的任务吗？', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    baseService.delete("/quartz/sysjob", {ids: selectedIds.value}).then(() => {
      proxy.$message.success('删除成功');
      refreshDataList();
    });
  });
};

// 新增处理
const handleAdd = () => {
  addOrUpdateHandle(); // 打开新增任务页面
};

// 编辑处理
const handleEdit = () => {
  if (selectedIds.value.length === 1) {
    addOrUpdateHandle(selectedIds.value[0]);
  } else {
    proxy.$message.warning('请选择一个任务进行编辑');
  }
};

// 单个删除
const handleSingleDelete = (id) => {
  proxy.$confirm('确认要删除该任务吗？', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    baseService.delete(`/quartz/sysjob/${id}`).then(() => {
      proxy.$message.success('删除成功');
      refreshDataList();
    });
  });
};
// 修改状态
const handleStatusChange = (row) => {
  const id = row.id;
  if (!id) {
    return;
  }
  const status = row.status;
  const msg = status === '0' ? '启用' : '暂停';

  proxy.$confirm(`确认${msg}任务"${row.jobName}"吗？`, "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    baseService.get(`/quartz/sysjob/changeStatus/${id}/${status}`).then(() => {
      proxy.$message.success(`${msg}成功`);
    }).finally(() => {
      refreshDataList();
    });
  });
};

// 立即执行
const runHandle = (id) => {
  return baseService.get(`/quartz/sysjob/run/${id}`).then((response) => {
    return {code: 200, msg: '执行成功', data: response};
  })
};

// 日志处理
const sysJobLogHandler = (jobId) => {

  nextTick(() => {
    sysJobLogRef.value.init(jobId);
  });
};

// C7JsonTable 会自动初始化数据
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.search-form {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.operate-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.operate-bar .c7-button {
  margin-right: 10px;
}

/* 表格样式优化 */
:deep(.c7-json-table) {
  border-radius: 4px;
  overflow: hidden;
}

/* 按钮组样式 */
:deep(.c7-button-group) {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

:deep(.c7-button-group .c7-button) {
  margin: 0;
  padding: 5px 8px;
  font-size: 12px;
}

/* 状态开关样式 */
:deep(.c7-switch-form) {
  margin: 0;
}
</style>
