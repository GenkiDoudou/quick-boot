<template>
  <el-cascader
    v-bind="mergedAttrs"
    :model-value="innerValue"
    :options="displayOptions"
    :props="cascaderProps"
    :clearable="clearable"
    :collapse-tags="collapseTags"
    :collapse-tags-tooltip="collapseTagsTooltip"
    :loading="loading"
    @update:model-value="handleChange"
    @visible-change="onVisibleChange"
  />
</template>

<script setup>
import { ref, computed, watch, onMounted, useAttrs } from 'vue'

defineOptions({ name: 'C7Cascader', inheritAttrs: false })

const props = defineProps({
  // ── 值绑定 ──
  modelValue: {
    type: [String, Number, Array],
    default: undefined
  },
  // ── 级联配置 ──
  multiple: {
    type: Boolean,
    default: false
  },
  /** 是否可选任意级，默认 true（保持原有行为）*/
  checkStrictly: {
    type: Boolean,
    default: true
  },
  /** 是否只返回叶子节点值而不返回路径，默认 false（只返回值）*/
  emitPath: {
    type: Boolean,
    default: false
  },
  // ── 数据源 ──
  /** 异步获取完整树数据（与 lazy 互斥，优先级低于 lazy）*/
  fetchData: {
    type: Function,
    default: null
  },
  /** 对 fetchData 返回数据做格式化处理 */
  dataFormatter: {
    type: Function,
    default: null
  },
  /** 节点 label 字段名 */
  labelKey: {
    type: String,
    default: 'label'
  },
  /** 节点 value 字段名 */
  valueKey: {
    type: String,
    default: 'value'
  },
  /** 节点 children 字段名 */
  resultKey: {
    type: String,
    default: 'children'
  },
  /** 静态树形数据 */
  dataList: {
    type: Array,
    default: null
  },
  /** 调用 fetchData 时额外附带的参数 */
  fetchParams: {
    type: Object,
    default: () => ({})
  },
  // ── 输出格式 ──
  /**
   * 1: 数组（多选时输出数组）
   * 2: 逗号分隔字符串
   * 不传: 单值（单选）或数组（多选默认）
   */
  resultType: {
    type: Number,
    default: undefined
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
  // ── 懒加载 ──
  /** 是否启用懒加载，启用后 fetchData(node) 按需请求子节点 */
  lazy: {
    type: Boolean,
    default: false
  },
  /** 懒加载根节点的 parentId，默认 0 */
  rootParentId: {
    type: [String, Number],
    default: 0
  },
  // ── el-cascader 常用默认值 ──
  clearable: {
    type: Boolean,
    default: true
  },
  collapseTags: {
    type: Boolean,
    default: true
  },
  collapseTagsTooltip: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change',
  'visible-change'
])

const attrs = useAttrs()

// ── 加载状态 ──
const loading = ref(false)
// ── 异步加载的树形数据 ──
const fetchedOptions = ref([])

// ── 展示用数据：优先 dataList，其次 fetchedOptions ──
const displayOptions = computed(() => {
  if (props.dataList) return props.dataList
  return fetchedOptions.value
})

// ── cascader props 配置 ──
const cascaderProps = computed(() => {
  const base = {
    checkStrictly: props.checkStrictly,
    emitPath: props.emitPath,
    multiple: props.multiple,
    label: props.labelKey,
    value: props.valueKey,
    children: props.resultKey
  }
  // 懒加载模式：注入 lazy + lazyLoad，不使用静态 options
  if (props.lazy && props.fetchData) {
    base.lazy = true
    base.lazyLoad = async (node, resolve) => {
      const { level, value } = node
      // 根节点 level === 0
      const parentId = level === 0 ? props.rootParentId : value
      try {
        const rawList = await props.fetchData(parentId)
        const list = Array.isArray(rawList) ? rawList : []
        // 格式化
        const nodes = (props.dataFormatter ? props.dataFormatter(list) : list).map(item => ({
          ...item,
          leaf: item.leaf ?? (!item[props.resultKey] || item[props.resultKey].length === 0)
        }))
        resolve(nodes)
      } catch {
        // 加载失败时将节点标记为叶子，避免无限 loading
        resolve([{ [props.labelKey]: '加载失败', [props.valueKey]: '__error__', leaf: true, disabled: true }])
      }
    }
  }
  return base
})

// ── attrs 透传（排除已明确声明的 props）──
const mergedAttrs = computed(() => attrs)

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

// ── 内部展示值：将外部 modelValue 解码为 el-cascader 需要的格式 ──
const innerValue = computed(() => {
  const mv = props.modelValue
  if (mv === undefined || mv === null || mv === '') return props.multiple ? [] : undefined
  // 逗号字符串 → 数组
  if (typeof mv === 'string' && mv.includes(',')) {
    return castArray(mv.split(',').filter(Boolean))
  }
  // 数组
  if (Array.isArray(mv)) return castArray(mv)
  // 单值
  return castValue(mv)
})

// ── 值变更处理：将 el-cascader 输出值编码为外部期望格式 ──
function handleChange(val) {
  let output = val

  if (props.multiple) {
    // 多选：val 是数组
    const arr = Array.isArray(val) ? val.map(castValue) : []
    if (props.resultType === 2) {
      // 逗号字符串
      output = arr.join(',')
    } else {
      // 数组
      output = arr
    }
  } else {
    // 单选
    output = val !== undefined && val !== null ? castValue(val) : val
  }

  emit('update:modelValue', output)
  emit('change', output)
}

function onVisibleChange(visible) {
  emit('visible-change', visible)
}

// ── 异步加载完整树（非懒加载模式）──
async function loadOptions() {
  if (props.lazy || !props.fetchData || props.dataList) return
  loading.value = true
  try {
    const rawData = await props.fetchData(props.fetchParams)
    const list = Array.isArray(rawData) ? rawData : []
    fetchedOptions.value = props.dataFormatter ? props.dataFormatter(list) : list
  } catch {
    fetchedOptions.value = []
  } finally {
    loading.value = false
  }
}

// ── fetchParams 变化时重新加载（非懒加载）──
watch(
  () => props.fetchParams,
  () => loadOptions(),
  { deep: true }
)

onMounted(() => {
  loadOptions()
})
</script>
