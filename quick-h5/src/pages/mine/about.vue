<script setup lang="ts">
/**
 * 关于页：应用名称、版本与简介说明。
 */
const appName = 'Quick H5'
const version = '1.0.0'
</script>

<template>
  <view class="qb-page about">
    <view class="about__hero">
      <view class="about__logo" />
      <text class="about__name">{{ appName }}</text>
      <text class="qb-muted">版本 {{ version }}</text>
    </view>

    <view class="about__card">
      <text class="about__p">
        Quick Boot 移动端入口，对接现有登录与权限体系。工作台菜单由后台配置下发。
      </text>
    </view>

    <text class="qb-muted about__hint">清除缓存在「我的」页操作，默认保留登录态</text>
  </view>
</template>

<style scoped lang="scss">
.about {
  padding: 48rpx 28rpx;
  background: #f3f5f7;
}

.about__hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.about__logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 28rpx;
  margin-bottom: 8rpx;
  background: linear-gradient(145deg, #34d399, #059669);
  box-shadow: 0 12rpx 28rpx rgba(5, 150, 105, 0.25);
}

.about__name {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
}

.about__card {
  padding: 32rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.04);
}

.about__p {
  font-size: 28rpx;
  color: #374151;
  line-height: 1.6;
}

.about__hint {
  display: block;
  margin-top: 24rpx;
  text-align: center;
}
</style>
