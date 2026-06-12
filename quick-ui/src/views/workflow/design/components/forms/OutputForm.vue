<template>
  <div class="output-form">
    <p class="output-form__desc">工作流的最终节点，用于返回工作流运行后的结果信息</p>

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

    <!-- 返回变量：结构化 JSON 输出 -->
    <div v-if="outputMode === 'variables'" class="output-form__section">
      <div class="output-form__section-header">
        <div class="output-form__section-title-wrap">
          <span class="output-form__section-title">输出变量</span>
          <el-tooltip content="将上游变量映射为结构化输出字段" placement="top">
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
          <VariableSelectField
            v-model="row.value"
            :variable-tree="variableTree"
            class="output-form__col output-form__col--value"
            @update:model-value="sync"
          />
          <el-button link type="danger" class="output-form__col output-form__col--actions" title="删除" @click="removeRow(idx)">
            <el-icon :size="16"><Minus /></el-icon>
          </el-button>
        </div>
      </div>
      <div v-else class="output-form__empty">暂无输出变量，点击右上角 + 添加</div>
    </div>

    <!-- 返回文本：模板渲染为一段话 -->
    <div v-if="outputMode === 'text'" class="output-form__section">
      <div class="output-form__section-header">
        <div class="output-form__section-title-wrap">
          <span class="output-form__section-title">回答内容</span>
          <el-tooltip
            content="可使用 {{变量名}}、{{变量名.子变量名}}、{{变量名[0]}} 引用上游变量"
            placement="top"
          >
            <el-icon class="output-form__info"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="output-form__stream">
          <span class="output-form__stream-label">流式输出</span>
          <el-tooltip content="开启后，运行调试时以流式方式展示回答文本" placement="top">
            <el-icon class="output-form__info"><InfoFilled /></el-icon>
          </el-tooltip>
          <el-switch v-model="streaming" size="small" @change="sync" />
        </div>
      </div>

      <TemplateField
        v-model="answerContent"
        :variable-tree="variableTree"
        :rows="5"
        placeholder="可以使用{{变量名}}、{{变量名.子变量名}}、{{变量名[数组索引]}}的方式引用输出参数中的变量"
        :class="{ 'output-form__answer--error': errors.output }"
        @update:model-value="onAnswerChange"
      />
      <p v-if="errors.output" class="output-form__error">{{ errors.output }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { InfoFilled, Plus, Minus } from '@element-plus/icons-vue'
import VariableSelectField from './VariableSelectField.vue'
import TemplateField from './TemplateField.vue'

defineOptions({ name: 'OutputForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  variableTree: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const outputMode = ref('variables')
const answerContent = ref('')
const streaming = ref(false)
const rows = ref([])
let rowSeq = 0

watch(
  () => props.modelValue,
  (val) => {
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

/**
 * 兼容旧版仅 output 模板配置（无 outputVariables 时）。
 * @param {object} data
 * @returns {Array<{ key: string, value: string }>}
 */
function normalizeLegacyOutputVariables(data) {
  const vars = []
  if (data?.output && !data?.outputVariables) {
    return vars
  }
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
  gap: 4px;
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
