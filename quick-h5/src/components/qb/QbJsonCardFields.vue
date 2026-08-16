<script setup lang="ts">
/**
 * JSON 配置驱动的卡片 meta 字段区。
 * 按 columns 渲染 qb-row / qb-col-* / qb-kv；支持 text、dict（QbDictTag）、slot。
 */
import { computed } from 'vue'
import type { QbCardColumn } from './qbCardColumn'

const props = defineProps<{
  /** 当前行数据 */
  row: Record<string, unknown>
  /** 列配置 */
  columns: QbCardColumn[]
}>()

defineSlots<{
  [name: string]: (props: { row: Record<string, unknown>; column: QbCardColumn; value: unknown }) => unknown
}>()

const SPAN_CLASS: Record<number, string> = {
  6: 'qb-col-6',
  8: 'qb-col-8',
  12: 'qb-col-12',
  16: 'qb-col-16',
  24: 'qb-col-24',
}

/** 将 span 映射到已有栅格类，非法值回退 12 */
function colClass(span?: number) {
  const n = Number(span)
  return SPAN_CLASS[n] || 'qb-col-12'
}

function cellValue(col: QbCardColumn) {
  return props.row?.[col.prop]
}

function isEmpty(val: unknown) {
  return val == null || val === ''
}

function displayText(col: QbCardColumn) {
  const val = cellValue(col)
  if (isEmpty(val)) return col.emptyText ?? '—'
  return String(val)
}

function slotNameOf(col: QbCardColumn) {
  return col.slotName || col.prop
}

/** 过滤 showIfProp 后的可见列 */
const visibleColumns = computed(() =>
  (props.columns || []).filter((col) => {
    if (!col.showIfProp) return true
    return !isEmpty(cellValue(col))
  }),
)
</script>

<template>
  <view class="qb-row">
    <view
      v-for="col in visibleColumns"
      :key="col.prop + (col.slotName || '')"
      class="qb-col"
      :class="colClass(col.span)"
    >
      <view
        class="qb-kv"
        :class="col.kv === 'stack' ? 'qb-kv--stack' : 'qb-kv--row'"
      >
        <text class="qb-kv__k">{{ col.label }}</text>
        <template v-if="(col.type || 'text') === 'slot'">
          <slot
            :name="slotNameOf(col)"
            :row="row"
            :column="col"
            :value="cellValue(col)"
          />
        </template>
        <view v-else-if="col.type === 'dict'" class="qb-kv__v">
          <QbDictTag :value="cellValue(col) as string | number | null" :options="col.options" />
        </view>
        <text v-else class="qb-kv__v">{{ displayText(col) }}</text>
      </view>
    </view>
  </view>
</template>
