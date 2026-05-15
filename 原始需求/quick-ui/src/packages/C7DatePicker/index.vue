<template>
  <el-date-picker
    v-bind="$attrs"
    v-model="internalValue"
    :type="type"
    :format="resolvedFormat"
    :value-format="resolvedValueFormat"
    @change="handleChange"
    @blur="handleBlur"
    @focus="handleFocus"
  />
</template>

<script setup>
import { computed } from 'vue'

defineOptions({ name: 'C7DatePicker', inheritAttrs: false })

// ── 支持的日期类型 ──
const rangeTypes = ['daterange', 'datetimerange', 'monthrange', 'yearrange']

// ── 各 type 默认显示格式映射 ──
const defaultFormatMap = {
  date:          'YYYY-MM-DD',
  daterange:     'YYYY-MM-DD',
  year:          'YYYY',
  years:         'YYYY',
  month:         'YYYY-MM',
  months:        'YYYY-MM',
  monthrange:    'YYYY-MM',
  yearrange:     'YYYY',
  datetime:      'YYYY-MM-DD HH:mm:ss',
  datetimerange: 'YYYY-MM-DD HH:mm:ss',
  week:          'YYYY 第 ww 周',
  dates:         'YYYY-MM-DD',
}

const props = defineProps({
  /** v-model 绑定值，支持字符串、字符串数组、Date 对象 */
  modelValue: {
    type: [String, Array, Date],
    default: undefined
  },
  /**
   * 日期选择器类型
   * 'date' | 'daterange' | 'year' | 'years' | 'month' | 'months' |
   * 'datetime' | 'datetimerange' | 'week' | 'dates' | 'monthrange' | 'yearrange'
   */
  type: {
    type: String,
    default: 'date'
  },
  /** 显示格式，不传则根据 type 自动推断 */
  format: {
    type: String,
    default: undefined
  },
  /** 绑定值格式，不传则根据 type 自动推断 */
  valueFormat: {
    type: String,
    default: undefined
  },
  /**
   * 范围类型值是否合并为字符串，默认 true
   * true:  对外输出逗号分隔字符串（例如 '2024-01-01,2024-12-31'）
   * false: 对外输出数组（例如 ['2024-01-01', '2024-12-31']）
   */
  rangeMerge: {
    type: Boolean,
    default: true
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
  return props.format ?? defaultFormatMap[props.type] ?? 'YYYY-MM-DD'
})

// ── 推断绑定值格式 ──
const resolvedValueFormat = computed(() => {
  return props.valueFormat ?? defaultFormatMap[props.type] ?? 'YYYY-MM-DD'
})

// ── 是否是范围类型 ──
const isRange = computed(() => rangeTypes.includes(props.type))

/**
 * internalValue 使用 computed 双向绑定，统一处理合并/拆分逻辑
 * get: 外部 modelValue → el-date-picker 需要的格式
 *   - 范围类型 + rangeMerge + 字符串值 → 按分隔符拆分为数组
 * set: el-date-picker 输出 → 对外 emit
 *   - 范围类型 + rangeMerge + 数组值 → 按分隔符合并为字符串
 */
const internalValue = computed({
  get() {
    const mv = props.modelValue
    if (isRange.value && props.rangeMerge && typeof mv === 'string' && mv !== '') {
      // 合并字符串 → 拆分为数组供 el-date-picker 使用
      return mv.split(props.rangeSeparator).filter(Boolean)
    }
    return mv
  },
  set(val) {
    if (isRange.value && props.rangeMerge && Array.isArray(val)) {
      // 数组 → 合并为分隔符字符串后对外发出
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
