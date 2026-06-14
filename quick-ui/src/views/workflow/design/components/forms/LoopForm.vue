<template>
  <div class="loop-form">
    <el-form label-position="top" size="small">
      <el-form-item label="循环类型">
        <el-radio-group v-model="local.loopType" @change="sync">
          <el-radio value="count">指定次数</el-radio>
          <el-radio value="array">数组循环</el-radio>
          <el-radio value="infinite">无限循环</el-radio>
        </el-radio-group>
        <div v-if="local.loopType === 'infinite'" class="loop-form__hint">
          无限循环须配合循环体内「终止循环」节点；引擎安全上限 1000 轮。
        </div>
      </el-form-item>

      <template v-if="local.loopType === 'count'">
        <el-form-item label="次数来源">
          <el-radio-group v-model="local.countMode" @change="sync">
            <el-radio value="fixed">固定次数</el-radio>
            <el-radio value="ref">引用上游数值</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="local.countMode === 'fixed'" label="循环次数">
          <el-input-number v-model="local.count" :min="1" :max="1000" controls-position="right" @change="sync" />
        </el-form-item>
        <el-form-item v-else label="次数引用">
          <ConditionValueField
            v-model="local.countSource"
            :variable-tree="variableTree"
            placeholder="如 {{start_1.times}}"
            @update:model-value="sync"
          />
        </el-form-item>
      </template>

      <el-form-item v-else-if="local.loopType === 'array'" label="循环数组">
        <div class="loop-form__hint">变量名供体内引用 {{loopId}}.name；多数组时取最短长度</div>
        <div class="loop-form__table-head">
          <span>变量名</span>
          <span>数组引用</span>
          <span />
        </div>
        <div v-for="(row, idx) in arrayRows" :key="row._id" class="loop-form__row">
          <el-input v-model="row.key" placeholder="如 item" size="small" @change="syncArray" />
          <ConditionValueField
            v-model="row.source"
            :variable-tree="variableTree"
            placeholder="如 {{start_1.items}}"
            class="loop-form__value"
            @update:model-value="syncArray"
          />
          <el-button link type="danger" @click="removeArray(idx)">
            <el-icon><Minus /></el-icon>
          </el-button>
        </div>
        <el-button size="small" @click="addArray">添加数组</el-button>
        <div v-pre class="loop-form__hint">索引固定为 index（从 0 开始）</div>
      </el-form-item>

      <el-form-item label="中间变量">
        <div class="loop-form__hint">在循环体中用「设置变量」更新；类型须与赋值一致</div>
        <div class="loop-form__table-head loop-form__table-head--iv">
          <span>变量名</span>
          <span>类型</span>
          <span>初始值</span>
          <span />
        </div>
        <div v-for="(row, idx) in intermediateRows" :key="row._id" class="loop-form__row loop-form__row--iv">
          <el-input v-model="row.key" placeholder="变量名" size="small" @change="syncIntermediate" />
          <el-select v-model="row.type" size="small" @change="syncIntermediate">
            <el-option v-for="t in IV_TYPES" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <ConditionValueField
            v-model="row.initialValue"
            :variable-tree="variableTree"
            placeholder="初始值"
            class="loop-form__value"
            @update:model-value="syncIntermediate"
          />
          <el-button link type="danger" @click="removeIntermediate(idx)">
            <el-icon><Minus /></el-icon>
          </el-button>
        </div>
        <el-button size="small" @click="addIntermediate">添加中间变量</el-button>
      </el-form-item>

      <el-form-item label="输出">
        <el-form-item label="输出变量名" class="loop-form__sub">
          <el-input v-model="local.outputVariableName" placeholder="默认 results" size="small" @change="sync" />
        </el-form-item>
        <el-form-item label="输出方式" class="loop-form__sub">
          <el-radio-group v-model="local.outputMode" @change="sync">
            <el-radio value="results">循环体结果集合（数组）</el-radio>
            <el-radio value="variable">中间变量最终值</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="local.outputMode === 'results'">
          <el-alert
            v-if="outputNodeStale"
            type="warning"
            :closable="false"
            show-icon
            class="loop-form__stale-alert"
            title="输出节点已失效"
            description="当前配置的体内节点不存在或已被删除，请重新选择；保存/校验时将尝试自动匹配体内节点。"
          />
          <div class="loop-form__table-head loop-form__table-head--out">
            <span>体内节点</span>
            <span>字段名</span>
          </div>
          <el-select
            v-model="local.outputNodeId"
            placeholder="循环体内节点"
            clearable
            filterable
            class="loop-form__select"
            @change="sync"
          >
            <el-option
              v-for="n in bodyOutputNodes"
              :key="n.id"
              :label="`${n.label} · ${n.id}`"
              :value="n.id"
            />
          </el-select>
          <el-input
            v-model="local.outputField"
            placeholder="留空=聚合体内节点全部输出变量；或填单字段如 round"
            size="small"
            class="loop-form__field"
            @change="sync"
          />
        </template>
        <el-form-item v-else label="输出中间变量" class="loop-form__sub">
          <el-select v-model="local.outputVariableKey" placeholder="选择中间变量" clearable @change="sync">
            <el-option v-for="k in intermediateKeyOptions" :key="k" :label="k" :value="k" />
          </el-select>
        </el-form-item>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { Minus } from '@element-plus/icons-vue'
import ConditionValueField from './ConditionValueField.vue'
import { getNodeLabel } from '../../nodeMeta'
import { collectLoopBodyChildIds, isLoopOutputNodeValid, normalizeLoopArrayParameters } from '../../utils/loopUtils'

defineOptions({ name: 'LoopForm' })

const props = defineProps({
  modelValue: { type: Object, required: true },
  nodeId: { type: String, default: '' },
  variableTree: { type: Array, default: () => [] },
  canvasNodes: { type: Array, default: () => [] },
  errors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const IV_TYPES = [
  { value: 'any', label: '任意' },
  { value: 'string', label: '字符串' },
  { value: 'number', label: '数值' },
  { value: 'boolean', label: '布尔' },
  { value: 'array', label: '数组' },
  { value: 'object', label: '对象' }
]

const local = reactive({
  loopType: 'count',
  countMode: 'fixed',
  count: 10,
  countSource: '',
  outputMode: 'results',
  outputVariableName: 'results',
  outputNodeId: '',
  outputField: 'text',
  outputVariableKey: ''
})

const arrayRows = ref([])
const intermediateRows = ref([])

const loopId = computed(() => props.nodeId || 'loop_1')
const bodyId = computed(() => props.modelValue?.bodyId || '')

const bodyOutputNodes = computed(() => {
  const ids = collectLoopBodyChildIds(bodyId.value, props.canvasNodes)
  return (props.canvasNodes || [])
    .filter((n) =>
      ids.has(n.id) &&
      ['answer', 'template-transform', 'text-process', 'variable-assign', 'loop-set-variable', 'llm'].includes(n.data?.wfType)
    )
    .map((n) => ({
      id: n.id,
      label: n.data?.label || getNodeLabel(n.data?.wfType)
    }))
})

const outputNodeStale = computed(() => {
  if (local.outputMode !== 'results') return false
  const outputNodeId = (local.outputNodeId || '').trim()
  if (!outputNodeId) return false
  return !isLoopOutputNodeValid(
    { outputMode: local.outputMode, outputNodeId },
    bodyId.value,
    props.canvasNodes
  )
})

const intermediateKeyOptions = computed(() =>
  intermediateRows.value.map((r) => (r.key || '').trim()).filter(Boolean)
)

watch(
  () => props.modelValue,
  (val) => {
    local.loopType = val?.loopType || 'count'
    local.countMode = val?.countSource ? 'ref' : 'fixed'
    local.count = val?.count ?? 10
    local.countSource = val?.countSource || ''
    local.outputMode = val?.outputMode || 'results'
    local.outputVariableName = val?.outputVariableName || 'results'
    local.outputNodeId = val?.outputNodeId || ''
    local.outputField = val?.outputField || 'text'
    local.outputVariableKey = val?.outputVariableKey || ''

    const arrays = normalizeLoopArrayParameters(val)
    arrayRows.value = arrays.map((item, idx) => ({
      key: item?.key || '',
      source: item?.source || '',
      _id: `arr_${idx}_${item?.key || ''}`
    }))
    if (!arrayRows.value.length) {
      arrayRows.value = [{ key: 'item', source: '', _id: 'arr_0' }]
    }

    const list = Array.isArray(val?.intermediateVariables) ? val.intermediateVariables : []
    intermediateRows.value = list.map((item, idx) => ({
      key: item?.key || '',
      type: item?.type || 'any',
      initialValue: item?.initialValue ?? '',
      _id: `iv_${idx}_${item?.key || ''}`
    }))
    if (!intermediateRows.value.length) {
      intermediateRows.value = [{ key: '', type: 'any', initialValue: '', _id: 'iv_0' }]
    }
  },
  { immediate: true, deep: true }
)

function buildPayload(extra = {}) {
  const arrayParameters = arrayRows.value
    .map((r) => ({
      key: (r.key || '').trim() || 'item',
      source: (r.source || '').trim()
    }))
    .filter((r) => r.key && r.source)
  const intermediateVariables = intermediateRows.value
    .map((r) => ({
      key: (r.key || '').trim(),
      type: r.type || 'any',
      initialValue: r.initialValue ?? ''
    }))
    .filter((r) => r.key)
  return {
    ...props.modelValue,
    ...local,
    countSource: local.countMode === 'ref' ? local.countSource : '',
    arrayParameters,
    arraySource: arrayParameters[0]?.source || '',
    intermediateVariables,
    ...extra
  }
}

function syncArray() {
  emit('update:modelValue', buildPayload())
}

function syncIntermediate() {
  emit('update:modelValue', buildPayload())
}

function sync() {
  emit('update:modelValue', buildPayload())
}

function addArray() {
  arrayRows.value.push({ key: '', source: '', _id: `arr_${Date.now()}` })
}

function removeArray(idx) {
  arrayRows.value.splice(idx, 1)
  if (!arrayRows.value.length) addArray()
  syncArray()
}

function addIntermediate() {
  intermediateRows.value.push({ key: '', type: 'any', initialValue: '', _id: `iv_${Date.now()}` })
}

function removeIntermediate(idx) {
  intermediateRows.value.splice(idx, 1)
  if (!intermediateRows.value.length) addIntermediate()
  syncIntermediate()
}
</script>

<style scoped lang="scss">
.loop-form__hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.45;
  margin-bottom: 8px;
}

.loop-form__table-head {
  display: grid;
  grid-template-columns: 100px 1fr 28px;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;

  &--iv {
    grid-template-columns: 88px 88px 1fr 28px;
  }

  &--out {
    grid-template-columns: 1fr 1fr;
    margin-top: 8px;
  }
}

.loop-form__row {
  display: grid;
  grid-template-columns: 100px 1fr 28px;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  &--iv {
    grid-template-columns: 88px 88px 1fr 28px;
    align-items: flex-start;
  }
}

.loop-form__value {
  min-width: 0;
}

.loop-form__sub {
  margin-bottom: 8px;
}

.loop-form__select {
  width: 100%;
  margin-bottom: 8px;
}

.loop-form__field {
  width: 100%;
}
</style>
