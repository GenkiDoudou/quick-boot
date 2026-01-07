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
import { ref, onMounted, defineOptions, watch, nextTick } from 'vue'
import { useFetchOptions } from '../../../hooks/c7Hook'

defineOptions({ name: 'C7TreeSelect' })

const props = defineProps({
  modelValue: [Array, String, Number],
  multiple: Boolean,
  checkStrictly: Boolean,

  fetchData: Function,
  resultKey: { type: String, default: 'data' },

  labelKey: { type: String, default: 'label' },
  valueKey: { type: String, default: 'id' },
  childrenKey: { type: String, default: 'children' }
})

const emit = defineEmits(['update:modelValue'])

/* ================= 数据 ================= */
const { options, fetchAndUpdate } = useFetchOptions({
  fetchData: props.fetchData,
  resultKey: props.resultKey
})

/* ================= 内部唯一状态 ================= */
const selectedValue = ref<any>(props.multiple ? [] : null)

/* ================= Tree props ================= */
const treeProps = {
  label: props.labelKey,
  value: props.valueKey,
  children: props.childrenKey
}

/* ================= 关键：先数据，后回显 ================= */
onMounted(async () => {
  await fetchAndUpdate('')
  await nextTick()
  syncFromModelValue(props.modelValue)
})

/* ================= 外部 → 内部 ================= */
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

/* ================= 内部 → 外部 ================= */
watch(selectedValue, val => {
  emit('update:modelValue', val)
})
</script>
