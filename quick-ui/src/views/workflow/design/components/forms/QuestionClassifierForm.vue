<template>
  <div class="intent-form">
    <div class="intent-form__section">
      <div class="intent-form__section-header">
        <span class="intent-form__label">大模型</span>
        <el-tooltip content="留空则使用工作流绑定的 Chat 模型或全局默认" placement="top">
          <el-icon class="intent-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <el-select
        v-model="data.chatModelId"
        filterable
        clearable
        placeholder="选择大模型"
        class="intent-form__full"
        :loading="modelLoading"
        @change="emitUpdate"
      >
        <el-option
          v-for="item in modelOptions"
          :key="item.modelId"
          :label="formatModelLabel(item)"
          :value="item.modelId"
        />
      </el-select>
    </div>

    <WfVariableTableSection
      title="输入"
      tooltip="声明待识别文本的参数名，并从上游映射取值；系统提示词中通过 {{参数名}} 引用"
      :columns="inputColumns"
      :has-rows="inputRows.length > 0"
      empty-text="请添加入参"
      add-title="添加参数"
      @add="addInputRow"
    >
      <div
        v-for="(row, idx) in inputRows"
        :key="row._id"
        class="wf-vt-section__row"
        :class="{ 'wf-vt-section__row--error': errors[`inputVariables.${idx}.key`] || errors.query }"
      >
        <el-input
          v-model="row.key"
          size="small"
          placeholder="参数名，如 query"
          class="wf-vt-section__col wf-vt-section__col--name"
          @change="syncInputs"
        />
        <ConditionValueField
          v-model="row.value"
          :variable-tree="variableTree"
          placeholder="输入或引用变量"
          class="wf-vt-section__col wf-vt-section__col--value"
          @update:model-value="syncInputs"
        />
        <el-button
          v-if="inputRows.length > 1"
          link
          type="danger"
          class="wf-vt-section__col wf-vt-section__col--actions"
          title="删除"
          @click.stop="removeInputRow(idx)"
        >
          <el-icon :size="16"><Minus /></el-icon>
        </el-button>
      </div>
    </WfVariableTableSection>

    <WfVariableTableSection
      title="意图匹配"
      tooltip="每增加一个意图，画布右侧自动增加一个出口（1、2、…）；末尾固定「其他」兜底"
      :columns="intentColumns"
      :has-rows="rows.length > 0"
      empty-text="暂无意图，点击右上角 + 添加"
      add-title="添加意图"
      @add="addRow"
    >
      <div
        v-for="(row, idx) in rows"
        :key="row._id"
        class="intent-form__block"
        :class="{ 'intent-form__block--expanded': isExpanded(row._id) }"
      >
        <div
          class="wf-vt-section__row"
          :class="{ 'wf-vt-section__row--error': errors[`intents.${idx}.name`] }"
        >
          <span class="intent-form__index">{{ idx + 1 }}</span>
          <el-input
            v-model="row.name"
            size="small"
            placeholder="意图名称"
            class="wf-vt-section__col wf-vt-section__col--flex"
            @change="sync"
          />
          <div class="wf-vt-section__col wf-vt-section__col--actions intent-form__actions">
            <el-button link title="典型示例" @click.stop="toggleExpand(row._id)">
              <el-icon :size="14"><FullScreen /></el-icon>
            </el-button>
            <el-button link type="danger" title="删除" @click.stop="removeRow(idx)">
              <el-icon :size="14"><Minus /></el-icon>
            </el-button>
          </div>
        </div>
        <div v-if="isExpanded(row._id)" class="intent-form__expand">
          <div class="intent-form__expand-label">典型示例（一行一例，可选）</div>
          <el-input
            v-model="row.examplesText"
            size="small"
            type="textarea"
            :rows="3"
            placeholder="例如：想买一台笔记本"
            @change="sync"
          />
        </div>
      </div>
    </WfVariableTableSection>

    <div class="intent-form__section">
      <div class="intent-form__section-header">
        <span class="intent-form__label">系统提示词</span>
        <el-tooltip
          content="补充各意图的详细描述与更多示例；仅可引用上方「输入」中已声明的参数，如 {{query}}"
          placement="top"
        >
          <el-icon class="intent-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <TemplateField
        v-model="data.systemPrompt"
        :variable-tree="promptVariableTree"
        :rows="4"
        placeholder="例如：意图「售后」还包括换货、保修咨询；可写 {{query}} 作为上下文说明"
        @update:model-value="emitUpdate"
      />
    </div>

    <div class="intent-form__section">
      <div class="intent-form__section-header">
        <span class="intent-form__label">输出</span>
        <el-tooltip content="节点执行后可供下游引用的固定输出字段" placement="top">
          <el-icon class="intent-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="intent-form__output-table">
        <div class="intent-form__output-thead">
          <span class="intent-form__output-col intent-form__output-col--name">变量名</span>
          <span class="intent-form__output-col intent-form__output-col--type">类型</span>
        </div>
        <div
          v-for="item in INTENT_OUTPUT_VARIABLES"
          :key="item.key"
          class="intent-form__output-row"
        >
          <span class="intent-form__output-col intent-form__output-col--name">{{ item.key }}</span>
          <span class="intent-form__output-col intent-form__output-col--type">{{ item.type }}</span>
        </div>
        <p class="intent-form__output-desc">
          {{ INTENT_OUTPUT_VARIABLES.map((v) => `${v.key}：${v.description}`).join('；') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, reactive, watch } from 'vue'
import { FullScreen, InfoFilled, Minus } from '@element-plus/icons-vue'
import { listModelOptions } from '@/api/ai/model'
import { buildLlmPromptVariableTree } from '../../composables/useUpstreamVariables'
import ConditionValueField from './ConditionValueField.vue'
import TemplateField from './TemplateField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import { useWfFormRows } from './shared/useWfFormRows'
import {
  INTENT_DEFAULT_INPUT_KEY,
  INTENT_MAX_INTENTS,
  INTENT_OUTPUT_VARIABLES,
  intentFormToRow,
  intentMaxCount,
  normalizeIntentNodeData,
  serializeIntentRows
} from '../../utils/intentUtils'

defineOptions({ name: 'QuestionClassifierForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const data = reactive({
  chatModelId: null,
  systemPrompt: ''
})
const expandedIds = ref(new Set())
const inputRows = ref([])
const modelOptions = ref([])
const modelLoading = ref(false)
let intentSeq = 0
let inputRowSeq = 0

const inputColumns = [
  { key: 'name', label: '参数名', class: 'wf-vt-section__col--name' },
  { key: 'value', label: '参数值', class: 'wf-vt-section__col--value' }
]

const intentColumns = [
  { key: 'index', label: '#', class: 'intent-form__col-index' },
  { key: 'name', label: '意图名称', class: 'wf-vt-section__col--flex' }
]

const promptVariableTree = computed(() =>
  buildLlmPromptVariableTree(inputRows.value.map((row) => ({ key: row.key, value: row.value })))
)

watch(
  () => props.modelValue,
  (val) => {
    const normalized = normalizeIntentNodeData(val)
    data.chatModelId = normalized.chatModelId ?? null
    data.systemPrompt = normalized.systemPrompt ?? ''
    const inputs = normalized.inputVariables
    if (Array.isArray(inputs) && inputs.length) {
      inputRows.value = inputs.map((row, idx) => ({
        key: row?.key || '',
        value: row?.value || '',
        _id: inputRows.value[idx]?._id || `in_${++inputRowSeq}`
      }))
    } else if (!inputRows.value.length) {
      inputRows.value = [{
        _id: `in_${++inputRowSeq}`,
        key: INTENT_DEFAULT_INPUT_KEY,
        value: '{{start_1.question}}'
      }]
    }
  },
  { immediate: true, deep: true }
)

const { rows, sync, addRow, removeRow } = useWfFormRows({
  getSource: () => normalizeIntentNodeData(props.modelValue).intents,
  toRow: (item, idx, prevRows, id) => intentFormToRow(item, idx, prevRows, id),
  fromRows: serializeIntentRows,
  emitModel: (intents) => emitPayload(intents),
  createRow: (id) => {
    intentSeq += 1
    return { _id: id('intent'), name: `意图${intentSeq}`, examplesText: '' }
  },
  allowEmpty: false
})

onMounted(() => {
  modelLoading.value = true
  listModelOptions('CHAT')
    .then((res) => {
      modelOptions.value = res.data || []
    })
    .finally(() => {
      modelLoading.value = false
    })
})

function formatModelLabel(item) {
  const star = item.defaultSlot ? ' ★' : ''
  return `${item.name} (${item.code})${star}`
}

function serializeInputRows() {
  return inputRows.value
    .map(({ _id, ...rest }) => ({
      key: (rest.key || '').trim(),
      value: (rest.value || '').trim()
    }))
    .filter((row) => row.key)
}

function emitPayload(intents) {
  const limit = intentMaxCount()
  const trimmed = intents.slice(0, limit)
  const inputVariables = serializeInputRows()
  const primaryKey = inputVariables[0]?.key || INTENT_DEFAULT_INPUT_KEY
  emit('update:modelValue', {
    ...normalizeIntentNodeData(props.modelValue),
    chatModelId: data.chatModelId || null,
    inputVariables,
    query: primaryKey ? `{{${primaryKey}}}` : '',
    systemPrompt: data.systemPrompt,
    outputVariables: INTENT_OUTPUT_VARIABLES.map((item) => ({ ...item })),
    intents: trimmed
  })
}

function syncInputs() {
  emitPayload(serializeIntentRows(rows.value))
}

function addInputRow() {
  inputRows.value.push({
    _id: `in_${++inputRowSeq}`,
    key: `param${inputRows.value.length + 1}`,
    value: ''
  })
  syncInputs()
}

function removeInputRow(idx) {
  inputRows.value.splice(idx, 1)
  syncInputs()
}

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
  emitPayload(serializeIntentRows(rows.value))
}
</script>

<style scoped lang="scss">
.intent-form__section {
  margin-bottom: 16px;
}

.intent-form__label {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.intent-form__full {
  width: 100%;
}

.intent-form__section-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}

.intent-form__info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.intent-form__index {
  width: 28px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  text-align: center;
}

.intent-form__block {
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }

  &--expanded {
    background: #f0f7ff;
  }
}

.intent-form__actions {
  display: flex;
  width: 52px !important;
  gap: 2px;
  justify-content: flex-end;
}

.intent-form__expand {
  padding: 0 10px 10px 38px;
}

.intent-form__expand-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.intent-form__output-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}

.intent-form__output-thead,
.intent-form__output-row {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  gap: 8px;
}

.intent-form__output-thead {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.intent-form__output-row {
  font-size: 13px;
  color: #303133;
  border-bottom: 1px solid #f0f2f5;

  &:last-of-type {
    border-bottom: none;
  }
}

.intent-form__output-col {
  &--name {
    flex: 1;
    font-family: Consolas, Monaco, monospace;
  }

  &--type {
    width: 72px;
    color: #909399;
    text-align: right;
  }
}

.intent-form__output-desc {
  margin: 0;
  padding: 8px 10px 10px;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
  border-top: 1px solid #ebeef5;
}
</style>
