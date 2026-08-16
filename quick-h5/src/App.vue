<script setup lang="ts">
/**
 * 应用根组件：启动时初始化 RUM、恢复登录态并刷新 /auth/me 权限；未登录则跳转登录页；onShow 上报页面访问。
 */
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores'
import { setupLiteRum, trackPageShow, setRumUin } from '@/monitor/liteRum'

onLaunch(async () => {
  setupLiteRum()
  const userStore = useUserStore()
  userStore.hydrateFromStorage()
  if (!userStore.isLoggedIn) {
    uni.reLaunch({
      url: '/pages/login/login',
    })
    return
  }
  try {
    // 有 token 必须拉 me，避免仅持久化了旧 permissions
    await userStore.refreshMe()
  }
  catch {
    userStore.logout()
    uni.reLaunch({
      url: '/pages/login/login',
    })
    return
  }
  if (userStore.username) {
    setRumUin(userStore.username)
  }
})

onShow(() => {
  trackPageShow()
})
</script>

<style lang="scss">
@import "uview-pro/index.scss";
@import "common/style.scss";
</style>
