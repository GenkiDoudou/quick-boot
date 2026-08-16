<script setup lang="ts">
/**
 * 列表底部状态区：空态 / 加载中 / 没有更多。
 */
defineProps<{
  loading?: boolean
  finished?: boolean
  empty?: boolean
  emptyText?: string
  hasRows?: boolean
}>()
</script>

<template>
  <view class="qb-list-foot-wrap">
    <view v-if="empty && !loading" class="qb-list-foot qb-list-foot--empty">
      <view class="qb-list-foot__icon" />
      <text class="qb-list-foot__text">{{ emptyText || '暂无数据' }}</text>
      <text class="qb-list-foot__hint">下拉可刷新</text>
    </view>
    <view v-else-if="loading" class="qb-list-foot">
      <text class="qb-list-foot__text qb-list-foot__text--muted">加载中…</text>
    </view>
    <view v-else-if="finished && hasRows" class="qb-list-foot">
      <text class="qb-list-foot__line" />
      <text class="qb-list-foot__text qb-list-foot__text--muted">没有更多了</text>
      <text class="qb-list-foot__line" />
    </view>
  </view>
</template>

<style scoped lang="scss">
.qb-list-foot-wrap {
  padding: 8rpx 0 24rpx;
}

.qb-list-foot {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 36rpx 0 16rpx;
}

.qb-list-foot--empty {
  flex-direction: column;
  padding: 72rpx 24rpx 48rpx;
  gap: 12rpx;
}

.qb-list-foot__icon {
  width: 96rpx;
  height: 96rpx;
  margin-bottom: 8rpx;
  border-radius: 28rpx;
  background:
    linear-gradient(145deg, #ecfdf5 0%, #f1f5f9 100%);
  border: 1rpx solid #e2e8f0;
  box-shadow: inset 0 0 0 12rpx rgba(255, 255, 255, 0.65);
}

.qb-list-foot__text {
  font-size: 28rpx;
  font-weight: 600;
  color: #334155;
}

.qb-list-foot__text--muted {
  font-size: 24rpx;
  font-weight: 500;
  color: #94a3b8;
}

.qb-list-foot__hint {
  font-size: 22rpx;
  color: #94a3b8;
}

.qb-list-foot__line {
  width: 48rpx;
  height: 2rpx;
  background: #e2e8f0;
}
</style>
