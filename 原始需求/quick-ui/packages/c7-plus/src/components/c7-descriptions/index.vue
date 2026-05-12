<template>
  <el-descriptions
    :title="title"
    :column="column"
    :direction="direction"
    :size="size"
    :border="border"
    :label-class-name="labelClassName"
    :content-class-name="contentClassName"
  >
    <el-descriptions-item
      v-for="(item, index) in items"
      :key="item.prop || index"
      :label="item.label"
      :span="item.span"
      :label-class-name="item.labelClassName"
      :content-class-name="item.contentClassName"
    >
      <!-- 自定义插槽 -->
      <slot
        v-if="item.slotName"
        :name="item.slotName"
        :value="getValue(item)"
        :item="item"
      />
      
      <!-- 字典标签显示 -->
      <c7-dict-tag
        v-else-if="item.dictList && Array.isArray(item.dictList) && item.dictList.length > 0"
        :options="item.dictList"
        :model-value="getValue(item)"
        :show-value="showValue"
      />
      
      <!-- 默认显示 -->
      <span v-else>{{ formatValue(item) }}</span>
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
import { computed, defineOptions } from 'vue'
import C7DictTag from '../c7-dict-tag/index.vue'

defineOptions({
  name: 'C7Descriptions'
})

/**
 * 描述项配置接口
 */
export interface DescriptionItem {
  // 标签文本
  label: string
  // 数据字段名
  prop?: string
  // 栅格占据的列数
  span?: number
  // 自定义标签类名
  labelClassName?: string
  // 自定义内容类名
  contentClassName?: string
  // 字典数据列表（用于值转换）
  dictList?: Array<{ label: string; value: string | number; [key: string]: any }>
  // 自定义格式化函数
  formatter?: (value: any, item: DescriptionItem) => string
  // 插槽名称
  slotName?: string
  // 空值显示文本
  emptyText?: string
}

/**
 * 组件属性接口
 */
interface Props {
  // 数据对象
  data?: Record<string, any>
  // 描述项配置列表
  items?: DescriptionItem[]
  // 标题
  title?: string
  // 列数（响应式：{ xs: 1, sm: 2, md: 3, lg: 4 }）
  column?: number | Record<string, number>
  // 排列方向
  direction?: 'horizontal' | 'vertical'
  // 尺寸
  size?: 'large' | 'default' | 'small'
  // 是否显示边框
  border?: boolean
  // 自定义标签类名
  labelClassName?: string
  // 自定义内容类名
  contentClassName?: string
  // 未匹配字典值时是否显示原始值
  showValue?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ({}),
  items: () => [],
  title: '',
  column: 3,
  direction: 'horizontal',
  size: 'default',
  border: true,
  labelClassName: '',
  contentClassName: '',
  showValue: true
})

/**
 * 获取字段值
 */
const getValue = (item: DescriptionItem): any => {
  if (!item.prop) return ''
  
  // 支持嵌套属性（如 'user.name'）
  const keys = item.prop.split('.')
  let value = props.data
  
  for (const key of keys) {
    if (value && typeof value === 'object' && key in value) {
      value = value[key]
    } else {
      return undefined
    }
  }
  
  return value
}

/**
 * 格式化值
 */
const formatValue = (item: DescriptionItem): string => {
  const value = getValue(item)
  
  // 空值处理
  if (value === null || value === undefined || value === '') {
    return item.emptyText || '暂无'
  }
  
  // 自定义格式化函数
  if (typeof item.formatter === 'function') {
    return item.formatter(value, item)
  }
  
  // 数组处理
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  
  // 对象处理
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  
  return String(value)
}
</script>

<style scoped>
/* 可在此处添加组件样式 */
</style>

