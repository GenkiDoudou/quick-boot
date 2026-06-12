<template>
  <div class="wf-form">
    <div v-for="(row, idx) in assignments" :key="idx" class="wf-form__row">
      <el-form-item label="目标变量" :error="errors[`assignments.${idx}.target`]">
        <el-input v-model="row.target" placeholder="var1" @change="sync" />
      </el-form-item>
      <el-form-item label="值">
        <TemplateField
          v-model="row.value"
          :variable-tree="variableTree"
          :rows="2"
          @update:model-value="sync"
        />
      </el-form-item>
      <el-button link type="danger" @click="removeRow(idx)">删除</el-button>
    </div>
    <el-button size="small" @click="addRow">+ 添加赋值</el-button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'VariableAssignForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const assignments = ref([])

watch(
  () => props.modelValue?.assignments,
  (val) => {
    assignments.value = JSON.parse(JSON.stringify(val || []))
    if (!assignments.value.length) {
      assignments.value = [{ target: '', value: '' }]
    }
  },
  { immediate: true, deep: true }
)

function sync() {
  emit('update:modelValue', { ...props.modelValue, assignments: [...assignments.value] })
}

function addRow() {
  assignments.value.push({ target: '', value: '' })
  sync()
}

function removeRow(idx) {
  assignments.value.splice(idx, 1)
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
