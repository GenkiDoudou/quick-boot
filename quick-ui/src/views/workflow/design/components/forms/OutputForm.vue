<template>
  <div class="output-form">
    <p class="output-form__desc">{{ descText }}</p>

    <div class="output-form__mode">
      <button
        type="button"
        class="output-form__mode-btn"
        :class="{ 'output-form__mode-btn--active': outputMode === 'variables' }"
        @click="setOutputMode('variables')"
      >
        返回变量
      </button>
      <button
        type="button"
        class="output-form__mode-btn"
        :class="{ 'output-form__mode-btn--active': outputMode === 'text' }"
        @click="setOutputMode('text')"
      >
        返回文本
      </button>
    </div>

    <!-- 输出变量：两种模式共用 -->
    <div class="output-form__section">
      <div class="output-form__section-header">
        <div class="output-form__section-title-wrap">
          <span class="output-form__section-title">输出变量</span>
          <el-tooltip
            :content="outputMode === 'text'
              ? '返回文本时也可配置变量映射；值支持纯文本或 {{节点.字段}} 引用上游'
              : '将上游变量或文本映射为结构化 JSON 输出；值支持 {{节点.字段}}'"
            placement="top"
          >
            <el-icon class="output-form__info"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <el-button link class="output-form__add-btn" title="添加输出变量" @click.stop="addRow">
          <el-icon :size="16"><Plus /></el-icon>
        </el-button>
      </div>

      <div v-if="rows.length" class="output-form__table">
        <div class="output-form__thead">
          <span class="output-form__col output-form__col--name">变量名</span>
          <span class="output-form__col output-form__col--value">变量值</span>
          <span class="output-form__col output-form__col--actions" />
        </div>
        <div
          v-for="(row, idx) in rows"
          :key="row._id"
          class="output-form__row"
          :class="{ 'output-form__row--error': errors[`outputVariables.${idx}.key`] || errors[`outputVariables.${idx}.value`] }"
        >
          <el-input
            v-model="row.key"
            size="small"
            placeholder="变量名"
            class="output-form__col output-form__col--name"
            @change="sync"
          />
          <ConditionValueField
            v-model="row.value"
            :variable-tree="variableTree"
            placeholder="输入或引用变量"
            class="output-form__col output-form__col--value"
            @update:model-value="sync"
          />
          <el-button
            link
            type="danger"
            class="output-form__col output-form__col--actions"
            title="删除"
            @click.stop="removeRow(idx)"
          >
            <el-icon :size="16"><Minus /></el-icon>
          </el-button>
        </div>
      </div>
      <div v-else class="output-form__empty">暂无输出变量，点击右上角 + 添加</div>
    </div>

    <!-- 返回文本：模板 + 流式 -->
    <div v-if="outputMode === 'text'" class="output-form__section">
      <div class="output-form__section-header">
        <div class="output-form__section-title-wrap">
          <span class="output-form__section-title">回答内容</span>
          <el-tooltip
            content="Dify 式：用 {{节点ID.字段}} 引用上游变量，如 {{llm_1.output}}、{{start_1.input}}"
            placement="top"
          >
            <el-icon class="output-form__info"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="output-form__stream">
          <span class="output-form__stream-label">流式输出</span>
          <el-switch v-model="streaming" size="small" @change="sync" />
        </div>
      </div>

      <TemplateField
        v-model="answerContent"
        :variable-tree="variableTree"
        :rows="5"
        :hint="DIFY_TEMPLATE_HINT"
        placeholder="例如：# {{llm_1.output}}\n\n> {{kb_1.contextText}}"
        :class="{ 'output-form__answer--error': errors.output }"
        @update:model-value="onAnswerChange"
      />
      <p v-if="errors.output" class="output-form__error">{{ errors.output }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { InfoFilled, Plus, Minus } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'OutputForm' })

const DIFY_TEMPLATE_HINT =
  'Dify 式：在文本中插入 {{节点ID.字段}} 引用上游输出，如 {{llm_1.output}}、{{变量名.子字段}}。'

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) },
  /** answer：中间输出节点；end：固定结束节点 */
  variant: { type: String, default: 'answer' }
})

const descText = computed(() =>
  props.variant === 'end'
    ? '工作流固定出口，在此配置 API 最终返回的变量或文本'
    : '中间输出节点，可将上游结果映射为变量或文本（可添加多个）'
)

const emit = defineEmits(['update:modelValue'])

const outputMode = ref('variables')
const answerContent = ref('')
const streaming = ref(false)
const rows = ref([])
let rowSeq = 0
let syncing = false

watch(
  () => props.modelValue,
  (val) => {
    if (syncing) return
    outputMode.value = val?.outputMode === 'text' ? 'text' : 'variables'
    answerContent.value = val?.output ?? ''
    streaming.value = !!val?.streaming

    const vars = val?.outputVariables
    if (Array.isArray(vars)) {
      rows.value = vars.map((row, idx) => ({
        key: row?.key || '',
        value: row?.value || '',
        _id: rows.value[idx]?._id || `row_${++rowSeq}`
      }))
      return
    }
    rows.value = normalizeLegacyOutputVariables(val).map((row) => ({
      ...row,
      _id: row._id || `row_${++rowSeq}`
    }))
  },
  { immediate: true, deep: true }
)

function normalizeLegacyOutputVariables(data) {
  const vars = []
  if (data?.citations) {
    vars.push({ key: 'citations', value: data.citations })
  }
  return vars
}

function setOutputMode(mode) {
  outputMode.value = mode
  sync()
}

function onAnswerChange(val) {
  answerContent.value = val
  sync()
}

function sync() {
  syncing = true
  const outputVariables = rows.value.map((row) => ({
    key: (row.key || '').trim(),
    value: (row.value || '').trim()
  }))

  emit('update:modelValue', {
    ...props.modelValue,
    outputMode: outputMode.value,
    outputVariables,
    output: answerContent.value,
    streaming: outputMode.value === 'text' ? streaming.value : false
  })
  queueMicrotask(() => {
    syncing = false
  })
}

function addRow() {
  rows.value.push({ _id: `row_${++rowSeq}`, key: '', value: '' })
  sync()
}

function removeRow(idx) {
  rows.value.splice(idx, 1)
  sync()
}
</script>

<style scoped lang="scss">
.output-form__desc {
  margin: 0 0 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.output-form__mode {
  display: flex;
  gap: 0;
  margin-bottom: 16px;
  padding: 3px;
  background: #f0f2f5;
  border-radius: 8px;
}

.output-form__mode-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 6px 12px;
  font-size: 13px;
  color: #606266;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;

  &--active {
    background: #fff;
    color: #303133;
    font-weight: 600;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  }
}

.output-form__section {
  margin-bottom: 16px;
}

.output-form__section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 8px;
}

.output-form__section-title-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.output-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.output-form__info {
  font-size: 14px;
  color: #909399;
  cursor: help;
}

.output-form__add-btn {
  color: #409eff;
  padding: 4px;
  flex-shrink: 0;
}

.output-form__stream {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.output-form__stream-label {
  font-size: 12px;
  color: #606266;
}

.output-form__table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.output-form__thead {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
}

.output-form__row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-bottom: 1px solid #f0f2f5;

  &:last-child {
    border-bottom: none;
  }
}

.output-form__col {
  &--name {
    width: 96px;
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

.output-form__empty {
  padding: 20px 12px;
  text-align: center;
  font-size: 12px;
  color: #909399;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
}

.output-form__answer--error :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}

.output-form__error {
  margin: 4px 0 0;
  font-size: 12px;
  color: #f56c6c;
}

.output-form__row--error .output-form__col--name :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #f56c6c inset;
}
</style>
