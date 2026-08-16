<script setup lang="ts">
/**
 * 字典值展示为圆角标签：优先用 options 解析文案与色调，否则按 0/1 正常停用兜底。
 */
import { computed } from 'vue'
import { dictLabel, type DictOption } from '@/api/system/dict'

const props = defineProps<{
  value?: string | number | null
  options?: DictOption[]
  /** 无字典时的兜底：'0' 正常 / 其它 停用 */
  fallbackNormal?: boolean
}>()

/** 展示文案：字典命中 label，否则按 status 约定或原值 */
const label = computed(() => {
  if (props.options?.length) {
    return dictLabel(props.options, props.value, '—')
  }
  if (props.fallbackNormal !== false) {
    return String(props.value) === '1' ? '停用' : '正常'
  }
  return props.value == null || props.value === '' ? '—' : String(props.value)
})

/** 标签色调：listClass 映射 success/danger，或 value === '1' 视为停用 */
const tone = computed(() => {
  const opt = (props.options || []).find((o) => o.value === String(props.value ?? ''))
  const lc = String(opt?.listClass || '').toLowerCase()
  if (lc === 'danger' || lc === 'warning') return 'off'
  if (lc === 'success' || lc === 'primary') return 'on'
  return String(props.value) === '1' ? 'off' : 'on'
})
</script>

<template>
  <view class="qb-dict-tag" :class="tone === 'off' ? 'is-off' : 'is-on'">
    <view class="qb-dict-tag__dot" />
    <text>{{ label }}</text>
  </view>
</template>

<style scoped lang="scss">
.qb-dict-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
  padding: 8rpx 16rpx 8rpx 12rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 600;
  line-height: 1.2;
}

.qb-dict-tag__dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.85;
}

.qb-dict-tag.is-on {
  color: #047857;
  background: #ecfdf5;
  border: 1rpx solid #a7f3d0;
}

.qb-dict-tag.is-off {
  color: #be123c;
  background: #fff1f2;
  border: 1rpx solid #fecdd3;
}
</style>
