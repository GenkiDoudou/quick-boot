<template>
  <div class="oauth-callback">
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
/**
 * OAuth 社交登录回调页：用 ticket 换 token，写入本地后跳转首页。
 */
import { ElMessage } from 'element-plus'
import { socialComplete } from '@/api/login'
import { setToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = ref('正在完成社交登录…')

onMounted(() => {
  const ticket = String(route.query.ticket || '')
  if (!ticket) {
    message.value = '缺少 ticket，请重新登录'
    setTimeout(() => router.replace('/login'), 1500)
    return
  }
  socialComplete(ticket)
    .then((res) => {
      const token = res.data?.accessToken || res.data?.access_token
      if (!token) {
        message.value = '未拿到 Token，请重新登录'
        return
      }
      setToken(token)
      userStore.token = token
      ElMessage.success('登录成功')
      router.replace('/')
    })
    .catch(() => {
      message.value = '社交登录未完成，请返回登录页重试'
      setTimeout(() => router.replace('/login'), 1500)
    })
})
</script>

<style scoped>
.oauth-callback {
  padding: 80px 24px;
  text-align: center;
}
</style>
