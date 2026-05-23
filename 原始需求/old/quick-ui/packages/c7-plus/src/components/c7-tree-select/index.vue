<template>
  <el-tree-select
      v-bind="$attrs"
      v-model="selectedValue"
      :data="options"
      :props="treeProps"
      :multiple="multiple"
      :check-strictly="checkStrictly"
      show-checkbox
      clearable
      collapse-tags
      style="width: 100%"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineOptions, watch, nextTick } from 'vue'
import { useFetchOptions } from '../../hooks/useFetchOptions'

defineOptions({ name: 'C7TreeSelect' })

/**
 * 组件属性接口
 */
interface Props {
  modelValue?: any[] | string | number  // 绑定值
  multiple?: boolean                     // 是否多选
  checkStrictly?: boolean                // 是否严格模式
  fetchData?: Function                   // 异步获取数据的函数
  resultKey?: string                     // 异步返回结果中的数据路径
  labelKey?: string                      // 标签字段名
  valueKey?: string                      // 值字段名
  childrenKey?: string                   // 子节点字段名
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  multiple: false,
  checkStrictly: false,
  fetchData: undefined,
  resultKey: 'data',
  labelKey: 'label',
  valueKey: 'id',
  childrenKey: 'children'
})

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()

/**
 * 使用 hook 获取选项数据
 */
const { options, fetchAndUpdate } = useFetchOptions({
  fetchData: props.fetchData,
  resultKey: props.resultKey
})

/**
 * 内部唯一状态
 */
const selectedValue = ref<any>(props.multiple ? [] : null)

/**
 * Tree props 配置
 */
const treeProps = computed(() => ({
  label: props.labelKey,
  value: props.valueKey,
  children: props.childrenKey
}))

/**
 * 组件挂载后加载数据并回显
 */
onMounted(async () => {
  if (fetchAndUpdate) {
    await fetchAndUpdate('')
  }
  await nextTick()
  syncFromModelValue(props.modelValue)
})

/**
 * 外部 → 内部：同步 modelValue 到 selectedValue
 * @param val 外部值
 */
function syncFromModelValue(val: any) {
  if (props.multiple) {
    if (Array.isArray(val)) {
      selectedValue.value = val.map(Number)
    } else if (typeof val === 'string') {
      selectedValue.value = val
          .split(',')
          .filter(Boolean)
          .map(Number)
    } else {
      selectedValue.value = []
    }
  } else {
    selectedValue.value = val != null ? Number(val) : null
  }
}

/**
 * 内部 → 外部：监听 selectedValue 变化并触发更新
 */
watch(selectedValue, val => {
  emit('update:modelValue', val)
})

/**
 * 监听外部 modelValue 变化
 */
watch(() => props.modelValue, val => {
  syncFromModelValue(val)
})
</script>

