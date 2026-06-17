<template>
  <div class="json-serialize-form">
    <WfVariableTableSection
      title="输入"
      tooltip="待序列化的数据结构；可引用上游 Object/Array 或填写固定 JSON 文本"
      :columns="inputColumns"
      :has-rows="inputRows.length > 0"
      empty-text="请配置输入参数"
      :show-add="inputRows.length === 0"
      add-title="添加参数"
      :show-actions="false"
      @add="ensureInputRow"
    >
      <div
        v-for="row in inputRows"
        :key="row._id"
        class="wf-vt-section__row"
        :class="{ 'wf-vt-section__row--error': errors['inputVariables.0.key'] || errors['inputVariables.0.value'] }"
      >
        <el-input
          v-model="row.key"
          size="small"
          placeholder="参数名，如 input"
          class="wf-vt-section__col wf-vt-section__col--name"
          @change="syncInputs"
        />
        <ConditionValueField
          v-model="row.value"
          :variable-tree="variableTree"
          placeholder="固定值或引用上游变量"
          class="wf-vt-section__col wf-vt-section__col--value"
          @update:model-value="syncInputs"
        />
      </div>
    </WfVariableTableSection>

    <div class="json-serialize-form__section">
      <div class="json-serialize-form__section-header">
        <span class="json-serialize-form__section-title">输出</span>
        <el-tooltip content="节点执行后固定输出 JSON 字符串，下游通过 {{节点Id.output}} 引用" placement="top">
          <el-icon class="json-serialize-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="json-serialize-form__output-list">
        <div class="json-serialize-form__output-row">
          <span class="json-serialize-form__output-key">output</span>
          <span class="json-serialize-form__output-type">string</span>
        </div>
      </div>
      <p class="json-serialize-form__output-desc">将输入序列化为紧凑 JSON 字符串；若输入已是合法 JSON 文本则原样透传。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'

defineOptions({ name: 'JsonSerializeForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const inputColumns = [
  { key: 'name', label: '参数名', class: 'wf-vt-section__col--name' },
  { key: 'value', label: '参数值', class: 'wf-vt-section__col--value' }
]

const inputRows = ref([])
let rowSeq = 0

watch(
  () => props.modelValue,
  (val) => {
    const inputs = val?.inputVariables
    if (Array.isArray(inputs) && inputs.length) {
      const row = inputs[0]
      inputRows.value = [{
        key: row?.key || 'input',
        value: row?.value || '',
        _id: inputRows.value[0]?._id || `in_${++rowSeq}`
      }]
    } else if (!inputRows.value.length) {
      inputRows.value = [{ _id: `in_${++rowSeq}`, key: 'input', value: '' }]
    }
  },
  { immediate: true, deep: true }
)

function ensureInputRow() {
  if (!inputRows.value.length) {
    inputRows.value = [{ _id: `in_${++rowSeq}`, key: 'input', value: '' }]
    syncInputs()
  }
}

function syncInputs() {
  const row = inputRows.value[0] || { key: 'input', value: '' }
  emit('update:modelValue', {
    ...(props.modelValue || {}),
    inputVariables: [{
      key: (row.key || '').trim() || 'input',
      value: (row.value || '').trim()
    }]
  })
}
</script>

<style scoped lang="scss">
.json-serialize-form__section {
  margin-top: 16px;
}

.json-serialize-form__section-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}

.json-serialize-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.json-serialize-form__info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.json-serialize-form__output-list {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}

.json-serialize-form__output-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  font-size: 13px;
  color: #303133;
}

.json-serialize-form__output-key {
  font-family: Consolas, Monaco, monospace;
}

.json-serialize-form__output-type {
  color: #909399;
  font-size: 12px;
}

.json-serialize-form__output-desc {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}
</style>
