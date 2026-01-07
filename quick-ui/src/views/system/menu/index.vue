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

      <!-- 菜单管理树形展示 -->
      <el-table
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="menuName" label="菜单名称" width="200"></el-table-column>
        <el-table-column prop="menuType" label="菜单类型" align="center" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.menuType === 'M'" type="info">目录</el-tag>
            <el-tag v-else-if="scope.row.menuType === 'C'" type="success">菜单</el-tag>
            <el-tag v-else-if="scope.row.menuType === 'F'" type="warning">按钮</el-tag>
            <el-tag v-else type="info">{{ scope.row.menuType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" align="center" width="60">
          <template #default="scope">
            <svg-icon :icon-class="scope.row.icon" />
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="60"></el-table-column>
        <el-table-column prop="buttonPerms" label="按钮权限标识" show-overflow-tooltip></el-table-column>
        <el-table-column prop="apiPerms" label="接口权限标识" show-overflow-tooltip>
          <template #default="scope">
            <div v-if="scope.row.apiPerms" class="api-perms-display">
              <el-tag 
                v-for="(perm, index) in scope.row.apiPerms.split(',').filter(item => item.trim())" 
                :key="index"
                size="small"
                type="primary"
                style="margin-right: 4px; margin-bottom: 2px;"
              >
                {{ perm.trim() }}
              </el-tag>
            </div>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="component" label="组件路径" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" prop="createTime" width="160px">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
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
                  <el-form-item label="上级菜单">
                     <el-tree-select
                        v-model="form.parentId"
                        :data="menuOptions"
                        :props="{ value: 'id', label: 'menuName', children: 'children' }"
                        value-key="id"
                        placeholder="选择上级菜单"
                        check-strictly
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="菜单类型" prop="menuType">
                     <el-radio-group v-model="form.menuType">
                        <el-radio value="M">目录</el-radio>
                        <el-radio value="C">菜单</el-radio>
                        <el-radio value="F">按钮</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item label="菜单图标" prop="icon">
                     <el-popover
                        placement="bottom-start"
                        :width="540"
                        trigger="click"
                     >
                        <template #reference>
                           <el-input v-model="form.icon" placeholder="点击选择图标" @blur="showSelectIcon" readonly>
                              <template #prefix>
                                 <svg-icon
                                    v-if="form.icon"
                                    :icon-class="form.icon"
                                    class="el-input__icon"
                                    style="height: 32px;width: 16px;"
                                 />
                                 <el-icon v-else style="height: 32px;width: 16px;"><search /></el-icon>
                              </template>
                           </el-input>
                        </template>
                        <icon-select ref="iconSelectRef" @selected="selected" :active-icon="form.icon" />
                     </el-popover>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="显示排序" prop="orderNum">
                     <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="菜单名称" prop="menuName">
                     <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item prop="routeName">
                     <template #label>
                        <span>
                           <el-tooltip content="默认不填则和路由地址相同：如地址为：`user`，则名称为`User`（注意：因为router会删除名称相同路由，为避免名字的冲突，特殊情况下请自定义，保证唯一性）" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           路由名称
                        </span>
                     </template>
                     <el-input v-model="form.routeName" placeholder="请输入路由名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择是外链则路由地址需要以`http(s)://`开头" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>是否外链
                        </span>
                     </template>
                     <el-radio-group v-model="form.isFrame">
                        <el-radio value="0">是</el-radio>
                        <el-radio value="1">否</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item prop="path">
                     <template #label>
                        <span>
                           <el-tooltip content="访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           路由地址
                        </span>
                     </template>
                     <el-input v-model="form.path" placeholder="请输入路由地址" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item prop="component">
                     <template #label>
                        <span>
                           <el-tooltip content="访问的组件路径，如：`system/user/index`，默认在`views`目录下" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           组件路径
                        </span>
                     </template>
                     <el-input v-model="form.component" placeholder="请输入组件路径" />
                  </el-form-item>
               </el-col>
               <el-col :span="24" v-if="form.menuType != 'M'">
                  <el-form-item>
                     <el-input v-model="form.buttonPerms" placeholder="请输入按钮权限标识" maxlength="100" />
                     <template #label>
                        <span>
                           <el-tooltip content="按钮权限标识，用于前端按钮权限控制，如：user:add, user:edit" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           按钮权限标识
                        </span>
                     </template>
                  </el-form-item>
               </el-col>
               <el-col :span="24" v-if="form.menuType != 'M'">
                  <el-form-item>
                    <div class="api-perms-container">
                      <div 
                        v-for="(item, index) in form.apiPermsList" 
                        :key="index" 
                        class="api-perm-item"
                      >
                        <el-input 
                          v-model="form.apiPermsList[index]" 
                          placeholder="请输入接口权限标识"
                          style="flex: 1; margin-right: 8px;"
                        />
                        <el-button 
                          type="danger" 
                          icon="Delete" 
                          circle 
                          size="small"
                          @click="removeApiPerm(index)"
                        />
                      </div>
                      <el-button 
                        type="primary" 
                        icon="Plus" 
                        @click="addApiPerm"
                        style="width: 100%; margin-top: 8px;"
                      >
                        添加接口权限
                      </el-button>
                    </div>
                    <template #label>
                      <span>
                        <el-tooltip content="接口权限标识，用于后端接口权限控制" placement="top">
                          <el-icon><question-filled /></el-icon>
                        </el-tooltip>
                        接口权限标识
                      </span>
                    </template>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item>
                     <el-input v-model="form.query" placeholder="请输入路由参数" maxlength="255" />
                     <template #label>
                        <span>
                           <el-tooltip content='访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`' placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           路由参数
                        </span>
                     </template>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择是则会被`keep-alive`缓存，需要匹配组件的`name`和地址保持一致" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           是否缓存
                        </span>
                     </template>
                     <el-radio-group v-model="form.isCache">
                        <el-radio value="0">缓存</el-radio>
                        <el-radio value="1">不缓存</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择隐藏则路由将不会出现在侧边栏，但仍然可以访问" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           显示状态
                        </span>
                     </template>
                     <el-radio-group v-model="form.visible">
                        <el-radio
                           v-for="dict in sys_show_hide"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择停用则路由将不会出现在侧边栏，也不能被访问" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           菜单状态
                        </span>
                     </template>
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
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Menu">
import { addMenu, delMenu, getMenu, listMenu, updateMenu ,listMenuTree} from "@/api/system/menu";
import { C7Button, C7ButtonGroup } from '@/components/c7';
import SvgIcon from "@/components/SvgIcon";
import IconSelect from "@/components/IconSelect";

const { proxy } = getCurrentInstance();
const { sys_show_hide, sys_normal_disable } = proxy.useDict("sys_show_hide", "sys_normal_disable");

// 事件处理函数
const handleAdd = (row) => {
  reset();
  getTreeselect();
  if (row != null && row.id) {
    form.value.parentId = row.id;
  } else {
    form.value.parentId = 0;
  }
  open.value = true;
  title.value = "添加菜单";
};

const handleEdit = (row) => {
  handleUpdate(row);
};

const handleDelete = (row) => {
  console.log('删除行数据:', row);
  console.log('行ID:', row?.id);
  
  if (!row || !row.id) {
    proxy.$modal.msgError("删除失败：无效的菜单数据");
    return;
  }
  
  proxy.$modal.confirm('是否确认删除名称为"' + row.menuName + '"的数据项?').then(function() {
    console.log('准备删除ID:', row.id);
    return delMenu(row.id);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch((error) => {
    console.error('删除失败:', error);
    proxy.$modal.msgError("删除失败");
  });
};


const menuList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const menuOptions = ref([]);
const isExpandAll = ref(false);
const refreshTable = ref(true);
const iconSelectRef = ref(null);

const data = reactive({
  form: {},
  queryParams: {
    menuName: undefined,
    visible: undefined
  },
  rules: {
    menuName: [{ required: true, message: "菜单名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "菜单顺序不能为空", trigger: "blur" }],
    path: [{ required: true, message: "路由地址不能为空", trigger: "blur" }]
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询菜单列表 */
function getList() {
  loading.value = true;
  listMenuTree().then(response => {
    // menuList.value = proxy.handleTree(response.data, "id");
    menuList.value = response.data;
    console.log('菜单列表:', menuList)
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

/** 查询菜单下拉树结构 */
function getTreeselect() {
  menuOptions.value = [];
  listMenuTree().then(response => {
    const menu = { id: '0', menuName: "主类目", children: [] };
    // menu.children = proxy.handleTree(response.data, "id");
    menu.children = response.data;
    menuOptions.value.push(menu);
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
    id: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: "M",
    orderNum: undefined,
    isFrame: "1",
    isCache: "0",
    visible: "0",
    status: "0",
    buttonPerms: undefined,
    apiPerms: undefined,
    apiPermsList: []
  };
  proxy.resetForm("menuRef");
}

/** 展示下拉图标 */
function showSelectIcon() {
  iconSelectRef.value.reset();
}

/** 选择图标 */
function selected(name) {
  form.value.icon = name;
}

/** 添加接口权限 */
function addApiPerm() {
  if (!form.value.apiPermsList) {
    form.value.apiPermsList = [];
  }
  form.value.apiPermsList.push('');
}

/** 删除接口权限 */
function removeApiPerm(index) {
  form.value.apiPermsList.splice(index, 1);
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

/** 新增按钮操作 - 重写为兼容新架构 */
function handleAddOriginal(row) {
  reset();
  getTreeselect();
  if (row != null && row.id) {
    form.value.parentId = row.id;
  } else {
    form.value.parentId = 0;
  }
  open.value = true;
  title.value = "添加菜单";
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    refreshTable.value = true;
  });
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset();
  await getTreeselect();
  getMenu(row.id).then(response => {
    form.value = response.data;
    // 处理接口权限列表
    if (form.value.apiPerms) {
      form.value.apiPermsList = form.value.apiPerms.split(',').filter(item => item.trim());
    } else {
      form.value.apiPermsList = [];
    }
    open.value = true;
    title.value = "修改菜单";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["menuRef"].validate(valid => {
    if (valid) {
      // 处理接口权限列表，转换为逗号分隔的字符串
      if (form.value.apiPermsList && form.value.apiPermsList.length > 0) {
        form.value.apiPerms = form.value.apiPermsList.filter(item => item.trim()).join(',');
      } else {
        form.value.apiPerms = '';
      }
      
      if (form.value.id != undefined) {
        updateMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
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
