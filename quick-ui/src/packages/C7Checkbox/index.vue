<template>
  <div class="c7-checkbox">
    <el-checkbox
        v-if="showSelectAllRow"
        :model-value="allSelectableSelected"
        :indeterminate="masterIndeterminate"
        :disabled="masterDisabled"
        @change="onSelectAllChange"
    >
      全选
    </el-checkbox>
    <el-checkbox-group
        v-bind="forwardedAttrs"
        :model-value="innerModel"
        :disabled="disabled"
        @update:model-value="onInnerGroupUpdate"
    >
      <template v-if="checkboxStyle === 'button'">
        <el-checkbox-button
            v-for="(row, idx) in resolvedRows"
            :key="rowKey(row, idx)"
            :label="row.value"
            :disabled="row.disabled || optionSelectDisabled(row)"
        >
          {{ row.label }}
        </el-checkbox-button>
      </template>
      <template v-else>
        <el-checkbox
            v-for="(row, idx) in resolvedRows"
            :key="rowKey(row, idx)"
            :label="row.value"
            :border="checkboxStyle === 'border'"
            :disabled="row.disabled || optionSelectDisabled(row)"
        >
          {{ row.label }}
        </el-checkbox>
      </template>
    </el-checkbox-group>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, shallowRef, useAttrs, watch} from 'vue'
import get from 'lodash/get'

defineOptions({name: 'C7Checkbox', inheritAttrs: false})

/**
 * C7 多选框：在 `ElCheckboxGroup` 上统一静态/异步选项加载、与 `C7Select` 一致的 **`response.data` → `resultKey` → `dataFormatter`** 解析链、
 * **`joinValue`** 对外编码（对齐 `C7Select` 的 **`separator`**：逗号串空值为 **`''`**）、以及可选「全选/半选」。
 *
 * **静态优先**：若父级传入的 **`dataList` 不为 `undefined`**（含空数组），则选项来自 **`dataList`**，**不会**因 **`autoLoad`** 发起 **`fetchData`**。
 *
 * **`reload()`**：仅当存在 **`fetchData`** 时重新请求；仅有静态 **`dataList`** 时为 **no-op**。
 *
 * **`change`**：载荷 **始终**为 **`string[]`**；**`update:modelValue`** 由 **`joinValue`** 决定 **逗号串**或 **`string[]`**。
 *
 * **限制**：若选项 `value` 本身可能含英文逗号，请勿使用 **`joinValue=true`**。
 *
 * @emits update:modelValue 由 `joinValue` 决定对外 `string` 或 `string[]`
 * @emits change 始终 `string[]`
 * @emits loading-change `fetchData` 并发进行中为 true
 */

const RESERVED_ATTR_KEYS = new Set([
  'dataList',
  'fetchData',
  'fetchParams',
  'resultKey',
  'dataFormatter',
  'labelKey',
  'valueKey',
  'autoLoad',
  'joinValue',
  'showSelectAll',
  'indeterminate',
  'min',
  'max',
  'disabled',
  'checkboxStyle',
  'modelValue'
])

const props = defineProps({
  /** 静态选项；**`!== undefined` 即视为静态模式**（含 `[]`），优先级高于异步 */
  dataList: {type: Array, default: undefined},
  /**
   * 异步加载：`mergedParams` 为 **`{ ...fetchParams }`**（**不含 `query`**）。
   * @param {Record<string, *>} mergedParams
   * @returns {Promise<import('axios').AxiosResponse|any>}
   */
  fetchData: {type: Function, default: undefined},
  fetchParams: {type: Object, default: () => ({})},
  /** 从 `response.data` 上取列表的点路径 */
  resultKey: {type: String, default: ''},
  /**
   * 在 `resultKey` 解析后对数组做最终整形
   * @param {*} list
   * @returns {Array}
   */
  dataFormatter: {type: Function, default: undefined},
  /** 行对象上 label 字段路径，默认 `label` */
  labelKey: {type: String, default: 'label'},
  /** 行对象上 value 字段路径，默认 `value` */
  valueKey: {type: String, default: 'value'},
  /** 无静态 `dataList` 时，挂载后自动 `fetchData` 一次 */
  autoLoad: {type: Boolean, default: false},
  /**
   * 为 true 时对外 **`v-model` / `update:modelValue`** 使用逗号分隔 **string**；空选择固定为 **`''`**（与 **`C7Select.separator`** 一致）。
   * 为 false 时对外为 **`string[]`**。
   */
  joinValue: {type: Boolean, default: false},
  /** 是否展示「全选」行 */
  showSelectAll: {type: Boolean, default: false},
  /**
   * **deprecated**：旧文档用名；语义同 **`showSelectAll`**，展示行条件为 **`showSelectAll || indeterminate`**。
   */
  indeterminate: {type: Boolean, default: false},
  /** 最小选中数（不在组件内拦截提交，仅保留 props 供页面校验或后续扩展） */
  min: {type: Number, default: undefined},
  /** 最大选中数；达到上限时未选项禁用新增选中；且影响「全选」是否可点 */
  max: {type: Number, default: undefined},
  /** 整组禁用 */
  disabled: {type: Boolean, default: false},
  /** `default` | `button` | `border` */
  checkboxStyle: {type: String, default: 'default'}
})

const emit = defineEmits(['update:modelValue', 'change', 'loading-change'])

const attrs = useAttrs()
const forwardedAttrs = computed(() => {
  const out = {}
  for (const key of Object.keys(attrs)) {
    if (RESERVED_ATTR_KEYS.has(key)) continue
    out[key] = attrs[key]
  }
  return out
})

const modelValue = defineModel()

/** 内部选中值，始终 `string[]`（可含当前 options 中不存在的 value，以满足「保留」策略） */
const innerModel = ref([])

const staticBindingDefined = computed(() => props.dataList !== undefined)

const staticRows = computed(() => {
  if (!staticBindingDefined.value) return []
  return normalizeDataList(props.dataList)
})

const asyncRows = shallowRef([])

const resolvedRows = computed(() => {
  if (staticBindingDefined.value) return staticRows.value
  return asyncRows.value
})

const showSelectAllRow = computed(() => !!(props.showSelectAll || props.indeterminate))

const maxNum = computed(() => {
  if (props.max === undefined || props.max === null) return undefined
  const n = Number(props.max)
  return Number.isFinite(n) ? n : undefined
})

const selectableRows = computed(() => resolvedRows.value.filter((r) => !r.disabled))
const selectableValues = computed(() => selectableRows.value.map((r) => r.value))
const selectableCount = computed(() => selectableValues.value.length)

const optionValueSet = computed(() => new Set(resolvedRows.value.map((r) => r.value)))

const selectAllDisabled = computed(() => {
  if (maxNum.value === undefined) return false
  return selectableCount.value > maxNum.value
})

const allSelectableSelected = computed(() => {
  const sel = innerModel.value
  const sv = selectableValues.value
  if (sv.length === 0) return false
  return sv.every((v) => sel.includes(v))
})

const masterIndeterminate = computed(() => {
  const sel = innerModel.value
  const sv = selectableValues.value
  if (sv.length === 0) return false
  const any = sv.some((v) => sel.includes(v))
  return any && !allSelectableSelected.value
})

const masterDisabled = computed(() => props.disabled || selectAllDisabled.value)

let fetchGeneration = 0
const inFlightCount = ref(0)
const loadingInternal = computed(() => inFlightCount.value > 0)

watch(
    loadingInternal,
    (v) => {
      emit('loading-change', v)
    },
    {flush: 'post'}
)

/**
 * 父 -> 内：统一解析为 `string[]`（支持逗号串与数组）
 * @param {*} outer
 * @returns {string[]}
 */
function outerToInner(outer) {
  if (outer == null || outer === '') return []
  if (Array.isArray(outer)) return outer.map((x) => String(x)).filter((s) => s.length > 0)
  return String(outer)
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s.length > 0)
}

/**
 * 内 -> 外
 * @param {string[]} inner
 * @returns {string|string[]}
 */
function innerToOuter(inner) {
  if (props.joinValue) {
    if (!inner || inner.length === 0) return ''
    return inner.map((v) => String(v)).join(',')
  }
  if (!inner || inner.length === 0) return []
  return [...inner]
}

function emitModel(inner) {
  emit('update:modelValue', innerToOuter(inner))
  emit('change', [...inner])
}

watch(
    () => modelValue.value,
    (v) => {
      innerModel.value = outerToInner(v)
    },
    {immediate: true, deep: true}
)

watch(
    () => props.joinValue,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

/**
 * @param {*} raw
 * @returns {{label: string, value: string, disabled: boolean}}
 */
function mapRawRow(raw) {
  const label = get(raw, props.labelKey) ?? raw?.label ?? raw?.text
  const val = get(raw, props.valueKey) ?? raw?.value
  return {
    label: label == null ? '' : String(label),
    value: val == null ? '' : String(val),
    disabled: raw?.disabled === true
  }
}

/**
 * @param {*} rows
 * @returns {Array<{label: string, value: string, disabled: boolean}>}
 */
function normalizeDataList(rows) {
  if (!Array.isArray(rows)) return []
  return rows.map(mapRawRow)
}

/**
 * @param {Record<string, *>} row
 * @param {number} idx
 */
function rowKey(row, idx) {
  return row.value ? String(row.value) : `i-${idx}`
}

/**
 * 已达 `max` 时禁止继续勾选未选项（仍允许取消已选）
 * @param {{label: string, value: string, disabled: boolean}} row
 */
function optionSelectDisabled(row) {
  if (props.disabled || row.disabled) return true
  if (maxNum.value === undefined) return false
  if (innerModel.value.includes(row.value)) return false
  return innerModel.value.length >= maxNum.value
}

/**
 * 将 EP 组更新与「保留不在 options 中的 value」合并
 * @param {string[]} nextFromGroup EP 返回的选中集合（通常仅含当前子项 value）
 */
function mergeOrphans(prev, nextFromGroup) {
  const next = (nextFromGroup || []).map((x) => String(x))
  const known = optionValueSet.value
  const orphans = prev.filter((v) => !known.has(v))
  return [...new Set([...orphans, ...next])]
}

/**
 * @param {string[]} next
 */
function onInnerGroupUpdate(next) {
  innerModel.value = mergeOrphans(innerModel.value, next)
  emitModel(innerModel.value)
}

/**
 * @param {boolean} checked
 */
function onSelectAllChange(checked) {
  const selectable = selectableValues.value
  let next
  if (checked) {
    next = [...new Set([...innerModel.value, ...selectable])]
  } else {
    const selSet = new Set(selectable)
    next = innerModel.value.filter((v) => !selSet.has(v))
  }
  innerModel.value = next
  emitModel(innerModel.value)
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
    const rawData = res?.data
    let list = rawData
    if (props.resultKey) list = get(rawData, props.resultKey)
    if (typeof props.dataFormatter === 'function') list = props.dataFormatter(list)
    if (!Array.isArray(list)) list = []
    asyncRows.value = normalizeDataList(list)
  } catch {
    // 失败不静默清空已有选项
  } finally {
    inFlightCount.value--
  }
}

function reload() {
  if (typeof props.fetchData !== 'function') return
  if (staticBindingDefined.value) return
  executeFetch({...props.fetchParams})
}

onMounted(() => {
  if (staticBindingDefined.value) return
  if (!props.autoLoad || typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
})

onMounted(() => {
  if (props.autoLoad && typeof props.fetchData !== 'function') {
    if (import.meta.env.DEV) {
      console.warn('[C7Checkbox] autoLoad=true 但未提供 fetchData，已跳过。')
    }
  }
})

defineExpose({
  /** 是否存在进行中的 `fetchData` */
  loading: loadingInternal,
  /**
   * 重新拉取选项：无静态 `dataList` 且存在 `fetchData` 时发起请求；否则 no-op。
   */
  reload
})
</script>

<style scoped>
.c7-checkbox {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
</style>
