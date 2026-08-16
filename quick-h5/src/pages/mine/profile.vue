<script setup lang="ts">
/**
 * 个人信息只读页：展示当前登录用户的用户名、昵称、ID。
 */
import { computed } from 'vue'
import { useUserStore } from '@/stores'

const user = useUserStore()
const displayName = computed(() => user.nickName || user.username || '用户')
</script>

<template>
  <view class="qb-page profile">
    <view class="profile__hero">
      <view class="profile__avatar">
        <text>{{ displayName.slice(0, 1) }}</text>
      </view>
      <text class="profile__name">{{ displayName }}</text>
    </view>

    <view class="profile__card">
      <view class="profile__row">
        <text class="profile__label">用户名</text>
        <text class="profile__value">{{ user.username || '—' }}</text>
      </view>
      <view class="profile__row">
        <text class="profile__label">昵称</text>
        <text class="profile__value">{{ user.nickName || '—' }}</text>
      </view>
      <view class="profile__row">
        <text class="profile__label">用户 ID</text>
        <text class="profile__value profile__value--mono">{{ user.userId || '—' }}</text>
      </view>
    </view>
    <text class="qb-muted profile__hint">编辑能力后续按需接入</text>
  </view>
</template>

<style scoped lang="scss">
.profile {
  padding: 32rpx 28rpx;
  background: #f3f5f7;
}

.profile__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 32rpx;
  padding: 40rpx 24rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.04);
}

.profile__avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(145deg, #34d399, #059669);
  text-transform: uppercase;
}

.profile__name {
  font-size: 34rpx;
  font-weight: 700;
  color: #111827;
}

.profile__card {
  border-radius: 28rpx;
  background: #fff;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.04);
}

.profile__row {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  padding: 30rpx 32rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.profile__row:last-child {
  border-bottom: 0;
}

.profile__label {
  font-size: 28rpx;
  color: #6b7280;
}

.profile__value {
  font-size: 28rpx;
  color: #111827;
  text-align: right;
  word-break: break-all;
}

.profile__value--mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 24rpx;
}

.profile__hint {
  display: block;
  margin-top: 24rpx;
  text-align: center;
}
</style>
