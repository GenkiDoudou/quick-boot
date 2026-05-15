<template>
  <el-time-picker
    v-bind="$attrs"
    v-model="internalValue"
    :format="resolvedFormat"
    :value-format="resolvedValueFormat"
    @change="handleChange"
    @blur="handleBlur"
    @focus="handleFocus"
  />
</template>

<script setup>
import { computed, useAttrs } from 'vue'

defineOptions({ name: 'C7TimePicker', inheritAttrs: false })

const attrs = useAttrs()

const props = defineProps({
  /** v-model 绑定值，支持字符串、Date、[Date, Date]、[string, string] */
  modelValue: {
    type: [String, Date, Array],
    default: undefined
  },
  /** 显示格式，默认 'HH:mm:ss' */
  format: {
    type: String,
    default: undefined
  },
  /** 绑定值格式，默认 'HH:mm:ss' */
  valueFormat: {
    type: String,
    default: undefined
  },
  /**
   * 范围类型值是否合并为字符串，默认 false
   * true:  对外输出逗号分隔字符串（例如 '08:00:00,18:00:00'）
   * false: 对外输出数组（例如 ['08:00:00', '18:00:00']）
   */
  rangeMerge: {
    type: Boolean,
    default: false
  },
  /** 范围值合并时的分隔符，默认 ',' */
  rangeSeparator: {
    type: String,
    default: ','
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'blur',
  'focus'
])

// ── 推断显示格式 ──
const resolvedFormat = computed(() => {
  return props.format ?? 'HH:mm:ss'
})

// ── 推断绑定值格式 ──
const resolvedValueFormat = computed(() => {
  return props.valueFormat ?? 'HH:mm:ss'
})

// ── 是否是范围模式（通过 $attrs.isRange 判断） ──
const isRange = computed(() => {
  // el-time-picker 使用 is-range prop 开启范围选择
  // 透传时 $attrs 中会有 isRange 或 is-range
  return !!(attrs['isRange'] ?? attrs['is-range'])
})

/**
 * internalValue 使用 computed 双向绑定，统一处理合并/拆分逻辑
 * get: 外部 modelValue → el-time-picker 需要的格式
 *   - 范围模式 + rangeMerge + 字符串值 → 按分隔符拆分为数组
 * set: el-time-picker 输出 → 对外 emit
 *   - 范围模式 + rangeMerge + 数组值 → 按分隔符合并为字符串
 */
const internalValue = computed({
  get() {
    const mv = props.modelValue
    if (isRange.value && props.rangeMerge && typeof mv === 'string' && mv !== '') {
      return mv.split(props.rangeSeparator).filter(Boolean)
    }
    return mv
  },
  set(val) {
    if (isRange.value && props.rangeMerge && Array.isArray(val)) {
      emit('update:modelValue', val.join(props.rangeSeparator))
    } else {
      emit('update:modelValue', val)
    }
  }
})

// ── 事件透传 ──
function handleChange(val) {
  emit('change', val)
}

function handleBlur(event) {
  emit('blur', event)
}

function handleFocus(event) {
  emit('focus', event)
}
</script>
