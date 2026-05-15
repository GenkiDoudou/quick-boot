<template>
  <el-select
    v-bind="attrs"
    :model-value="innerValue"
    :multiple="multiple"
    :loading="loading"
    :remote="remote"
    :remote-method="remote ? handleRemoteSearch : undefined"
    :filterable="remote || filterable"
    clearable
    @update:model-value="handleChange"
    @change="handleElChange"
    @visible-change="handleVisibleChange"
    @clear="handleClear"
    @focus="handleFocus"
  >
    <!-- 前置内容 -->
    <template v-if="$slots.prefix" #prefix>
      <slot name="prefix" />
    </template>

    <!-- 已选标签 -->
    <template v-if="$slots.label" #label="scope">
      <slot name="label" v-bind="scope" />
    </template>

    <!-- 选项列表 -->
    <el-option
      v-for="item in options"
      :key="item[valueKey]"
      :label="item[labelKey]"
      :value="item[valueKey]"
      :disabled="item.disabled"
    >
      <!-- 自定义选项内容 -->
      <slot name="option" :item="item">
        {{ item[labelKey] }}
      </slot>
    </el-option>

    <!-- 无数据提示 -->
    <template v-if="$slots.empty" #empty>
      <slot name="empty" />
    </template>
  </el-select>
</template>

<script setup>
import { ref, computed, watch, onMounted, useAttrs } from 'vue'

defineOptions({ name: 'C7Select', inheritAttrs: false })

const props = defineProps({
  // ── 值绑定 ──
  modelValue: {
    type: [String, Number, Array],
    default: undefined
  },
  // ── 数据来源 ──
  /** 静态选项列表（主名称） */
  dataList: {
    type: Array,
    default: null
  },
  /** 静态选项列表（别名，与 dataList 合并取非空者） */
  options: {
    type: Array,
    default: null
  },
  /** 异步获取选项函数 */
  fetchData: {
    type: Function,
    default: null
  },
  /** 调用 fetchData 时附带的额外参数，变化时自动重新请求 */
  fetchParams: {
    type: Object,
    default: () => ({})
  },
  /** fetchData 返回数据的取值 key（为空则取整个返回值） */
  resultKey: {
    type: String,
    default: ''
  },
  /** 对 fetchData 返回数据进行格式化 */
  dataFormatter: {
    type: Function,
    default: null
  },
  /** 选项显示文本的字段名 */
  labelKey: {
    type: String,
    default: 'label'
  },
  /** 选项值的字段名 */
  valueKey: {
    type: String,
    default: 'value'
  },
  // ── 行为 ──
  /** 是否多选，默认: false */
  multiple: {
    type: Boolean,
    default: false
  },
  /**
   * 多选时是否将数组序列化为逗号字符串输出，默认: true
   * true  → emit 逗号字符串
   * false → emit 数组
   */
  separator: {
    type: Boolean,
    default: true
  },
  /** 是否远程搜索模式，默认: false */
  remote: {
    type: Boolean,
    default: false
  },
  /** 是否可过滤（非远程模式下的本地搜索），默认: false */
  filterable: {
    type: Boolean,
    default: false
  },
  /** 是否在挂载时自动加载（非远程模式），默认: true */
  autoLoad: {
    type: Boolean,
    default: true
  },
  /**
   * 清空时是否重新加载全量列表（远程搜索模式有效）
   * 默认: true
   */
  reloadOnClear: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'visible-change',
  'loading-change'
])

const attrs = useAttrs()

// ── 加载状态 ──
const loading = ref(false)
// ── 异步加载的选项数据 ──
const fetchedOptions = ref([])
// ── 是否已完成首次加载（聚焦懒加载用）──
const hasFetched = ref(false)

// ── 展示用选项：优先 dataList / options，其次 fetchedOptions ──
const staticList = computed(() => props.dataList ?? props.options ?? null)
const displayOptions = computed(() => {
  if (staticList.value) return staticList.value
  return fetchedOptions.value
})

// ── 将外部 modelValue 解码为 el-select 内部值 ──
const innerValue = computed(() => {
  const mv = props.modelValue
  if (mv === undefined || mv === null || mv === '') {
    return props.multiple ? [] : undefined
  }
  if (props.multiple) {
    // 逗号字符串 → 数组
    if (typeof mv === 'string') {
      return mv.split(',').filter(Boolean)
    }
    return Array.isArray(mv) ? mv : [mv]
  }
  return mv
})

// ── el-select update:modelValue → 统一处理输出 ──
function handleChange(val) {
  let output = val
  if (props.multiple) {
    const arr = Array.isArray(val) ? val : []
    output = props.separator ? arr.join(',') : arr
  }
  emit('update:modelValue', output)
}

// ── change 事件透传 ──
function handleElChange(val) {
  let output = val
  if (props.multiple) {
    const arr = Array.isArray(val) ? val : []
    output = props.separator ? arr.join(',') : arr
  }
  emit('change', output)
}

// ── visible-change 透传 ──
function handleVisibleChange(open) {
  emit('visible-change', open)
}

// ── clear 处理：远程模式下重新加载全量列表 ──
function handleClear() {
  if (props.remote && props.reloadOnClear && props.fetchData) {
    fetchAndUpdate('')
  }
}

// ── 首次聚焦时加载（remote 模式下按需加载）──
function handleFocus() {
  if (props.remote && !hasFetched.value && props.fetchData) {
    fetchAndUpdate('')
  }
}

// ── 远程搜索回调 ──
function handleRemoteSearch(query) {
  if (props.fetchData) {
    fetchAndUpdate(query)
  }
}

// ── 核心：加载并更新选项 ──
async function fetchAndUpdate(query = undefined) {
  if (staticList.value || !props.fetchData) return
  loading.value = true
  emit('loading-change', true)
  try {
    const params = query !== undefined
      ? { ...props.fetchParams, query }
      : props.fetchParams
    const rawData = await props.fetchData(params)
    const list = props.resultKey
      ? (rawData?.[props.resultKey] ?? [])
      : (Array.isArray(rawData) ? rawData : [])
    fetchedOptions.value = props.dataFormatter ? props.dataFormatter(list) : list
    hasFetched.value = true
  } catch {
    fetchedOptions.value = []
  } finally {
    loading.value = false
    emit('loading-change', false)
  }
}

// ── fetchParams 变化时重新加载（非远程模式）──
watch(
  () => props.fetchParams,
  () => {
    if (!props.remote) fetchAndUpdate()
  },
  { deep: true }
)

// ── 挂载时自动加载（非远程、非静态数据模式）──
onMounted(() => {
  if (props.autoLoad && !props.remote) {
    fetchAndUpdate()
  }
})

// ── 暴露给父组件 ──
defineExpose({
  loading,
  reload: () => fetchAndUpdate('')
})
</script>

<style scoped>
.c7-select {
  width: 100%;
}
</style>
