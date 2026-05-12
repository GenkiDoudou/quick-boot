<template>

  <c7-dialog :visible="visibleRef" mode="dialog" title="表sql导入" @close="visibleRef = false"
             :footer="true" @submit="submit">
    <span>创建表语句(支持多个建表语句)：</span>
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">
      <el-input :rows="10" v-model="dataForm.content" type="textarea" placeholder="请输入sql"/>
      <el-row>
        <!--        <el-col :span="12">
                  <el-form-item label="字典标签" prop="dictLabel">
                    <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签"/>
                  </el-form-item>
                  <el-form-item label="回显样式" prop="listClass">
                    <q-dict-select v-model="dataForm.listClass" dictType="sys_dict_list_class" type="select">
                    </q-dict-select>

                </el-col>-->


      </el-row>
    </el-form>
  </c7-dialog>

</template>

<script setup>
import baseService from "@/service/baseService.js";

const {proxy} = getCurrentInstance();
const emit = defineEmits(["refreshDataList"]);

const visibleRef = ref(false);

// 接口url
const interfaceUrl = "/generator/gentable/createTableSave";

// 表单数据
const dataForm = ref({
  content: ""
});
// 数据校验
const rules = ref({
      // roleName: [{required: true, message: '请输入角色名称', trigger: 'blur'}],

    }
)
const dataFormRef = ref()
// 提交
const submit = () => {
  dataFormRef.value.validate(valid => {
    if (valid) {
      (baseService.post)(interfaceUrl, dataForm.value).then((res) => {
        proxy.$modal.msgSuccess("操作成功");
        visibleRef.value = false;
        emit("refreshDataList");
      });
    }
  });
}


const init = () => {
  visibleRef.value = true;
  console.log(dataFormRef)
  if (dataFormRef.value) {
    dataFormRef.value.resetFields();
  }

}


defineExpose({
  init
})
</script>
