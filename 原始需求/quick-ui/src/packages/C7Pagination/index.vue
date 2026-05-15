<template>
  <el-pagination
    v-bind="$attrs"
    :current-page="innerCurrentPage"
    :page-size="innerPageSize"
    :total="total"
    :page-sizes="pageSizes"
    :layout="layout"
    :background="background"
    :small="small"
    :disabled="disabled"
    :hide-on-single-page="hideOnSinglePage"
    class="c7-pagination"
    @update:current-page="handleCurrentChange"
    @update:page-size="handleSizeChange"
    @prev-click="(page) => emit('prev-click', page)"
    @next-click="(page) => emit('next-click', page)"
  />
</template>

<script setup>
import { computed } from 'vue'

defineOptions({ name: 'C7Pagination', inheritAttrs: false })

const props = defineProps({
  /** 当前页码，v-model:currentPage */
  currentPage: {
    type: Number,
    default: 1
  },
  /** 每页条数，v-model:pageSize */
  pageSize: {
    type: Number,
    default: 10
  },
  /** 总条数 */
  total: {
    type: Number,
    default: 0
  },
  /** 每页条数选项 */
  pageSizes: {
    type: Array,
    default: () => [10, 20, 30, 50, 100]
  },
  /** 组件布局 */
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  /** 是否带背景色 */
  background: {
    type: Boolean,
    default: true
  },
  /** 小型分页 */
  small: {
    type: Boolean,
    default: false
  },
  /** 是否禁用 */
  disabled: {
    type: Boolean,
    default: false
  },
  /** 只有一页时是否隐藏 */
  hideOnSinglePage: {
    type: Boolean,
    default: false
  },
  /** 切换 pageSize 时是否自动重置 currentPage 到第一页，默认 true */
  autoReset: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits([
  'update:currentPage',
  'update:pageSize',
  'size-change',
  'current-change',
  'prev-click',
  'next-click',
  'change'
])

// ── 内部 currentPage（computed 双向绑定）──
const innerCurrentPage = computed({
  get: () => props.currentPage,
  set: (val) => emit('update:currentPage', val)
})

// ── 内部 pageSize（computed 双向绑定）──
const innerPageSize = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
})

/**
 * 页码变化处理
 */
function handleCurrentChange(page) {
  innerCurrentPage.value = page
  emit('current-change', page)
  emit('change', page, innerPageSize.value)
}

/**
 * 每页条数变化处理
 * - 修正：set 时 update:pageSize 已触发，此处仅额外 emit size-change 和重置页码
 */
function handleSizeChange(size) {
  innerPageSize.value = size
  emit('size-change', size)
  // autoReset=true 时切换条数自动回到第一页
  if (props.autoReset) {
    innerCurrentPage.value = 1
    emit('current-change', 1)
  }
  emit('change', props.autoReset ? 1 : innerCurrentPage.value, size)
}
</script>

<style scoped>
.c7-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
