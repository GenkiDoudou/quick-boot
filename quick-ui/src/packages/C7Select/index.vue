<template>
  <el-select
      ref="selectRef"
      v-bind="forwardedAttrs"
      :model-value="innerModel"
      :multiple="multiple"
      :remote="remote"
      :remote-method="remote ? remoteMethodHandler : undefined"
      :filterable="remote ? true : forwardedAttrs.filterable"
      :loading="loadingInternal"
      @update:model-value="onInnerModelUpdate"
      @change="onInnerChange"
      @visible-change="onVisibleChange"
  >
    <template v-if="$slots.prefix" #prefix>
      <slot name="prefix"/>
    </template>
    <template v-if="$slots.label" #label>
      <slot name="label"/>
    </template>
    <template v-if="$slots.option" #option="scope">
      <slot name="option" v-bind="scope"/>
    </template>
    <template v-if="$slots.empty" #empty>
      <slot name="empty"/>
    </template>
    <el-option
        v-for="(row, idx) in resolvedOptionRows"
        :key="optionRowKey(row, idx)"
        :label="getOptionLabel(row)"
        :value="getOptionValue(row)"
        :disabled="row?.disabled === true"
    />
  </el-select>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref, shallowRef, useAttrs, watch} from 'vue'
import { debounce, get } from '@/utils/object'

defineOptions({name: 'C7Select', inheritAttrs: false})

/**
 * C7 业务下拉：在 `ElSelect` 上统一静态/异步/远程选项加载与多选 `separator` 对外格式。
 *
 * **静态来源**：`dataList` 与 `options` 为别名语义；**同时存在时 `dataList` 优先**（见 props 说明）。
 * **远程**：首次展开下拉触发 **不含 `query` 键** 的 `fetchData`；输入关键字经防抖后触发 `{ ...fetchParams, query }`。
 * **多选 + `separator`**：对外 `v-model` / `change` 为逗号分隔字符串，空为 `''`；对内始终数组。外部逗号字符串会解析为数组；
 * 若部分 value 不在当前选项中，**不静默删除**（保留策略）。
 *
 * **限制**：若 option 的 `value` 本身可能含英文逗号，请勿使用 `separator` 模式。
 *
 * @emits update:modelValue 对外形态由 `multiple` + `separator` 决定
 * @emits change 载荷与 `update:modelValue` 一致
 * @emits visible-change 透传 `ElSelect` 可见性
 * @emits loading-change `fetchData` 并发计数 >0 为 true
 */

const RESERVED_ATTR_KEYS = new Set([
  'dataList',
  'options',
  'fetchData',
  'fetchParams',
  'resultKey',
  'dataFormatter',
  'autoLoad',
  'reloadOnClear',
  'separator',
  'modelValue',
  'remote',
  'multiple'
])

const props = defineProps({
  /** 静态选项；与 `options` 二选一语义；**同时存在时优先于 `options`** */
  dataList: {type: Array, default: undefined},
  /** `dataList` 别名；**若 `dataList` 已传入（含 `undefined` 占位由父组件决定）以 `dataList` 为准**——此处简化为：`dataList !== undefined` 时只用 `dataList` */
  options: {type: Array, default: undefined},
  /**
   * 异步加载：`mergedParams` 至少包含 `fetchParams` 浅拷贝；远程搜索时包含 `query`。
   * @param {Record<string, *>} mergedParams
   * @returns {Promise<import('axios').AxiosResponse|any>}
   */
  fetchData: {type: Function, default: undefined},
  /** 合并进 `fetchData` 的自定义参数 */
  fetchParams: {type: Object, default: () => ({})},
  /** 从 `response.data` 上取列表的点路径，如 `rows` 或 `data.list` */
  resultKey: {type: String, default: ''},
  /**
   * 在 `resultKey` 解析后对数组做最终整形
   * @param {*} list
   * @returns {Array}
   */
  dataFormatter: {type: Function, default: undefined},
  /** `remote` 为 false 时，挂载后自动 `fetchData` 一次（无 `query`）；若已配置静态 `dataList`/`options` 则跳过以避免无意义请求 */
  autoLoad: {type: Boolean, default: false},
  /** 是否远程搜索模式（映射到 `ElSelect` 的 `remote`） */
  remote: {type: Boolean, default: false},
  /** 远程模式下，清空已选后是否重新拉取「全量」（无 `query`） */
  reloadOnClear: {type: Boolean, default: false},
  /**
   * 多选时是否对外使用逗号分隔字符串；空选择对外固定为 `''`。
   * 非多选时该 prop **无效**。
   */
  separator: {type: Boolean, default: false},
  /** 多选；与 `ElSelect` 一致，须由本组件声明以便 v-model 适配 */
  multiple: {type: Boolean, default: false}
})

const emit = defineEmits(['update:modelValue', 'change', 'visible-change', 'loading-change'])

const attrs = useAttrs()

const forwardedAttrs = computed(() => {
  const out = {}
  for (const key of Object.keys(attrs)) {
    if (RESERVED_ATTR_KEYS.has(key)) continue
    out[key] = attrs[key]
  }
  return out
})

/** 父级 `v-model`（未适配形态） */
const modelValue = defineModel()

const selectRef = ref(null)

/** 是否存在「静态 props 显式绑定」（`dataList` 或 `options` 任一非 `undefined`） */
const staticBindingDefined = computed(() => props.dataList !== undefined || props.options !== undefined)

/** 静态选项行：优先级 `dataList` > `options` */
const staticOptionRows = computed(() => {
  if (props.dataList !== undefined) return normalizeOptionRows(props.dataList)
  if (props.options !== undefined) return normalizeOptionRows(props.options)
  return []
})

/** 异步/远程合并后的展示列表 */
const asyncOptionRows = shallowRef([])

const resolvedOptionRows = computed(() => {
  if (props.remote) return asyncOptionRows.value
  if (staticBindingDefined.value) return staticOptionRows.value
  return asyncOptionRows.value
})

const innerModel = ref(undefined)

/** 远程场景最后一次 `remote-method` 收到的关键字（含空串） */
const lastRemoteQuery = ref('')

/** 首次展开是否已触发「无 query」全量拉取（避免与 `remote-method('')` 首帧重复打两次） */
const remoteInitialLoaded = ref(false)

/** 毫秒时间戳：此前忽略 `query` 为空的防抖远程请求（仅抑制首帧噪声，不影响用户主动清空关键字后的搜索） */
let suppressEmptyRemoteUntil = 0

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
 * @param {*} rows
 * @returns {Array<Record<string, *>>}
 */
function normalizeOptionRows(rows) {
  if (!Array.isArray(rows)) return []
  return rows
}

/**
 * @param {Record<string, *>} row
 * @param {number} idx
 */
function optionRowKey(row, idx) {
  const v = row?.value ?? row?.id
  return v !== undefined && v !== null ? String(v) : `i-${idx}`
}

function getOptionLabel(row) {
  return row?.label ?? row?.text ?? String(row?.value ?? '')
}

function getOptionValue(row) {
  return row?.value
}

/**
 * 父 -> 内：`ElSelect` 始终使用与 EP 一致的值形态（多选为数组）
 * @param {*} outer
 * @returns {*}
 */
function outerToInner(outer) {
  if (!props.multiple) return outer
  if (props.separator) {
    if (outer == null || outer === '') return []
    if (Array.isArray(outer)) return [...outer]
    return String(outer)
        .split(',')
        .map((s) => s.trim())
        .filter((s) => s.length > 0)
  }
  if (Array.isArray(outer)) return [...outer]
  if (outer == null || outer === '') return []
  return [outer]
}

/**
 * 内 -> 外
 * @param {*} inner
 * @returns {*}
 */
function innerToOuter(inner) {
  if (!props.multiple) return inner
  if (props.separator) {
    if (!inner || inner.length === 0) return ''
    return inner.map((v) => String(v)).join(',')
  }
  if (!inner || inner.length === 0) return []
  return [...inner]
}

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

/**
 * @param {*} mergedParams `fetchData` 入参；**全量**调用方须 **不包含 `query` 键**
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
    asyncOptionRows.value = normalizeOptionRows(list)
  } catch {
    // 失败不静默清空已有选项，避免闪断
  } finally {
    inFlightCount.value--
  }
}

const debouncedRemoteFetch = debounce((query) => {
  if (String(query ?? '').length === 0 && Date.now() < suppressEmptyRemoteUntil) return
  lastRemoteQuery.value = query
  executeFetch({...props.fetchParams, query})
}, 300)

/**
 * `ElSelect` `remote-method`：空串仍带 `query` 键，以区别于「全量无 query」首载
 * @param {string} query
 */
function remoteMethodHandler(query) {
  debouncedRemoteFetch(query)
}

/**
 * @param {boolean} visible
 */
function onVisibleChange(visible) {
  emit('visible-change', visible)
  if (!visible || !props.remote || !props.fetchData) return
  if (remoteInitialLoaded.value) return
  remoteInitialLoaded.value = true
  suppressEmptyRemoteUntil = Date.now() + 400
  const params = {...props.fetchParams}
  executeFetch(params)
}

onMounted(() => {
  if (props.remote) return
  if (staticBindingDefined.value) return
  if (!props.autoLoad || typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
})

onMounted(() => {
  if (props.autoLoad && typeof props.fetchData !== 'function') {
    if (import.meta.env.DEV) {
      console.warn('[C7Select] autoLoad=true 但未提供 fetchData，已跳过。')
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
 * @param {*} v
 */
function hasMeaningfulValue(v) {
  if (v == null) return false
  if (Array.isArray(v)) return v.length > 0
  if (typeof v === 'string') return v.length > 0
  return true
}

watch(
    () => modelValue.value,
    (v, oldV) => {
      if (!props.reloadOnClear || !props.remote || typeof props.fetchData !== 'function') return
    if (!hasMeaningfulValue(oldV) || hasMeaningfulValue(v)) return
    if (typeof debouncedRemoteFetch.cancel === 'function') debouncedRemoteFetch.cancel()
    const params = {...props.fetchParams}
      executeFetch(params)
      lastRemoteQuery.value = ''
    }
)

/**
 * 重新拉取选项：`remote=true` 时若存在最后一次关键字则带 `query` 重拉，否则拉全量（无 `query`）。
 */
function reload() {
  if (typeof props.fetchData !== 'function') return
  if (typeof debouncedRemoteFetch.cancel === 'function') debouncedRemoteFetch.cancel()
  if (props.remote) {
    if (lastRemoteQuery.value !== undefined && lastRemoteQuery.value !== null && String(lastRemoteQuery.value).length > 0) {
      executeFetch({...props.fetchParams, query: lastRemoteQuery.value})
    } else {
      const params = {...props.fetchParams}
      executeFetch(params)
    }
  } else {
    executeFetch({...props.fetchParams})
  }
}

defineExpose({
  /** 是否存在进行中的 `fetchData` */
  loading: loadingInternal,
  /** 重新拉取选项（会取消挂起的防抖远程请求） */
  reload,
  /** 原生 `ElSelect` 实例（若需） */
  selectRef
})

onUnmounted(() => {
  if (typeof debouncedRemoteFetch.cancel === 'function') debouncedRemoteFetch.cancel()
})
</script>
