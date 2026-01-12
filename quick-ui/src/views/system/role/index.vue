<template>
  <div class="app-container">
    <!-- 角色管理表格 -->
    <c7-json-table
      ref="tableRef"
      :listFunction="listRole"
      :tableColumns="tableColumns"
      :searchColumns="searchColumns"
      :tableProps="tableProps"
      :delete-function="delRole"
      rowsKey="data.records"
      totalKey="data.total"
      @addBtnHandle="handleAdd"
      @editBtnHandle="handleEdit"
      :exportFunction="exportRole"
    >
      <!-- 操作列插槽 -->
      <template #table-operate="scope">
        <C7ButtonGroup>
          <C7Button 
            type="primary" 
            link 
            icon="Edit" 
            @click="handleEdit(scope.row)"
            v-hasPermi="['system:role:edit']"
          >
            修改
          </C7Button>
          <C7Button 
            type="danger" 
            link 
            icon="Delete" 
            @click="tableRef.handleDelete(scope.row.id)"
            v-hasPermi="['system:role:remove']"
          >
            删除
          </C7Button>
        </C7ButtonGroup>
      </template>
    </c7-json-table>

    <!-- 新增/编辑角色弹窗 -->
    <add-or-update
      :key="addKey"
      ref="addOrUpdateRef"
      @refreshDataList="tableRef.refreshData()"
    />
  </div>
</template>


<script setup>
import { ref, getCurrentInstance, nextTick } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import AddOrUpdate from "./add-or-update.vue";
import { listRole, delRole,exportRole } from '@/api/system/role.js';
import { C7JsonTable, C7Button, C7ButtonGroup } from '@/components/c7';

// 获取当前实例和字典数据
const { proxy } = getCurrentInstance();
const {sys_normal_disable,role_data_scope} = proxy.useDict("sys_normal_disable","role_data_scope");

// 表格引用
const tableRef = ref();
const addOrUpdateRef = ref();
const addKey = ref(0);

// 搜索字段配置
const searchColumns = ref([
  {
    prop: "roleName",
    label: "角色名称",
    type: "input"
  },
  {
    prop: "roleKey", 
    label: "权限字符",
    type: "input"
  },
  {
    prop: "status",
    label: "状态",
    type: "select",
    dataList: sys_normal_disable
  }
]);

// 表格列配置
const tableColumns = ref([
  {
    label: "角色名称",
    prop: "roleName",
    showOverflowTooltip: true
  },
  {
    label: "权限字符",
    prop: "roleKey",
    showOverflowTooltip: true
  },
  {
    label: "数据权限",
    prop: "dataScope",
    showOverflowTooltip: true,
    columnType: 'tag',
    dictList: role_data_scope
  },
  {
    label: "显示顺序",
    prop: "roleSort"
  },
  {
    label: "状态",
    prop: "status",
    columnType: 'tag',
    dictList: sys_normal_disable
  },
  {
    label: "创建时间",
    prop: "createTime"
  },
  {
    label: "操作",
    prop: "table-operate",
    width: 160,
    fixed: "right"
  }
]);

// 表格配置
const tableProps = ref({
  selection: true,
  showAdd: proxy.checkPermission('system:role:add'),
  showEdit: proxy.checkPermission('system:role:edit'),
  showDelete: proxy.checkPermission('system:role:remove'),
  showRefresh: true,
  showExport: proxy.checkPermission('system:role:export'),
  showImport: proxy.checkPermission('system:role:import'),
  border: true,
  stripe: true,
  height: 'auto'
});

// 事件处理函数
const handleAdd = () => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init();
  });
};

const handleEdit = (row) => {
  addKey.value++;
  nextTick(() => {
    addOrUpdateRef.value.init(row.id);
  });
};



</script>
