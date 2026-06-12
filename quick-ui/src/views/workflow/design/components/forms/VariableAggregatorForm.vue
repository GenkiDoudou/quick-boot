<template>
  <div class="wf-form">
    <div v-for="(row, idx) in variables" :key="idx" class="wf-form__row">
      <el-form-item :label="`变量 ${idx + 1}`" :error="errors[`variables.${idx}`]">
        <TemplateField
          v-model="variables[idx]"
          :variable-tree="variableTree"
          :rows="2"
          @update:model-value="sync"
        />
      </el-form-item>
      <el-button link type="danger" @click="removeRow(idx)">删除</el-button>
    </div>
    <el-button size="small" @click="addRow">+ 添加变量</el-button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'VariableAggregatorForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const variables = ref([])

watch(
  () => props.modelValue?.variables,
  (val) => {
    variables.value = JSON.parse(JSON.stringify(val || []))
    if (!variables.value.length) variables.value = ['']
  },
  { immediate: true, deep: true }
)

function sync() {
  emit('update:modelValue', { ...props.modelValue, variables: [...variables.value] })
}

function addRow() {
  variables.value.push('')
  sync()
}

function removeRow(idx) {
  variables.value.splice(idx, 1)
  sync()
}
</script>

<style scoped>
.wf-form__row {
  padding: 8px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}
</style>
