<script setup lang="ts">
/**
 * 联系我们：展示占位联系方式，邮箱/电话行点击复制到剪贴板。
 */
function copy(text: string) {
  uni.setClipboardData({
    data: text,
    success: () => uni.showToast({ title: '已复制', icon: 'success' }),
  })
}
</script>

<template>
  <view class="qb-page contact">
    <view class="contact__card">
      <view class="contact__row" @click="copy('support@quickboot.local')">
        <text class="contact__label">邮箱</text>
        <text class="contact__value">support@quickboot.local</text>
      </view>
      <view class="contact__row" @click="copy('400-000-0000')">
        <text class="contact__label">电话</text>
        <text class="contact__value">400-000-0000</text>
      </view>
      <view class="contact__row">
        <text class="contact__label">工作时间</text>
        <text class="contact__value">工作日 9:00–18:00</text>
      </view>
    </view>
    <text class="qb-muted contact__hint">点击邮箱 / 电话可复制（占位信息，可改为 CMS）</text>
  </view>
</template>

<style scoped lang="scss">
.contact {
  padding: 32rpx 28rpx;
  background: #f3f5f7;
}

.contact__card {
  border-radius: 28rpx;
  background: #fff;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.04);
}

.contact__row {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  padding: 30rpx 32rpx;
  border-bottom: 1rpx solid #f1f5f9;
}

.contact__row:last-child {
  border-bottom: 0;
}

.contact__label {
  font-size: 28rpx;
  color: #6b7280;
}

.contact__value {
  font-size: 28rpx;
  color: #111827;
  text-align: right;
}

.contact__hint {
  display: block;
  margin-top: 24rpx;
  text-align: center;
}
</style>
