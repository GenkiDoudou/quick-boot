<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="pageUser"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="removeUser"
      row-key="userId"
      :show-add-button="true"
      :add-button-permi="['system:user:add']"
      :delete-button-permi="['system:user:remove']"
      :on-add="openAdd"
      :export-function="exportUser"
      :export-button-permi="['system:user:export']"
      export-default-file-name="user.xlsx"
      :import-function="importUser"
      :import-template-download-fn="downloadUserImportTemplate"
      :import-button-permi="['system:user:import']"
      import-template-file-name="user-import-template.xlsx"
      :show-import-button="true"
    >
      <template #deptId="{ formData }">
        <el-tree-select
          v-model="formData.deptId"
          :data="deptTree"
          :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
          value-key="deptId"
          check-strictly
          clearable
          placeholder="请选择部门"
          style="width: 100%"
        />
      </template>
      <template #status="{ row }">
        <el-switch
          :model-value="String(row.status ?? '0')"
          active-value="0"
          inactive-value="1"
          :disabled="String(row.userId) === '1'"
          v-hasPermi="['system:user:edit']"
          @change="(v) => onStatusChange(row, v)"
        />
      </template>
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['system:user:edit']" @click="openEdit(row)">修改</el-button>
        <el-button
          v-if="String(row.userId) !== '1'"
          link
          type="warning"
          v-hasPermi="['system:user:resetPwd']"
          @click="handleResetPwd(row)"
        >重置密码</el-button>
      </template>
    </C7JsonTable>

    <C7Dialog v-model="formVisible" :title="isAdd ? '新增用户' : '修改用户'" width="640px" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户账号" prop="userName">
          <el-input v-model="form.userName" :disabled="!isAdd" maxlength="64" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickName"><el-input v-model="form.nickName" /></el-form-item>
        <el-form-item label="归属部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
            value-key="deptId"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="isAdd" label="用户密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="默认 admin123" />
        </el-form-item>
        <el-form-item label="手机号码" prop="phonenumber"><el-input v-model="form.phonenumber" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio v-for="d in (sys_user_sex || [])" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="d in (sys_normal_disable || [])" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple clearable style="width: 100%" placeholder="请选择角色">
            <el-option
              v-for="r in roleOptions"
              :key="String(r.roleId)"
              :label="r.roleName"
              :value="String(r.roleId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
    </C7Dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import {
  pageUser, getUser, addUser, updateUser, removeUser, changeUserStatus, resetUserPwd,
  exportUser, importUser, downloadUserImportTemplate
} from '@/api/system/user'
import { treeselectDept } from '@/api/system/dept'
import { pageRole } from '@/api/system/role'

defineOptions({ name: 'SysUser' })

const { sys_normal_disable, sys_user_sex } = useDict('sys_normal_disable', 'sys_user_sex')

const tableRef = ref(null)
const formRef = ref(null)
const formVisible = ref(false)
const isAdd = ref(true)
const deptTree = ref([])
const roleOptions = ref([])
const form = reactive({
  userId: null, userName: '', nickName: '', deptId: undefined, password: '',
  phonenumber: '', email: '', sex: '0', status: '0', roleIds: [], remark: ''
})
const rules = {
  userName: [{ required: true, message: '必填', trigger: 'blur' }],
  nickName: [{ required: true, message: '必填', trigger: 'blur' }],
  roleIds: [{ type: 'array', required: true, min: 1, message: '请选择角色', trigger: 'change' }]
}
const defaultSearch = { userName: '', nickName: '', phonenumber: '', status: '', deptId: undefined }
const searchColumns = computed(() => [
  { prop: 'userName', label: '用户账号', type: 'input', span: 8 },
  { prop: 'nickName', label: '用户昵称', type: 'input', span: 8 },
  { prop: 'phonenumber', label: '手机号码', type: 'input', span: 8 },
  { prop: 'status', label: '状态', type: 'select', span: 8, options: sys_normal_disable.value || [] },
  { prop: 'deptId', label: '归属部门', type: 'slot', span: 8 }
])
const tableColumns = computed(() => [
  { prop: 'userName', label: '账号', minWidth: 120 },
  { prop: 'nickName', label: '昵称', minWidth: 120 },
  { prop: 'deptName', label: '部门', minWidth: 120 },
  { prop: 'phonenumber', label: '手机', width: 120 },
  { prop: 'sex', label: '性别', width: 80, columnType: 'tag', options: sys_user_sex.value || [] },
  { prop: 'roleNames', label: '角色', minWidth: 140 },
  { prop: 'status', label: '状态', width: 90, columnType: 'slot', slotName: 'status' },
  { prop: 'createTime', label: '创建时间', minWidth: 160 },
  { prop: 'action', label: '操作', width: 180, fixed: 'right', columnType: 'slot', slotName: 'action' }
])

function openAdd() {
  isAdd.value = true
  Object.assign(form, {
    userId: null, userName: '', nickName: '', deptId: undefined, password: '',
    phonenumber: '', email: '', sex: '0', status: '0', roleIds: [], remark: ''
  })
  formVisible.value = true
}

async function openEdit(row) {
  isAdd.value = false
  const res = await getUser(row.userId)
  const d = res.data || {}
  Object.assign(form, {
    userId: d.userId,
    userName: d.userName,
    nickName: d.nickName,
    deptId: d.deptId != null ? String(d.deptId) : undefined,
    password: '',
    phonenumber: d.phonenumber,
    email: d.email,
    sex: d.sex ?? '0',
    status: d.status ?? '0',
    roleIds: (d.roleIds || []).map((id) => String(id)),
    remark: d.remark
  })
  formVisible.value = true
}

async function submitForm() {
  await formRef.value?.validate()
  const payload = {
    ...form,
    deptId: form.deptId != null && form.deptId !== '' ? form.deptId : null,
    roleIds: (form.roleIds || []).map((id) => String(id))
  }
  if (isAdd.value) {
    const { userId, ...addPayload } = payload
    await addUser(addPayload)
  } else {
    await updateUser(payload)
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  tableRef.value?.refreshData?.()
}

function onStatusChange(row, status) {
  changeUserStatus({ userId: row.userId, status }).then(() => {
    ElMessage.success('状态已更新')
    tableRef.value?.refreshData?.()
  }).catch(() => tableRef.value?.refreshData?.())
}

function handleResetPwd(row) {
  ElMessageBox.prompt(`请输入用户「${row.userName}」的新密码`, '重置密码', {
    inputType: 'password',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => resetUserPwd({ userId: row.userId, password: value }))
    .then(() => ElMessage.success('密码已重置'))
    .catch(() => {})
}

onMounted(() => {
  treeselectDept().then((res) => { deptTree.value = res.data || [] })
  pageRole({ current: 1, size: 200, param: {} }).then((res) => {
    roleOptions.value = (res.data?.records || []).map((r) => ({
      ...r,
      roleId: String(r.roleId)
    }))
  })
})
</script>
