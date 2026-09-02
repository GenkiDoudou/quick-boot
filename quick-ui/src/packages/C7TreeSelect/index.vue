<template>
  <el-tree-select
      ref="treeSelectRef"
      v-bind="forwardedAttrs"
      :data="displayTreeData"
      :model-value="innerModel"
      :multiple="multiple"
      :loading="mergedLoading"
      @update:model-value="onInnerModelUpdate"
      @change="onInnerChange"
      @visible-change="onVisibleChange"
  />
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref, shallowRef, useAttrs, watch} from 'vue'
import { get } from '@/utils/object'

defineOptions({name: 'C7TreeSelect', inheritAttrs: false})

/**
 * C7 树选择：在 `ElTreeSelect` 上统一静态/异步整树加载、字段映射、多选 `separator` 与 `valueType`。
 *
 * **静态**：`dataList` 与 `options` 为别名；**`dataList !== undefined` 时仅用 `dataList`**（与 `C7Select` 一致）。
 * **异步**：`autoLoad` 为 true、且无静态 props 绑定时，挂载后 `fetchData({ ...fetchParams })`（**无 `query`**）。
 * **树字段**：内部将节点规范为 `{ label, value, children }` 供 EP 使用（`mapTree`）。
 * **多选 + `separator`**：对外 `v-model`/`change` 为逗号字符串，空为 `''`；对内为数组（与 `C7Select` 一致）。
 * **`valueType`**：`auto` 时以 **规范树根节点第一条** 的 `value` 的 `typeof` 为准（仅为 `number` 时用 number，否则按 string）；大整数/精度问题与 `Number()` 固有限制相同，业务应避免超大 ID。
 * **`loading`**：展示态为 **内部请求中** 或 **父级经 attrs 传入的 `loading`**（后者与 `:loading="mergedLoading"` 合并为 OR）。
 *
 * @emits update:modelValue
 * @emits change 载荷与对外 `modelValue` 一致
 * @emits load-error 异步失败
 * @emits visible-change 下拉可见性
 * @emits loading-change 请求进行中为 true
 */

const RESERVED_ATTR_KEYS = new Set([
  'dataList',
  'options',
  'fetchData',
  'fetchParams',
  'resultKey',
  'dataFormatter',
  'autoLoad',
  'separator',
  'modelValue',
  'multiple',
  'labelKey',
  'valueKey',
  'childrenKey',
  'valueType'
])

const props = defineProps({
  dataList: {type: Array, default: undefined},
  options: {type: Array, default: undefined},
  fetchData: {type: Function, default: undefined},
  fetchParams: {type: Object, default: () => ({})},
  resultKey: {type: String, default: ''},
  dataFormatter: {type: Function, default: undefined},
  autoLoad: {type: Boolean, default: false},
  multiple: {type: Boolean, default: false},
  separator: {type: Boolean, default: false},
  labelKey: {type: String, default: 'label'},
  valueKey: {type: String, default: 'value'},
  childrenKey: {type: String, default: 'children'},
  /** `auto`：由规范树根首节点的 `value` 类型推断对外单选标量/多选元素类型 */
  valueType: {type: String, default: 'auto', validator: (v) => ['auto', 'string', 'number'].includes(v)}
})

const emit = defineEmits(['update:modelValue', 'change', 'load-error', 'visible-change', 'loading-change'])

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
const treeSelectRef = ref(null)

const staticBindingDefined = computed(() => props.dataList !== undefined || props.options !== undefined)

const staticTreeRaw = computed(() => {
  if (props.dataList !== undefined) return props.dataList
  if (props.options !== undefined) return props.options
  return []
})

const asyncTreeRaw = shallowRef([])

const resolvedSourceRows = computed(() => {
  if (staticBindingDefined.value) return staticTreeRaw.value
  return asyncTreeRaw.value
})

/**
 * 将业务树映射为 EP 需要的 `label`/`value`/`children`。
 * @param {*} nodes
 * @returns {Array<{label: *, value: *, children?: *}>}
 */
function mapTree(nodes) {
  if (!Array.isArray(nodes)) return []
  const lk = props.labelKey
  const vk = props.valueKey
  const ck = props.childrenKey
  return nodes.map((n) => {
    if (!n || typeof n !== 'object') return {label: '', value: undefined, children: undefined}
    const children = Array.isArray(n[ck]) && n[ck].length > 0 ? mapTree(n[ck]) : undefined
    return {
      label: n[lk],
      value: n[vk],
      children,
      disabled: n.disabled === true
    }
  })
}

const displayTreeData = computed(() => mapTree(resolvedSourceRows.value))

/** `auto` 模式下用于单选/多选元素类型：仅根第一条映射后节点的 `value` 为 `number` 时为 `number`，否则 `string` */
const autoCoerceKind = computed(() => {
  const rows = displayTreeData.value
  if (!rows.length) return 'string'
  const t = typeof rows[0].value
  return t === 'number' && !Number.isNaN(rows[0].value) ? 'number' : 'string'
})

const effectiveValueKind = computed(() => {
  if (props.valueType === 'string') return 'string'
  if (props.valueType === 'number') return 'number'
  return autoCoerceKind.value
})

/**
 * @param {*} v
 * @returns {*}
 */
function coerceOutScalar(v) {
  if (v === undefined || v === null) return v
  if (effectiveValueKind.value === 'number') {
    const n = Number(v)
    return Number.isNaN(n) ? v : n
  }
  return String(v)
}

/**
 * @param {*} v
 * @returns {*}
 */
function coerceInScalar(v) {
  if (v === undefined || v === null) return v
  if (effectiveValueKind.value === 'number') {
    const n = Number(v)
    return Number.isNaN(n) ? v : n
  }
  return String(v)
}

/**
 * @param {*} outer
 * @returns {*}
 */
function outerToInner(outer) {
  if (!props.multiple) {
    if (outer === undefined || outer === null || outer === '') return undefined
    return coerceInScalar(outer)
  }
  if (props.separator) {
    if (outer == null || outer === '') return []
    if (Array.isArray(outer)) return outer.map((x) => coerceInScalar(x))
    return String(outer)
        .split(',')
        .map((s) => s.trim())
        .filter((s) => s.length > 0)
        .map((x) => coerceInScalar(x))
  }
  if (Array.isArray(outer)) return outer.map((x) => coerceInScalar(x))
  if (outer == null || outer === '') return []
  return [coerceInScalar(outer)]
}

/**
 * @param {*} inner
 * @returns {*}
 */
function innerToOuter(inner) {
  if (!props.multiple) {
    if (inner === undefined || inner === null || inner === '') return undefined
    return coerceOutScalar(inner)
  }
  if (props.separator) {
    if (!inner || inner.length === 0) return ''
    return inner.map((v) => String(v)).join(',')
  }
  if (!inner || inner.length === 0) return []
  return inner.map((v) => coerceOutScalar(v))
}

const innerModel = ref(undefined)

watch(
    () => modelValue.value,
    (v) => {
      innerModel.value = outerToInner(v)
    },
    {immediate: true, deep: true}
)

watch(
    () => props.multiple,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

watch(
    () => props.separator,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

watch(
    () => props.valueType,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

watch(
    () => [props.labelKey, props.valueKey, props.childrenKey],
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

/** 异步树首帧为空时 `auto` 可能为 string；数据到达后需按新首节点类型重算内外值 */
watch(autoCoerceKind, () => {
  innerModel.value = outerToInner(modelValue.value)
})

/** 静态树数据晚于 modelValue 到达时需重新匹配节点以显示 label */
watch(
  () => displayTreeData.value,
  () => {
    innerModel.value = outerToInner(modelValue.value)
  },
  { deep: true }
)

let fetchGeneration = 0
const inFlightCount = ref(0)
const loadingInternal = computed(() => inFlightCount.value > 0)

const mergedLoading = computed(() => loadingInternal.value || !!attrs.loading)

watch(
    loadingInternal,
    (v) => {
      emit('loading-change', v)
    },
    {flush: 'post'}
)

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
    asyncTreeRaw.value = list
  } catch (err) {
    emit('load-error', err)
  } finally {
    inFlightCount.value--
  }
}

onMounted(() => {
  if (staticBindingDefined.value) return
  if (!props.autoLoad || typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
})

onMounted(() => {
  if (props.autoLoad && typeof props.fetchData !== 'function') {
    if (import.meta.env.DEV) {
      console.warn('[C7TreeSelect] autoLoad=true 但未提供 fetchData，已跳过。')
    }
  }
})

/**
 * @param {*} v
 */
function onInnerModelUpdate(v) {
  innerModel.value = v
  emit('update:modelValue', innerToOuter(v))
}

/**
 * @param {*} v
 */
function onInnerChange(v) {
  emit('change', innerToOuter(v))
}

/**
 * @param {boolean} visible
 */
function onVisibleChange(visible) {
  emit('visible-change', visible)
}

function reload() {
  if (staticBindingDefined.value) {
    innerModel.value = outerToInner(modelValue.value)
    return
  }
  if (typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
}

defineExpose({
  loading: loadingInternal,
  reload,
  treeSelectRef
})

onUnmounted(() => {
  fetchGeneration++
})
</script>
