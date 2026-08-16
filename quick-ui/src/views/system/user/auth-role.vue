<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/system/user')" content="分配角色" class="mb8" />
    <el-descriptions :column="2" border class="mb8">
      <el-descriptions-item label="用户账号">{{ info.userName }}</el-descriptions-item>
      <el-descriptions-item label="用户昵称">{{ info.nickName }}</el-descriptions-item>
    </el-descriptions>
    <el-checkbox-group v-model="roleIds">
      <el-checkbox v-for="r in roles" :key="r.roleId" :value="r.roleId" :label="r.roleId">
        {{ r.roleName }}（{{ r.roleKey }}）
      </el-checkbox>
    </el-checkbox-group>
    <div class="mt16">
      <el-button type="primary" @click="submit">保存</el-button>
      <el-button @click="$router.back()">取消</el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * 用户分配角色页：路由参数 userId，勾选 roleIds 后提交授权。
 */
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAuthRole, updateAuthRole } from '@/api/system/user'

defineOptions({ name: 'SysUserAuthRole' })

const route = useRoute()
const router = useRouter()
const userId = route.params.userId
const info = ref({})
const roles = ref([])
const roleIds = ref([])

onMounted(async () => {
  const res = await getAuthRole(userId)
  const data = res.data || {}
  info.value = data
  roles.value = data.roles || []
  roleIds.value = (data.roleIds || []).map((id) => (typeof id === 'string' ? Number(id) : id))
})

async function submit() {
  if (!roleIds.value.length) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  await updateAuthRole({ userId, roleIds: roleIds.value })
  ElMessage.success('保存成功')
  router.push('/system/user')
}
</script>

<style scoped>
.mb8 { margin-bottom: 12px; }
.mt16 { margin-top: 16px; }
</style>
