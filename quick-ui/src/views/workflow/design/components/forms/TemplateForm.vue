<template>
  <el-form label-position="top" size="small">
    <el-form-item label="模板" :error="errors.template">
      <TemplateField
        v-model="data.template"
        :variable-tree="variableTree"
        :rows="5"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'TemplateForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ template: '' })

watch(
  () => props.modelValue?.template,
  (val) => {
    data.template = val ?? ''
  },
  { immediate: true }
)

function emitUpdate() {
  emit('update:modelValue', { ...props.modelValue, template: data.template })
}
</script>
