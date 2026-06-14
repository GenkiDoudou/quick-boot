<template>
  <div class="llm-form">
    <!-- 大模型选择 -->
    <div class="llm-form__section">
      <div class="llm-form__section-header">
        <span class="llm-form__section-title">大模型</span>
        <el-tooltip content="留空则使用工作流绑定的 Chat 模型或全局默认" placement="top">
          <el-icon class="llm-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <el-select
        v-model="data.chatModelId"
        filterable
        clearable
        placeholder="选择大模型"
        class="llm-form__model-select"
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

    <!-- 输入参数 -->
    <WfVariableTableSection
      title="输入参数"
      tooltip="Coze 式：在此声明本节点可用的变量名，并从上游映射取值；提示词中通过 {{参数名}} 引用"
      :columns="inputColumns"
      :has-rows="inputRows.length > 0"
      empty-text="请先添加入参：参数名自定义，参数值选择上游变量"
      add-title="添加参数"
      @add="addInputRow"
    >
      <div v-for="(row, idx) in inputRows" :key="row._id" class="wf-vt-section__row">
        <el-input
          v-model="row.key"
          size="small"
          placeholder="参数名"
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

    <!-- 系统提示词 -->
    <div class="llm-form__section">
      <div class="llm-form__section-header">
        <span class="llm-form__section-title">系统提示词</span>
        <el-tooltip content="可从提示词库选择模板，也可在下方自行编写；仅可引用上方「输入参数」中已声明的变量" placement="top">
          <el-icon class="llm-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <el-select
        v-model="selectedPromptId"
        filterable
        clearable
        placeholder="从提示词库选择（可选）"
        class="llm-form__prompt-select"
        :loading="promptLoading"
        @change="onPromptSelect"
      >
        <el-option
          v-for="item in promptOptions"
          :key="item.promptId"
          :label="formatPromptLabel(item)"
          :value="item.promptId"
        />
      </el-select>
      <TemplateField
        v-model="data.systemPrompt"
        :variable-tree="promptVariableTree"
        :rows="6"
        expandable
        expand-title="系统提示词"
        :dialog-rows="20"
        :hint="LLM_PROMPT_HINT"
        placeholder="例如：你是助手，请根据 {{context}} 回答"
        @update:model-value="onSystemPromptInput"
      />
      <p v-if="systemPromptWarnings.length" class="llm-form__warn">
        未在输入参数中声明：{{ systemPromptWarnings.join('、') }}
      </p>
    </div>

    <!-- 用户提示词 -->
    <div class="llm-form__section">
      <div class="llm-form__section-header">
        <span class="llm-form__section-title">用户提示词</span>
        <span class="llm-form__required">*</span>
        <el-tooltip content="仅可引用输入参数，如 {{question}}；不可直接写 {{start_1.xxx}}" placement="top">
          <el-icon class="llm-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <TemplateField
        v-model="data.userPrompt"
        :variable-tree="promptVariableTree"
        :rows="8"
        expandable
        expand-title="用户提示词"
        :dialog-rows="22"
        :hint="LLM_PROMPT_HINT"
        placeholder="例如：请回答 {{question}}"
        :class="{ 'llm-form__field--error': errors.userPrompt }"
        @update:model-value="emitUpdate"
      />
      <p v-if="userPromptWarnings.length" class="llm-form__warn">
        未在输入参数中声明：{{ userPromptWarnings.join('、') }}
      </p>
    </div>

    <!-- 输出（Coze 风格） -->
    <div class="llm-form__section">
      <div class="llm-form__section-header llm-form__section-header--output">
        <div class="llm-form__section-title-wrap">
          <span class="llm-form__section-title">输出</span>
          <el-tooltip
            :content="outputFormatHint"
            placement="top"
          >
            <el-icon class="llm-form__info"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="llm-form__format-wrap">
          <span class="llm-form__format-label">输出格式</span>
          <el-select
            v-model="data.outputFormat"
            size="small"
            class="llm-form__format-select"
            @change="onOutputFormatChange"
          >
            <el-option
              v-for="item in LLM_OUTPUT_FORMATS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-button
            v-if="data.outputFormat === 'json'"
            link
            class="llm-form__add-btn"
            title="添加输出变量"
            @click.stop="addOutputRow"
          >
            <el-icon :size="16"><Plus /></el-icon>
          </el-button>
        </div>
      </div>

      <div v-if="outputRows.length" class="llm-form__output-table">
        <div class="llm-form__thead">
          <span class="llm-form__col llm-form__col--name">变量名</span>
          <span class="llm-form__col llm-form__col--type">变量类型</span>
          <span class="llm-form__col llm-form__col--actions" />
        </div>
        <div
          v-for="(row, idx) in outputRows"
          :key="row._id"
          class="llm-form__block"
          :class="{ 'llm-form__block--expanded': isOutputExpanded(row._id) }"
        >
          <div class="llm-form__row">
            <el-input
              v-model="row.key"
              size="small"
              placeholder="变量名"
              class="llm-form__col llm-form__col--name"
              :disabled="data.outputFormat !== 'json' && outputRows.length === 1"
              @change="syncOutputs"
            />
            <el-select
              v-model="row.type"
              size="small"
              class="llm-form__col llm-form__col--type"
              @change="syncOutputs"
            >
              <el-option
                v-for="t in LLM_OUTPUT_TYPES"
                :key="t.value"
                :label="t.label"
                :value="t.value"
              />
            </el-select>
            <div class="llm-form__col llm-form__col--actions llm-form__row-actions">
              <el-button link class="llm-form__action-btn" title="展开描述" @click="toggleOutputExpand(row._id)">
                <el-icon :size="14"><FullScreen /></el-icon>
              </el-button>
              <el-button
                v-if="data.outputFormat === 'json'"
                link
                type="danger"
                class="llm-form__action-btn"
                title="删除"
                @click.stop="removeOutputRow(idx)"
              >
                <el-icon :size="14"><Minus /></el-icon>
              </el-button>
            </div>
          </div>
          <div v-if="isOutputExpanded(row._id)" class="llm-form__expand">
            <div class="llm-form__expand-label">描述</div>
            <el-input
              v-model="row.description"
              size="small"
              type="textarea"
              :rows="2"
              placeholder="帮助大模型准确了解参数的作用"
              @change="syncOutputs"
            />
          </div>
        </div>
      </div>

      <div class="llm-form__output-row">
        <span class="llm-form__output-label">流式输出</span>
        <el-switch v-model="data.streaming" size="small" @change="emitUpdate" />
      </div>
      <div class="llm-form__output-row">
        <span class="llm-form__output-label">温度</span>
        <div class="llm-form__temp">
          <el-slider v-model="data.temperature" :min="0" :max="1" :step="0.1" @change="emitUpdate" />
          <el-input-number v-model="data.temperature" :min="0" :max="1" :step="0.1" size="small" @change="emitUpdate" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { InfoFilled, Plus, Minus, FullScreen } from '@element-plus/icons-vue'
import { listModelOptions } from '@/api/ai/model'
import { getPromptInfo, listPromptOptions } from '@/api/ai/prompt'
import { ElMessage } from 'element-plus'
import TemplateField from './TemplateField.vue'
import ConditionValueField from './ConditionValueField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import {
  buildLlmPromptVariableTree,
  findUndeclaredLlmPromptReferences
} from '../../composables/useUpstreamVariables'

defineOptions({ name: 'LlmForm' })

const LLM_PROMPT_HINT =
  '提示：先在上方「输入参数」声明变量并映射上游值，再在此用 {{参数名}} 或 {{参数名.子字段}} 引用。'

const LLM_OUTPUT_FORMATS = [
  { value: 'text', label: '文本' },
  { value: 'markdown', label: 'Markdown' },
  { value: 'json', label: 'JSON' }
]

const LLM_OUTPUT_TYPES = [
  { value: 'string', label: 'String' },
  { value: 'number', label: 'Number' },
  { value: 'boolean', label: 'Boolean' },
  { value: 'object', label: 'Object' },
  { value: 'array', label: 'Array' }
]

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const modelOptions = ref([])
const modelLoading = ref(false)
const promptOptions = ref([])
const promptLoading = ref(false)
const selectedPromptId = ref(null)
/** 最近一次从提示词库载入的正文，用于区分「库模板」与「用户手改」 */
const linkedPromptContent = ref('')
const inputRows = ref([])
const outputRows = ref([])
const expandedOutputIds = ref(new Set())
let inputRowSeq = 0
let outputRowSeq = 0
let syncing = false

const inputColumns = [
  { key: 'name', label: '参数名', class: 'wf-vt-section__col--name' },
  { key: 'value', label: '参数值', class: 'wf-vt-section__col--flex' }
]

const data = reactive({
  chatModelId: null,
  systemPrompt: '',
  systemPromptId: null,
  userPrompt: '',
  temperature: 0.3,
  streaming: true,
  outputFormat: 'text'
})

const outputFormatHint = computed(() => {
  if (data.outputFormat === 'json') {
    return 'JSON 模式：大模型回复将解析为 JSON，并按下方变量名展开字段'
  }
  if (data.outputFormat === 'markdown') {
    return 'Markdown 模式：大模型回复作为 Markdown 文本写入 output 变量'
  }
  return '文本模式：大模型回复作为纯文本写入 output 变量'
})

const declaredInputKeys = computed(() =>
  inputRows.value.map((row) => (row.key || '').trim()).filter(Boolean)
)

const promptVariableTree = computed(() =>
  buildLlmPromptVariableTree(
    inputRows.value.map((row) => ({ key: row.key, value: row.value }))
  )
)

const systemPromptWarnings = computed(() =>
  findUndeclaredLlmPromptReferences(data.systemPrompt, declaredInputKeys.value)
)

const userPromptWarnings = computed(() =>
  findUndeclaredLlmPromptReferences(data.userPrompt, declaredInputKeys.value)
)

watch(
  () => props.modelValue,
  (val) => {
    if (syncing) return
    data.chatModelId = val?.chatModelId ?? null
    data.systemPrompt = val?.systemPrompt ?? ''
    data.systemPromptId = val?.systemPromptId ?? null
    selectedPromptId.value = data.systemPromptId
    linkedPromptContent.value = data.systemPromptId ? data.systemPrompt : ''
    data.userPrompt = val?.userPrompt ?? ''
    data.temperature = val?.temperature ?? 0.3
    data.streaming = val?.streaming ?? true
    data.outputFormat = val?.outputFormat ?? 'text'

    const inputs = val?.inputVariables
    if (Array.isArray(inputs)) {
      inputRows.value = inputs.map((row, idx) => ({
        key: row?.key || '',
        value: row?.value || '',
        _id: inputRows.value[idx]?._id || `in_${++inputRowSeq}`
      }))
    }

    syncOutputRowsFromModel(val)
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  modelLoading.value = true
  listModelOptions('CHAT')
    .then((res) => {
      modelOptions.value = res.data || []
    })
    .finally(() => {
      modelLoading.value = false
    })

  promptLoading.value = true
  listPromptOptions()
    .then((res) => {
      promptOptions.value = res.data || []
    })
    .catch(() => {
      promptOptions.value = []
    })
    .finally(() => {
      promptLoading.value = false
    })
})

/**
 * @param {object} item
 * @returns {string}
 */
function formatModelLabel(item) {
  const star = item.defaultSlot ? ' ★' : ''
  return `${item.name} (${item.code})${star}`
}

/**
 * @param {{ name?: string, category?: string }} item
 * @returns {string}
 */
function formatPromptLabel(item) {
  const category = (item.category || '').trim()
  return category ? `${item.name}（${category}）` : item.name
}

function onSystemPromptInput() {
  if (
    data.systemPromptId != null
    && data.systemPrompt !== linkedPromptContent.value
  ) {
    data.systemPromptId = null
    selectedPromptId.value = null
    linkedPromptContent.value = ''
  }
  emitUpdate()
}

/**
 * 从提示词库选择后载入正文；清空选择时不改动当前正文。
 * @param {number|string|null} promptId
 */
async function onPromptSelect(promptId) {
  if (!promptId) {
    data.systemPromptId = null
    linkedPromptContent.value = ''
    emitUpdate()
    return
  }

  promptLoading.value = true
  try {
    const res = await getPromptInfo(promptId)
    const content = res.data?.content ?? ''
    data.systemPrompt = content
    data.systemPromptId = promptId
    linkedPromptContent.value = content
    emitUpdate()
  } catch {
    ElMessage.error('加载提示词失败')
    selectedPromptId.value = data.systemPromptId
  } finally {
    promptLoading.value = false
  }
}

function defaultOutputRow(key = 'output') {
  return {
    _id: `out_${++outputRowSeq}`,
    key,
    type: 'string',
    description: ''
  }
}

function syncOutputRowsFromModel(val) {
  const outputs = val?.outputVariables
  if (Array.isArray(outputs) && outputs.length) {
    outputRows.value = outputs.map((row, idx) => ({
      key: row?.key || 'output',
      type: row?.type || 'string',
      description: row?.description || '',
      _id: outputRows.value[idx]?._id || `out_${++outputRowSeq}`
    }))
    return
  }
  outputRows.value = [defaultOutputRow()]
}

function onOutputFormatChange() {
  if (data.outputFormat !== 'json' && outputRows.value.length !== 1) {
    outputRows.value = [defaultOutputRow()]
  }
  if (data.outputFormat !== 'json' && outputRows.value.length === 1 && !outputRows.value[0].key) {
    outputRows.value[0].key = 'output'
  }
  if (data.outputFormat === 'json' && !outputRows.value.length) {
    outputRows.value = [defaultOutputRow()]
  }
  emitUpdate()
}

function isOutputExpanded(id) {
  return expandedOutputIds.value.has(id)
}

function toggleOutputExpand(id) {
  const next = new Set(expandedOutputIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  expandedOutputIds.value = next
}

function syncInputs() {
  emitUpdate()
}

function syncOutputs() {
  emitUpdate()
}

function emitUpdate() {
  syncing = true
  const inputVariables = inputRows.value.map((row) => ({
    key: (row.key || '').trim(),
    value: (row.value || '').trim()
  }))
  const outputVariables = outputRows.value.map((row) => ({
    key: (row.key || '').trim(),
    type: row.type || 'string',
    description: (row.description || '').trim()
  }))
  emit('update:modelValue', {
    ...props.modelValue,
    chatModelId: data.chatModelId || null,
    inputVariables,
    outputVariables,
    systemPrompt: data.systemPrompt,
    systemPromptId: data.systemPromptId || null,
    userPrompt: data.userPrompt,
    temperature: data.temperature,
    streaming: data.streaming,
    outputFormat: data.outputFormat
  })
  queueMicrotask(() => {
    syncing = false
  })
}

function addInputRow() {
  inputRows.value.push({ _id: `in_${++inputRowSeq}`, key: '', value: '' })
  emitUpdate()
}

function removeInputRow(idx) {
  inputRows.value.splice(idx, 1)
  emitUpdate()
}

function addOutputRow() {
  outputRows.value.push(defaultOutputRow(''))
  emitUpdate()
}

function removeOutputRow(idx) {
  outputRows.value.splice(idx, 1)
  if (!outputRows.value.length) {
    outputRows.value = [defaultOutputRow()]
  }
  emitUpdate()
}
</script>

<style scoped lang="scss">
.llm-form__section {
  margin-bottom: 16px;
}

.llm-form__section-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;

  &--output {
    justify-content: space-between;
    gap: 8px;
  }
}

.llm-form__section-title-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
}

.llm-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.llm-form__required {
  color: #f56c6c;
  font-size: 13px;
}

.llm-form__info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.llm-form__add-btn {
  color: #409eff;
  padding: 4px;
}

.llm-form__format-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.llm-form__format-label {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.llm-form__model-select,
.llm-form__format-select {
  width: 120px;
}

.llm-form__prompt-select {
  width: 100%;
  margin-bottom: 8px;
}

.llm-form__output-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 10px;
}

.llm-form__thead {
  display: flex;
  gap: 8px;
  padding: 8px 10px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
}

.llm-form__block {
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }
}

.llm-form__row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
}

.llm-form__row-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  width: auto !important;
}

.llm-form__action-btn {
  padding: 2px;
}

.llm-form__expand {
  padding: 0 10px 10px;
}

.llm-form__expand-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.llm-form__col {
  &--name {
    width: 96px;
    flex-shrink: 0;
  }

  &--type {
    width: 110px;
    flex-shrink: 0;
  }

  &--value {
    flex: 1;
    min-width: 0;
  }

  &--actions {
    width: 28px;
    flex-shrink: 0;
    padding: 0;
  }
}

.llm-form__empty {
  padding: 16px 12px;
  text-align: center;
  font-size: 12px;
  color: #909399;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
}

.llm-form__output-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.llm-form__output-label {
  width: 72px;
  flex-shrink: 0;
  font-size: 12px;
  color: #606266;
}

.llm-form__temp {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;

  .el-slider {
    flex: 1;
  }
}

.llm-form__field--error :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}

.llm-form__warn {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #e6a23c;
}
</style>
