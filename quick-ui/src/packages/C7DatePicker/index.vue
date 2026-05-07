<template>
  <el-date-picker
      v-bind="pickerAttrs"
      :model-value="innerModelValue"
      @update:model-value="onInnerUpdate"
      @change="onInnerChange"
      @blur="onInnerBlur"
      @focus="onInnerFocus"
  />
</template>

<script setup>
import {computed, useAttrs} from 'vue'

defineOptions({name: 'C7DatePicker', inheritAttrs: false})

/**
 * C7 日期选择器：基于 **Element Plus 2.10+** 的 **`ElDatePicker`**。
 *
 * **默认格式**：当父级 **未** 传入 **`format` / `valueFormat`**（含 **`value-format`**）时，按 **`type`** 注入内置映射（见脚本内 **`FORMAT_DEFAULTS`**）；**未命中映射的 `type`** 不注入，行为与裸 **`ElDatePicker`** 一致。
 *
 * **范围类 `type`**（**`daterange` / `datetimerange` / `monthrange` / `yearrange`**）：
 * - **`rangeMerge`**（默认 **`false`**）：为 **`true`** 时 **`v-model`** 对外为 **单字符串**（两段用 **`mergeDelimiter`** 拼接）；为 **`false`** 时对外为 **EP 原生范围值**（一般为 **二元数组**）。
 * - **`mergeDelimiter`**（默认 **`','`**）：仅用于 **存储合并串** 的拼接/拆分；**与 EP `rangeSeparator`（面板「起—止」展示）语义分离**。若传入 **空字符串**，**回退为 `','`**。
 * - 父级传入 **合并串**（含分隔符）时拆成 **二元数组** 供 EP 回显；**非法串**（段数不为 2、空段等）→ 内部 **`null`** 并 **`console.warn`**。
 *
 * **非范围 `type`**：**`rangeMerge` / `mergeDelimiter`** 无效果。
 *
 * **`change`**：载荷经 **`innerToOuter`** 归一化，与 **`update:modelValue`** 对外形态一致（含清空为 **`null`**）。
 *
 * @prop {unknown} modelValue 对外绑定值；范围且 **`rangeMerge`** 时为 **`string | null`**；范围且非合并时为 **EP 范围形态**；非范围时同 EP。
 * @prop {boolean} [rangeMerge=false] 范围类型下是否将值合并为单字符串对外输出。
 * @prop {string} [mergeDelimiter=','] 存储用分隔符；空串按 **`','`** 处理。
 * @emits update:modelValue 归一化后的值
 * @emits change 归一化后的值（与 **`update:modelValue`** 形态一致）
 * @emits blur
 * @emits focus
 */

/** 与 EP 2.10 + dayjs 占位符对齐；`week` 与 Element Plus 文档「周」选择器常用格式一致（`gggg-wo`：年 + 周序）。 */
const FORMAT_DEFAULTS = Object.freeze({
  date: {format: 'YYYY-MM-DD', valueFormat: 'YYYY-MM-DD'},
  daterange: {format: 'YYYY-MM-DD', valueFormat: 'YYYY-MM-DD'},
  datetime: {format: 'YYYY-MM-DD HH:mm:ss', valueFormat: 'YYYY-MM-DD HH:mm:ss'},
  datetimerange: {format: 'YYYY-MM-DD HH:mm:ss', valueFormat: 'YYYY-MM-DD HH:mm:ss'},
  month: {format: 'YYYY-MM', valueFormat: 'YYYY-MM'},
  monthrange: {format: 'YYYY-MM', valueFormat: 'YYYY-MM'},
  year: {format: 'YYYY', valueFormat: 'YYYY'},
  yearrange: {format: 'YYYY', valueFormat: 'YYYY'},
  week: {format: 'gggg-wo', valueFormat: 'gggg-wo'}
})

const RANGE_TYPES = Object.freeze(['daterange', 'datetimerange', 'monthrange', 'yearrange'])

/** 同一非法外向值只 warn 一次，避免 computed 重复触发刷屏。 */
let lastParseWarnKey = ''

function warnParseOnce(key, message, extra) {
  if (key === lastParseWarnKey) {
    return
  }
  lastParseWarnKey = key
  console.warn(`[C7DatePicker] ${message}`, extra)
}

const props = defineProps({
  modelValue: {type: [String, Number, Date, Array, Object], default: undefined},
  rangeMerge: {type: Boolean, default: false},
  mergeDelimiter: {type: String, default: ','}
})

const emit = defineEmits(['update:modelValue', 'change', 'blur', 'focus'])

const attrs = useAttrs()

const LISTENER_KEYS = new Set(['onUpdate:modelValue', 'onChange', 'onBlur', 'onFocus'])

/** 有效分隔符：`mergeDelimiter` 为空时回退逗号。 */
function effectiveDelimiter() {
  const d = props.mergeDelimiter
  return d == null || String(d).length === 0 ? ',' : String(d)
}

function isRangeType(type) {
  return RANGE_TYPES.includes(type)
}

function resolvedPickerType() {
  const t = attrs.type
  return typeof t === 'string' && t ? t : 'date'
}

/**
 * 外向 → EP 内向。
 * @param {unknown} outer 父级 `modelValue`
 * @returns {unknown} 传给 `ElDatePicker` 的 `model-value`
 */
function outerToInner(outer) {
  const type = resolvedPickerType()
  if (!isRangeType(type)) {
    return outer
  }
  if (outer == null || outer === '') {
    return null
  }
  const delim = effectiveDelimiter()
  if (typeof outer === 'string') {
    if (!outer.includes(delim)) {
      warnParseOnce(`${type}|${outer}`, '范围类型下 modelValue 为字符串但缺少 mergeDelimiter，已清空', {type, outer})
      return null
    }
    const parts = outer.split(delim).map((s) => s.trim())
    if (parts.length !== 2 || parts[0] === '' || parts[1] === '') {
      warnParseOnce(`${type}|${outer}`, '合并串非法（需恰好两段非空），已清空', {type, outer})
      return null
    }
    return parts
  }
  if (Array.isArray(outer)) {
    if (outer.length !== 2) {
      warnParseOnce(`${type}|arr|${JSON.stringify(outer)}`, '范围数组长度不为 2，已清空', {type, outer})
      return null
    }
    return outer
  }
  warnParseOnce(`${type}|${String(outer)}`, '范围类型 modelValue 类型不支持，已清空', {type, outer})
  return null
}

/**
 * EP 内向 → 外向。
 * @param {unknown} inner `ElDatePicker` 回传值
 * @returns {unknown} 归一化后 `emit` 给父级的值
 */
function innerToOuter(inner) {
  const type = resolvedPickerType()
  if (!isRangeType(type)) {
    return inner
  }
  if (inner == null) {
    return null
  }
  if (!Array.isArray(inner)) {
    return null
  }
  if (inner.length !== 2) {
    return null
  }
  const a = inner[0]
  const b = inner[1]
  if (props.rangeMerge) {
    const delim = effectiveDelimiter()
    if (a == null && b == null) {
      return null
    }
    return `${a == null ? '' : a}${delim}${b == null ? '' : b}`
  }
  return inner
}

const innerModelValue = computed(() => outerToInner(props.modelValue))

const pickerAttrs = computed(() => {
  const raw = {...attrs}
  for (const k of LISTENER_KEYS) {
    delete raw[k]
  }
  const type = resolvedPickerType()
  const defaults = FORMAT_DEFAULTS[type]
  const out = {...raw}
  if (defaults) {
    if (out.format === undefined) {
      out.format = defaults.format
    }
    if (out.valueFormat === undefined && out['value-format'] === undefined) {
      out.valueFormat = defaults.valueFormat
    }
  }
  return out
})

function onInnerUpdate(val) {
  const next = innerToOuter(val)
  emit('update:modelValue', next)
}

function onInnerChange(val) {
  emit('change', innerToOuter(val))
}

function onInnerBlur(e) {
  emit('blur', e)
}

function onInnerFocus(e) {
  emit('focus', e)
}
</script>
