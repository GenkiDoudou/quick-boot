<template>
  <el-cascader
      v-bind="$attrs"
      :model-value="selectedValue"
      @update:modelValue="onChange"
      :options="options"
      :props="cascaderProps"
      clearable
      collapse-tags
      collapse-tags-tooltip
  />
</template>

<script setup lang="ts">
import { ref, watch, computed, defineOptions } from 'vue'
import { useFetchOptions } from '../../../hooks/c7Hook'
import { jsonGet } from '../../../utils/utils'

defineOptions({ name: 'C7Cascader' })

/* ======================= props ======================= */

const props = defineProps({
  modelValue: [String, Array, Number],

  multiple: Boolean,
  checkStrictly: Boolean,

  fetchData: Function,
  dataFormatter: Function,

  labelKey: { type: String, default: 'label' },
  valueKey: { type: String, default: 'value' },
  resultKey: { type: String, default: 'data' },

  dataList: { type: Array, default: () => [] },
  fetchParams: { type: Object, default: () => ({}) },

  /**
   * 1: Array
   * 2: 逗号字符串
   */
  resultType: { type: Number, default: 1 },

  lazy: Boolean,
  rootParentId: {
    type: [String, Number],
    default: -1
  }
})

const emit = defineEmits(['update:modelValue'])

/* ======================= el-cascader props（核心） ======================= */

const cascaderProps = computed(() => ({
  multiple: props.multiple,
  checkStrictly: true,     // ⭐ 强制 strict
  emitPath: false,         // ⭐ 生死线
  lazy: props.lazy,
  label: props.labelKey,
  value: props.valueKey,
  children: 'children',
  lazyLoad: props.lazy ? lazyLoad : undefined
}))

/* ======================= options ======================= */

const { options } = useFetchOptions({
  fetchData: props.fetchData,
  fetchParams: props.fetchParams,
  resultKey: props.resultKey,
  dataFormatter: props.dataFormatter,
  dataList: props.dataList,
  lazy: props.lazy
})

/* ======================= el-cascader 内部值 ======================= */
/**
 * strict + emitPath=false：
 * 单选：[1]
 * 多选：[1,2,3]
 */
const selectedValue = ref<any[]>([])

/* ======================= 外部值 → 内部 ======================= */

watch(
    () => props.modelValue,
    val => {
      if (Array.isArray(val)) {
        selectedValue.value = val.map(Number)
      } else if (typeof val === 'string') {
        selectedValue.value = val
            .split(',')
            .filter(Boolean)
            .map(Number)
      } else if (val != null) {
        selectedValue.value = [Number(val)]
      } else {
        selectedValue.value = []
      }
    },
    { immediate: true }
)

/* ======================= 内部变化 ======================= */

function onChange(val: any[]) {
  if (!props.multiple) {
    const v = val?.[0]
    emit(
        'update:modelValue',
        props.resultType === 2 ? String(v ?? '') : v
    )
    return
  }

  emit(
      'update:modelValue',
      props.resultType === 2 ? val.join(',') : val
  )
}

/* ======================= lazyLoad ======================= */

function lazyLoad(node, resolve) {
  props.fetchData(node.value ?? props.rootParentId).then(res => {
    resolve(jsonGet(res, props.resultKey, []))
  })
}
</script>
