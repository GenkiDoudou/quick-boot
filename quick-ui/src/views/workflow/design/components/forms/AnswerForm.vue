<template>
  <el-form label-position="top" size="small">
    <el-form-item label="输出模板" :error="errors.output">
      <TemplateField
        v-model="data.output"
        :variable-tree="variableTree"
        :rows="4"
        placeholder="{{llm_1.text}}"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <el-form-item label="引用模板">
      <TemplateField
        v-model="data.citations"
        :variable-tree="variableTree"
        :rows="2"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'AnswerForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ output: '', citations: '' })

watch(
  () => props.modelValue,
  (val) => {
    data.output = val?.output ?? ''
    data.citations = val?.citations ?? ''
  },
  { immediate: true, deep: true }
)

function emitUpdate() {
  emit('update:modelValue', { ...props.modelValue, output: data.output, citations: data.citations })
}
</script>
