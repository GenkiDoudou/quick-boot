<script setup lang="ts">
/**
 * 登录页：用户名密码表单，成功后写入 userStore 并 reLaunch 到首页。
 */
import { ref } from 'vue'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const username = ref('admin')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await userStore.login(username.value.trim(), password.value.trim())
    uni.showToast({ title: '登录成功', icon: 'success' })
    uni.reLaunch({ url: '/pages/home/home' })
  }
  catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '登录失败'
    uni.showToast({ title: msg, icon: 'none' })
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="qb-page login">
    <view class="login__hero">
      <view class="login__brand-mark" />
      <text class="login__brand">Quick H5</text>
      <text class="login__tagline">移动端入口 · 对接 quickboot</text>
    </view>

    <view class="login__panel">
      <text class="login__panel-title">账号登录</text>

      <view class="login__field">
        <text class="login__label">用户名</text>
        <u-input
          v-model="username"
          placeholder="请输入用户名"
          clearable
          border="surround"
          :custom-style="{ background: '#f8fafc', borderRadius: '12rpx' }"
        />
      </view>

      <view class="login__field">
        <text class="login__label">密码</text>
        <u-input
          v-model="password"
          type="password"
          placeholder="请输入密码"
          clearable
          border="surround"
          :custom-style="{ background: '#f8fafc', borderRadius: '12rpx' }"
        />
      </view>

      <u-button
        type="primary"
        :loading="loading"
        :custom-style="{ marginTop: '16rpx', height: '88rpx', borderRadius: '16rpx', fontSize: '30rpx' }"
        @click="handleLogin"
      >
        登录
      </u-button>

      <text class="qb-muted login__hint">开发环境可使用 admin / admin123</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
.login {
  display: flex;
  flex-direction: column;
  background: linear-gradient(165deg, #ecfdf5 0%, #f3f5f7 42%, #f3f5f7 100%);
}

.login__hero {
  padding: 120rpx 48rpx 56rpx;
}

.login__brand-mark {
  width: 72rpx;
  height: 72rpx;
  margin-bottom: 28rpx;
  border-radius: 20rpx;
  background: linear-gradient(145deg, #10b981 0%, #059669 100%);
  box-shadow: 0 12rpx 28rpx rgba(5, 150, 105, 0.28);
}

.login__brand {
  display: block;
  font-size: 52rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
  color: #064e3b;
  line-height: 1.2;
}

.login__tagline {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #6b7280;
}

.login__panel {
  margin: 0 32rpx 48rpx;
  padding: 40rpx 36rpx 36rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 32rpx rgba(15, 23, 42, 0.06);
}

.login__panel-title {
  display: block;
  margin-bottom: 28rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #111827;
}

.login__field {
  margin-bottom: 28rpx;
}

.login__label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.login__hint {
  display: block;
  margin-top: 28rpx;
  text-align: center;
}
</style>
