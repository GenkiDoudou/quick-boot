<template>
  <div class="c7-radio-root">
    <!-- 空态：位于 ElRadioGroup 之上（EP 无 empty 插槽），见组件 JSDoc -->
    <div v-if="showEmptyBar" class="c7-radio__empty">
      <template v-if="props.emptyDisplay === 'text'">{{ emptyTextResolved }}</template>
      <slot v-else name="empty"/>
    </div>
    <el-radio-group
        ref="groupRef"
        v-bind="forwardedAttrs"
        :model-value="modelValue"
        @update:model-value="onUpdateModelValue"
        @change="onChange"
    >
      <template v-for="(item, idx) in displayItems" :key="item.key">
        <el-radio-button
            v-if="props.radioStyle === 'button'"
            :label="item.value"
            :disabled="item.disabled"
        >
          {{ item.label }}
        </el-radio-button>
        <el-radio
            v-else
            :label="item.value"
            :border="props.radioStyle === 'border'"
            :disabled="item.disabled"
        >
          {{ item.label }}
        </el-radio>
      </template>
    </el-radio-group>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, shallowRef, useAttrs, useSlots, watch} from 'vue'
import get from 'lodash/get'

defineOptions({name: 'C7Radio', inheritAttrs: false})

/**
 * C7 业务单选组：在 `ElRadioGroup` 上统一 **静态 `dataList` / `options`** 与 **`fetchData` + `fetchParams`**
 * 异步字典、**`response.data` → `resultKey` → `dataFormatter`** 解析链，以及 **`labelKey` / `valueKey`** 行映射。
 *
 * **与 `C7Select` 对齐**：`dataList` 与 `options` 为别名，**同时存在时 `dataList` 优先**；`fetchData(mergedParams)` **不注入 `query`**。
 * **与 `C7Select` 不同**：**`autoLoad` 默认 `true`**（单选无「展开再拉」，首屏尽早出选项）。
 *
 * **根级透传**：除 `RESERVED_ATTR_KEYS` 外，`$attrs` 绑定至 **`ElRadioGroup`**（如 `size`、`disabled`、`fill`、`text-color`），便于与 **`el-form-item`** 常规用法配合。
 *
 * **值比较**：选项就绪后校验「当前 `modelValue` 是否在选项中」使用 **`Object.is`**；若业务使用 **深度相等对象** 作 value，请避免或自行保证引用一致。
 *
 * **空态插槽 `#empty`**：渲染在 **`.c7-radio__empty`** 内、**`ElRadioGroup` 之前**（`emptyDisplay='slot'` 时）。
 *
 * @emits update:modelValue
 * @emits change 与 `v-model` 值形态一致
 * @emits loading-change `fetchData` 并发计数 >0 为 true
 */

const RESERVED_ATTR_KEYS = new Set([
  'dataList',
  'options',
  'fetchData',
  'fetchParams',
  'resultKey',
  'dataFormatter',
  'labelKey',
  'valueKey',
  'autoLoad',
  'radioStyle',
  'fetchErrorBehavior',
  'invalidModelBehavior',
  'emptyDisplay',
  'emptyText',
  'suppressInvalidModelDevWarn',
  'modelValue'
])

const props = defineProps({
  /** 静态选项；与 `options` 别名；**同时存在时优先** */
  dataList: {type: Array, default: undefined},
  /** `dataList` 别名；**`dataList !== undefined` 时仅用 `dataList`** */
  options: {type: Array, default: undefined},
  /**
   * 异步加载选项；`mergedParams` 为 **`{ ...fetchParams }` 浅拷贝**，**不含 `query`**。
   * @param {Record<string, *>} mergedParams
   * @returns {Promise<import('axios').AxiosResponse|any>}
   */
  fetchData: {type: Function, default: undefined},
  fetchParams: {type: Object, default: () => ({})},
  resultKey: {type: String, default: ''},
  /**
   * 在 `resultKey` 解析后对数组做最终整形
   * @param {*} list
   * @returns {Array}
   */
  dataFormatter: {type: Function, default: undefined},
  /** 从行对象取展示文案的点路径，默认 **`label`** */
  labelKey: {type: String, default: 'label'},
  /** 从行对象取选项值的点路径，默认 **`value`** */
  valueKey: {type: String, default: 'value'},
  /** 无静态绑定且存在 `fetchData` 时，是否在挂载后自动拉取一次 */
  autoLoad: {type: Boolean, default: true},
  /** `default`：普通 `ElRadio`；`button`：`ElRadioButton`；`border`：`ElRadio` + border */
  radioStyle: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'button', 'border'].includes(v)
  },
  /** `fetch` reject 时的选项与模型策略 */
  fetchErrorBehavior: {
    type: String,
    default: 'keep-last',
    validator: (v) => ['keep-last', 'clear-options', 'reset-model'].includes(v)
  },
  /** 当前 `modelValue` 不在选项中时的策略 */
  invalidModelBehavior: {
    type: String,
    default: 'keep',
    validator: (v) => ['keep', 'clear'].includes(v)
  },
  /** 无选项时的占位：`none` | `text` | `slot` */
  emptyDisplay: {
    type: String,
    default: 'none',
    validator: (v) => ['none', 'text', 'slot'].includes(v)
  },
  /** `emptyDisplay='text'` 时展示；空串则不渲染文本 */
  emptyText: {type: String, default: ''},
  /** 为 true 时不输出「当前值不在选项中」的开发期 `console.warn` */
  suppressInvalidModelDevWarn: {type: Boolean, default: false}
})

const emit = defineEmits(['update:modelValue', 'change', 'loading-change'])

const attrs = useAttrs()
const slots = useSlots()

const forwardedAttrs = computed(() => {
  const out = {}
  for (const key of Object.keys(attrs)) {
    if (RESERVED_ATTR_KEYS.has(key)) continue
    out[key] = attrs[key]
  }
  return out
})

const modelValue = defineModel()

const groupRef = ref(null)

/** `dataList` / `options` 任一 props 被显式传入（含 `undefined` 占位）即视为静态路径已定义，与 `C7Select` 一致 */
const staticBindingDefined = computed(() => props.dataList !== undefined || props.options !== undefined)

const staticSourceRows = computed(() => {
  if (props.dataList !== undefined) return normalizeRawRows(props.dataList)
  if (props.options !== undefined) return normalizeRawRows(props.options)
  return []
})

const asyncSourceRows = shallowRef([])

const sourceRows = computed(() => {
  if (staticBindingDefined.value) return staticSourceRows.value
  return asyncSourceRows.value
})

/**
 * 将原始行映射为 `{ label, value, disabled, raw, key }`；**无有效 value 的行被过滤**。
 * label 降级：`get(row,labelKey)` → `row.label` → `row.text` → `String(value)` → `''`
 */
const displayItems = computed(() => {
  const out = []
  let i = 0
  for (const row of sourceRows.value) {
    const value = pickValue(row)
    if (value === undefined) {
      i++
      continue
    }
    const label = pickLabel(row, value)
    const disabled = row?.disabled === true
    const key = value !== undefined && value !== null ? String(value) : `i-${i}`
    out.push({label, value, disabled, raw: row, key})
    i++
  }
  return out
})

const emptyTextResolved = computed(() => {
  const t = props.emptyText
  if (t == null || String(t).length === 0) return ''
  return String(t)
})

const showEmptyBar = computed(() => {
  if (displayItems.value.length > 0) return false
  if (props.emptyDisplay === 'text') return emptyTextResolved.value.length > 0
  /** 无 `#empty` 时降级为不展示占位（与 spec 一致） */
  if (props.emptyDisplay === 'slot') return typeof slots.empty === 'function'
  return false
})

const inFlightCount = ref(0)
const loadingInternal = computed(() => inFlightCount.value > 0)

watch(
    loadingInternal,
    (v) => {
      emit('loading-change', v)
    },
    {flush: 'post'}
)

let fetchGeneration = 0

/** 开发期「值不在选项中」仅 warn 一次，避免刷屏 */
let invalidValueWarned = false

/**
 * @param {*} rows
 * @returns {Array<*>}
 */
function normalizeRawRows(rows) {
  if (!Array.isArray(rows)) return []
  return rows
}

/**
 * @param {Record<string, *>} row
 * @returns {*}
 */
function pickValue(row) {
  const v = get(row, props.valueKey)
  if (v !== undefined) return v
  if (row && Object.prototype.hasOwnProperty.call(row, 'value')) return row.value
  return undefined
}

/**
 * @param {Record<string, *>} row
 * @param {*} value
 * @returns {string}
 */
function pickLabel(row, value) {
  const lb = get(row, props.labelKey)
  if (lb != null && lb !== '') return String(lb)
  if (row?.label != null && row.label !== '') return String(row.label)
  if (row?.text != null && row.text !== '') return String(row.text)
  if (value !== undefined && value !== null) return String(value)
  return ''
}

/**
 * @param {*} res `fetchData` resolve 值
 * @returns {Array<*>}
 */
function extractListFromResponse(res) {
  const rawData = res?.data
  let list = rawData
  if (props.resultKey) list = get(rawData, props.resultKey)
  if (typeof props.dataFormatter === 'function') list = props.dataFormatter(list)
  if (!Array.isArray(list)) list = []
  return list
}

/**
 * @param {Record<string, *>} mergedParams
 */
async function executeFetch(mergedParams) {
  if (typeof props.fetchData !== 'function') return
  const gen = ++fetchGeneration
  inFlightCount.value++
  try {
    const res = await props.fetchData(mergedParams)
    if (gen !== fetchGeneration) return
    const list = extractListFromResponse(res)
    asyncSourceRows.value = normalizeRawRows(list)
  } catch {
    if (gen !== fetchGeneration) return
    if (props.fetchErrorBehavior === 'clear-options') {
      asyncSourceRows.value = []
    } else if (props.fetchErrorBehavior === 'reset-model') {
      modelValue.value = undefined
      emit('change', undefined)
    }
    // keep-last：不改 asyncSourceRows
  } finally {
    inFlightCount.value--
  }
}

function reload() {
  if (typeof props.fetchData !== 'function') return
  if (staticBindingDefined.value) return
  executeFetch({...props.fetchParams})
}

/**
 * @param {*} val
 * @returns {boolean}
 */
function isValueInOptions(val) {
  return displayItems.value.some((it) => Object.is(it.value, val))
}

function applyInvalidModelPolicy() {
  if (displayItems.value.length === 0) return
  const mv = modelValue.value
  /** 未选（`undefined`/`null`）不视为「非法值」，避免与表单项空态冲突 */
  if (mv === undefined || mv === null) return
  if (isValueInOptions(mv)) return
  if (props.invalidModelBehavior === 'clear') {
    modelValue.value = undefined
    emit('change', undefined)
    return
  }
  if (import.meta.env.DEV && !props.suppressInvalidModelDevWarn && !invalidValueWarned) {
    invalidValueWarned = true
    console.warn('[C7Radio] 当前 modelValue 不在选项列表中，已按 invalidModelBehavior=keep 保留。', mv)
  }
}

watch(
    () => [displayItems.value, modelValue.value],
    () => {
      applyInvalidModelPolicy()
    },
    {flush: 'post'}
)

onMounted(() => {
  if (props.autoLoad && !staticBindingDefined.value && typeof props.fetchData !== 'function') {
    if (import.meta.env.DEV) {
      console.warn('[C7Radio] autoLoad=true 但未提供 fetchData，已跳过。')
    }
  }
  if (staticBindingDefined.value) return
  if (!props.autoLoad || typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
})

/**
 * @param {*} v
 */
function onUpdateModelValue(v) {
  modelValue.value = v
}

/**
 * @param {*} v
 */
function onChange(v) {
  emit('change', v)
}

defineExpose({
  /** 是否存在进行中的 `fetchData` */
  loading: loadingInternal,
  /** 重新拉取异步选项（静态绑定时 no-op） */
  reload,
  /** 原生 `ElRadioGroup` 实例 */
  groupRef
})
</script>

<style scoped>
.c7-radio-root {
  display: inline-block;
  vertical-align: middle;
}

.c7-radio__empty {
  margin-bottom: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
