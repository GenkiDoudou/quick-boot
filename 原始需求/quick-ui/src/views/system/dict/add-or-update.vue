<template>

  <c7-dialog :visible="visibleRef" mode="dialog" :title="(!dataForm.id)?'新增':'修改'" @close="visibleRef=false"
             @submit="submit">

    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">

      <el-row>
        <el-col :span="20">
          <el-form-item label="字典名称" prop="dictName">
            <el-input v-model="dataForm.dictName" placeholder="请输入字典名称"/>
          </el-form-item>
        </el-col>


      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="字典类型" prop="dictType">
            <el-input v-model="dataForm.dictType" placeholder="请输入字典类型"/>
          </el-form-item>
        </el-col>

      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="状态" prop="status">
            <c7-radio :data-list="sys_normal_disable" v-model="dataForm.status" placeholder="请选择状态"></c7-radio>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="20">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="dataForm.remark" type="textarea" placeholder="请输入内容"></el-input>
          </el-form-item>
        </el-col>
      </el-row>


    </el-form>

  </c7-dialog>


</template>


<script setup>
import {reactive, ref, getCurrentInstance} from "vue";
import {C7Dialog, C7Radio} from "@c7-plus"
import baseService from "@/service/baseService.js";
import {getType, addType, updateType} from "@/api/system/dict/type.js"

const {proxy} = getCurrentInstance();


// 获取字典数据
const dictData = proxy.useDict("sys_normal_disable");
const sys_normal_disable = dictData.sys_normal_disable;
const emit = defineEmits(["refreshDataList"]);

const visibleRef = ref(false);

const dataFormRef = ref();

const dataForm = ref({
  dictName: "",
  dictType: "",
  status: "0",
  remark: "",
  id: ""
});

const rules = ref(
    {
      dictName: [{required: true, message: '请输入字典名称', trigger: 'blur'}],
      dictType: [{required: true, message: '请输入字典类型', trigger: 'blur'}],
      status: [{required: true, message: '请输入状态', trigger: 'blur'}],
    }
);



const init = (id) => {
  console.log(id)

  // 重置表单数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }
  visibleRef.value = true;
  dataForm.value.id = id;
  if (id) {
    getInfo(id);
  }
}

const getInfo = (id) => {

  getType(id).then(res => {
    dataForm.value = res.data;
   console.log(dataForm.value)
  })

}
const submit = () => {
  dataFormRef.value.validate(valid => {
    if (valid) {
      if (dataForm.value.id != undefined) {
        // 修改
        updateType(dataForm.value).then(res => {
          proxy.$modal.msgSuccess("修改成功");
          visibleRef.value = false;
          emit("refreshDataList");
        })
      } else {
        //保存
        addType(dataForm.value).then(res => {
          proxy.$modal.msgSuccess("新增成功");
          visibleRef.value = false;
          emit("refreshDataList");
        })
      }
    }
  });
}
defineExpose({
  init
})
</script>
