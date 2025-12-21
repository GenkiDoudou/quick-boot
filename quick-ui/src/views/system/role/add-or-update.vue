<template>

  <c7-dialog v-model="visibleRef" :footer="true" :title="(!dataForm.id)?'新增':'修改'" @submit="submitDataScope"
             @close="visibleRef = false">

    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="dataForm.roleName"/>
      </el-form-item>

      <el-form-item prop="roleKey">
        <template #label>
                  <span>
                     <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasRole('admin')`)"
                                 placement="top">
                        <el-icon><question-filled/></el-icon>
                     </el-tooltip>
                     权限字符
                  </span>
        </template>
        <el-input v-model="dataForm.roleKey" placeholder="请输入权限字符" :disabled="(dataForm.id)"/>
      </el-form-item>
      <el-form-item label="角色顺序" prop="roleSort">
        <el-input-number v-model="dataForm.roleSort" controls-position="right" :min="0"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <c7-radio :dataList="sys_normal_disable" v-model="dataForm.status"></c7-radio>
      </el-form-item>
      <el-form-item label="菜单权限">
        <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event)">展开/折叠</el-checkbox>
        <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event)">全选/全不选</el-checkbox>
        <el-checkbox v-model="dataForm.menuCheckStrictly" @change="handleCheckedTreeConnect($event)">父子联动
        </el-checkbox>
        <el-tree
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            ref="menuRef"
            node-key="id"
            :check-strictly="!dataForm.menuCheckStrictly"
            empty-text="加载中，请稍候"
            :props="{ label: 'label', children: 'children' }"
        ></el-tree>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="dataForm.remark" type="textarea" placeholder="请输入内容"></el-input>
      </el-form-item>
    </el-form>
  </c7-dialog>
</template>
<script setup >
import {C7Dialog, C7Radio} from "@/components/c7";
import baseService from "@/service/baseService.js";
import {reactive, ref, getCurrentInstance, nextTick} from "vue";

const {proxy} = getCurrentInstance();
const emit = defineEmits(["refreshDataList"]);

// 获取字典数据
const dictData = proxy.useDict("sys_normal_disable");
const sys_normal_disable = dictData.sys_normal_disable;

const visibleRef = ref(false);

const dataForm = ref({
  id: undefined,
  roleName: undefined,
  roleKey: undefined,
  status:"0",
  dataScope: undefined,
  menuCheckStrictly: true,
  menuIds: [],
  roleSort: 0
})

const rules = ref(
    {
      roleName: [{required: true, message: '请输入角色名称', trigger: 'blur'}],
      roleKey: [{required: true, message: '请输入权限字符', trigger: 'blur'}],
      roleSort: [{required: true, message: '请输入角色顺序', trigger: 'blur'}],
      status: [{required: true, message: '请选择状态', trigger: 'blur'}]

    }
)
const dataFormRef = ref();
const menuExpand = ref(false);
const menuNodeAll = ref(false);
const menuOptions = ref([]);
const menuRef = ref(null);
const init = (id) => {

  visibleRef.value = true;


  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }
  if (menuRef.value != undefined) {
    menuRef.value.setCheckedKeys([]);
  }
  menuExpand.value = false;
  menuNodeAll.value = false;
  menuOptions.value = [];

  //
  if (id) {


    getInfo(id);
  } else {
    getMenuTreeselect();
  }

};

/**
 * 根据角色id查询菜单树结构
 */
const getRoleMenuTreeselect = (roleId) => {
  return baseService.get(`system/menu/roleMenuTreeselect/${roleId}`).then(res => {
    menuOptions.value = res.data.menus
    return res;
  })
}


const getInfo = (id) => {
  const roleMenu = getRoleMenuTreeselect(id);
  baseService.get(`system/role/${id}`).then(res => {
    Object.assign(dataForm.value, res.data)
    dataForm.value.roleSort = Number(dataForm.value.roleSort);
    if (dataForm.value.menuCheckStrictly == 0) {
      dataForm.value.menuCheckStrictly = true;
    } else {
      dataForm.value.menuCheckStrictly = false;
    }
    nextTick(() => {
      roleMenu.then(ress => {

        let checkedKeys = ress.data.checkedKeys;
        checkedKeys.forEach((v) => {
          nextTick(() => {
            menuRef.value.setChecked(v, true, false);
          });
        });
      });
    })
  })
}
const handleClose = () => {
  visibleRef.value = false;
};

const submitDataScope = () => {
  dataFormRef.value.validate(valid => {
    if (valid) {
      if (dataForm.value.menuCheckStrictly == true) {
        dataForm.value.menuCheckStrictly = '0'
      } else {
        dataForm.value.menuCheckStrictly = '1'
      }
      // 获取所有的菜单
      dataForm.value.menuIds = getMenuAllCheckedKeys();
      if (dataForm.value.id != undefined) {
        // 修改
        baseService.put("/system/role", dataForm.value).then(res => {
          proxy.$modal.msgSuccess("修改成功");
          visibleRef.value = false;
          emit("refreshDataList");
        })
      } else {
        // 保存
        baseService.post("/system/role", dataForm.value).then(res => {
          proxy.$modal.msgSuccess("新增成功");
          visibleRef.value = false;
          emit("refreshDataList");
        })
      }

    }
  })
}

/**
 * 所有菜单节点数据
 */

const getMenuAllCheckedKeys = () => {
  // 目前被选中的菜单节点
  let checkedKeys = menuRef.value.getCheckedKeys();
  // 半选中的菜单节点
  let halfCheckedKeys = menuRef.value.getHalfCheckedKeys();
  checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
  return checkedKeys;

}
/**
 * 树权限(展开/折叠)
 */
const handleCheckedTreeExpand = (value) => {

  let treeList = menuOptions.value;
  for (let i = 0; i < treeList.length; i++) {
    menuRef.value.store.nodesMap[treeList[i].id].expanded = value;
  }
}
/**
 * 树权限(全选/不全选)
 */
const handleCheckedTreeNodeAll = (value) => {
  menuRef.value.setCheckedNodes(value ? menuOptions.value : []);
}

/**
 * 树权限(父子联动)
 */
const handleCheckedTreeConnect = (value) => {
  dataForm.value.menuCheckStrictly = value ? true : false;
}

/**
 * 查询菜单树结构
 */
const getMenuTreeselect = () => {
  baseService.get("/system/menu/treeselect").then(res => {
    menuOptions.value = res.data
  })

}
defineExpose({
  init
})
</script>
