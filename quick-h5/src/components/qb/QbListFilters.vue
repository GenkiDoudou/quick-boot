<script setup lang="ts">
/**
 * 列表页筛选条：在搜索栏下方展示「全部 + 字典选项」芯片。
 * 空字符串表示不传筛选（全部）；变更由外层 v-model 驱动 usePagedList.filters。
 */
import { computed } from 'vue'
import type { DictOption } from '@/api/system/dict'

const props = defineProps<{
  modelValue: string
  /** 不含「全部」的选项；默认正常/停用 */
  options?: DictOption[]
  label?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [string]
}>()

const chips = computed((): DictOption[] => {
  const base = props.options?.length
    ? props.options
    : ([
        { label: '正常', value: '0' },
        { label: '停用', value: '1' },
      ] as DictOption[])
  return [{ label: '全部', value: '' }, ...base]
})

function onUpdate(v: string) {
  emit('update:modelValue', v)
}
</script>

<template>
  <view class="qb-list-filters">
    <text v-if="label" class="qb-list-filters__label">{{ label }}</text>
    <QbStatusChips
      :model-value="modelValue"
      :options="chips"
      @update:model-value="onUpdate"
    />
  </view>
</template>
