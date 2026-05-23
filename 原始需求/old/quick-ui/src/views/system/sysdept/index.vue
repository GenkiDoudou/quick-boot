<template>
  <div class="app-container">
    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd()"
        >新增</el-button>
      </el-col>
    </el-row>

    <!-- 部门管理树形展示 -->
    <el-table
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        v-loading="loading"
        style="width: 100%"
    >
      <el-table-column prop="name" label="部门名称" ></el-table-column>
      <el-table-column prop="leader" label="负责人"></el-table-column>
      <el-table-column prop="phone" label="联系电话" ></el-table-column>
      <el-table-column prop="email" label="邮箱" ></el-table-column>
      <el-table-column prop="weight" label="排序" ></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160px">
        <template #default="scope">
          <span>{{ utils.parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180px">
        <template #default="scope">
          <C7ButtonGroup  mode = 'inline'>
            <C7Button
                type="primary"
                link
                icon="Edit"
                @click="handleEdit(scope.row)"
                v-hasPermi="['system:menu:edit']"
            >
              修改
            </C7Button>
            <C7Button
                type="success"
                link
                icon="Plus"
                @click="handleAdd(scope.row)"
                v-hasPermi="['system:menu:add']"
            >
              新增
            </C7Button>
            <C7Button
                type="danger"
                link
                icon="Delete"
                @click="handleDelete(scope.row)"
                v-hasPermi="['system:menu:remove']"
            >
              删除
            </C7Button>
          </C7ButtonGroup>
        </template>
      </el-table-column>
    </el-table>


    <!-- 添加或修改菜单对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="menuRef" :model="form" :rules="rules" label-width="120px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="上级部门" prop="parentId">
              <el-tree-select
                  v-model="form.parentId"
                  :data="menuOptions"
                  :props="{ value: 'id', label: 'deptName', children: 'children' }"
                  value-key="id"
                  placeholder="选择上级菜单"
                  check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" placeholder="请输入部门名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="leader">
              <el-input v-model="form.leader" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>


          <el-col :span="12">
            <el-form-item label="部门状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio
                    v-for="dict in sys_normal_disable"
                    :key="dict.value"
                    :value="dict.value"
                >{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Dept">
import {delDept, getDept, listTreeDept, updateDept,addDept} from "@/api/system/dept";
import { C7Button, C7ButtonGroup } from '@c7-plus';
import { ref, reactive, toRefs } from 'vue';
import {useDict} from '@/composables/useDict';
import {useUtils} from '@/composables/useUtils';
import {useModal} from '@/composables/useModal';

// 使用 composables 替代 getCurrentInstance
const { sys_show_hide, sys_normal_disable } = useDict("sys_show_hide", "sys_normal_disable");
const utils = useUtils();
const modal = useModal();

// 事件处理函数
const handleAdd = (row) => {
  reset();
  getTreeselect();
  if (row){
    form.value.parentId = row.id;
  }

  open.value = true;
  title.value = "添加部门";
};

const handleEdit = (row) => {
  handleUpdate(row);
};

const handleDelete = (row) => {
  console.log('删除行数据', row);
  console.log('行ID:', row?.deptId);

  if (!row || !row.deptId) {
    modal.msgError("删除失败：无效的部门数据");
    return;
  }

  modal.confirm('是否确认删除名称为"' + row.deptName + '"的数据项?').then(function() {
    console.log('准备删除ID:', row.deptId);
    return delDept(row.deptId);
  }).then(() => {
    getList();
    modal.msgSuccess("删除成功");
  }).catch((error) => {
    console.error('删除失败:', error);
    modal.msgError("删除失败");
  });
};


const menuList = ref([]);
const open = ref(false);
const loading = ref(true);
const title = ref("");
const menuOptions = ref([]);
const menuRef = ref(null);
const queryRef = ref(null);


const data = reactive({
  form: {
    deptId: undefined,
    parentId: "1",
    deptName: undefined,
    orderNum : 0,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: "0",
  },
  queryParams: {
    deptName: undefined,
  },
  rules: {
    parentId: [{ required: true, message: "上级部门不能为空", trigger: "blur" }],
    deptName: [{ required: true, message: "部门名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "顺序不能为空", trigger: "blur" }],
    status: [{ required: true, message: "状态不能为空", trigger: "blur" }],
    phone: [ {validator: utils.validate.validatePhone, trigger: "blur"}],
    email: [ {validator: utils.validate.validateEmail, trigger: "blur"}]
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询菜单列表 */
function getList() {
  loading.value = true;
  listTreeDept().then(response => {
    menuList.value = response.data;
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

/** 查询菜单下拉树结构 */
function getTreeselect() {
  menuOptions.value = [];
  listTreeDept().then(response => {
    let  data = response.data;
    // 判断 如果data为数组并且长度大于 则取第一个元素
    if (Array.isArray(data) && data.length > 0) {
      menuOptions.value.push(data[0]);
    }else {
      menuOptions.value.push({});
    }

  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    deptId: undefined,
    parentId: "1",
    deptName: undefined,
    orderNum : 0,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: "0",

  };
  if (menuRef.value) {
    menuRef.value.resetFields();
  }
}





/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  if (queryRef.value) {
    queryRef.value.resetFields();
  }
  handleQuery();
}




/** 修改按钮操作 */
async function handleUpdate(row) {
  reset();
  await getTreeselect();
  getDept(row.deptId).then(response => {
    form.value = response.data;

    open.value = true;
    title.value = "修改部门";
  });
}

/** 提交按钮 */
function submitForm() {
  if (!menuRef.value) return;
  
  menuRef.value.validate(valid => {
    if (valid) {
      if (form.value.deptId != undefined) {
        updateDept(form.value).then(response => {
          modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addDept(form.value).then(response => {
          modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}


getList();
</script>

<style scoped>
.api-perms-container {
  width: 100%;
}

.api-perm-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.api-perm-item:last-of-type {
  margin-bottom: 0;
}

.api-perms-display {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.text-gray-400 {
  color: #9ca3af;
}
</style>
