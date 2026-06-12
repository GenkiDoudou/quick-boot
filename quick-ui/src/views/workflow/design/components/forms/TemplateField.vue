<template>
  <div class="wf-template-field">
    <div class="wf-template-field__toolbar">
      <VariablePicker :variable-tree="variableTree" @insert="insertAtCursor" />
    </div>
    <el-input
      ref="inputRef"
      :model-value="modelValue"
      type="textarea"
      :rows="rows"
      :placeholder="placeholder"
      @update:model-value="$emit('update:modelValue', $event)"
      @focus="onFocus"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import VariablePicker from '../VariablePicker.vue'

defineOptions({ name: 'TemplateField' })

defineProps({
  modelValue: { type: String, default: '' },
  variableTree: { type: Array, default: () => [] },
  rows: { type: Number, default: 3 },
  placeholder: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const inputRef = ref(null)
const cursorPos = ref(0)

function onFocus(e) {
  cursorPos.value = e.target.selectionStart ?? 0
}

function insertAtCursor(text) {
  const el = inputRef.value?.textarea || inputRef.value?.input
  const current = el?.value ?? ''
  const pos = el?.selectionStart ?? cursorPos.value
  const next = current.slice(0, pos) + text + current.slice(pos)
  emit('update:modelValue', next)
  cursorPos.value = pos + text.length
}
</script>

<style scoped>
.wf-template-field__toolbar {
  margin-bottom: 4px;
}
</style>
