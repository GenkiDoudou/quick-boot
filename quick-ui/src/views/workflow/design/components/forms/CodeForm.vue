<template>
  <div class="code-form">
    <!-- 输入参数 -->
    <WfVariableTableSection
      title="输入"
      tooltip="声明代码中使用的变量；代码内通过 params['参数名'] 取值"
      :columns="inputColumns"
      :has-rows="inputRows.length > 0"
      empty-text="点击 + 添加输入参数"
      add-title="添加输入"
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
          placeholder="固定值或引用上游变量"
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

    <!-- 语言与超时 -->
    <div class="code-form__section">
      <div class="code-form__section-header">
        <span class="code-form__section-title">代码</span>
        <el-tooltip
          content="须定义 main 入口并 return 对象；JS: function main({ params })，Python: def main(args)"
          placement="top"
        >
          <el-icon class="code-form__info"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <el-form-item label="语言" class="code-form__field">
        <el-radio-group v-model="data.language" @change="onLanguageChange">
          <el-radio value="javascript">JavaScript</el-radio>
          <el-radio value="python">Python</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="超时(秒)" class="code-form__field">
        <el-input-number
          v-model="data.timeoutSec"
          :min="0.1"
          :max="60"
          :step="0.5"
          :precision="1"
          controls-position="right"
          @change="emitUpdate"
        />
      </el-form-item>
      <WfCodeEditor
        v-model="data.code"
        :language="data.language"
        class="code-form__editor"
        @change="emitUpdate"
      />
      <p v-if="errors.code" class="code-form__error">{{ errors.code }}</p>
    </div>

    <!-- 输出结构 -->
    <WfVariableTableSection
      title="输出"
      tooltip="与代码 return 对象字段名、类型保持一致；异常时另含 isSuccess、errorBody"
      :columns="outputColumns"
      :has-rows="outputRows.length > 0"
      empty-text="点击 + 定义输出字段"
      add-title="添加输出"
      @add="addOutputRow"
    >
      <div v-for="(row, idx) in outputRows" :key="row._id" class="wf-vt-section__row">
        <el-input
          v-model="row.key"
          size="small"
          placeholder="字段名"
          class="wf-vt-section__col wf-vt-section__col--name"
          @change="syncOutputs"
        />
        <el-select
          v-model="row.type"
          size="small"
          class="wf-vt-section__col wf-vt-section__col--type"
          @change="syncOutputs"
        >
          <el-option label="String" value="string" />
          <el-option label="Number" value="number" />
          <el-option label="Boolean" value="boolean" />
          <el-option label="Object" value="object" />
          <el-option label="Array" value="array" />
        </el-select>
        <el-button
          link
          type="danger"
          class="wf-vt-section__col wf-vt-section__col--actions"
          title="删除"
          @click.stop="removeOutputRow(idx)"
        >
          <el-icon :size="16"><Minus /></el-icon>
        </el-button>
      </div>
    </WfVariableTableSection>

    <!-- 异常处理 -->
    <div class="code-form__section">
      <div class="code-form__section-header">
        <span class="code-form__section-title">异常处理</span>
      </div>
      <el-form-item label="处理方式" class="code-form__field">
        <el-radio-group v-model="data.errorMode" @change="emitUpdate">
          <el-radio value="abort">中断流程</el-radio>
          <el-radio value="fallback">返回设定内容</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="data.errorMode === 'fallback'" label="异常返回 JSON" class="code-form__field">
        <el-input
          v-model="fallbackJson"
          type="textarea"
          :rows="4"
          placeholder='{"result": ""}'
          @change="onFallbackChange"
        />
      </el-form-item>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Minus } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import WfVariableTableSection from './shared/WfVariableTableSection.vue'
import WfCodeEditor from './shared/WfCodeEditor.vue'

defineOptions({ name: 'CodeForm' })

const DEFAULT_JS = `function main({ params }) {
  return {
    result: params.input
  };
}`

const DEFAULT_PY = `def main(args):
    params = args.get('params') or {}
    return {
        'result': params.get('input', '')
    }`

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

const outputColumns = [
  { key: 'name', label: '字段名', class: 'wf-vt-section__col--name' },
  { key: 'type', label: '类型', class: 'wf-vt-section__col--type' }
]

const data = reactive({
  language: 'javascript',
  code: DEFAULT_JS,
  timeoutSec: 60,
  errorMode: 'abort',
  inputVariables: [],
  outputVariables: []
})

const inputRows = ref([])
const outputRows = ref([])
const fallbackJson = ref('{"result": ""}')
let inputSeq = 0
let outputSeq = 0
let syncing = false

watch(
  () => props.modelValue,
  (val) => {
    if (syncing) return
    data.language = val?.language === 'python' ? 'python' : 'javascript'
    data.code = val?.code ?? (data.language === 'python' ? DEFAULT_PY : DEFAULT_JS)
    data.timeoutSec = val?.timeoutSec ?? 60
    data.errorMode = val?.errorMode === 'fallback' ? 'fallback' : 'abort'
    data.inputVariables = Array.isArray(val?.inputVariables) ? val.inputVariables : []
    data.outputVariables = Array.isArray(val?.outputVariables) ? val.outputVariables : [{ key: 'result', type: 'string' }]
    inputRows.value = (data.inputVariables.length ? data.inputVariables : [{ key: 'input', value: '' }]).map((row) => ({
      key: row.key || '',
      value: row.value || '',
      _id: `in_${++inputSeq}`
    }))
    outputRows.value = (data.outputVariables.length ? data.outputVariables : [{ key: 'result', type: 'string' }]).map((row) => ({
      key: row.key || '',
      type: row.type || 'string',
      _id: `out_${++outputSeq}`
    }))
    const fb = val?.fallbackOutputs
    if (fb && typeof fb === 'object' && !Array.isArray(fb)) {
      try {
        fallbackJson.value = JSON.stringify(fb, null, 2)
      } catch {
        fallbackJson.value = '{"result": ""}'
      }
    } else {
      fallbackJson.value = '{"result": ""}'
    }
  },
  { immediate: true, deep: true }
)

function syncInputs() {
  data.inputVariables = inputRows.value
    .map((row) => ({ key: (row.key || '').trim(), value: row.value ?? '' }))
    .filter((row) => row.key)
  emitUpdate()
}

function addInputRow() {
  inputRows.value.push({ key: '', value: '', _id: `in_${++inputSeq}` })
}

function removeInputRow(idx) {
  inputRows.value.splice(idx, 1)
  syncInputs()
}

function syncOutputs() {
  data.outputVariables = outputRows.value
    .map((row) => ({ key: (row.key || '').trim(), type: row.type || 'string' }))
    .filter((row) => row.key)
  emitUpdate()
}

function addOutputRow() {
  outputRows.value.push({ key: '', type: 'string', _id: `out_${++outputSeq}` })
}

function removeOutputRow(idx) {
  outputRows.value.splice(idx, 1)
  syncOutputs()
}

function onLanguageChange() {
  if (data.language === 'python') {
    if (!data.code || data.code.includes('function main')) {
      data.code = DEFAULT_PY
    }
  } else if (!data.code || data.code.includes('def main')) {
    data.code = DEFAULT_JS
  }
  emitUpdate()
}

function onFallbackChange() {
  try {
    const parsed = JSON.parse(fallbackJson.value || '{}')
    emitUpdate(parsed)
  } catch {
    ElMessage.warning('异常返回须为合法 JSON')
  }
}

function emitUpdate(fallbackOverride) {
  syncing = true
  let fallbackOutputs = { result: '' }
  if (fallbackOverride) {
    fallbackOutputs = fallbackOverride
  } else if (data.errorMode === 'fallback') {
    try {
      fallbackOutputs = JSON.parse(fallbackJson.value || '{}')
    } catch {
      const prev = props.modelValue?.fallbackOutputs
      fallbackOutputs = prev && typeof prev === 'object' && !Array.isArray(prev) ? prev : { result: '' }
    }
  }
  emit('update:modelValue', {
    ...props.modelValue,
    language: data.language,
    code: data.code,
    timeoutSec: data.timeoutSec,
    errorMode: data.errorMode,
    inputVariables: data.inputVariables,
    outputVariables: data.outputVariables,
    fallbackOutputs
  })
  queueMicrotask(() => {
    syncing = false
  })
}
</script>

<style scoped lang="scss">
.code-form__section {
  margin-top: 14px;
}

.code-form__section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.code-form__section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.code-form__info {
  font-size: 14px;
  color: #c0c4cc;
  cursor: help;
}

.code-form__field {
  margin-bottom: 8px;
}

.code-form__editor {
  width: 100%;
}

.code-form__error {
  margin: 4px 0 0;
  font-size: 12px;
  color: #f56c6c;
}

.wf-vt-section__col--type {
  width: 110px;
  flex-shrink: 0;
  padding-right: 8px;
}
</style>
