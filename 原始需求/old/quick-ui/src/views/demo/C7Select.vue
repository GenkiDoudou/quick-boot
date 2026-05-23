<template>
  <el-select
      v-bind="$attrs"
      v-model="selectedValue"
      :loading="loading"
      filterable
      @focus="handleLoad"
      @clear="clearOptions"
      @visible-change="visibleChange"
      @change="onChange"
  >
    <el-option
        v-for="item in options"
        :key="item[valueKey]"
        :label="item[labelKey]"
        :value="item[valueKey]"
    />
  </el-select>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number, Array],
    default: ''
  },
  fetchData: {
    type: Function,
    required: true
  },
  dataFormatter: {
    type: Function,
    default: null
  },
  labelKey: {
    type: String,
    default: 'label'
  },
  valueKey: {
    type: String,
    default: 'value'
  },
  autoLoad: {
    type: Boolean,
    default: true
  },
  fetchParams: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const options = ref([])
const loading = ref(false)

// 计算属性处理双向绑定
const selectedValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 监听fetchParams变化
watch(
    () => props.fetchParams,
    () => {
      handleLoad()
    },
    { deep: true }
)

onMounted(() => {
  if (props.autoLoad) {
    handleLoad()
  }
})

const handleLoad = async (query = '') => {
  loading.value = true
  try {
    const params = { ...props.fetchParams, query }
    let result = await props.fetchData(params)

    // 处理返回数据
    if (props.dataFormatter && typeof props.dataFormatter === 'function') {
      result = props.dataFormatter(result)
    }

    // 确保数据是数组
    options.value = Array.isArray(result) ? result : []
  } catch (err) {
    console.error('ElSelectAsync load error:', err)
    options.value = []
  } finally {
    loading.value = false
  }
}

const clearOptions = () => {
  options.value = []
}

const visibleChange = (open) => {
  if (open && !options.value.length) {
    handleLoad()
  }
}

const onChange = (val) => {
  emit('change', val)
}
</script>
