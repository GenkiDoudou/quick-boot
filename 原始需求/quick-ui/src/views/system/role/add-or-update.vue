<template>
  <c7-dialog 
    v-model="visibleRef" 
    mode="dialog" 
    :title="(!dataForm.id)?'新增':'修改'" 
    @close="visibleRef = false"
    @submit="submit"
  >
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">
      <el-row>
        <el-col :span="20">
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="dataForm.roleName" placeholder="请输入角色名称"/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="权限字符" prop="roleKey">
            <el-input v-model="dataForm.roleKey" placeholder="请输入权限字符"/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="显示顺序" prop="roleSort">
            <el-input-number v-model="dataForm.roleSort" controls-position="right" :min="0"/>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="数据权限" prop="dataScope">
            <c7-radio :dataList="role_data_scope" v-model="dataForm.dataScope"></c7-radio>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="角色状态" prop="status">
            <c7-radio :dataList="sys_normal_disable" v-model="dataForm.status"></c7-radio>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="dataForm.remark" type="textarea" placeholder="请输入备注"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </c7-dialog>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { C7Dialog, C7Radio } from "@c7-plus";
import { getRole, addRole, updateRole } from '@/api/system/role.js';

const visibleRef = ref(false)
const { proxy } = getCurrentInstance();
const emit = defineEmits(["refreshDataList"]);

// 获取字典数据
const { sys_normal_disable, role_data_scope } = proxy.useDict("sys_normal_disable", "role_data_scope");

const dataFormRef = ref();

const dataForm = ref({
  id: undefined,
  roleName: "",
  roleKey: "",
  roleSort: 0,
  dataScope: "1",
  status: "0",
  remark: ""
})

// 表单验证规则
const rules = ref({
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleKey: [
    { required: true, message: '请输入权限字符', trigger: 'blur' }
  ],
  roleSort: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' }
  ],
  dataScope: [
    { required: true, message: '请选择数据权限', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择角色状态', trigger: 'change' }
  ]
});

// 初始化方法
const init = (id) => {
  visibleRef.value = true;
  // 重置表单
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }
  
  // 重置表单数据
  dataForm.value = {
    id: undefined,
    roleName: "",
    roleKey: "",
    roleSort: 0,
    dataScope: "1",
    status: "0",
    remark: ""
  };
  
  if (id) {
    dataForm.value.id = id;
    getInfo(id);
  }
}

// 根据id查询详情
const getInfo = (id) => {
  getRole(id).then(res => {
    dataForm.value = res.data;
  }).catch(error => {
    console.error('获取角色详情失败:', error);
    proxy.$modal.msgError('获取角色详情失败');
  });
}

// 提交表单
const submit = () => {
  dataFormRef.value.validate(valid => {
    if (valid) {
      if (dataForm.value.id != undefined) {
        // 修改
        updateRole(dataForm.value).then(res => {
          proxy.$modal.msgSuccess("修改成功");
          visibleRef.value = false;
          emit("refreshDataList");
        }).catch(error => {
          console.error('修改失败:', error);
        });
      } else {
        // 新增
        addRole(dataForm.value).then(res => {
          proxy.$modal.msgSuccess("新增成功");
          visibleRef.value = false;
          emit("refreshDataList");
        }).catch(error => {
          console.error('新增失败:', error);
        });
      }
    }
  });
}

defineExpose({
  init
})
</script>

