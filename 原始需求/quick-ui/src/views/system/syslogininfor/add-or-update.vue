<template>

  <C7Dialog :visible="visibleRef" mode="dialog" title="查看" width="80%" :footer="false"
             @close="handleClose">


    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">


      <el-row>
        <el-col :span="8">
          <el-form-item label="用户账号" prop="userName">
            {{ dataForm.userName }}
          </el-form-item>
        </el-col>

        <el-col :span="8">
          <el-form-item label="登录IP地址" prop="ipaddr">
            {{ dataForm.ipaddr }}
          </el-form-item>
        </el-col>

      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="类型" prop="loginType">
            {{ dataForm.loginType }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="结果" prop="loginResult">
            <dict-tag dictType="sys_normal_disable" v-model:value="dataForm.loginResult" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="8">
          <el-form-item label="登录地点" prop="loginLocation">
            {{ dataForm.loginLocation }}

          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="登录时间" prop="loginTime">
            {{ dataForm.loginTime }}
          </el-form-item>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="8">
          <el-form-item label="浏览器类型" prop="browser">
            {{ dataForm.browser }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="操作系统" prop="os">
            {{ dataForm.os }}
          </el-form-item>
        </el-col>
      </el-row>





      <el-row>

      </el-row>


      <el-row>

      </el-row>

    </el-form>
  </C7Dialog>
</template>

<script setup>
import {C7Dialog} from "@c7-plus"
import {reactive, ref, getCurrentInstance} from "vue";
import { getLogininfor } from '@/api/system/logininfor.js';
import DictTag from "@/components/DictTag/index.vue";

const {proxy} = getCurrentInstance();
const emit = defineEmits(["refreshDataList"]);
const visibleRef = ref(false);
const dataForm = ref({
  infoId: "",
  userName: "",
  ipaddr: "",
  loginLocation: "",
  browser: "",
  os: "",
  status: "",
  msg: "",
  loginTime: "",
  clientId: ""
})
const handleClose = () => {
  visibleRef.value = false;
};
//  校验
const rules = ref(
    {
      // xxx: [{required: true, message: '请输入xxx', trigger: 'blur'}]


    }
);

// 初始化方法
const init = (id) => {
  visibleRef.value = true;
  if (id) {
    dataForm.value.infoId = id;
    getInfo(id);
  }
}
// 根据id查询详情
const getInfo = (id) => {
  console.log('获取登录信息详情，ID:', id);
  getLogininfor(id).then(res => {
    console.log('登录信息详情响应:', res);
    dataForm.value = res.data;
  }).catch(error => {
    console.error('获取登录信息详情失败:', error);
    proxy.$message.error('获取详情失败');
  });
}
const dataFormRef = ref()


defineExpose({
  init
})
</script>
