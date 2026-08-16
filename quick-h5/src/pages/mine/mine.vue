<script setup lang="ts">
/**
 * 「我的」Tab：用户信息入口、联系/关于、清除应用缓存（保留登录态）、退出登录。
 */
import { computed, ref } from 'vue'
import { useUserStore } from '@/stores'

const CACHE_KEY = 'quick_h5_app_cache'
const user = useUserStore()
const displayName = computed(() => user.nickName || user.username || '用户')
const cacheSizeText = ref('—')

/** 估算本地 CACHE_KEY 占用并展示为 KB 或「空」 */
function refreshCacheHint() {
  try {
    const raw = uni.getStorageSync(CACHE_KEY)
    const bytes = typeof raw === 'string' ? raw.length : JSON.stringify(raw || '').length
    cacheSizeText.value = bytes > 0 ? `${(bytes / 1024).toFixed(1)} KB` : '空'
  }
  catch {
    cacheSizeText.value = '—'
  }
}

refreshCacheHint()

function go(url: string) {
  uni.navigateTo({ url })
}

/** 清除应用缓存键，不调用 logout */
function clearCache() {
  uni.showModal({
    title: '清除缓存',
    content: '仅清除应用缓存，保留登录态。确定继续？',
    success: (res) => {
      if (!res.confirm) {
        return
      }
      uni.removeStorageSync(CACHE_KEY)
      refreshCacheHint()
      uni.showToast({ title: '已清除', icon: 'success' })
    },
  })
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确定退出当前账号？',
    success: (res) => {
      if (!res.confirm) {
        return
      }
      user.logout()
      uni.reLaunch({ url: '/pages/login/login' })
    },
  })
}
</script>

<template>
  <view class="qb-page mine">
    <view class="mine__profile" @click="go('/pages/mine/profile')">
      <view class="mine__avatar">
        <text class="mine__avatar-text">{{ displayName.slice(0, 1) }}</text>
      </view>
      <view class="mine__meta">
        <text class="mine__name">{{ displayName }}</text>
        <text class="mine__sub">@{{ user.username || '—' }} · ID {{ user.userId || '—' }}</text>
      </view>
      <text class="mine__chevron">›</text>
    </view>

    <view class="mine__card">
      <view class="mine__cell" @click="go('/pages/mine/profile')">
        <view class="mine__cell-l">
          <view class="mine__ico">人</view>
          <text>个人信息</text>
        </view>
        <text class="mine__cell-r">查看 ›</text>
      </view>
    </view>

    <view class="mine__card">
      <view class="mine__cell" @click="go('/pages/mine/contact')">
        <view class="mine__cell-l">
          <view class="mine__ico">联</view>
          <text>联系我们</text>
        </view>
        <text class="mine__cell-r">›</text>
      </view>
      <view class="mine__cell" @click="go('/pages/mine/about')">
        <view class="mine__cell-l">
          <view class="mine__ico">关</view>
          <text>关于</text>
        </view>
        <text class="mine__cell-r">v1.0.0 ›</text>
      </view>
      <view class="mine__cell" @click="clearCache">
        <view class="mine__cell-l">
          <view class="mine__ico">清</view>
          <text>清除缓存</text>
        </view>
        <text class="mine__cell-r">{{ cacheSizeText }} ›</text>
      </view>
    </view>

    <u-button
      type="error"
      plain
      :custom-style="{ marginTop: '40rpx', height: '88rpx', borderRadius: '16rpx' }"
      @click="logout"
    >
      退出登录
    </u-button>
  </view>
</template>

<style scoped lang="scss">
.mine {
  padding: 32rpx 28rpx calc(32rpx + env(safe-area-inset-bottom));
  background: #f3f5f7;
}

.mine__profile {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 24rpx;
  padding: 36rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #064e3b 0%, #059669 70%, #34d399 100%);
  box-shadow: 0 12rpx 28rpx rgba(5, 150, 105, 0.22);
}

.mine__avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border: 2rpx solid rgba(255, 255, 255, 0.35);
}

.mine__avatar-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
  text-transform: uppercase;
}

.mine__meta {
  flex: 1;
  min-width: 0;
}

.mine__name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 6rpx;
}

.mine__sub {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}

.mine__chevron {
  font-size: 40rpx;
  color: rgba(255, 255, 255, 0.7);
}

.mine__card {
  margin-bottom: 24rpx;
  border-radius: 28rpx;
  background: #fff;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.04);
}

.mine__cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f1f5f9;
  font-size: 28rpx;
  color: #111827;
}

.mine__cell:last-child {
  border-bottom: 0;
}

.mine__cell-l {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.mine__ico {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
  color: #059669;
  background: #ecfdf5;
}

.mine__cell-r {
  font-size: 24rpx;
  color: #9ca3af;
}
</style>
