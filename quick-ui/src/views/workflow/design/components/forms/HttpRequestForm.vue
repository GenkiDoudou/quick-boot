<template>
  <el-form label-position="top" size="small">
    <el-form-item label="方法">
      <el-select v-model="data.method" @change="emitUpdate">
        <el-option label="GET" value="GET" />
        <el-option label="POST" value="POST" />
        <el-option label="PUT" value="PUT" />
        <el-option label="DELETE" value="DELETE" />
        <el-option label="PATCH" value="PATCH" />
      </el-select>
    </el-form-item>
    <el-form-item label="URL" :error="errors.url">
      <TemplateField
        v-model="data.url"
        :variable-tree="variableTree"
        :rows="2"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
    <div class="wf-form__section">Headers</div>
    <div v-for="(row, idx) in headerRows" :key="idx" class="wf-form__row">
      <el-form-item label="Key">
        <el-input v-model="row.key" @change="syncHeaders" />
      </el-form-item>
      <el-form-item label="Value">
        <TemplateField
          v-model="row.value"
          :variable-tree="variableTree"
          :rows="1"
          @update:model-value="syncHeaders"
        />
      </el-form-item>
      <el-button link type="danger" @click="removeHeader(idx)">删除</el-button>
    </div>
    <el-button size="small" @click="addHeader">+ 添加 Header</el-button>
    <el-form-item label="Body" style="margin-top: 12px">
      <TemplateField
        v-model="data.body"
        :variable-tree="variableTree"
        :rows="4"
        @update:model-value="emitUpdate"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'HttpRequestForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ method: 'GET', url: '', body: '' })
const headerRows = ref([])

watch(
  () => props.modelValue,
  (val) => {
    data.method = val?.method ?? 'GET'
    data.url = val?.url ?? ''
    data.body = val?.body ?? ''
    const headers = val?.headers || {}
    headerRows.value = Object.entries(headers).map(([key, value]) => ({ key, value: String(value) }))
    if (!headerRows.value.length) headerRows.value = []
  },
  { immediate: true, deep: true }
)

function syncHeaders() {
  const headers = {}
  headerRows.value.forEach((r) => {
    if (r.key) headers[r.key] = r.value
  })
  emit('update:modelValue', { ...props.modelValue, method: data.method, url: data.url, body: data.body, headers })
}

function emitUpdate() {
  syncHeaders()
}

function addHeader() {
  headerRows.value.push({ key: '', value: '' })
}

function removeHeader(idx) {
  headerRows.value.splice(idx, 1)
  syncHeaders()
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
