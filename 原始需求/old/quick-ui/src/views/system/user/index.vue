<template>
  <div class="app-container">
    <!-- 用户管理表格 -->
    <c7-json-table
        ref="tableRef"
        :listFunction="listUser"
        :delete-function="delUser"
        :tableColumns="tableColumns"
        :searchColumns="searchColumns"
        :tableProps="tableProps"
        rowsKey="data.records"
        totalKey="data.total"
        :exportFunction="exportUser"
        @addBtnHandle="handleAdd"
        @editBtnHandle="handleEdit"
        @importBtnHandle="importBtnHandle"
    >
      <template #status="{ row }">
        <el-switch
            v-model="row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(row)"
        />
      </template>
      <!-- 操作列插槽-->
      <template #table-operate="scope">
        <C7ButtonGroup mode="inline">
          <C7Button
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(scope.row)"
              v-hasPermi="['system:user:edit']"
          >
            修改
          </C7Button>
          <C7Button
              type="danger"
              link
              icon="Delete"
              @click="tableRef.handleDelete(scope.row.id)"
              v-hasPermi="['system:user:remove']"
          >
            删除
          </C7Button>
          <C7Button
              type="warning"
              link
              icon="Key"
              @click="handleResetPwd(scope.row)"
              v-hasPermi="['system:user:resetPwd']"
          >
            重置密码
          </C7Button>
        </C7ButtonGroup>
      </template>
    </c7-json-table>

    <!-- 新增/编辑用户弹窗 -->
    <add-or-update
        :key="addKey"
        ref="addOrUpdateRef"
        @refreshDataList="tableRef.refreshData()"
    />

    <!-- 导入用户对话框-->
    <C7Dialog
        :visible="importDialogVisible"
        mode="dialog"
        title="用户导入"
        :modal-props="{
          width: '60%',
          'close-on-click-modal': false
        }"
        :footer="true"
        @close="importDialogVisible = false"
    >
      <C7Upload style="margin-left: 10%"
                ref="uploadRef"
                :auto-upload="false"
                :on-remove="handleFileRemove"
                :before-upload="beforeUpload"
                :limit="1"
                :file-type="'xlsx,xls'"
                :file-size="10"
                :upload-url="'111'"
      >
        <el-icon class="el-icon--upload">
          <UploadFilled/>
        </el-icon>
        <div class="el-upload__text">
          将文件拖到此处，<em>点击上传</em>
        </div>
      </C7Upload>

      <el-checkbox v-model="updateSupport" style="margin-top: 20px; margin-left: 10%">
        是否更新已经存在的用户数据
      </el-checkbox>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmImport">确定</el-button>
        </div>
      </template>
    </C7Dialog>

    <!-- 导入结果对话框-->
    <C7Dialog
        :visible="importResultDialogVisible"
        mode="dialog"
        title="导入结果"
        :modal-props="{
          width: '600px',
          'close-on-click-modal': false
        }"
        :footer="false"
        @close="closeImportResult"
    >
      <div v-if="importResult">
        <el-alert
            :title="`导入完成！成功 ${importResult.successNum} 条，失败 ${importResult.failureNum} 条`"
            :type="importResult.failureNum > 0 ? 'warning' : 'success'"
            :closable="false"
            style="margin-bottom: 20px;"
        />

        <div v-if="importResult.failureNum > 0 && importResult.failureList && importResult.failureList.length > 0">
          <div style="margin-bottom: 10px; font-weight: bold;">失败数据详情</div>
          <el-table
              :data="importResult.failureList"
              border
              max-height="300"
          >
            <el-table-column
                prop="rowNum"
                label="行号"
                width="80"
                align="center"
            />
            <el-table-column
                prop="errorMsg"
                label="失败原因"
                show-overflow-tooltip
            />
          </el-table>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <C7Button type="primary" @click="closeImportResult">确定</C7Button>
        </div>
      </template>
    </C7Dialog>
  </div>
</template>


<script setup>
import {C7JsonTable, C7Button, C7ButtonGroup, C7Radio, C7Upload, C7Dialog} from "@c7-plus";
import {ref, nextTick, onMounted, defineOptions} from "vue";
import {ElMessage, ElMessageBox} from 'element-plus';
import {UploadFilled} from '@element-plus/icons-vue';
import AddOrUpdate from "./add-or-update.vue";
import {listUser, delUser, resetUserPwd, changeUserStatus, exportUser, importUser} from '@/api/system/user.js';
import {useDict} from '@/composables/useDict';
import {useUtils} from '@/composables/useUtils';
import {useModal} from '@/composables/useModal';

// 定义组件名称，用于 keep-alive 缓存
defineOptions({
  name: 'User'
})

// 使用 composables 替代 getCurrentInstance
const dictData = useDict("sys_normal_disable", "sys_user_sex");
const sys_normal_disable = dictData.sys_normal_disable;
const utils = useUtils();
const modal = useModal();

// 表格引用
const tableRef = ref(null);
const addOrUpdateRef = ref();
const addKey = ref(0);
// 表格配置
const tableProps = ref({
  selection: true,
  showAdd: utils.checkPermission('system:user:add'), // 直接使用权限控制
  showEdit: utils.checkPermission('system:user:edit'),
  showDelete: utils.checkPermission('system:user:remove'),
  showRefresh: true,
  showExport: utils.checkPermission('system:user:export'),
  showImport: utils.checkPermission('system:user:import'),
  border: true,
  stripe: true,
  height: 'auto',
  align: "center",
  headAlign: "center"
});

// 搜索字段配置
const searchColumns = ref([
  {
    label: "用户账号",
    prop: "userName",
    type: "input",
    placeholder: "请输入用户账号",
    clearable: true
  },
  {
    label: "用户昵称",
    prop: "nickName",
    type: "input",
    placeholder: "请输入用户昵称",
    clearable: true
  },
  {
    label: "手机号码",
    prop: "phonenumber",
    type: "input",
    placeholder: "请输入手机号码",
    clearable: true
  },
  {
    label: "帐号状态",
    prop: "status",
    type: "select",
    placeholder: "请选择帐号状态",
    dataList: sys_normal_disable,
    clearable: true
  },
  {
    label: "创建时间",
    prop: "searchCreateTime",
    type: "daterange",

    placeholder: ["开始日期", "结束日期"]
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
    label: "用户昵称",
    prop: "nickName",

    showOverflowTooltip: true
  },
  {
    label: "部门名称",
    prop: "deptName",

  },
  {
    label: "用户邮箱",
    prop: "email",

    showOverflowTooltip: true
  },
  {
    label: "手机号码",
    prop: "phonenumber",

  },
  {
    label: "角色",
    prop: "roleNames",

    showOverflowTooltip: true
  },
  {
    label: "帐号状态",
    prop: "status",
    slotName: 'status'
  },
  {
    label: "创建时间",
    prop: "createTime",

  },
  {
    label: "最后登录时间",
    prop: "loginDate",

  },
  {
    label: "操作",
    prop: "table-operate",
    width: 200,
    fixed: "right"
  }
]);

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


const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(
        '是否确认重置用户"' + row.userName + '"的密码？',
        '系统提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    );
    await resetUserPwd(row.id);
    ElMessage.success('重置成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败');
    }
  }
};


// 状态修改
// 修改 handleStatusChange 方法，添加防重复调用逻辑
const handleStatusChange = (row) => {
  console.log(row)
  // 添加判断，避免初始化时触发
  const id = row.id;
  if (!id) {
    return;
  }
  let text = row.status === "0" ? "启用" : "停用";
  modal.confirm('确认要' + text + '用户"' + row.userName + '"吗？').then(function () {
    return changeUserStatus(row.id, row.status);
  }).then(() => {
    modal.msgSuccess(text + "成功");
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0";
  });
};


// 导入相关
const importDialogVisible = ref(false)
const uploadRef = ref()
const importFile = ref(null)
const updateSupport = ref(false)
const importResult = ref(null)
const importResultDialogVisible = ref(false)

// 导入按钮处理
const importBtnHandle = () => {
  importDialogVisible.value = true
  importFile.value = null
  updateSupport.value = false
  importResult.value = null
  // 清空上传组件
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 导入文件上传前的验证
const beforeUpload = (file) => {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  // 保存文件，但不自动上传
  importFile.value = file
  return false // 阻止自动上传
}

// 文件移除
const handleFileRemove = () => {
  importFile.value = null
}

// 确认导入
const confirmImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先上传 Excel 文件')
    return
  }

  try {
    const response = await importUser(importFile.value, updateSupport.value)

    if (response.code === 200) {
      importResult.value = response.data
      importDialogVisible.value = false
      importResultDialogVisible.value = true

      if (importResult.value.failureNum > 0) {
        ElMessage.warning(`导入完成！成功 ${importResult.value.successNum} 条，失败 ${importResult.value.failureNum} 条`)
      } else {
        ElMessage.success(`导入成功！共导入 ${importResult.value.successNum} 条数据`)
      }

      // 刷新列表
      tableRef.value.refreshData()
    } else {
      ElMessage.error(response.msg || '导入失败')
    }
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error(error.message || '导入失败，请检查文件格式是否正确')
  }
}

// 关闭导入结果对话框
const closeImportResult = () => {
  importResultDialogVisible.value = false
  importResult.value = null
}

// onMounted
onMounted(() => {
  console.log("111")
})
</script>
<style scoped lang="scss">

</style>
