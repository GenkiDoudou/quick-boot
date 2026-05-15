<template>
  <el-tree-select
    v-bind="$attrs"
    :model-value="innerValue"
    :data="displayData"
    :multiple="multiple"
    :show-checkbox="showCheckbox"
    :check-strictly="checkStrictly"
    :props="treeProps"
    :filterable="filterable"
    :filter-node-method="filterNodeMethod"
    :loading="loading"
    clearable
    collapse-tags
    collapse-tags-tooltip
    style="width: 100%"
    @update:model-value="handleChange"
    @change="handleElChange"
  />
</template>

<script setup>
import { ref, computed, onMounted, watch, useAttrs } from 'vue'

defineOptions({ name: 'C7TreeSelect', inheritAttrs: false })

const props = defineProps({
  /** v-model 绑定值，多选时支持数组或逗号字符串 */
  modelValue: {
    type: [String, Number, Array],
    default: undefined
  },
  // ── 数据来源 ──
  /** 异步获取树数据函数 */
  fetchData: {
    type: Function,
    default: null
  },
  /** 静态树数据，与 fetchData 二选一 */
  dataList: {
    type: Array,
    default: null
  },
  /** fetchData 返回数据的取值 key（为空则取整个返回值） */
  resultKey: {
    type: String,
    default: ''
  },
  // ── 节点字段映射 ──
  labelKey: {
    type: String,
    default: 'label'
  },
  valueKey: {
    type: String,
    default: 'value'
  },
  childrenKey: {
    type: String,
    default: 'children'
  },
  // ── 行为 ──
  /** 是否多选，默认 false */
  multiple: {
    type: Boolean,
    default: false
  },
  /** 父子不关联，默认 false */
  checkStrictly: {
    type: Boolean,
    default: false
  },
  /**
   * 值类型转换
   * 'auto':   不强制转换，保持原始类型（默认）
   * 'number': 强制转为 Number
   * 'string': 强制转为 String
   */
  valueType: {
    type: String,
    default: 'auto',
    validator: (v) => ['auto', 'string', 'number'].includes(v)
  },
  /**
   * 多选时是否将数组序列化为逗号字符串输出，默认 false
   * true  → emit 逗号字符串
   * false → emit 数组
   */
  rangeMerge: {
    type: Boolean,
    default: false
  },
  /** 分隔符，默认 ',' */
  rangeSeparator: {
    type: String,
    default: ','
  },
  /** 是否可搜索，默认 false */
  filterable: {
    type: Boolean,
    default: false
  },
  /** 自定义节点过滤方法 */
  filterNodeMethod: {
    type: Function,
    default: undefined
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'load-error'
])

// ── 加载状态 ──
const loading = ref(false)
// ── 异步加载的树数据 ──
const fetchedData = ref([])

// ── 展示用数据：优先 dataList，其次 fetchedData ──
const displayData = computed(() => {
  if (props.dataList) return props.dataList
  return fetchedData.value
})

// ── 单选时不显示复选框 ──
const showCheckbox = computed(() => props.multiple)

// ── el-tree-select 节点字段配置 ──
const treeProps = computed(() => ({
  label: props.labelKey,
  value: props.valueKey,
  children: props.childrenKey
}))

// ── 值类型转换 ──
function castValue(val) {
  if (props.valueType === 'number') return Number(val)
  if (props.valueType === 'string') return String(val)
  return val
}

function castArray(arr) {
  if (!Array.isArray(arr)) return arr
  return arr.map(castValue)
}

// ── 内部值：将外部 modelValue 解码为 el-tree-select 需要的格式 ──
const innerValue = computed(() => {
  const mv = props.modelValue
  if (mv === undefined || mv === null || mv === '') {
    return props.multiple ? [] : undefined
  }
  if (props.multiple) {
    // 逗号字符串 → 数组
    if (typeof mv === 'string' && mv.includes(props.rangeSeparator)) {
      return castArray(mv.split(props.rangeSeparator).filter(Boolean))
    }
    return Array.isArray(mv) ? castArray(mv) : castArray([mv])
  }
  return castValue(mv)
})

// ── el-tree-select update:modelValue → 统一处理输出 ──
function handleChange(val) {
  let output = val
  if (props.multiple) {
    const arr = Array.isArray(val) ? val.map(castValue) : []
    output = props.rangeMerge ? arr.join(props.rangeSeparator) : arr
  } else {
    output = val !== undefined && val !== null ? castValue(val) : val
  }
  emit('update:modelValue', output)
}

function handleElChange(val) {
  let output = val
  if (props.multiple) {
    const arr = Array.isArray(val) ? val.map(castValue) : []
    output = props.rangeMerge ? arr.join(props.rangeSeparator) : arr
  } else {
    output = val !== undefined && val !== null ? castValue(val) : val
  }
  emit('change', output)
}

// ── 异步加载树数据 ──
async function fetchAndUpdate() {
  if (props.dataList || !props.fetchData) return
  loading.value = true
  try {
    const rawData = await props.fetchData()
    const list = props.resultKey
      ? (rawData?.[props.resultKey] ?? [])
      : (Array.isArray(rawData) ? rawData : [])
    fetchedData.value = list
  } catch (error) {
    fetchedData.value = []
    emit('load-error', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAndUpdate()
})

// ── 暴露方法 ──
defineExpose({
  reload: fetchAndUpdate
})
</script>
