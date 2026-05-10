<template>
  <el-pagination
      ref="paginationRef"
      v-bind="innerAttrs"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      :page-sizes="pageSizes"
      :layout="layout"
      :background="background"
      :small="small"
      :disabled="disabled"
      :hide-on-single-page="hideOnSinglePage"
      @update:current-page="onUpdateCurrentPage"
      @update:page-size="onUpdatePageSize"
      @size-change="onSizeChange"
      @current-change="onCurrentChange"
      @change="onInnerChange"
      @prev-click="onPrevClick"
      @next-click="onNextClick"
  />
</template>

<script setup>
import {computed, nextTick, ref, useAttrs} from 'vue'

defineOptions({name: 'C7Pagination', inheritAttrs: false})

/**
 * C7 业务分页：在 `ElPagination` 上统一 **双绑**、**切换 pageSize 后回第一页（`autoReset`）** 与 **`change(page, pageSize)` 单次汇总**。
 *
 * **`change` 语义**：每次用户操作导致的 **最终** `currentPage` + `pageSize` 只通知一次。`autoReset=true` 且用户改条数时，内部会忽略 Element Plus 在同一更新周期内可能产生的 **中间态** `change`，并在 `nextTick` 中 **`emit('change', 1, newPageSize)`**。
 *
 * **`autoReset=false`**：不强制回第 1 页；页码纠正与 `ElPagination` 一致。
 *
 * @emits update:currentPage
 * @emits update:pageSize
 * @emits current-change 当前页变化（与 EP 一致）
 * @emits size-change 每页条数变化
 * @emits prev-click / next-click
 * @emits change(page, pageSize) 汇总后的最终态
 */
const props = defineProps({
  /** 当前页（从 1 起），与 `v-model:currentPage` 同步 */
  currentPage: {type: Number, required: true},
  /** 每页条数，与 `v-model:pageSize` 同步 */
  pageSize: {type: Number, required: true},
  /** 总条数，与 `ElPagination` 一致 */
  total: {type: Number, default: undefined},
  /** 可选每页条数列表 */
  pageSizes: {type: Array, default: undefined},
  /** 布局字符串，未传则使用 Element Plus 默认 */
  layout: {type: String, default: undefined},
  background: Boolean,
  small: Boolean,
  disabled: Boolean,
  hideOnSinglePage: Boolean,
  /**
   * 为 `true`（默认）时，用户切换 `pageSize` 后将 `currentPage` 置为 1。
   * 为 `false` 时不强制回第一页。
   */
  autoReset: {type: Boolean, default: true},
})

const emit = defineEmits([
  'update:currentPage',
  'update:pageSize',
  'current-change',
  'size-change',
  'prev-click',
  'next-click',
  'change',
])

const attrs = useAttrs()

/** 透传至 `ElPagination`（已声明的 props 不会出现在 attrs 中） */
const innerAttrs = computed(() => ({...attrs}))

const paginationRef = ref(null)

/** `autoReset` 条数切换周期内忽略底层 `change`，避免中间态双发 */
const ignoreInnerChange = ref(false)

function onUpdateCurrentPage(p) {
  emit('update:currentPage', p)
}

function onUpdatePageSize(s) {
  emit('update:pageSize', s)
}

function onSizeChange(s) {
  emit('size-change', s)
  if (props.autoReset) {
    ignoreInnerChange.value = true
    emit('update:currentPage', 1)
    nextTick(() => {
      emit('change', 1, s)
      ignoreInnerChange.value = false
    })
  }
}

function onCurrentChange(p) {
  emit('current-change', p)
}

function onInnerChange(page, size) {
  if (ignoreInnerChange.value) {
    return
  }
  emit('change', page, size)
}

function onPrevClick(p) {
  emit('prev-click', p)
}

function onNextClick(p) {
  emit('next-click', p)
}

defineExpose({
  /** 底层 `ElPagination` 实例（与 Element Plus 版本字段一致） */
  paginationRef,
})
</script>
