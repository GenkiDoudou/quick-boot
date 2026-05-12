<template>
  <div class="app-container">
    <C7SwitchForm
        :showIndexs="showIndexs"
        v-model="showIndex"
        @update:modelValue="console.log($event)"
    >
      <template #table>
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
              plain
              icon="Download"
              :disabled="selectedIds.length === 0"
              @click="generatorHandler()"
              v-hasPermi="['generator:gen:code']"
            >
              生成
            </C7Button>
            <C7Button
              type="primary"
              plain
              icon="Plus"
              @click="open('1')"
              v-hasPermi="['generator:gen:import']"
            >
              创建
            </C7Button>
            <C7Button
              type="info"
              plain
              icon="Upload"
              @click="open('2')"
              v-hasPermi="['generator:gen:import']"
            >
              导入
            </C7Button>
          </template>

          <template #rowOperate="{ row }">
            <C7ButtonGroup mode="inline">
              <C7Button
                link
                type="primary"
                icon="View"
                @click="previewCodeHandler(row.id)"
                v-hasPermi="['generator:gen:preview']"
              >
                预览
              </C7Button>
              <C7Button
                link
                type="primary"
                icon="Edit"
                @click="editTableHandler(row.id)"
                v-hasPermi="['generator:gen:edit']"
              >
                修改
              </C7Button>
              <C7Button
                link
                type="danger"
                icon="Delete"
                :clickFunction="() => handleDelete(row.id)"
                :confirm="true"
                confirmMessage="确认要删除该表吗？"
                successMessage="删除成功"
                v-hasPermi="['generator:gen:remove']"
              >
                删除
              </C7Button>
              <C7Button
                link
                type="warning"
                icon="Refresh"
                :clickFunction="() => syncTableHandler(row.id)"
                :confirm="true"
                confirmMessage="确认要强制同步数据吗？"
                successMessage="同步成功"
                v-hasPermi="['generator:gen:edit']"
              >
                同步
              </C7Button>
              <C7Button
                link
                type="success"
                icon="Download"
                :clickFunction="() => generatorHandler(row)"
                :confirm="true"
                confirmMessage="确定进行代码生成操作?"
                successMessage="生成成功"
                v-hasPermi="['generator:gen:code']"
              >
                生成代码
              </C7Button>
            </C7ButtonGroup>
          </template>
        </C7JsonTable>

        <!-- 导入-->
        <ImportTable ref="importRef" :key="addKey" @refreshDataList="refreshDataList"></ImportTable>
        <!-- 创建表 -->
        <create-table ref="createTableRef" :key="addKey" @refreshDataList="refreshDataList"
                      @close="showIndex ='table'"></create-table>
      </template>

      <template #edit>
        <edit-table :key="addKey" ref="editTableRef" @refreshDataList="refreshDataList"
                    @close="showIndex ='table'"></edit-table>
      </template>

      <template #preview>
        <preview-code :key="addKey" ref="previewCodeRef" @close="showIndex ='table'"></preview-code>
      </template>
    </C7SwitchForm>
  </div>
</template>
<script setup>
import {C7SwitchForm, C7JsonTable, C7Button, C7ButtonGroup} from "@c7-plus";
import ImportTable from "@/views/tool/gen/ImportTable.vue";
import EditTable from "@/views/tool/gen/editTable.vue";
import {ref, nextTick, getCurrentInstance, reactive} from "vue";
import {getToken} from "@/utils/auth.js";
import PreviewCode from "@/views/tool/gen/previewCode.vue";
import createTable from "@/views/tool/gen/createTable.vue";

const {proxy} = getCurrentInstance();
import baseService from "@/service/baseService.js";
import {ElMessage, ElMessageBox} from "element-plus";

const showIndexs = ref([{
  name: 'table',
  header: false
},
  {
    name: 'edit',
    header: true,
    title: "修改表",
    closeIndex: "table"
  },
  {
    name: 'preview',
    header: true,
    title: "预览代码",
    closeIndex: "table"
  }])

const showIndex = ref("table")

// 数据相关
const tableRef = ref();
const selectedIds = ref([]);

// 搜索字段配置
const searchColumns = ref([
  { label: "表名称", prop: "tableName", type: "input", placeholder: "请输入表名称" },
  { label: "表描述", prop: "tableComment", type: "input", placeholder: "请输入表描述" },
  { label: "实体类名称", prop: "className", type: "input", placeholder: "请输入实体类名称" }
]);

// 表格列配置
const tableColumns = ref([
  { label: "表名称", prop: "tableName"},
  { label: "表描述", prop: "tableComment", showOverflowTooltip: true },
  { label: "实体类名称", prop: "className" },
  { label: "创建时间", prop: "createTime" },
  { label: "操作", prop: "operate", slotName: "rowOperate", width: 300, fixed: "right" }
]);

// 表格属性配置
const tableProps = ref({
  selection: true,
  showAdd: proxy.checkPermission('generator:gen:import'),
  showEdit: proxy.checkPermission('generator:gen:edit'),
  showDelete: proxy.checkPermission('generator:gen:remove'),
  showRefresh: true,
  showExport: proxy.checkPermission('generator:gen:export'),
  showImport: proxy.checkPermission('generator:gen:import'),
  border: true,
  stripe: true,
  height: 'auto'
});
// 获取数据列表
const getDataList = async (params) => {
  try {
    const response = await baseService.get("/generator/gentable/list", params);
    return response;
  } catch (error) {
    console.error('获取数据列表失败:', error);
    return { rows: [], total: 0 };
  }
};

// 选择变化处理
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id);
};

// 刷新数据列表
const refreshDataList = () => {
  if (tableRef.value) {
    tableRef.value.getDataList();
  }
};

// 删除处理
const handleDelete = (id) => {
  return baseService.delete(`/generator/gentable/${id}`).then((response) => {
    refreshDataList();
    return { code: 200, msg: '删除成功', data: response };
  }).catch((error) => {
    proxy.$message.error('删除失败');
    throw error;
  });
};

// 批量删除处理
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    proxy.$message.warning('请选择要删除的数据');
    return;
  }
  
  proxy.$confirm('确认要删除选中的表吗？', "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  }).then(() => {
    baseService.delete("/generator/gentable", { ids: selectedIds.value }).then(() => {
      proxy.$message.success('删除成功');
      refreshDataList();
    });
  });
};

// 新增处理
const handleAdd = () => {
  open('1'); // 打开创建表页面
};

// 编辑处理
const handleEdit = () => {
  if (selectedIds.value.length === 1) {
    editTableHandler(selectedIds.value[0]);
  } else {
    proxy.$message.warning('请选择一个表进行编辑');
  }
};

const addKey = ref(0);
const importRef = ref(false);
const createTableRef = ref();

const open = (type) => {
  addKey.value++;
  if (type == '1') {
    // 打开创建页面
    nextTick(() => {
      createTableRef.value.init();
    })
  } else if (type == '2') {
    nextTick(() => {
      importRef.value.init();
    })
  }
}

const syncTableHandler = (tableId) => {
  return baseService.get("/generator/gentable/tableSyn/" + tableId).then((response) => {
    return { code: 200, msg: '同步成功', data: response };
  }).catch((error) => {
    proxy.$message.error('同步失败');
    throw error;
  });
}

const editTableRef = ref()

const editTableHandler = (id) => {
  showIndex.value = 'edit'
  nextTick(() => {
    editTableRef.value.init(id)
  })
}


// 代码生成
const generatorHandler = (row) => {
  if (!row && selectedIds.value.length === 0) {
    ElMessage.warning({
      message: '请选择操作项',
      duration: 500
    });
    return Promise.reject('请选择操作项');
  }
  
  let tableIds = (row ? [row.id] : selectedIds.value).join(",");
  console.log(tableIds)
  
  let genType = "0";
  if (row) {
    genType = row.genType;
  }
  
  if (genType == '0') {
    let token = getToken();
    window.location.href = import.meta.env.VITE_APP_BASE_API + "/generator/gentable/generator/download?tableIds=" + tableIds + "&Authorization=Bearer " + token
    return Promise.resolve({ code: 200, msg: '生成成功', data: null });
  } else {
    return baseService.get("/generator/gentable/generator?tableIds=" + tableIds).then((res) => {
      refreshDataList();
      return { code: 200, msg: '生成成功', data: res };
    }).catch((error) => {
      proxy.$message.error('生成失败');
      throw error;
    });
  }
}

// 预览代码
const previewCodeRef = ref();

const previewCodeHandler = (id) => {
  showIndex.value = 'preview'
  nextTick(() => {
    previewCodeRef.value.init(id)
  })
}
</script>
<style scoped>

</style>
