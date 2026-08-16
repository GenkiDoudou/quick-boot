<script setup lang="ts">
/**
 * CRUD 列表卡片：标题 / 副标题 / 元信息 + status、actions 插槽。
 * 支持 depth 缩进（部门树）；操作区将 qb-link 渲染为胶囊按钮。
 */
defineProps<{
  title?: string
  subtitle?: string
  meta?: string
  /** 左侧缩进层级（部门树） */
  depth?: number
  depthStep?: number
}>()
</script>

<template>
  <view class="qb-list-card">
    <view class="qb-list-card__accent" />
    <view
      class="qb-list-card__body"
      :style="depth
        ? { paddingLeft: `${28 + (depth || 0) * (depthStep || 28)}rpx` }
        : undefined"
    >
      <view class="qb-list-card__top">
        <view class="qb-list-card__main">
          <slot name="title">
            <text v-if="title" class="qb-list-card__title">{{ title }}</text>
          </slot>
          <slot name="subtitle">
            <text v-if="subtitle" class="qb-list-card__subtitle">{{ subtitle }}</text>
          </slot>
        </view>
        <view v-if="$slots.status" class="qb-list-card__status">
          <slot name="status" />
        </view>
      </view>
      <slot name="meta">
        <text v-if="meta" class="qb-list-card__meta">{{ meta }}</text>
      </slot>
      <view v-if="$slots.actions" class="qb-list-card__actions">
        <slot name="actions" />
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.qb-list-card {
  position: relative;
  margin-bottom: 20rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 8rpx 28rpx rgba(15, 23, 42, 0.06);
  border: 1rpx solid rgba(226, 232, 240, 0.9);
  overflow: hidden;
}

.qb-list-card__accent {
  position: absolute;
  left: 0;
  top: 28rpx;
  bottom: 28rpx;
  width: 6rpx;
  border-radius: 0 8rpx 8rpx 0;
  background: linear-gradient(180deg, #34d399 0%, #059669 100%);
  opacity: 0.85;
}

.qb-list-card__body {
  padding: 28rpx 28rpx 24rpx 32rpx;
}

.qb-list-card__top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.qb-list-card__main {
  min-width: 0;
  flex: 1;
}

.qb-list-card__title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.35;
  letter-spacing: 0.01em;
}

.qb-list-card__subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.45;
  word-break: break-all;
}

.qb-list-card__status {
  flex-shrink: 0;
  margin-top: 4rpx;
}

.qb-list-card__meta {
  display: block;
  margin-top: 14rpx;
  padding: 10rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #64748b;
  background: #f8fafc;
  line-height: 1.4;
  word-break: break-all;
}

.qb-list-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 20rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #f1f5f9;
}

/* 插槽内 qb-link 做成可点胶囊，避免纯文字链难找 */
.qb-list-card__actions :deep(.qb-link) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 56rpx;
  padding: 0 22rpx;
  border-radius: 14rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #047857;
  background: #ecfdf5;
  border: 1rpx solid #a7f3d0;
}

.qb-list-card__actions :deep(.qb-link--warn) {
  color: #b45309;
  background: #fffbeb;
  border-color: #fde68a;
}

.qb-list-card__actions :deep(.qb-link--danger) {
  color: #be123c;
  background: #fff1f2;
  border-color: #fecdd3;
}
</style>
