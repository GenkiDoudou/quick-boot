<template>
  <div class="social-bind-page">
    <h2>社交账号绑定</h2>
    <p v-if="pending">身份：{{ pending.registrationId }} / {{ pending.externalSubject }}（{{ pending.displayName || '-' }}）</p>
    <p v-else class="muted">未检测到待绑定社交身份，请从登录页重新发起授权。</p>
    <div v-if="pending" class="actions">
      <el-button type="primary" :loading="loading" @click="onAutoCreate">自动建号并登录</el-button>
      <el-divider>或绑定已有账号</el-divider>
      <el-form @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password />
        </el-form-item>
        <el-button type="success" :loading="loading" @click="onBind">绑定并登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { getSocialPending, socialAutoCreate, socialBind } from '@/api/login'
import { setToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const pending = ref(null)
const ticket = ref(String(route.query.ticket || ''))
const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)

onMounted(() => {
  if (!ticket.value) {
    return
  }
  getSocialPending(ticket.value)
    .then((res) => {
      pending.value = res.data
    })
    .catch(() => {
      pending.value = null
    })
})

function applyToken(data) {
  const token = data.accessToken || data.access_token
  setToken(token)
  userStore.token = token
  router.push('/')
}

function onAutoCreate() {
  loading.value = true
  socialAutoCreate(ticket.value)
    .then((res) => {
      ElMessage.success('建号成功')
      applyToken(res.data)
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

function onBind() {
  loading.value = true
  socialBind(ticket.value, username.value, password.value)
    .then((res) => {
      ElMessage.success('绑定成功')
      applyToken(res.data)
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}
</script>

<style scoped>
.social-bind-page {
  max-width: 420px;
  margin: 80px auto;
  padding: 24px;
}
.muted {
  color: #888;
}
.actions {
  margin-top: 16px;
}
</style>
