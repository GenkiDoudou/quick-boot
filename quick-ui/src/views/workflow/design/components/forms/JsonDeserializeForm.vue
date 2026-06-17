<template>
  <div class="json-deserialize-form">
    <WfVariableTableSection
      title="输入"
      tooltip="待解析的 JSON 字符串；可引用上游 HTTP body 等"
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

    <WfVariableTableSection
      title="输出字段"
      tooltip="可选；配置后从 JSON 根对象按点路径提取字段；留空则整包输出到 output"
      :columns="fieldColumns"
      :has-rows="fieldRows.length > 0"
      empty-text="未配置字段时将整包解析结果写入 output"
      add-title="添加字段"
      @add="addFieldRow"
    >
      <template #header-extra>
        <el-button size="small" link type="primary" @click="importDialogVisible = true">
          导入 JSON 示例
        </el-button>
      </template>
      <div
        v-for="(row, idx) in fieldRows"
        :key="row._id"
        class="wf-vt-section__row"
        :class="{ 'wf-vt-section__row--error': errors[`outputFields.${idx}.key`] }"
      >
        <el-input
          v-model="row.key"
          size="small"
          placeholder="字段 key"
          class="wf-vt-section__col wf-vt-section__col--name"
          @change="syncFields"
        />
        <el-input
          v-model="row.path"
          size="small"
          placeholder="点路径，如 data.user.name"
          class="wf-vt-section__col wf-vt-section__col--path"
          @change="syncFields"
        />
        <el-select
          v-model="row.type"
          size="small"
          class="wf-vt-section__col wf-vt-section__col--type"
          @change="syncFields"
        >
          <el-option
            v-for="t in TYPE_OPTIONS"
            :key="t"
            :label="t"
            :value="t"
          />
        </el-select>
        <div class="wf-vt-section__col wf-vt-section__col--actions">
          <el-button link type="danger" title="删除" @click.stop="removeFieldRow(idx)">
            <el-icon :size="14"><Minus /></el-icon>
          </el-button>
        </div>
      </div>
    </WfVariableTableSection>

    <div class="json-deserialize-form__section">
      <div class="json-deserialize-form__section-header">
        <span class="json-deserialize-form__section-title">输出</span>
        <el-tooltip content="节点执行后固定输出 object/array；下游通过 {{节点Id.output.xxx}} 引用" placement="top">
          <el-icon class="json-deserialize-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="json-deserialize-form__output-list">
        <div class="json-deserialize-form__output-row">
          <span class="json-deserialize-form__output-key">output</span>
          <span class="json-deserialize-form__output-type">object</span>
        </div>
        <div
          v-for="field in configuredFieldKeys"
          :key="field"
          class="json-deserialize-form__output-row json-deserialize-form__output-row--sub"
        >
          <span class="json-deserialize-form__output-key">output.{{ field }}</span>
          <span class="json-deserialize-form__output-type">字段</span>
        </div>
      </div>
      <p class="json-deserialize-form__output-desc">
        将 JSON 字符串解析为对象；非法 JSON、空输入或嵌套超过 3 层时节点失败。
      </p>
    </div>

    <el-dialog
      v-model="importDialogVisible"
      title="导入 JSON 示例"
      width="520px"
      destroy-on-close
      @closed="importSampleText = ''"
    >
      <p class="json-deserialize-form__import-hint">
        粘贴 JSON 对象示例，将自动生成叶子字段（深度 ≤ 3）。
      </p>
      <el-input
        v-model="importSampleText"
        type="textarea"
        :rows="10"
        placeholder='{"data":{"user":{"name":"张三","age":18}}}'
      />
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="applyImportSample">生成字段</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Minus } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import { useWfFormRows } from './shared/useWfFormRows'
import { TYPE_OPTIONS, generateFieldsFromJsonExample } from '../../utils/jsonDeserializeUtils'

defineOptions({ name: 'JsonDeserializeForm' })

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

const fieldColumns = [
  { key: 'name', label: '字段 key', class: 'wf-vt-section__col--name' },
  { key: 'path', label: '点路径', class: 'wf-vt-section__col--path' },
  { key: 'type', label: '类型', class: 'wf-vt-section__col--type' }
]

const inputRows = ref([])
let inputRowSeq = 0
const importDialogVisible = ref(false)
const importSampleText = ref('')

watch(
  () => props.modelValue,
  (val) => {
    const inputs = val?.inputVariables
    if (Array.isArray(inputs) && inputs.length) {
      const row = inputs[0]
      inputRows.value = [{
        key: row?.key || 'input',
        value: row?.value || '',
        _id: inputRows.value[0]?._id || `in_${++inputRowSeq}`
      }]
    } else if (!inputRows.value.length) {
      inputRows.value = [{ _id: `in_${++inputRowSeq}`, key: 'input', value: '' }]
    }
  },
  { immediate: true, deep: true }
)

const { rows: fieldRows, sync: syncFields, addRow: addFieldRow, removeRow: removeFieldRow } = useWfFormRows({
  getSource: () => props.modelValue?.outputFields,
  allowEmpty: true,
  toRow: (item, idx, prevRows, nextRowId) => ({
    key: item?.key || '',
    path: item?.path || item?.key || '',
    type: item?.type || 'string',
    _id: prevRows[idx]?._id || nextRowId('field')
  }),
  fromRows: (rows) => ({
    ...(props.modelValue || {}),
    inputVariables: props.modelValue?.inputVariables || [{ key: 'input', value: '' }],
    outputFields: rows
      .map((row) => ({
        key: (row.key || '').trim(),
        path: (row.path || row.key || '').trim(),
        type: row.type || 'string'
      }))
      .filter((row) => row.key)
  }),
  emitModel: (next) => emit('update:modelValue', next),
  createRow: (nextRowId) => ({
    _id: nextRowId('field'),
    key: '',
    path: '',
    type: 'string'
  })
})

const configuredFieldKeys = computed(() => {
  const fields = props.modelValue?.outputFields
  if (!Array.isArray(fields)) return []
  return fields.map((f) => (f?.key || '').trim()).filter(Boolean)
})

function ensureInputRow() {
  if (!inputRows.value.length) {
    inputRows.value = [{ _id: `in_${++inputRowSeq}`, key: 'input', value: '' }]
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
    }],
    outputFields: props.modelValue?.outputFields || []
  })
}

function applyImportSample() {
  try {
    const fields = generateFieldsFromJsonExample(importSampleText.value)
    emit('update:modelValue', {
      ...(props.modelValue || {}),
      inputVariables: props.modelValue?.inputVariables || [{ key: 'input', value: '' }],
      outputFields: fields
    })
    importDialogVisible.value = false
    ElMessage.success(`已生成 ${fields.length} 个字段`)
  } catch (err) {
    ElMessage.error(err?.message || '导入失败')
  }
}
</script>

<style scoped lang="scss">
.json-deserialize-form__section {
  margin-top: 16px;
}

.json-deserialize-form__section-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}

.json-deserialize-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.json-deserialize-form__info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.json-deserialize-form__output-list {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}

.json-deserialize-form__output-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  font-size: 13px;
  color: #303133;

  &--sub {
    padding-left: 20px;
    border-top: 1px solid #ebeef5;
    background: #fff;
  }
}

.json-deserialize-form__output-key {
  font-family: Consolas, Monaco, monospace;
}

.json-deserialize-form__output-type {
  color: #909399;
  font-size: 12px;
}

.json-deserialize-form__output-desc {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.json-deserialize-form__import-hint {
  margin: 0 0 8px;
  font-size: 12px;
  color: #909399;
}

:deep(.wf-vt-section__col--path) {
  flex: 1.4;
}
</style>
