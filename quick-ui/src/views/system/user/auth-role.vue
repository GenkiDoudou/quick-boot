<template>
  <div class="app-container">
    <el-page-header @back="goBack">
      <template #content>
        <span class="text-large font-600 mr-3">分配角色</span>
      </template>
    </el-page-header>

    <div v-loading="loading" class="auth-role-body">
      <p class="user-line">用户：{{ nickName }}（{{ userName }}）</p>
      <el-checkbox-group v-model="roleIds">
        <el-checkbox v-for="r in roles" :key="r.roleId" :label="r.roleId">
          {{ r.roleName }}
        </el-checkbox>
      </el-checkbox-group>
      <div class="actions">
        <el-button type="primary" @click="save">保存</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAuthRole, updateAuthRole } from '@/api/system/user'

defineOptions({ name: 'SysUserAuthRole' })

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const userId = ref(null)
const userName = ref('')
const nickName = ref('')
const roles = ref([])
const roleIds = ref([])

function load() {
  const uid = route.query.userId
  if (!uid) {
    ElMessage.error('缺少用户参数')
    return
  }
  loading.value = true
  getAuthRole(uid)
    .then((res) => {
      const d = res.data || res
      userId.value = d.userId
      userName.value = d.userName
      nickName.value = d.nickName
      roles.value = d.roles || []
      roleIds.value = [...(d.roleIds || [])]
    })
    .finally(() => {
      loading.value = false
    })
}

watch(
  () => route.query.userId,
  () => load(),
  { immediate: true }
)

function goBack() {
  router.push({ path: '/system/user' })
}

function save() {
  if (!userId.value) {
    return
  }
  if (!roleIds.value || roleIds.value.length === 0) {
    ElMessage.warning('至少选择一个角色')
    return
  }
  updateAuthRole({ userId: userId.value, roleIds: roleIds.value }).then(() => {
    ElMessage.success('保存成功')
    goBack()
  })
}
</script>

<style scoped>
.auth-role-body {
  margin-top: 16px;
  max-width: 720px;
}
.user-line {
  margin-bottom: 12px;
}
.actions {
  margin-top: 16px;
}
</style>
