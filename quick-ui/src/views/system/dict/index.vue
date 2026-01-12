<template>
  <div class="app-container">
    <c7-switch-form
        :showIndexs="showIndexs"
        v-model="showIndex"
        @update:modelValue="console.log($event)"
    >
      <template #list>
        <!-- 字典管理表格 -->
        <c7-json-table
            ref="tableRef"
            :listFunction="listType"
            :tableColumns="tableColumns"
            :delete-function ="delType"
            :searchColumns="searchColumns"
            :tableProps="tableProps"
            rowsKey="data.records"
            totalKey="data.total"
            :export-function="exportDict"
            @addBtnHandle="handleAdd"
            @editBtnHandle="handleEdit"
        >
          <template #dictType="scope">
            <el-link type="primary" @click="toData(scope.row)">{{ scope.row.dictType }}</el-link>
          </template>
          <template #table-operate="scope">
            <!-- 使用 c7-button-group 和 c7-button 组件 -->
            <c7-button-group :maxVisible="4" mode="auto" trigger="click" >
              <c7-button 
                btnType="edit" 
                @click="handleEdit(scope.row)" 
                v-hasPermi="['system:dict:edit']"
              />

              <c7-button 
                btnType="delete" 
                :confirm="true"
                :confirmMessage="`确定删除字典类型${scope.row.dictType}吗？`"
                @click="tableRef.handleDelete(scope.row.id)"
                v-hasPermi="['system:dict:remove']"
              />


            </c7-button-group>
          </template>
        </c7-json-table>

        <!-- 弹窗, 新增 / 修改 -->
        <add-or-update ref="addOrUpdateRef" @refreshDataList="tableRef.getDataList()"></add-or-update>
      </template>

      <template #data>
        <!--字典项列表 -->

        <data-list ref="dataRef" :key="addKey"></data-list>
      </template>

    </c7-switch-form>
  </div>
</template>
<script setup>
import {C7JsonTable, C7Button, C7SwitchForm, C7ButtonGroup} from "@/components/c7";
import {reactive, ref, toRefs, nextTick, getCurrentInstance} from "vue";
import {ElMessage, ElMessageBox} from 'element-plus';
import AddOrUpdate from "./add-or-update.vue";
import {listType,delType,exportDict} from '@/api/system/dict/type.js';
import DataList from "@/views/system/dict/data.vue";
// 搜索字段
const searchColumns = ref([

  {
    label: '字典名称',
    prop: 'dictName',
    type: 'input',
    // columnsWidth: "80px"
  },
  {
    label: '字典类型',
    prop: 'dictType',
    type: 'input',
  },
  {
    prop: "status",
    label: "状态",
    type: 'select',
    dictType: "sys_normal_disable"
  },

]);


// 获取当前实例和字典数据
const {proxy} = getCurrentInstance();
const dictData = proxy.useDict("sys_normal_disable");
const sys_normal_disable = dictData.sys_normal_disable;

// 表格引用
const tableRef = ref();
const addOrUpdateRef = ref();
const addKey = ref(0);

// 表格列配置
const tableColumns = ref([
  {
    label: "字典名称",
    prop: "dictName",
  },
  {
    label: "字典类型",
    prop: "dictType",
    columnType: 'slot',
    slotName: 'dictType',
  },
  {
    label: "状态",
    prop: "status",
    columnType: 'tag',
    dictList: sys_normal_disable,
  },
  {
    label: "备注",
    prop: "remark",
    showOverflowTooltip: true,
  },
  {
    label: "创建时间",
    prop: "createTime",
  },
  {
    label: "操作",
    prop: "table-operate",


  }
]);


// 表格配置
const tableProps = ref({
  selection: true,
  showAdd: proxy.checkPermission('system:dict:add'),
  showEdit: proxy.checkPermission('system:dict:edit'),
  showDelete: proxy.checkPermission('system:dict:remove'),
  showRefresh: true,
  showExport: proxy.checkPermission('system:dict:export'),
  showImport: proxy.checkPermission('system:dict:import'),
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





// 查看字典详情
const handleView = (row) => {
  ElMessage.info(`查看字典: ${row.dictType}`);
  // 这里可以添加查看详情的逻辑
};


const showIndexs = ref([{
  title: '列表',
  name: 'list',

},
  {
    title: '字典项',
    name: 'data',
    header: true,
    closeIndex: 'list'
  }])
const showIndex = ref("list")
const dataRef = ref();

// 跳转到字典项管理页面
const toData = (row) => {
  addKey.value++;
  showIndex.value = "data";
  nextTick(() => {
    console.log(dataRef)
    dataRef.value.init(row.dictType);
  })
}

</script>

<style scoped>


</style>
