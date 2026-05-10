<template>
  <el-time-picker
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

defineOptions({name: 'C7TimePicker', inheritAttrs: false})

/**
 * C7 时间选择器：基于 **Element Plus 2.10+** 的 **`ElTimePicker`**。
 *
 * **默认格式**：当 **`format`、** **`valueFormat`、** **`value-format` 均为 `undefined`** 时，同时注入 **`format` 与 `valueFormat` 为 `HH:mm:ss`**；**任一已传**则不注入对应项，与裸 **`ElTimePicker`** 一致。
 *
 * **范围模式**（**`is-range` / `isRange` 为真**）：
 * - **`rangeMerge`**（默认 **`false`**）：为 **`true`** 时 **`v-model`** 对外为 **单字符串**（两段用 **`mergeDelimiter`** 拼接）；为 **`false`** 时对外为 **EP 原生范围值**（一般为 **二元数组**）。**与 `C7DatePicker` 的差异**：**`rangeMerge=false`** 时 **不**将外向 **`string`** 按 **`mergeDelimiter`** 解析为回显数组（误传合并串将得到 **`null` + warn**，见 **`outerToInner`**）。
 * - **`mergeDelimiter`**（默认 **`','`**）：仅用于 **存储合并串**；**与 EP `range-separator`（面板展示）语义分离**。**`null` / `undefined` / 空字符串** 时有效分隔符回退为 **`','`**。
 * - **`rangeMerge=true`** 下父级传入合并串时拆成 **二元数组** 供 EP 回显；非法串 → 内向 **`null`** 并 **`console.warn`**（去重防刷屏）。
 *
 * **非范围**：**`rangeMerge` / `mergeDelimiter`** 无效果，**`outerToInner` / `innerToOuter`** 直透传。
 *
 * **`change`**：载荷经 **`innerToOuter`**，与 **`update:modelValue`** 外向形态一致（含清空为 **`null`**）。
 *
 * @prop {unknown} modelValue 对外绑定；范围且 **`rangeMerge`** 时为 **`string | null`**；范围且非合并时为 **EP 范围形态**；非范围时同 EP。
 * @prop {boolean} [rangeMerge=false] 范围模式下是否将值合并为单字符串对外输出。
 * @prop {string} [mergeDelimiter=','] 存储用分隔符；空串按 **`','`** 处理。
 * @emits update:modelValue 归一化后的值
 * @emits change 归一化后的值（与 **`update:modelValue`** 形态一致）
 * @emits blur
 * @emits focus
 */

/** 同一非法外向值只 warn 一次，避免 computed 重复触发刷屏。 */
let lastParseWarnKey = ''

function warnParseOnce(key, message, extra) {
  if (key === lastParseWarnKey) {
    return
  }
  lastParseWarnKey = key
  console.warn(`[C7TimePicker] ${message}`, extra)
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

/**
 * 是否处于 EP 时间范围选择模式（`is-range` / `isRange` 为真或布尔空属性）。
 * @returns {boolean}
 */
function isRangeMode() {
  const v = attrs['is-range'] !== undefined ? attrs['is-range'] : attrs.isRange
  if (v === undefined) {
    return false
  }
  return v === true || v === ''
}

/**
 * 外向 → EP 内向。
 * @param {unknown} outer 父级 `modelValue`
 * @returns {unknown} 传给 `ElTimePicker` 的 `model-value`
 */
function outerToInner(outer) {
  const range = isRangeMode()
  if (!range) {
    return outer
  }
  if (outer == null || outer === '') {
    return null
  }
  if (props.rangeMerge) {
    const delim = effectiveDelimiter()
    if (typeof outer === 'string') {
      if (!outer.includes(delim)) {
        warnParseOnce(`rng|str|${outer}`, '范围且 rangeMerge 下 modelValue 为字符串但缺少 mergeDelimiter，已清空', {outer})
        return null
      }
      const parts = outer.split(delim).map((s) => s.trim())
      if (parts.length !== 2 || parts[0] === '' || parts[1] === '') {
        warnParseOnce(`rng|str|${outer}`, '合并串非法（需恰好两段非空），已清空', {outer})
        return null
      }
      return parts
    }
    if (Array.isArray(outer)) {
      if (outer.length !== 2) {
        warnParseOnce(`rng|arr|${JSON.stringify(outer)}`, '范围数组长度不为 2，已清空', {outer})
        return null
      }
      return outer
    }
    warnParseOnce(`rng|${String(outer)}`, '范围且 rangeMerge 下 modelValue 类型不支持，已清空', {outer})
    return null
  }
  /** `rangeMerge=false`：不解析合并串，仅接受二元数组（与 OpenSpec `ui-c7-time-picker` 一致）。 */
  if (Array.isArray(outer)) {
    if (outer.length !== 2) {
      warnParseOnce(`rng0|arr|${JSON.stringify(outer)}`, '范围且非 rangeMerge 下数组长度不为 2，已清空', {outer})
      return null
    }
    return outer
  }
  warnParseOnce(`rng0|${String(outer)}`, '范围且非 rangeMerge 下 modelValue 须为长度 2 的数组，已清空', {outer})
  return null
}

/**
 * EP 内向 → 外向。
 * @param {unknown} inner `ElTimePicker` 回传值
 * @returns {unknown} 归一化后 `emit` 给父级的值
 */
function innerToOuter(inner) {
  const range = isRangeMode()
  if (!range) {
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
  const out = {...raw}
  if (out.format === undefined && out.valueFormat === undefined && out['value-format'] === undefined) {
    out.format = 'HH:mm:ss'
    out.valueFormat = 'HH:mm:ss'
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
