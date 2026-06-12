<template>
  <el-form label-position="top" size="small">
    <el-form-item label="操作">
      <el-select v-model="data.operation" @change="emitUpdate">
        <el-option label="first" value="first" />
        <el-option label="last" value="last" />
        <el-option label="filter" value="filter" />
        <el-option label="map" value="map" />
        <el-option label="count" value="count" />
      </el-select>
    </el-form-item>
    <el-form-item label="列表引用" :error="errors.listRef">
      <TemplateField
        v-model="data.listRef"
        :variable-tree="variableTree"
        :rows="2"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <el-form-item label="字段名">
      <el-input v-model="data.field" placeholder="content" @change="emitUpdate" />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'ListOperatorForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ operation: 'first', listRef: '', field: 'content' })

watch(
  () => props.modelValue,
  (val) => {
    data.operation = val?.operation ?? 'first'
    data.listRef = val?.listRef ?? ''
    data.field = val?.field ?? 'content'
  },
  { immediate: true, deep: true }
)

function emitUpdate() {
  emit('update:modelValue', {
    ...props.modelValue,
    operation: data.operation,
    listRef: data.listRef,
    field: data.field
  })
}
</script>
