<template>

  <c7-dialog v-model="visibleRef" :footer="true" :title="(!dataForm.id)?'新增':'修改'" @submit="submit"
             @close="visibleRef = false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">


      <el-row>
        <el-col :span="20">
          <el-form-item label="用户账号" prop="userName">
            <el-input v-model="dataForm.userName" :disabled="(!dataForm.id)?false:true"
                      placeholder="请输入用户账号"/>

          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="20">
          <el-form-item label="用户昵称" prop="nickName">
            <el-input v-model="dataForm.nickName"
                      placeholder="请输入用户昵称"/>

          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="20">
          <el-form-item label="用户邮箱" prop="email">
            <el-input v-model="dataForm.email"
                      placeholder="请输入用户邮箱"/>

          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="20">
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="dataForm.phonenumber"
                      placeholder="请输入手机号码"/>

          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="20">
          <el-form-item label="用户性别" prop="sex">
            <c7-radio :dataList="sys_user_sex" v-model="dataForm.sex"></c7-radio>
          </el-form-item>
        </el-col>
      </el-row>


      <el-row v-if="!dataForm.id">
        <el-col :span="20">
          <el-form-item label="密码" prop="password">
            <el-input v-model="dataForm.password" type="password"
                      placeholder="请输入密码"/>

          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="20">
          <el-form-item label="帐号状态" prop="status">
            <c7-radio :dataList="sys_normal_disable" v-model="dataForm.status"></c7-radio>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="角色" prop="roleIds">
            <el-select
                v-model="dataForm.roleIds"
                multiple
                placeholder="请选择角色"
                style="width: 240px"
            >
              <el-option
                  v-for="item in roleData"
                  :key="item.id"
                  :label="item.roleName"
                  :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="dataForm.remark" type="textarea"
                      placeholder="请输入备注"/>

          </el-form-item>
        </el-col>
      </el-row>

    </el-form>

  </c7-dialog>
</template>

<script setup>
import {ref, getCurrentInstance} from 'vue'

import {C7Dialog, C7Radio} from "@/components/c7";
import baseService from "@/service/baseService.js";
import {validateEmail, validatePassword, validatePhone} from "@/utils/validate.js";

const visibleRef = ref(false)
const {proxy} = getCurrentInstance();
const emit = defineEmits(["refreshDataList"]);

// 获取字典数据
const dictData = proxy.useDict("sys_user_sex", "sys_normal_disable");
const sys_user_sex = dictData.sys_user_sex;
const sys_normal_disable = dictData.sys_normal_disable;

const dataForm = ref({
  id: "",
  userName: "",
  nickName: "",
  email: "",
  phonenumber: "",
  sex: "",
  password: "",
  status: "0",
  remark: "",
  roleIds: ''


})

//  校验
const rules = ref(
    {
      // xxx: [{required: true, message: '请输入xxx', trigger: 'blur'}]
      userName: [{required: true, message: '请输入用户账号', trigger: 'blur'}],
      nickName: [{required: true, message: '请输入用户昵称', trigger: 'blur'}],
      password: [
        {required: true, message: '请输入密码', trigger: 'blur'},
        {validator: proxy.$validate.validatePassword, trigger: "blur"}
      ],
      status: [{required: true, message: '请输入帐号状态', trigger: 'blur'}],
      roleIds: [{required: true, message: '请输入角色', trigger: 'blur'}],
      phonenumber: [
        {validator: proxy.$validate.validatePhone, trigger: "blur"}
      ],
      email: [
        {validator: proxy.$validate.validateEmail, trigger: "blur"}
      ]


    }
);
// 初始化方法
const init = (id) => {
  visibleRef.value = true;
  getAllRoleHandler();
  if (id) {
    dataForm.value.id = id;
    getInfo(id);
  }

}
// 根据id查询详情
const getInfo = (id) => {
  baseService.get("/sys/user/" + id).then(res => {
    dataForm.value = res.data;
  })
}
const dataFormRef = ref()
// 提交
const submit = () => {
  dataFormRef.value.validate(valid => {
    console.log(valid)
    if (valid) {


      if (dataForm.value.id) {
        // 修改
        baseService.put("/sys/user", dataForm.value).then(res => {
          proxy.$modal.msgSuccess("修改成功");
          visibleRef.value = false;
          emit("refreshDataList");
        })
      } else {
        //保存
        baseService.post("/sys/user", dataForm.value).then(res => {
          proxy.$modal.msgSuccess("新增成功");
          visibleRef.value = false;
          emit("refreshDataList");
        })
      }
    }
  });
}

const roleData = ref([]);
const getAllRoleHandler = () => {
  baseService.get("/system/role/lists").then(res => {
    roleData.value = res.data
  })
}

defineExpose({
  init
})


</script>
