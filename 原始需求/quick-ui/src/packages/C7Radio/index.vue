<template>
  <el-radio-group
    v-model="innerValue"
    :disabled="disabled"
    :size="size"
    class="c7-radio"
    @change="handleChange"
  >
    <!-- 按钮样式 -->
    <template v-if="radioStyle === 'button'">
      <el-radio-button
        v-for="opt in options"
        :key="opt[valueKey]"
        :value="opt[valueKey]"
        :disabled="opt.disabled"
      >
        {{ opt[labelKey] }}
      </el-radio-button>
    </template>

    <!-- 默认 / 边框样式 -->
    <template v-else>
      <el-radio
        v-for="opt in options"
        :key="opt[valueKey]"
        :value="opt[valueKey]"
        :disabled="opt.disabled"
        :border="radioStyle === 'border'"
      >
        {{ opt[labelKey] }}
      </el-radio>
    </template>
  </el-radio-group>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

defineOptions({ name: 'C7Radio' })

const props = defineProps({
  // ── 值绑定 ──
  modelValue: {
    type: [String, Number],
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
  // ── 样式与状态 ──
  /**
   * 单选框样式：'default' | 'button' | 'border'
   * 默认: 'default'
   */
  radioStyle: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'button', 'border'].includes(v)
  },
  /** 整体禁用，默认: false */
  disabled: {
    type: Boolean,
    default: false
  },
  /** 尺寸：'large' | 'default' | 'small' */
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['large', 'default', 'small'].includes(v)
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change'
])

// ── 内部值 ──
const innerValue = ref(props.modelValue)

// ── 监听外部 modelValue 变化 ──
watch(
  () => props.modelValue,
  (val) => {
    if (val !== innerValue.value) {
      innerValue.value = val
    }
  }
)

// ── 变化处理 ──
function handleChange(val) {
  emit('update:modelValue', val)
  emit('change', val)
}

// ── 加载状态 ──
const loading = ref(false)
// ── 异步加载的选项数据 ──
const fetchedOptions = ref([])

// ── 展示用选项：优先 dataList，其次 fetchedOptions ──
const options = computed(() => {
  if (props.dataList) return props.dataList
  return fetchedOptions.value
})

// ── 异步加载选项 ──
async function loadOptions() {
  if (!props.fetchData || props.dataList) return
  loading.value = true
  try {
    const rawData = await props.fetchData(props.fetchParams)
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
.c7-radio {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
</style>
