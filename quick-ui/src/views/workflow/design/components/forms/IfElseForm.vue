<template>
  <div class="wf-form">
    <div v-for="(row, idx) in conditions" :key="idx" class="wf-form__row">
      <el-form-item label="左值" :error="errors[`conditions.${idx}.left`]">
        <TemplateField
          v-model="row.left"
          :variable-tree="variableTree"
          :rows="2"
          @update:model-value="sync"
        />
      </el-form-item>
      <el-form-item label="运算符">
        <el-select v-model="row.operator" @change="sync">
          <el-option label="不为空" value="not-empty" />
          <el-option label="为空" value="empty" />
          <el-option label="等于" value="eq" />
          <el-option label="不等于" value="neq" />
          <el-option label="包含" value="contains" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="needsRight(row.operator)" label="右值">
        <TemplateField
          v-model="row.right"
          :variable-tree="variableTree"
          :rows="1"
          @update:model-value="sync"
        />
      </el-form-item>
      <el-button link type="danger" @click="removeRow(idx)">删除</el-button>
    </div>
    <el-button size="small" @click="addRow">+ 添加条件</el-button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'IfElseForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const conditions = ref([])

watch(
  () => props.modelValue?.conditions,
  (val) => {
    conditions.value = JSON.parse(JSON.stringify(val || []))
    if (!conditions.value.length) {
      conditions.value = [{ left: '', operator: 'not-empty', right: '' }]
    }
  },
  { immediate: true, deep: true }
)

function needsRight(op) {
  return !['not-empty', 'empty'].includes(op)
}

function sync() {
  emit('update:modelValue', { ...props.modelValue, conditions: [...conditions.value] })
}

function addRow() {
  conditions.value.push({ left: '', operator: 'eq', right: '' })
  sync()
}

function removeRow(idx) {
  conditions.value.splice(idx, 1)
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
