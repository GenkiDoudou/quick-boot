<script setup lang="ts">
/**
 * 状态单选芯片组：v-model 绑定字典 value，支持禁用指定选项。
 */
import { computed } from 'vue'
import type { DictOption } from '@/api/system/dict'

const props = defineProps<{
  modelValue: string
  options?: DictOption[]
  /** 整组只读（如 form mode=view） */
  disabled?: boolean
  /** 禁止选中的 value 列表 */
  disabledValues?: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [string]
}>()

const chips = computed(() => {
  if (props.options?.length) return props.options
  return [
    { label: '正常', value: '0' },
    { label: '停用', value: '1' },
  ] as DictOption[]
})

function isDisabled(value: string) {
  // 整组 disabled 优先；否则按 disabledValues 屏蔽单项
  if (props.disabled) return true
  return (props.disabledValues || []).includes(value)
}

function onPick(value: string) {
  if (isDisabled(value)) return
  emit('update:modelValue', value)
}
</script>

<template>
  <view class="qb-status-chips">
    <view
      v-for="c in chips"
      :key="c.value"
      class="qb-status-chips__item"
      :class="{
        on: modelValue === c.value,
        disabled: isDisabled(c.value),
      }"
      @click="onPick(c.value)"
    >
      {{ c.label }}
    </view>
  </view>
</template>

<style scoped lang="scss">
.qb-status-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.qb-status-chips__item {
  padding: 16rpx 30rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #64748b;
  background: #f1f5f9;
  border: 1rpx solid transparent;
  transition: background 0.15s ease, color 0.15s ease;
}

.qb-status-chips__item.on {
  color: #047857;
  background: #ecfdf5;
  border-color: #a7f3d0;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(5, 150, 105, 0.12);
}

.qb-status-chips__item.disabled {
  opacity: 0.4;
}
</style>
