<template>
  <el-cascader
      :key="reloadKey"
      ref="cascaderRef"
      v-bind="forwardedAttrs"
      :props="mergedPanelProps"
      :options="cascaderOptions"
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
import get from 'lodash/get'

defineOptions({name: 'C7Cascader', inheritAttrs: false})

/**
 * C7 级联选择：在 `ElCascader` 上统一静态/整树异步/懒加载、`resultKey`/`dataFormatter`、字段映射、多选 `separator` 与 `valueType`。
 *
 * **静态**：`dataList` 与 `options` 为别名；**`dataList !== undefined` 时仅用 `dataList`**（与 `C7TreeSelect` 一致）。
 * **整树异步**：无静态、**非懒加载**、**`autoLoad=true`** 时挂载后 `fetchData({ ...fetchParams })`（无 `parentId`）。
 * **懒加载**：**`lazy=true`** 且提供 **`fetchData`** 时，通过 EP **`props.lazy` + `props.lazyLoad`** 拉取子层；根层 **`parentId === rootParentId`**，子层 **`parentId`** 为展开父节点映射后的 **`value`**；接口返回 **当前层扁平列表**。
 * **`resultKey`**：从 **`response.data`** 取列表路径（与 `C7Select`/`C7TreeSelect` 一致），**不**表示 `children` 字段名（子节点字段为 **`childrenKey`**）。
 * **多选 + `separator`**：仅当 **`emit-path` 为 false** 且选中为 **一维标量** 时对外逗号串；**`emitPath` 默认 true** 时 **`separator` 无效**，开发环境 **`console.warn`**。
 * **`valueType`**：与 `C7TreeSelect` 对齐；**`emitPath=true`** 时对路径 **各层标量** 做类型转换。
 *
 * @emits update:modelValue
 * @emits change 载荷与对外 `modelValue` 一致
 * @emits load-error 异步失败，参数为 `err`
 * @emits visible-change
 * @emits loading-change 内部 `fetchData` 进行中为 true
 */

const RESERVED_ATTR_KEYS = new Set([
  'dataList',
  'options',
  'fetchData',
  'fetchParams',
  'resultKey',
  'dataFormatter',
  'autoLoad',
  'lazy',
  'rootParentId',
  'separator',
  'modelValue',
  'multiple',
  'labelKey',
  'valueKey',
  'childrenKey',
  'valueType',
  'props'
])

const props = defineProps({
  dataList: {type: Array, default: undefined},
  options: {type: Array, default: undefined},
  fetchData: {type: Function, default: undefined},
  fetchParams: {type: Object, default: () => ({})},
  resultKey: {type: String, default: ''},
  dataFormatter: {type: Function, default: undefined},
  autoLoad: {type: Boolean, default: false},
  /** 为 true 且无静态数据、且提供 `fetchData` 时，使用 EP `props.lazy` + `props.lazyLoad` 按层请求 */
  lazy: {type: Boolean, default: false},
  /**
   * 懒加载根请求使用的 parentId（`fetchData({ parentId, ...fetchParams })`）。
   * 子层使用展开节点的映射后 `value`。
   */
  /** 懒加载根层 `parentId`；子层使用父节点 `value` */
  rootParentId: {default: null},
  multiple: {type: Boolean, default: false},
  separator: {type: Boolean, default: false},
  labelKey: {type: String, default: 'label'},
  valueKey: {type: String, default: 'value'},
  childrenKey: {type: String, default: 'children'},
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

/** EP 默认 `emitPath` 为 true：仅当显式 `emit-path="false"` 时逗号 `separator` 才可能生效 */
const emitPathOff = computed(() => attrs.emitPath === false)

const modelValue = defineModel()
const cascaderRef = ref(null)
const reloadKey = ref(0)

const staticBindingDefined = computed(() => props.dataList !== undefined || props.options !== undefined)

const staticTreeRaw = computed(() => {
  if (props.dataList !== undefined) return props.dataList
  if (props.options !== undefined) return props.options
  return []
})

const asyncTreeRaw = shallowRef([])

const lazyListMode = computed(
    () => !staticBindingDefined.value && props.lazy === true && typeof props.fetchData === 'function'
)

const resolvedSourceRows = computed(() => {
  if (staticBindingDefined.value) return staticTreeRaw.value
  return asyncTreeRaw.value
})

/**
 * 整树递归映射为 EP 节点。
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

/**
 * 懒加载单层列表映射（子层由接口扁平返回）。
 * @param {*} nodes
 * @returns {Array<{label: *, value: *, leaf?: boolean, disabled?: boolean}>}
 */
function mapTreeFlat(nodes) {
  if (!Array.isArray(nodes)) return []
  const lk = props.labelKey
  const vk = props.valueKey
  const ck = props.childrenKey
  return nodes.map((n) => {
    if (!n || typeof n !== 'object') return {label: '', value: undefined, leaf: true}
    const rawCh = n[ck]
    const hasChildren = Array.isArray(rawCh) && rawCh.length > 0
    return {
      label: n[lk],
      value: n[vk],
      leaf: n.leaf === true ? true : n.leaf === false ? false : !hasChildren,
      disabled: n.disabled === true,
      children: hasChildren ? mapTreeFlat(rawCh) : undefined
    }
  })
}

const displayTreeData = computed(() => mapTree(resolvedSourceRows.value))

/** 非懒加载：来自静态或整树异步；懒加载：首屏空数组由 lazyLoad 填充根层 */
const cascaderOptions = computed(() => {
  if (lazyListMode.value) return []
  return displayTreeData.value
})

const userPanelProps = computed(() => {
  const raw = attrs.props
  if (raw && typeof raw === 'object' && !Array.isArray(raw)) return {...raw}
  return {}
})

const mergedPanelProps = computed(() => {
  const user = userPanelProps.value
  if (lazyListMode.value) {
    return {...user, lazy: true, lazyLoad: handleLazyLoad}
  }
  return Object.keys(user).length ? user : undefined
})

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
 * @param {*} seg
 * @returns {boolean}
 */
function isScalarish(seg) {
  return seg === null || typeof seg !== 'object'
}

/**
 * @param {*} path
 * @returns {*}
 */
function coercePathIn(path) {
  if (!Array.isArray(path)) return coerceInScalar(path)
  return path.map((x) => coerceInScalar(x))
}

/**
 * @param {*} path
 * @returns {*}
 */
function coercePathOut(path) {
  if (!Array.isArray(path)) return coerceOutScalar(path)
  return path.map((x) => coerceOutScalar(x))
}

/**
 * @returns {boolean}
 */
function separatorAllowedForInner(inner) {
  if (!props.multiple || !inner || !Array.isArray(inner)) return true
  return inner.every((x) => isScalarish(x))
}

/**
 * @returns {boolean}
 */
function useSeparatorEffective() {
  return props.multiple && props.separator && emitPathOff.value
}

/**
 * @param {*} outer
 * @returns {*}
 */
function outerToInner(outer) {
  /** Element Plus 级联默认 `emitPath` 为 true */
  const pathMode = attrs.emitPath !== false

  if (!props.multiple) {
    if (outer === undefined || outer === null || outer === '') return undefined
    if (pathMode) {
      if (Array.isArray(outer)) return coercePathIn(outer)
      return coerceInScalar(outer)
    }
    return coerceInScalar(outer)
  }

  if (pathMode) {
    if (outer == null || outer === '') return []
    if (!Array.isArray(outer)) return []
    return outer.map((p) => (Array.isArray(p) ? coercePathIn(p) : coerceInScalar(p)))
  }

  if (props.separator && emitPathOff.value) {
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
  const pathMode = attrs.emitPath !== false

  if (!props.multiple) {
    if (inner === undefined || inner === null || inner === '') return undefined
    if (pathMode) {
      if (Array.isArray(inner)) return coercePathOut(inner)
      return coerceOutScalar(inner)
    }
    return coerceOutScalar(inner)
  }

  if (pathMode) {
    if (!inner || inner.length === 0) return []
    return inner.map((p) => (Array.isArray(p) ? coercePathOut(p) : coerceOutScalar(p)))
  }

  const sepOk = useSeparatorEffective() && separatorAllowedForInner(inner)
  if (props.separator && sepOk) {
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

watch(
    () => attrs.emitPath,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

watch(autoCoerceKind, () => {
  innerModel.value = outerToInner(modelValue.value)
})

watch(
    () => [props.multiple, props.separator, attrs.emitPath],
    () => {
      if (!import.meta.env.DEV) return
      if (props.multiple && props.separator && attrs.emitPath !== false) {
        console.warn(
            '[C7Cascader] multiple + separator 与 emitPath（默认 true）不兼容；请设置 emit-path="false" 后再使用逗号串。'
        )
      }
    },
    {immediate: true}
)

watch(
    () => ({m: innerModel.value, sep: props.separator}),
    () => {
      if (!import.meta.env.DEV || !props.multiple || !props.separator || !emitPathOff.value) return
      if (!separatorAllowedForInner(innerModel.value)) {
        console.warn('[C7Cascader] 多选值为嵌套路径或非标量时 separator 无效，对外保持数组。')
      }
    },
    {deep: true}
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

/**
 * EP `props.lazyLoad`：按层调用 `fetchData`。
 * @param {*} node
 * @param {function(Array): void} resolve
 */
function handleLazyLoad(node, resolve) {
  if (typeof props.fetchData !== 'function') {
    resolve([])
    return
  }
  const parentId = node.level === 0 ? props.rootParentId : node.value
  const gen = ++fetchGeneration
  inFlightCount.value++
  props
      .fetchData({parentId, ...props.fetchParams})
      .then((res) => {
        if (gen !== fetchGeneration) {
          resolve([])
          return
        }
        const rawData = res?.data
        let list = rawData
        if (props.resultKey) list = get(rawData, props.resultKey)
        if (typeof props.dataFormatter === 'function') list = props.dataFormatter(list)
        if (!Array.isArray(list)) list = []
        resolve(mapTreeFlat(list))
      })
      .catch((err) => {
        emit('load-error', err)
        resolve([])
      })
      .finally(() => {
        inFlightCount.value--
      })
}

onMounted(() => {
  if (staticBindingDefined.value) return
  if (lazyListMode.value) return
  if (!props.autoLoad || typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
})

onMounted(() => {
  if (props.autoLoad && typeof props.fetchData !== 'function') {
    if (import.meta.env.DEV) {
      console.warn('[C7Cascader] autoLoad=true 但未提供 fetchData，已跳过。')
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
  if (lazyListMode.value) {
    reloadKey.value++
    fetchGeneration++
    return
  }
  if (typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
}

defineExpose({
  loading: loadingInternal,
  reload,
  cascaderRef
})

onUnmounted(() => {
  fetchGeneration++
})
</script>
