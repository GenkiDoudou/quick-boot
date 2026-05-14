<template>
  <el-dialog v-model="visibleRef" :footer="true" :title="(!dataForm.userId) ? '新增' : '修改'" @close="visibleRef = false">
    <el-form :model="dataForm" :rules="rules" ref="dataFormRef" label-width="100px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="用户账号" prop="userName">
            <el-input v-model="dataForm.userName" :disabled="!!dataForm.userId" placeholder="请输入用户账号" />
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
          <el-form-item label="归属部门" prop="deptId">
            <el-tree-select
              v-model="dataForm.deptId"
              :data="deptTree"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              placeholder="请选择部门"
              check-strictly
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="角色" prop="roleIds">
            <el-select v-model="dataForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
              <el-option v-for="item in roleData" :key="item.roleId" :label="item.roleName" :value="item.roleId" />
            </el-select>
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
      <el-row v-if="!dataForm.userId">
        <el-col :span="12">
          <el-form-item label="密码" prop="password">
            <el-input v-model="dataForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-else>
        <el-col :span="12">
          <el-form-item label="新密码" prop="password">
            <el-input v-model="dataForm.password" type="password" placeholder="不修改请留空" show-password />
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUser, addUser, updateUser } from '@/api/system/user'
import { listRole } from '@/api/system/role'
import { listTreeDept } from '@/api/system/dept'
import { useDict } from '@/utils/dict'

const visibleRef = ref(false)
const emit = defineEmits(['refreshDataList'])

const { sys_user_sex, sys_normal_disable } = useDict('sys_user_sex', 'sys_normal_disable')

const dataForm = ref({
  userId: undefined,
  userName: '',
  nickName: '',
  deptId: undefined,
  email: '',
  phonenumber: '',
  sex: '0',
  password: '',
  status: '0',
  remark: '',
  roleIds: []
})

const roleIdsValidator = (rule, value, callback) => {
  if (!value || value.length === 0) {
    callback(new Error('至少选择一个角色'))
  } else {
    callback()
  }
}

const rules = ref({
  userName: [{ required: true, message: '请输入用户账号', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择归属部门', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择帐号状态', trigger: 'blur' }],
  roleIds: [{ validator: roleIdsValidator, trigger: 'change' }]
})

const dataFormRef = ref()
const roleData = ref([])
const deptTree = ref([])

onMounted(() => {
  listTreeDept().then((res) => {
    deptTree.value = res.data || res || []
  })
})

const init = (uid) => {
  visibleRef.value = true
  dataForm.value = {
    userId: undefined,
    userName: '',
    nickName: '',
    deptId: undefined,
    email: '',
    phonenumber: '',
    sex: '0',
    password: '',
    status: '0',
    remark: '',
    roleIds: []
  }
  rules.value.password = uid
    ? []
    : [{ required: true, message: '请输入密码', trigger: 'blur' }]
  loadRoles()
  if (uid) {
    getUser(uid).then((res) => {
      const d = res.data || res
      dataForm.value = {
        userId: d.userId,
        userName: d.userName,
        nickName: d.nickName,
        deptId: d.deptId,
        email: d.email || '',
        phonenumber: d.phonenumber || '',
        sex: d.sex || '0',
        password: '',
        status: d.status || '0',
        remark: d.remark || '',
        roleIds: d.roleIds ? [...d.roleIds] : []
      }
    })
  }
}

function loadRoles() {
  listRole({ pageNum: 1, pageSize: 500 }).then((res) => {
    const page = res.data || res
    roleData.value = page.records || []
  })
}

function submit() {
  dataFormRef.value.validate(valid => {
    if (!valid) {
      return
    }
    if (!dataForm.value.roleIds || dataForm.value.roleIds.length === 0) {
      ElMessage.warning('至少选择一个角色')
      return
    }
    const payload = { ...dataForm.value }
    if (dataForm.value.userId) {
      if (!payload.password) {
        delete payload.password
      }
      updateUser(payload).then(() => {
        ElMessage.success('修改成功')
        visibleRef.value = false
        emit('refreshDataList')
      })
    } else {
      addUser(payload).then(() => {
        ElMessage.success('新增成功')
        visibleRef.value = false
        emit('refreshDataList')
      })
    }
  })
}

defineExpose({ init })
</script>
