<template>
  <el-date-picker
      v-bind="$attrs"
      v-model="internalValue"
      :type="props.type"
      :format="computedFormat"
      :value-format="computedValueFormat"
      @update:modelValue="handleValueChange"
  >
    <slot></slot>
  </el-date-picker>
</template>

<script setup lang="ts">
import { ref, computed, watch, defineOptions } from 'vue'

defineOptions({ name: 'C7DatePicker' })

/**
 * 日期选择器类型
 */
type DateType = 'date' | 'daterange' | 'year' | 'month' | 'datetime' | 'datetimerange'

/**
 * 组件属性接口
 */
interface Props {
  modelValue?: string | string[] | Date  // 绑定值
  type?: DateType                         // 日期类型
  format?: string                         // 显示格式
  valueFormat?: string                    // 绑定值格式
  rangeMerge?: boolean                    // 范围选择时是否合并为字符串
}

const props = withDefaults(defineProps<Props>(), {
  type: 'date',
  format: '',
  valueFormat: '',
  rangeMerge: true
})

const emit = defineEmits<{
  'update:modelValue': [value: string | string[] | Date | undefined]
}>()

/**
 * 默认格式映射表
 * 根据不同的日期类型提供默认的格式化字符串
 */
const defaultFormatMap: Record<DateType, string> = {
  date: 'YYYY-MM-DD',
  daterange: 'YYYY-MM-DD',
  year: 'YYYY',
  month: 'YYYY-MM',
  datetime: 'YYYY-MM-DD HH:mm:ss',
  datetimerange: 'YYYY-MM-DD HH:mm:ss'
}

/**
 * 计算属性：显示格式
 * 优先使用用户指定的格式，否则根据 type 自动推断
 */
const computedFormat = computed(() => props.format || defaultFormatMap[props.type] || 'YYYY-MM-DD')

/**
 * 计算属性：绑定值格式
 * 优先使用用户指定的格式，否则根据 type 自动推断
 */
const computedValueFormat = computed(() => props.valueFormat || defaultFormatMap[props.type] || 'YYYY-MM-DD')

/**
 * 内部值：用于 el-date-picker 的 v-model
 */
const internalValue = ref<any>(props.modelValue)

/**
 * 监听外部 modelValue 变更并处理范围值的拆分
 * 当 type 为 daterange 或 datetimerange 且 rangeMerge 为 true 时：
 * - 外部传入字符串 '2024-01-01,2024-01-31'
 * - 内部转换为数组 ['2024-01-01', '2024-01-31']
 */
watch(
    () => props.modelValue,
    (val) => {
      const isRangeType = ['daterange', 'datetimerange'].includes(props.type)
      
      if (isRangeType && typeof val === 'string' && props.rangeMerge) {
        // 范围类型且开启合并：将字符串拆分为数组
        const range = val.split(',').map(item => item.trim())
        internalValue.value = range.length === 2 ? range : []
      } else {
        // 其他情况：直接使用原值
        internalValue.value = val
      }
    },
    { immediate: true }
)

/**
 * 值变更处理函数
 * 当 type 为 daterange 或 datetimerange 且 rangeMerge 为 true 时：
 * - 内部数组 ['2024-01-01', '2024-01-31']
 * - 对外输出字符串 '2024-01-01,2024-01-31'
 * 
 * @param val 新值
 */
const handleValueChange = (val: any) => {
  const isRangeType = ['daterange', 'datetimerange'].includes(props.type)
  
  if (isRangeType && Array.isArray(val) && props.rangeMerge) {
    // 范围类型且开启合并：将数组合并为字符串
    emit('update:modelValue', val.join(','))
  } else {
    // 其他情况：直接输出
    emit('update:modelValue', val)
  }
}
</script>

