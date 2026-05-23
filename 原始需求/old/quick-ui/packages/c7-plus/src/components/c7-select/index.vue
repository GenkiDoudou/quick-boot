<template>
  <el-select
      v-bind="$attrs"
      v-model="selectedValue"
      :multiple="multiple"
      :loading="loading"
      filterable
      :remote="remote"
      :remote-method="handleRemoteSearch"
      @focus="onFocusLoad"
      @clear="clearOptions"
      @visible-change="visibleChange"
      @change="onChange"
  >
    <el-option
        v-for="(item, index) in options"
        :key="`${item[valueKey]}-${index}`"
        :label="item[labelKey]"
        :value="item[valueKey]"
        :disabled="item.disabled"
    />
    
    <!-- 默认 label 插槽 -->
    <template #label="{ label, value }">
      <slot name="label" :label="label" :value="value"/>
    </template>
  </el-select>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineOptions } from 'vue'
import { logger } from '../../utils/logger'
import { handleError } from '../../utils/errorHandler'

defineOptions({ name: 'C7Select' })

/**
 * 选项接口
 */
interface Option {
  label: string              // 显示文本
  value: string | number     // 选项值
  disabled?: boolean         // 是否禁用
}

/**
 * 组件属性接口
 */
interface Props {
  modelValue?: string | number | any[]  // 绑定值
  fetchData?: Function                  // 异步获取数据的函数
  dataFormatter?: Function              // 数据格式化函数
  labelKey?: string                     // 标签字段名
  valueKey?: string                     // 值字段名
  resultKey?: string                    // 异步返回结果中的数据路径
  options?: Option[]                    // 静态选项列表
  dataList?: Option[]                   // 静态数据列表（别名）
  fetchParams?: Record<string, any>     // 异步请求参数
  autoLoad?: boolean                    // 是否自动加载
  separator?: boolean                   // 多选时是否使用逗号分隔
  multiple?: boolean                    // 是否多选
  remote?: boolean                      // 是否远程搜索
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  fetchData: undefined,
  dataFormatter: undefined,
  labelKey: 'label',
  valueKey: 'value',
  resultKey: 'data',
  options: undefined,
  dataList: () => [],
  fetchParams: () => ({}),
  autoLoad: true,
  separator: true,
  multiple: false,
  remote: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | any[]]
  'change': [value: any]
  'visibleChange': [open: boolean]
}>()

// 本地状态
const options = ref<Option[]>([])
const loading = ref(false)

// 组件挂载后自动加载数据
onMounted(() => {
  if (props.autoLoad && !props.remote) {
    fetchAndUpdate('')
  }
})

/**
 * 工具函数：标准化值
 * 将字符串转换为数组（多选模式）
 */
const normalizeValue = (value: any, multiple: boolean, separator: boolean): any => {
  if (!multiple) return value
  if (typeof value === 'string') {
    return value === '' ? [] : value.split(',')
  }
  if (Array.isArray(value)) return value
  return value
}

/**
 * 工具函数：反标准化值
 * 将数组转换为字符串（多选模式）
 */
const denormalizeValue = (value: any, multiple: boolean, separator: boolean): any => {
  if (!multiple || !separator || !Array.isArray(value)) {
    return value
  }
  return value.join(',')
}

/**
 * 计算属性：双向绑定处理
 */
const selectedValue = computed({
  get: () => normalizeValue(props.modelValue, props.multiple, props.separator),
  set: (value: any) => {
    const payload = denormalizeValue(value, props.multiple, props.separator)
    emit('update:modelValue', payload)
  }
})

/**
 * 获取数据
 * @param query 搜索关键词
 */
const fetchAndUpdate = async (query = '') => {
  if (!props.fetchData) {
    // 使用静态数据
    const data = props.options || props.dataList
    options.value = Array.isArray(data) ? data : []
    return
  }

  loading.value = true
  try {
    const params = { ...props.fetchParams, query }
    const response = await props.fetchData(params)
    
    let data = response
    if (props.resultKey && response[props.resultKey]) {
      data = response[props.resultKey]
    }
    
    if (props.dataFormatter && typeof props.dataFormatter === 'function') {
      data = props.dataFormatter(data)
    }
    
    options.value = Array.isArray(data) ? data : []
  } catch (error) {
    handleError(error, {
      showToast: false,
      defaultMessage: '获取数据失败',
      logError: true
    })
    logger.error('获取数据失败:', error)
    options.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 远程搜索
 * @param query 搜索关键词
 */
function handleRemoteSearch(query: string) {
  fetchAndUpdate(query)
}

/**
 * 聚焦时触发远程首次加载
 */
function onFocusLoad() {
  if (props.remote && options.value.length === 0) {
    fetchAndUpdate('')
  }
}

/**
 * 清空选项列表
 */
function clearOptions() {
  options.value = []
}

/**
 * 下拉面板显示/隐藏事件
 * @param open 是否打开
 */
function visibleChange(open: boolean) {
  emit('visibleChange', open)
}

/**
 * 选中值改变时的回调
 * @param val 选中的值
 */
function onChange(val: any) {
  emit('change', val)
}
</script>

<style scoped>
/* 可在此处添加组件样式 */
</style>

