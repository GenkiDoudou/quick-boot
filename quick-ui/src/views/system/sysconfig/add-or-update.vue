<template>
  <c7-dialog
      :visible="visibleRef"
      mode="dialog"
      :title="(!dataForm.configId) ? '新增' : '修改'"
      @submit="submit"
      @close="handleClose"
  >
    <el-form
        :model="dataForm"
        :rules="rules"
        ref="dataFormRef"
        label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item
              label="参数名称"
              prop="configName"
          >

            <el-input
                v-model="dataForm.configName"
                placeholder="请输入参数名称"
            />

          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
              label="参数键名"
              prop="configKey"
          >

            <el-input
                :disabled="(dataForm && !dataForm.configId)?false:true"
                v-model="dataForm.configKey"
                placeholder="请输入参数键名"
            />

          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
              label="参数键值"
              prop="configValue"
          >

            <el-input
                v-model="dataForm.configValue"
                placeholder="请输入参数键值"
            />

          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
              label="系统内置"
              prop="configType"
          >

            <c7-radio
                v-model="dataForm.configType"
                :data-list="YES_NO"
            />


          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
              label="备注"
              prop="remark"
          >

            <el-input
                v-model="dataForm.remark"
                placeholder="请输入备注"
            />

          </el-form-item>
        </el-col>

      </el-row>
    </el-form>
  </c7-dialog>
</template>
<script setup>
import {ref, getCurrentInstance} from "vue"
import {C7Dialog, C7Select, C7Radio} from "@/components/c7"

/**
 * ================= API 引入（关键点）
 * 与你给的 dict 示例完全一致
 */
import {
  getSysConfig,
  addSysConfig,
  updateSysConfig
} from "@/api/system/sysconfig.js"

const {proxy} = getCurrentInstance()
const emit = defineEmits(["refreshDataList"])


const {
  YES_NO
} = proxy.useDict(
    "YES_NO"
)


const visibleRef = ref(false)
const dataFormRef = ref()

const dataForm = ref({
  configId: "",
  configId: "",
  configName: "",
  configKey: "",
  configValue: "",
  configType: "",
  remark: "",
})


const rules = ref({
  configName: [
    {required: true, message: '请输入参数名称', trigger: 'blur'}
  ],
  configKey: [
    {required: true, message: '请输入参数键名', trigger: 'blur'}
  ],
  configValue: [
    {required: true, message: '请输入参数键值', trigger: 'blur'}
  ],
  configType: [
    {required: true, message: '请输入系统内置', trigger: 'blur'}
  ],
})


const handleClose = () => {
  visibleRef.value = false
}

const init = (configId) => {
  // 防止新增 / 编辑串数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields()
  }

  visibleRef.value = true
  dataForm.value.configId = configId || ""

  if (configId) {
    getInfo(configId)
  }
}


const getInfo = (configId) => {
  getSysConfig(configId).then(res => {
    dataForm.value = res.data
  })
}


const submit = () => {
  dataFormRef.value.validate(valid => {
    if (!valid) return

    if (dataForm.value.configId) {
      // 修改
      updateSysConfig(dataForm.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        visibleRef.value = false
        emit("refreshDataList")
      })
    } else {
      // 新增
      addSysConfig(dataForm.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        visibleRef.value = false
        emit("refreshDataList")
      })
    }
  })
}

defineExpose({init})
</script>
