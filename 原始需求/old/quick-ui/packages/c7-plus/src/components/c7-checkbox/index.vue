<template>
  <div>
    <!-- 全选复选框：仅在 props.indeterminate 为 true 时显示 -->
    <el-checkbox
        v-if="indeterminate"
        v-model="checkAll"
        :indeterminate="isIndeterminate"
        @change="handleCheckAllChange"
    >
      全选
    </el-checkbox>

    <!-- 多选组：绑定 checkList（数组/字符串） -->
    <el-checkbox-group v-model="checkList" @change="handleCheckedChange">
      <!-- 普通复选框样式 -->
      <el-checkbox
          v-if="!button"
          v-for="item in options"
          :key="item.value"
          :label="item.value"
      >
        {{ item.label }}
      </el-checkbox>

      <!-- 按钮样式复选框 -->
      <el-checkbox-button
          v-if="button"
          v-for="item in options"
          :key="item.value"
          :label="item.value"
      >
        {{ item.label }}
      </el-checkbox-button>
    </el-checkbox-group>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed, defineOptions } from 'vue'
import { useFetchOptions } from '../../hooks/useFetchOptions'

defineOptions({ name: 'C7Checkbox' })

/**
 * 选项接口
 */
interface Option {
  label: string              // 显示文本
  value: string | number     // 选项值
}

/**
 * 组件属性接口
 */
interface Props {
  modelValue?: string[] | string           // 绑定值，支持数组或逗号分隔字符串
  fetchData?: Function                     // 异步获取数据的函数
  dataList?: Option[]                      // 静态数据列表
  fetchParams?: Record<string, any>        // 异步请求参数
  resultKey?: string                       // 异步返回结果中的数据路径
  dataFormatter?: Function                 // 数据格式化函数
  indeterminate?: boolean                  // 是否显示全选控件
  separator?: boolean                      // 是否将数组转换为逗号分隔字符串
  labelKey?: string                        // 数据项标签字段名
  valueKey?: string                        // 数据项值字段名
  button?: boolean                         // 是否使用按钮样式
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  fetchData: undefined,
  dataList: () => [],
  fetchParams: () => ({}),
  resultKey: 'data',
  dataFormatter: undefined,
  indeterminate: false,
  separator: true,
  labelKey: 'label',
  valueKey: 'value',
  button: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string[] | string]
  'change': [value: any[]]
}>()

// 使用自定义 hook 加载数据
const { options, loading, fetchAndUpdate } = useFetchOptions({
  fetchData: props.fetchData,
  fetchParams: props.fetchParams,
  resultKey: props.resultKey,
  dataFormatter: props.dataFormatter,
  dataList: props.dataList
})

// 组件挂载后自动加载数据
onMounted(() => {
  if (fetchAndUpdate) {
    fetchAndUpdate('')
  }
})

/**
 * 双向绑定的计算属性：处理数组与字符串之间的转换
 * - separator=true: 字符串绑定模式，内部使用数组，对外暴露字符串
 * - separator=false: 数组绑定模式，直接使用数组
 */
const checkList = computed({
  get: () => {
    const val = props.modelValue
    if (props.separator) {
      // 字符串绑定模式：将字符串转换为数组
      if (typeof val === 'string') return val === '' ? [] : val.split(',')
      if (Array.isArray(val)) return val
      return []
    } else {
      // 数组绑定模式：强制返回数组
      return Array.isArray(val) ? val : []
    }
  },
  set: (value: any[]) => {
    let payload: string[] | string = value
    if (props.separator && Array.isArray(value)) {
      // 字符串绑定模式：将数组转换为逗号分隔字符串
      payload = value.join(',')
    }
    emit('update:modelValue', payload)
  }
})

// 全选状态控制
const checkAll = ref(false)           // 是否全选
const isIndeterminate = ref(true)     // 是否半选状态

/**
 * 全选/全不选逻辑
 * @param val 全选状态
 */
const handleCheckAllChange = (val: boolean) => {
  const allValues = options.value.map((item: any) => item.value)
  checkList.value = val ? allValues : []
  isIndeterminate.value = false
}

/**
 * 单个选项变化时的逻辑
 * 更新全选状态和半选状态
 * @param value 当前选中的值数组
 */
const handleCheckedChange = (value: any[]) => {
  const checkedCount = value.length
  const totalCount = options.value.length
  
  // 更新全选状态
  checkAll.value = checkedCount === totalCount
  
  // 更新半选状态：选中数量大于0且小于总数
  isIndeterminate.value = checkedCount > 0 && checkedCount < totalCount
  
  // 触发 change 事件
  emit('change', value)
}

/**
 * 监听 options 数据变化，初始化全选状态
 * 在数据加载完成后更新 checkAll 和 isIndeterminate
 */
watch(() => options.value, (newOptions) => {
  if (newOptions.length > 0) {
    const checkedCount = checkList.value.length
    const totalCount = newOptions.length
    
    checkAll.value = checkedCount === totalCount
    isIndeterminate.value = checkedCount > 0 && checkedCount < totalCount
  }
}, { immediate: true })
</script>

