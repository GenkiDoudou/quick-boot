<template>
  <c7-dialog
      :visible="visibleRef"
      mode="dialog"
      :title="(!dataForm.id) ? '新增' : '修改'"
      @submit="submit"
      @close="handleClose"
  >
    <el-form
        :model="dataForm"
        :rules="rules"
        ref="dataFormRef"
        label-width="150px"
    >
      <el-row :gutter="20">

        <el-col :span="24">
          <el-form-item
              label="客户端名称"
              prop="clientName"
          >
            <el-input
                v-model="dataForm.clientName"
                placeholder="请输入客户端名称"
            />

          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item
              label="校验类型"
              prop="verifyType"
          >
            <c7-select
                multiple=true
                v-model="dataForm.verifyType"
                :data-list="sys_verify_type"
            />


          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item
              label="接口授权"
              prop="authorities"
          >
            <el-input
                type="textarea"
                v-model="dataForm.authorities"
                placeholder="请输入接口授权(多个用,隔开)"
            />

          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
              label="令牌有效时间"
              prop="accessTokenValidity"
          >

            <el-input
                type="number"
                min="1"
                v-model="dataForm.accessTokenValidity"
                placeholder="请输入令牌有效时间"
            />

          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
              label="刷新令牌有效时间"
              prop="refreshTokenValidity"
          >
            <el-input
                type="number"
                min="1"
                v-model="dataForm.refreshTokenValidity"
                placeholder="请输入刷新令牌有效时间"
            />

          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item
              label="ip白名单"
              prop="whitelistIp"
          >
            <el-input
                type="textarea"
                v-model="dataForm.whitelistIp"
                placeholder="请输入ip白名单"
            />

          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item
              label="状态"
              prop="status"
          >
            <c7-radio
                v-model="dataForm.status"
                :data-list="COMMON_STATUS"
            />


          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </c7-dialog>
</template>
<script setup>
import {ref, getCurrentInstance} from "vue"
import {C7Dialog, C7Select, C7Radio} from "@c7-plus"

/**
 * ================= API 引入（关键点）
 * 与你给的 dict 示例完全一致
 */
import {
  getSysOauthClient,
  addSysOauthClient,
  updateSysOauthClient
} from "@/api/system/sysoauthclient.js"

const {proxy} = getCurrentInstance()
const emit = defineEmits(["refreshDataList"])


const {
  sys_verify_type,COMMON_STATUS
} = proxy.useDict(
    "sys_verify_type","COMMON_STATUS"
)


const visibleRef = ref(false)
const dataFormRef = ref()

const dataForm = ref({
  id: "",
  clientId: "",
  clientName: '',
  clientSecret: "",
  scope: "",
  authorities: "/**",
  accessTokenValidity: "3600",
  refreshTokenValidity: "86400",
  whitelistIp: "*.*.*.*",
  verifyType: "",
  status:"0"
})


const rules = ref({
  clientId: [
    {required: true, message: '请输入客户端id', trigger: 'blur'}
  ],
  clientName: [
    {required: true, message: '请输入客户端名称', trigger: 'blur'}
  ],
  verifyType: [
    {required: true, message: '请选择校验类型', trigger: 'change'}
  ],
  accessTokenValidity: [
    {required: true, message: '请输入令牌有效时间', trigger: 'blur'}
  ],
  refreshTokenValidity: [
    {required: true, message: '请输入刷新令牌有效时间', trigger: 'blur'}
  ],
  authorities: [
    {required: true, message: '请输入接口授权', trigger: 'blur'}
  ],
  scope: [
    {required: true, message: '请输入接口授权', trigger: 'blur'}
  ],
  whitelistIp: [
    {required: true, message: '请输入ip白名单', trigger: 'blur'}
  ],
  status: [
    {required: true, message: '请选择状态', trigger: 'change'}
  ]

})


const handleClose = () => {
  visibleRef.value = false
}

const init = (id) => {
  // 防止新增 / 编辑串数据
  if (dataFormRef.value) {
    dataFormRef.value.resetFields()
  }

  visibleRef.value = true
  dataForm.value.id = id || ""

  if (id) {
    getInfo(id)
  }
}


const getInfo = (id) => {
  getSysOauthClient(id).then(res => {
    dataForm.value = res.data
  })
}


const submit = () => {
  dataFormRef.value.validate(valid => {
    if (!valid) return

    if (dataForm.value.id) {
      // 修改
      updateSysOauthClient(dataForm.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        visibleRef.value = false
        emit("refreshDataList")
      })
    } else {
      // 新增
      addSysOauthClient(dataForm.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        visibleRef.value = false
        emit("refreshDataList")
      })
    }
  })
}

defineExpose({init})
</script>
