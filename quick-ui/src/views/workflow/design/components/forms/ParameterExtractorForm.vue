<template>
  <el-form label-position="top" size="small">
    <el-form-item label="待提取文本" :error="errors.query">
      <TemplateField
        v-model="data.query"
        :variable-tree="variableTree"
        :rows="3"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <div class="wf-form__section">Schema 字段</div>
    <div v-for="(row, idx) in fields" :key="idx" class="wf-form__row">
      <el-form-item label="字段 key" :error="errors[`schema.fields.${idx}.key`]">
        <el-input v-model="row.key" @change="sync" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="row.type" @change="sync">
          <el-option label="字符串" value="string" />
          <el-option label="数字" value="number" />
          <el-option label="布尔" value="boolean" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="row.description" @change="sync" />
      </el-form-item>
      <el-form-item label="必填">
        <el-switch v-model="row.required" @change="sync" />
      </el-form-item>
      <el-button link type="danger" @click="removeRow(idx)">删除</el-button>
    </div>
    <el-button size="small" @click="addRow">+ 添加字段</el-button>
  </el-form>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'ParameterExtractorForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ query: '' })
const fields = ref([])

watch(
  () => props.modelValue,
  (val) => {
    data.query = val?.query ?? ''
    const schemaFields = val?.schema?.fields
    fields.value = JSON.parse(JSON.stringify(Array.isArray(schemaFields) ? schemaFields : []))
    if (!fields.value.length) {
      fields.value = [{ key: 'name', type: 'string', description: '', required: true }]
    }
  },
  { immediate: true, deep: true }
)

function sync() {
  emit('update:modelValue', {
    ...props.modelValue,
    query: data.query,
    schema: { fields: [...fields.value] }
  })
}

function emitUpdate() {
  sync()
}

function addRow() {
  fields.value.push({ key: '', type: 'string', description: '', required: false })
  sync()
}

function removeRow(idx) {
  fields.value.splice(idx, 1)
  sync()
}
</script>

<style scoped>
.wf-form__section {
  font-size: 13px;
  font-weight: 600;
  color: #0a2463;
  margin: 12px 0 8px;
}
.wf-form__row {
  padding: 8px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}
</style>
