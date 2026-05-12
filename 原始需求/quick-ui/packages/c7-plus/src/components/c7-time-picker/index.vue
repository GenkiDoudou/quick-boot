<template>
  <el-time-picker
    v-model="timeValue"
    :placeholder="placeholder"
    :start-placeholder="startPlaceholder"
    :end-placeholder="endPlaceholder"
    :is-range="isRange"
    :format="format"
    :value-format="valueFormat"
    :default-value="defaultValue"
    :name="name"
    :prefix-icon="prefixIcon"
    :clear-icon="clearIcon"
    :size="size"
    :disabled="disabled"
    :editable="editable"
    :readonly="readonly"
    :clearable="clearable"
    :popper-class="popperClass"
    :range-separator="rangeSeparator"
    :disabled-hours="disabledHours"
    :disabled-minutes="disabledMinutes"
    :disabled-seconds="disabledSeconds"
    @change="handleChange"
    @blur="handleBlur"
    @focus="handleFocus"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, defineOptions } from 'vue'

defineOptions({
  name: 'C7TimePicker'
})

/**
 * 组件属性接口
 */
interface Props {
  // v-model 绑定值
  modelValue?: string | Date | [Date, Date] | [string, string]
  // 占位符
  placeholder?: string
  // 范围选择时开始时间的占位内容
  startPlaceholder?: string
  // 范围选择时结束时间的占位内容
  endPlaceholder?: string
  // 是否为时间范围选择
  isRange?: boolean
  // 显示在输入框中的格式
  format?: string
  // 可选，绑定值的格式
  valueFormat?: string
  // 可选，选择器打开时默认显示的时间
  defaultValue?: Date | [Date, Date]
  // 原生属性
  name?: string
  // 自定义头部图标的类名
  prefixIcon?: string | object
  // 自定义清空图标的类名
  clearIcon?: string | object
  // 输入框尺寸
  size?: 'large' | 'default' | 'small'
  // 是否禁用
  disabled?: boolean
  // 是否可输入
  editable?: boolean
  // 完全只读
  readonly?: boolean
  // 是否显示清除按钮
  clearable?: boolean
  // 下拉框的类名
  popperClass?: string
  // 选择范围时的分隔符
  rangeSeparator?: string
  // 禁止选择的小时
  disabledHours?: () => number[]
  // 禁止选择的分钟
  disabledMinutes?: (hour: number) => number[]
  // 禁止选择的秒
  disabledSeconds?: (hour: number, minute: number) => number[]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  placeholder: '请选择时间',
  startPlaceholder: '开始时间',
  endPlaceholder: '结束时间',
  isRange: false,
  format: 'HH:mm:ss',
  valueFormat: 'HH:mm:ss',
  defaultValue: undefined,
  name: undefined,
  prefixIcon: undefined,
  clearIcon: undefined,
  size: 'default',
  disabled: false,
  editable: true,
  readonly: false,
  clearable: true,
  popperClass: '',
  rangeSeparator: '-',
  disabledHours: undefined,
  disabledMinutes: undefined,
  disabledSeconds: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | Date | [Date, Date] | [string, string] | undefined]
  'change': [value: string | Date | [Date, Date] | [string, string] | undefined]
  'blur': [event: FocusEvent]
  'focus': [event: FocusEvent]
}>()

/**
 * 内部时间值
 */
const timeValue = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit('update:modelValue', value)
  }
})

/**
 * 处理值变化
 */
const handleChange = (value: string | Date | [Date, Date] | [string, string] | undefined) => {
  emit('change', value)
}

/**
 * 处理失焦
 */
const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

/**
 * 处理聚焦
 */
const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}
</script>

<style scoped>
/* 可在此处添加组件样式 */
</style>

