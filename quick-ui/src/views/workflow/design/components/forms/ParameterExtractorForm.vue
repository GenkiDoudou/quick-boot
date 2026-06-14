<template>
  <div class="parameter-extractor-form">
    <div class="parameter-extractor-form__section">
      <div class="parameter-extractor-form__label">待提取文本</div>
      <TemplateField
        v-model="data.query"
        :variable-tree="variableTree"
        :rows="3"
        :class="{ 'parameter-extractor-form__field--error': errors.query }"
        @update:model-value="emitUpdate"
      />
    </div>

    <WfVariableTableSection
      title="Schema 字段"
      tooltip="定义要从文本中提取的结构化字段"
      :columns="schemaColumns"
      :has-rows="rows.length > 0"
      empty-text="暂无字段，点击右上角 + 添加"
      add-title="添加字段"
      @add="addRow"
    >
      <div
        v-for="(row, idx) in rows"
        :key="row._id"
        class="parameter-extractor-form__block"
        :class="{ 'parameter-extractor-form__block--expanded': isExpanded(row._id) }"
      >
        <div
          class="wf-vt-section__row"
          :class="{ 'wf-vt-section__row--error': errors[`schema.fields.${idx}.key`] }"
        >
          <el-input
            v-model="row.key"
            size="small"
            placeholder="字段 key"
            class="wf-vt-section__col wf-vt-section__col--name"
            @change="sync"
          />
          <el-select
            v-model="row.type"
            size="small"
            class="wf-vt-section__col wf-vt-section__col--type"
            @change="sync"
          >
            <el-option label="字符串" value="string" />
            <el-option label="数字" value="number" />
            <el-option label="布尔" value="boolean" />
          </el-select>
          <el-checkbox
            v-model="row.required"
            class="wf-vt-section__col wf-vt-section__col--required"
            @change="sync"
          />
          <div class="wf-vt-section__col wf-vt-section__col--actions parameter-extractor-form__actions">
            <el-button link title="展开描述" @click.stop="toggleExpand(row._id)">
              <el-icon :size="14"><FullScreen /></el-icon>
            </el-button>
            <el-button link type="danger" title="删除" @click.stop="removeRow(idx)">
              <el-icon :size="14"><Minus /></el-icon>
            </el-button>
          </div>
        </div>
        <div v-if="isExpanded(row._id)" class="parameter-extractor-form__expand">
          <div class="parameter-extractor-form__expand-label">描述</div>
          <el-input
            v-model="row.description"
            size="small"
            type="textarea"
            :rows="2"
            placeholder="帮助大模型理解该字段含义"
            @change="sync"
          />
        </div>
      </div>
    </WfVariableTableSection>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { FullScreen, Minus } from '@element-plus/icons-vue'
import TemplateField from './TemplateField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import { useWfFormRows } from './shared/useWfFormRows'

defineOptions({ name: 'ParameterExtractorForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({ query: '' })
const expandedIds = ref(new Set())

const schemaColumns = [
  { key: 'name', label: '字段名', class: 'wf-vt-section__col--name' },
  { key: 'type', label: '类型', class: 'wf-vt-section__col--type' },
  { key: 'required', label: '必填', class: 'wf-vt-section__col--required' }
]

watch(
  () => props.modelValue?.query,
  (val) => {
    data.query = val ?? ''
  },
  { immediate: true }
)

const { rows, sync, addRow, removeRow } = useWfFormRows({
  getSource: () => props.modelValue?.schema?.fields,
  toRow: (item, idx, prevRows, id) => ({
    key: item?.key || '',
    type: item?.type || 'string',
    description: item?.description || '',
    required: !!item?.required,
    _id: prevRows[idx]?._id || id('field')
  }),
  fromRows: (list) =>
    list.map(({ _id, ...rest }) => ({
      key: (rest.key || '').trim(),
      type: rest.type || 'string',
      description: (rest.description || '').trim(),
      required: !!rest.required
    })),
  emitModel: (fields) =>
    emit('update:modelValue', {
      ...props.modelValue,
      query: data.query,
      schema: { fields }
    }),
  createRow: (id) => ({
    _id: id('field'),
    key: '',
    type: 'string',
    description: '',
    required: false
  }),
  allowEmpty: true
})

function isExpanded(id) {
  return expandedIds.value.has(id)
}

function toggleExpand(id) {
  const next = new Set(expandedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  expandedIds.value = next
}

function emitUpdate() {
  sync()
}
</script>

<style scoped lang="scss">
.parameter-extractor-form__section {
  margin-bottom: 16px;
}

.parameter-extractor-form__label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.parameter-extractor-form__field--error :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}

.parameter-extractor-form__block {
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }

  &--expanded {
    background: #f0f7ff;
  }
}

.parameter-extractor-form__actions {
  display: flex;
  width: 52px !important;
  gap: 2px;
  justify-content: flex-end;
}

.parameter-extractor-form__expand {
  padding: 0 10px 10px;
}

.parameter-extractor-form__expand-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
</style>
