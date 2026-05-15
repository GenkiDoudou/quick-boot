<template>
  <div class="c7-checkbox">
    <!-- 全选控制行 -->
    <el-checkbox
      v-if="indeterminate"
      v-model="checkAll"
      :indeterminate="isIndeterminate"
      :disabled="disabled"
      class="c7-checkbox__all"
      @change="handleCheckAllChange"
    >
      全选
    </el-checkbox>

    <!-- 复选框组 -->
    <el-checkbox-group
      v-model="innerChecked"
      :disabled="disabled"
      :min="min"
      :max="max"
      @change="handleGroupChange"
    >
      <!-- 按钮样式 -->
      <template v-if="effectiveStyle === 'button'">
        <el-checkbox-button
          v-for="opt in options"
          :key="opt[valueKey]"
          :value="opt[valueKey]"
          :disabled="opt.disabled"
        >
          {{ opt[labelKey] }}
        </el-checkbox-button>
      </template>

      <!-- 默认 / 边框样式 -->
      <template v-else>
        <el-checkbox
          v-for="opt in options"
          :key="opt[valueKey]"
          :value="opt[valueKey]"
          :disabled="opt.disabled"
          :border="effectiveStyle === 'border'"
        >
          {{ opt[labelKey] }}
        </el-checkbox>
      </template>
    </el-checkbox-group>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

defineOptions({ name: 'C7Checkbox' })

const props = defineProps({
  // ── 值绑定 ──
  modelValue: {
    type: [Array, String],
    default: undefined
  },
  // ── 数据源 ──
  /** 静态选项列表 */
  dataList: {
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
  /**
   * true:  对外输出逗号字符串（默认）
   * false: 对外输出数组
   */
  joinValue: {
    type: Boolean,
    default: true
  },
  /** 已废弃别名，等同于 joinValue */
  separator: {
    type: Boolean,
    default: undefined
  },
  /** 是否显示全选控件，默认: false */
  indeterminate: {
    type: Boolean,
    default: false
  },
  /** 整体禁用，默认: false */
  disabled: {
    type: Boolean,
    default: false
  },
  /** 最少选中数量 */
  min: {
    type: Number,
    default: undefined
  },
  /** 最多选中数量 */
  max: {
    type: Number,
    default: undefined
  },
  // ── 样式 ──
  /**
   * 复选框样式：'default' | 'button' | 'border'
   * 默认: 'default'
   */
  checkboxStyle: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'button', 'border'].includes(v)
  },
  /** 已废弃别名，等同于 checkboxStyle='button' */
  button: {
    type: Boolean,
    default: undefined
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change'
])

// ── 计算实际生效的样式类型 ──
// button prop 向下兼容：button=true 等同于 checkboxStyle='button'
const effectiveStyle = computed(() => {
  if (props.button === true) return 'button'
  return props.checkboxStyle
})

// ── 计算实际生效的 joinValue ──
// separator prop 向下兼容
const effectiveJoinValue = computed(() => {
  if (props.separator !== undefined) return props.separator
  return props.joinValue
})

// ── 加载状态 ──
const loading = ref(false)
// ── 异步加载的选项数据 ──
const fetchedOptions = ref([])

// ── 展示用选项：优先 dataList，其次 fetchedOptions ──
const options = computed(() => {
  if (props.dataList) return props.dataList
  return fetchedOptions.value
})

// ── 将外部 modelValue 解码为内部数组 ──
function decodeValue(mv) {
  if (mv === undefined || mv === null || mv === '') return []
  // 逗号字符串 → 数组
  if (typeof mv === 'string') {
    return mv.split(',').filter(Boolean)
  }
  // 数组
  if (Array.isArray(mv)) return [...mv]
  return []
}

// ── 内部选中值（数组） ──
const innerChecked = ref(decodeValue(props.modelValue))

// ── 监听外部 modelValue 变化，同步到内部 ──
watch(
  () => props.modelValue,
  (val) => {
    const decoded = decodeValue(val)
    // 避免不必要的引用更新触发循环
    if (JSON.stringify(decoded) !== JSON.stringify(innerChecked.value)) {
      innerChecked.value = decoded
    }
  }
)

// ── 全选状态 ──
const checkAll = ref(false)
const isIndeterminate = ref(false)

// ── 监听内部选中值变化，更新全选状态 ──
watch(
  innerChecked,
  (val) => {
    if (!props.indeterminate) return
    const total = options.value.length
    const checked = val.length
    checkAll.value = checked > 0 && checked === total
    isIndeterminate.value = checked > 0 && checked < total
  },
  { immediate: true }
)

// ── 全选按钮变化处理 ──
function handleCheckAllChange(val) {
  if (val) {
    // 全选：选中所有未禁用的选项
    innerChecked.value = options.value
      .filter((opt) => !opt.disabled)
      .map((opt) => opt[props.valueKey])
  } else {
    innerChecked.value = []
  }
  emitValue(innerChecked.value)
}

// ── 复选框组变化处理 ──
function handleGroupChange(val) {
  emitValue(val)
}

// ── 将内部数组编码并向外发送 ──
function emitValue(arr) {
  const output = effectiveJoinValue.value ? arr.join(',') : [...arr]
  emit('update:modelValue', output)
  emit('change', [...arr])
}

// ── 异步加载选项（非 dataList 模式）──
async function loadOptions() {
  if (!props.fetchData || props.dataList) return
  loading.value = true
  try {
    const rawData = await props.fetchData(props.fetchParams)
    // 取指定 key 或直接使用返回值
    const list = props.resultKey
      ? (rawData?.[props.resultKey] ?? [])
      : (Array.isArray(rawData) ? rawData : [])
    fetchedOptions.value = props.dataFormatter ? props.dataFormatter(list) : list
  } catch {
    fetchedOptions.value = []
  } finally {
    loading.value = false
  }
}

// ── fetchParams 变化时重新加载 ──
watch(
  () => props.fetchParams,
  () => loadOptions(),
  { deep: true }
)

onMounted(() => {
  loadOptions()
})
</script>

<style scoped>
.c7-checkbox {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.c7-checkbox__all {
  margin-right: 8px;
}
</style>
