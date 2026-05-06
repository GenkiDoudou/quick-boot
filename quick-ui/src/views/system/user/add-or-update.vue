<template>
  <el-dialog v-model="visibleRef" :footer="true" :title="(!dataForm.id) ? '新增' : '修改'" @close="visibleRef = false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="用户账号" prop="userName">
            <el-input v-model="dataForm.userName" :disabled="!!dataForm.id" placeholder="请输入用户账号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用户昵称" prop="nickName">
            <el-input v-model="dataForm.nickName" placeholder="请输入用户昵称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="用户邮箱" prop="email">
            <el-input v-model="dataForm.email" placeholder="请输入用户邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="dataForm.phonenumber" placeholder="请输入手机号码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="用户性别" prop="sex">
            <el-select v-model="dataForm.sex" placeholder="请选择">
              <el-option v-for="d in sys_user_sex" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="帐号状态" prop="status">
            <el-radio-group v-model="dataForm.status">
              <el-radio v-for="d in sys_normal_disable" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="!dataForm.id">
        <el-col :span="12">
          <el-form-item label="密码" prop="password">
            <el-input v-model="dataForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="角色" prop="roleIds">
            <el-select v-model="dataForm.roleIds" multiple placeholder="请选择角色">
              <el-option v-for="item in roleData" :key="item.id" :label="item.roleName" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="20">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="dataForm.remark" type="textarea" placeholder="请输入备注" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="visibleRef = false">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUser } from '@/api/system/user'
import { useDict } from '@/utils/dict'
import request from '@/utils/request'

const visibleRef = ref(false)
const emit = defineEmits(['refreshDataList'])

const { sys_user_sex, sys_normal_disable } = useDict('sys_user_sex', 'sys_normal_disable')

const dataForm = ref({
  id: '',
  userName: '',
  nickName: '',
  email: '',
  phonenumber: '',
  sex: '',
  password: '',
  status: '0',
  remark: '',
  roleIds: []
})

const rules = ref({
  userName: [{ required: true, message: '请输入用户账号', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择帐号状态', trigger: 'blur' }]
})

const dataFormRef = ref()
const roleData = ref([])

const init = (id) => {
  visibleRef.value = true
  dataForm.value = { id: '', userName: '', nickName: '', email: '', phonenumber: '', sex: '', password: '', status: '0', remark: '', roleIds: [] }
  getAllRoles()
  if (id) {
    dataForm.value.id = id
    getUser(id).then(res => {
      dataForm.value = { ...res.data, roleIds: res.data.roleIds || [] }
    })
  }
}

function getAllRoles() {
  request({ url: '/system/role/lists', method: 'get' }).then(res => {
    roleData.value = res.data
  })
}

function submit() {
  dataFormRef.value.validate(valid => {
    if (valid) {
      const url = dataForm.value.id ? '/sys/user/update' : '/sys/user'
      const method = dataForm.value.id ? 'post' : 'post'
      const reqUrl = dataForm.value.id ? '/sys/user/update' : '/sys/user'
      request({ url: reqUrl, method: 'post', data: dataForm.value }).then(() => {
        ElMessage.success(dataForm.value.id ? '修改成功' : '新增成功')
        visibleRef.value = false
        emit('refreshDataList')
      })
    }
  })
}

defineExpose({ init })
</script>
