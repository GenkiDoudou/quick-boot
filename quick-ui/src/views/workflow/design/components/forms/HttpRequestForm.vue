<template>
  <div class="http-request-form">
    <div class="http-request-form__section">
      <div class="http-request-form__field">
        <span class="http-request-form__label">请求方法</span>
        <el-select v-model="data.method" size="small" class="http-request-form__method" @change="emitUpdate">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
          <el-option label="PATCH" value="PATCH" />
        </el-select>
      </div>
      <div class="http-request-form__field">
        <span class="http-request-form__label">URL</span>
        <TemplateField
          v-model="data.url"
          :variable-tree="variableTree"
          :rows="2"
          :class="{ 'http-request-form__field--error': errors.url }"
          @update:model-value="emitUpdate"
        />
      </div>
    </div>

    <WfVariableTableSection
      title="Headers"
      tooltip="HTTP 请求头，值支持 {{变量}} 模板"
      :columns="headerColumns"
      :has-rows="headerRows.length > 0"
      empty-text="暂无 Header，点击右上角 + 添加"
      add-title="添加 Header"
      @add="addHeader"
    >
      <div v-for="(row, idx) in headerRows" :key="row._id" class="wf-vt-section__row">
        <el-input
          v-model="row.key"
          size="small"
          placeholder="Header 名"
          class="wf-vt-section__col wf-vt-section__col--name"
          @change="syncHeaders"
        />
        <TemplateField
          v-model="row.value"
          :variable-tree="variableTree"
          :rows="1"
          class="wf-vt-section__col wf-vt-section__col--value"
          @update:model-value="syncHeaders"
        />
        <el-button
          link
          type="danger"
          class="wf-vt-section__col wf-vt-section__col--actions"
          title="删除"
          @click.stop="removeHeader(idx)"
        >
          <el-icon :size="16"><Minus /></el-icon>
        </el-button>
      </div>
    </WfVariableTableSection>

    <div class="http-request-form__section">
      <div class="http-request-form__field">
        <span class="http-request-form__label">Body</span>
        <TemplateField
          v-model="data.body"
          :variable-tree="variableTree"
          :rows="4"
          @update:model-value="emitUpdate"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { Minus } from '@element-plus/icons-vue'
import TemplateField from './TemplateField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'

defineOptions({ name: 'HttpRequestForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ method: 'GET', url: '', body: '' })
const headerRows = ref([])
let headerSeq = 0
let syncing = false

const headerColumns = [
  { key: 'key', label: 'Key', class: 'wf-vt-section__col--name' },
  { key: 'value', label: 'Value', class: 'wf-vt-section__col--flex' }
]

watch(
  () => props.modelValue,
  (val) => {
    if (syncing) return
    data.method = val?.method ?? 'GET'
    data.url = val?.url ?? ''
    data.body = val?.body ?? ''
    const headers = val?.headers || {}
    headerRows.value = Object.entries(headers).map(([key, value], idx) => ({
      key,
      value: String(value ?? ''),
      _id: headerRows.value[idx]?._id || `hdr_${++headerSeq}`
    }))
  },
  { immediate: true, deep: true }
)

function buildPayload() {
  const headers = {}
  headerRows.value.forEach((r) => {
    const k = (r.key || '').trim()
    if (k) headers[k] = r.value ?? ''
  })
  return {
    ...props.modelValue,
    method: data.method,
    url: data.url,
    body: data.body,
    headers
  }
}

function syncHeaders() {
  syncing = true
  emit('update:modelValue', buildPayload())
  queueMicrotask(() => {
    syncing = false
  })
}

function emitUpdate() {
  syncHeaders()
}

function addHeader() {
  headerRows.value.push({ _id: `hdr_${++headerSeq}`, key: '', value: '' })
  syncHeaders()
}

function removeHeader(idx) {
  headerRows.value.splice(idx, 1)
  syncHeaders()
}
</script>

<style scoped lang="scss">
.http-request-form__section {
  margin-bottom: 16px;
}

.http-request-form__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.http-request-form__label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.http-request-form__method {
  width: 120px;
}

.http-request-form__field--error :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}
</style>
