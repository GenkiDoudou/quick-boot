<template>
  <div class="text-process-form">
    <!-- 选择应用 -->
    <div class="text-process-form__section">
      <div class="text-process-form__section-header">
        <span class="text-process-form__section-title">选择应用</span>
      </div>
      <el-select
        v-model="data.processMode"
        size="small"
        class="text-process-form__mode-select"
        @change="emitUpdate"
      >
        <el-option label="字符串拼接" value="join" />
        <el-option label="字符串分隔" value="split" />
      </el-select>
    </div>

    <!-- 输入 -->
    <WfVariableTableSection
      title="输入"
      tooltip="声明本节点可用的字符串变量名，并从上游映射取值；下方模板仅可引用此处定义的参数名"
      :columns="inputColumns"
      :has-rows="inputRows.length > 0"
      empty-text="点击右上角 + 添加输入变量"
      add-title="添加输入"
      @add="addInputRow"
    >
      <div v-for="(row, idx) in inputRows" :key="row._id" class="wf-vt-section__row">
        <el-input
          v-model="row.key"
          size="small"
          placeholder="变量名"
          class="wf-vt-section__col wf-vt-section__col--name"
          @change="syncInputs"
        />
        <ConditionValueField
          v-model="row.value"
          :variable-tree="variableTree"
          placeholder="选择上游变量"
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

    <!-- 字符串拼接 -->
    <div v-if="data.processMode === 'join'" class="text-process-form__section">
      <div class="text-process-form__section-header">
        <span class="text-process-form__section-title">字符串拼接</span>
        <el-tooltip
          content="仅可引用上方「输入」中已声明的变量；数组默认以逗号连接各元素"
          placement="top"
        >
          <el-icon class="text-process-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <TemplateField
        v-model="data.template"
        :variable-tree="localVariableTree"
        :rows="8"
        expandable
        expand-title="字符串拼接"
        :dialog-rows="20"
        :hint="TEMPLATE_HINT"
        placeholder="例如：白色在英语中被称为{{en}}，在日语中被称为{{jp}}。"
        @update:model-value="emitUpdate"
      />
      <p v-if="templateWarnings.length" class="text-process-form__warn">
        未在输入中声明：{{ templateWarnings.join('、') }}
      </p>
    </div>

    <!-- 字符串分隔 -->
    <div v-else class="text-process-form__section">
      <div class="text-process-form__section-header">
        <span class="text-process-form__section-title">字符串分隔</span>
        <el-tooltip content="指定待拆分文本（引用输入参数）与分隔符，输出 items 数组" placement="top">
          <el-icon class="text-process-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <TemplateField
        v-model="data.source"
        :variable-tree="localVariableTree"
        :rows="4"
        expandable
        expand-title="待拆分内容"
        :dialog-rows="14"
        :hint="TEMPLATE_HINT"
        placeholder="例如：{{String1}}"
        @update:model-value="emitUpdate"
      />
      <div class="text-process-form__delimiter">
        <span class="text-process-form__delimiter-label">分隔符</span>
        <el-input
          v-model="data.delimiter"
          size="small"
          placeholder="如 , 或 ..."
          @change="emitUpdate"
        />
      </div>
      <p v-if="sourceWarnings.length" class="text-process-form__warn">
        未在输入中声明：{{ sourceWarnings.join('、') }}
      </p>
    </div>

    <!-- 输出 -->
    <div class="text-process-form__section">
      <div class="text-process-form__section-header">
        <span class="text-process-form__section-title">输出</span>
        <el-tooltip content="本节点执行后可供下游引用的变量" placement="top">
          <el-icon class="text-process-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <div v-if="data.processMode === 'join'" class="text-process-form__output-list">
        <div class="text-process-form__output-row">
          <span class="text-process-form__output-key">output</span>
          <el-tag size="small" type="info" effect="plain">String</el-tag>
        </div>
      </div>
      <div v-else class="text-process-form__output-list">
        <div class="text-process-form__output-row">
          <span class="text-process-form__output-key">items</span>
          <el-tag size="small" type="info" effect="plain">Array</el-tag>
        </div>
        <div class="text-process-form__output-row">
          <span class="text-process-form__output-key">count</span>
          <el-tag size="small" type="info" effect="plain">Number</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { InfoFilled, Minus } from '@element-plus/icons-vue'
import TemplateField from './TemplateField.vue'
import ConditionValueField from './ConditionValueField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import {
  buildLlmPromptVariableTree,
  findUndeclaredLlmPromptReferences
} from '../../composables/useUpstreamVariables'

defineOptions({ name: 'TextProcessForm' })

const TEMPLATE_HINT =
  '可使用 {{变量名}}、{{变量名.子变量名}}、{{变量名[数组索引]}} 引用上方输入参数'

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const inputColumns = [
  { key: 'name', label: '变量名', class: 'wf-vt-section__col--name' },
  { key: 'value', label: '变量值', class: 'wf-vt-section__col--flex' }
]

const data = reactive({
  processMode: 'join',
  template: '',
  source: '',
  delimiter: ','
})

const inputRows = ref([])
let syncing = false
let inputRowSeq = 0

const declaredInputKeys = computed(() =>
  inputRows.value.map((row) => (row.key || '').trim()).filter(Boolean)
)

const localVariableTree = computed(() =>
  buildLlmPromptVariableTree(
    inputRows.value.map((row) => ({ key: row.key, value: row.value }))
  )
)

const templateWarnings = computed(() =>
  data.processMode === 'join'
    ? findUndeclaredLlmPromptReferences(data.template, declaredInputKeys.value)
    : []
)

const sourceWarnings = computed(() =>
  data.processMode === 'split'
    ? findUndeclaredLlmPromptReferences(data.source, declaredInputKeys.value)
    : []
)

watch(
  () => props.modelValue,
  (val) => {
    if (syncing) return
    data.processMode = val?.processMode === 'split' ? 'split' : 'join'
    data.template = val?.template ?? ''
    data.source = val?.source ?? ''
    data.delimiter = val?.delimiter ?? ','

    const inputs = Array.isArray(val?.inputVariables) ? val.inputVariables : []
    inputRows.value = inputs.map((row, idx) => ({
      key: row?.key || '',
      value: row?.value || '',
      _id: inputRows.value[idx]?._id || `in_${++inputRowSeq}`
    }))
    if (!inputRows.value.length) {
      inputRows.value = [{ key: 'String1', value: '', _id: `in_${++inputRowSeq}` }]
    }
  },
  { immediate: true, deep: true }
)

function buildPayload() {
  return {
    ...props.modelValue,
    processMode: data.processMode,
    inputVariables: inputRows.value.map((row) => ({
      key: (row.key || '').trim(),
      value: (row.value || '').trim()
    })),
    template: data.template,
    source: data.source,
    delimiter: data.delimiter
  }
}

function emitUpdate() {
  syncing = true
  emit('update:modelValue', buildPayload())
  queueMicrotask(() => {
    syncing = false
  })
}

function syncInputs() {
  emitUpdate()
}

function addInputRow() {
  const n = inputRows.value.length + 1
  inputRows.value.push({ key: `String${n}`, value: '', _id: `in_${++inputRowSeq}` })
  emitUpdate()
}

function removeInputRow(idx) {
  inputRows.value.splice(idx, 1)
  if (!inputRows.value.length) {
    inputRows.value.push({ key: 'String1', value: '', _id: `in_${++inputRowSeq}` })
  }
  emitUpdate()
}
</script>

<style scoped lang="scss">
.text-process-form__section {
  margin-bottom: 16px;
}

.text-process-form__section-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.text-process-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.text-process-form__info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.text-process-form__mode-select {
  width: 100%;
}

.text-process-form__delimiter {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.text-process-form__delimiter-label {
  flex-shrink: 0;
  font-size: 12px;
  color: #606266;
  width: 48px;
}

.text-process-form__output-list {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.text-process-form__output-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  font-size: 13px;
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }
}

.text-process-form__output-key {
  font-family: Consolas, Monaco, monospace;
  color: #303133;
}

.text-process-form__warn {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #e6a23c;
}
</style>
