<template>
  <el-pagination
    v-model:current-page="currentPage"
    v-model:page-size="pageSize"
    :page-sizes="pageSizes"
    :total="total"
    :layout="layout"
    :background="background"
    :small="small"
    :disabled="disabled"
    :hide-on-single-page="hideOnSinglePage"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
    @prev-click="handlePrevClick"
    @next-click="handleNextClick"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, defineOptions } from 'vue'

defineOptions({
  name: 'C7Pagination'
})

/**
 * 组件属性接口
 */
interface Props {
  // 当前页数
  currentPage?: number
  // 每页显示条目个数
  pageSize?: number
  // 每页显示个数选择器的选项设置
  pageSizes?: number[]
  // 总条目数
  total?: number
  // 组件布局，子组件名用逗号分隔
  layout?: string
  // 是否为分页按钮添加背景色
  background?: boolean
  // 是否使用小型分页样式
  small?: boolean
  // 是否禁用
  disabled?: boolean
  // 只有一页时是否隐藏
  hideOnSinglePage?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  currentPage: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 30, 50, 100],
  total: 0,
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  small: false,
  disabled: false,
  hideOnSinglePage: false
})

const emit = defineEmits<{
  'update:currentPage': [page: number]
  'update:pageSize': [size: number]
  'size-change': [size: number]
  'current-change': [page: number]
  'prev-click': [page: number]
  'next-click': [page: number]
}>()

/**
 * 内部当前页
 */
const currentPage = computed({
  get: () => props.currentPage,
  set: (value) => {
    emit('update:currentPage', value)
  }
})

/**
 * 内部每页条数
 */
const pageSize = computed({
  get: () => props.pageSize,
  set: (value) => {
    emit('update:pageSize', value)
  }
})

/**
 * 处理每页条数变化
 */
const handleSizeChange = (size: number) => {
  emit('size-change', size)
  emit('update:pageSize', size)
  // 切换每页条数时，重置到第一页
  emit('update:currentPage', 1)
  emit('current-change', 1)
}

/**
 * 处理当前页变化
 */
const handleCurrentChange = (page: number) => {
  emit('current-change', page)
  emit('update:currentPage', page)
}

/**
 * 处理上一页点击
 */
const handlePrevClick = (page: number) => {
  emit('prev-click', page)
}

/**
 * 处理下一页点击
 */
const handleNextClick = (page: number) => {
  emit('next-click', page)
}
</script>

<style scoped>
/* 可在此处添加组件样式 */
</style>

