<script setup lang="ts">
/**
 * 列表页通用搜索栏：白底圆角容器 + 搜索 + 可选主色操作按钮。
 */
defineProps<{
  modelValue: string
  placeholder?: string
  addText?: string
  showAdd?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [string]
  search: []
  add: []
}>()

function onInput(v: string) {
  emit('update:modelValue', v)
}

function onSearch() {
  emit('search')
}
</script>

<template>
  <view class="qb-search-bar">
    <view class="qb-search-bar__inner">
      <u-search
        :model-value="modelValue"
        :placeholder="placeholder || '搜索'"
        shape="round"
        :show-action="true"
        action-text="搜索"
        bg-color="#f1f5f9"
        :action-style="{ color: '#059669', fontWeight: '600' }"
        @update:model-value="onInput"
        @search="onSearch"
        @custom="onSearch"
      />
      <u-button
        v-if="showAdd !== false"
        type="primary"
        size="small"
        :custom-style="{
          marginLeft: '16rpx',
          height: '68rpx',
          padding: '0 28rpx',
          borderRadius: '16rpx',
          fontWeight: '600',
          boxShadow: '0 6rpx 16rpx rgba(5, 150, 105, 0.28)',
        }"
        @click="emit('add')"
      >
        {{ addText || '新增' }}
      </u-button>
    </view>
  </view>
</template>

<style scoped lang="scss">
.qb-search-bar {
  margin-bottom: 24rpx;
}

.qb-search-bar__inner {
  display: flex;
  align-items: center;
  padding: 16rpx 16rpx 16rpx 20rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 8rpx 28rpx rgba(15, 23, 42, 0.06);
  border: 1rpx solid rgba(226, 232, 240, 0.95);
}

.qb-search-bar :deep(.u-search) {
  flex: 1;
  min-width: 0;
}
</style>
