<template>
  <aside class="wf-config">
    <template v-if="node">
      <div class="wf-config__sticky">
        <div class="wf-config__header">
          <div class="wf-config__header-main">
            <div class="wf-config__title">{{ nodeLabel }}</div>
            <div class="wf-config__id">{{ node.id }}</div>
          </div>
          <el-tooltip v-if="!deletable" content="开始/结束节点不可删除" placement="top">
            <el-button type="danger" link size="small" disabled>删除节点</el-button>
          </el-tooltip>
          <el-button v-else type="danger" link size="small" @click="$emit('delete-node')">
            删除节点
          </el-button>
        </div>
        <el-form-item label="显示名" class="wf-config__label-field">
          <el-input v-model="localData.label" size="small" @change="emitData" />
        </el-form-item>
        <el-tabs v-model="activeTab" class="wf-config__tabs">
          <el-tab-pane label="设置" name="settings" />
          <el-tab-pane label="上次运行" name="lastRun" />
        </el-tabs>
      </div>

      <div class="wf-config__scroll">
        <div v-show="activeTab === 'settings'" class="wf-config__tab-body">
          <p v-if="node.data?.wfType === 'loop-body'" class="wf-config__loop-body-hint">
            循环体容器：左侧「开始」连体内首节点，体内末节点连右侧「结束」；顶部入口仅接受循环节点底部「循环体」连线。
          </p>
          <p v-if="node.data?.wfType === 'batch-body'" class="wf-config__loop-body-hint">
            批处理体容器：左侧「开始」连体内首节点，体内末节点连右侧「结束」；顶部入口仅接受批处理节点底部「批处理体」连线。
          </p>
          <component
            :is="formComponent"
            v-if="formComponent"
            :key="`${node.id}_${node.data?.wfType}`"
            :model-value="localData"
            :node-id="node?.id"
            :canvas-nodes="canvasNodes"
            :variable-tree="variableTree"
            :errors="fieldErrors"
            :variant="node.data?.wfType === 'end' ? 'end' : 'answer'"
            @update:model-value="onFormUpdate"
          />
        </div>
        <div v-show="activeTab === 'lastRun'" class="wf-config__tab-body">
          <template v-if="node.data?.wfType === 'loop' && loopIterationTraces.length">
            <div class="wf-config__io-title">各轮迭代 Trace</div>
            <el-collapse class="wf-config__loop-trace">
              <el-collapse-item
                v-for="(trace, idx) in loopIterationTraces"
                :key="`it_${idx}`"
                :title="`第 ${Number(trace.index ?? idx) + 1} 轮`"
                :name="String(idx)"
              >
                <pre class="wf-config__io-pre">{{ formatIo(trace) }}</pre>
              </el-collapse-item>
            </el-collapse>
          </template>
          <template v-else-if="loopBodySteps.length">
            <div class="wf-config__io-title">循环体内各轮执行</div>
            <el-collapse class="wf-config__loop-trace">
              <el-collapse-item
                v-for="(group, idx) in loopBodySteps"
                :key="`body_${group.iteration}_${idx}`"
                :title="`第 ${group.iteration + 1} 轮 · ${group.steps.length} 步`"
                :name="String(group.iteration)"
              >
                <div v-for="(st, sidx) in group.steps" :key="sidx" class="wf-config__loop-step">
                  <div class="wf-config__io-title">{{ st.nodeId }} · {{ st.nodeType }}</div>
                  <pre class="wf-config__io-pre">{{ formatIo(st.outputs) }}</pre>
                </div>
              </el-collapse-item>
            </el-collapse>
          </template>
          <div v-else-if="lastRunStep && lastRunStep.nodeId === node.id" class="wf-config__run-io">
            <div class="wf-config__io-block">
              <div class="wf-config__io-title">输入</div>
              <pre class="wf-config__io-pre">{{ formatIo(lastRunStep.inputs) }}</pre>
            </div>
            <div class="wf-config__io-block">
              <div class="wf-config__io-title">输出</div>
              <pre class="wf-config__io-pre">{{ formatIo(lastRunStep.outputs) }}</pre>
            </div>
            <div v-if="lastRunStep.durationMs != null" class="wf-config__dur">
              耗时 {{ lastRunStep.durationMs }} ms · {{ lastRunStep.status }}
            </div>
          </div>
          <el-empty v-else description="运行后在此查看该节点 I/O" :image-size="64" />
        </div>
      </div>
    </template>
    <div v-else class="wf-config__empty">
      <el-empty description="选择画布节点进行配置" :image-size="80" />
      <p class="wf-config__hint">点击底部「添加节点」添加「输出」等业务节点；所有路径须汇入「结束」节点。</p>
    </div>
  </aside>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { getNodeLabel } from '../nodeMeta'
import { isFixedWorkflowNode } from '../utils/workflowNodePolicy'
import StartForm from './forms/StartForm.vue'
import OutputForm from './forms/OutputForm.vue'
import LlmForm from './forms/LlmForm.vue'
import KnowledgeForm from './forms/KnowledgeForm.vue'
import IfElseForm from './forms/IfElseForm.vue'
import QuestionClassifierForm from './forms/QuestionClassifierForm.vue'
import ParameterExtractorForm from './forms/ParameterExtractorForm.vue'
import VariableAssignForm from './forms/VariableAssignForm.vue'
import VariableAggregatorForm from './forms/VariableAggregatorForm.vue'
import HttpRequestForm from './forms/HttpRequestForm.vue'
import TemplateForm from './forms/TemplateForm.vue'
import TextProcessForm from './forms/TextProcessForm.vue'
import ListOperatorForm from './forms/ListOperatorForm.vue'
import LoopForm from './forms/LoopForm.vue'
import BatchForm from './forms/BatchForm.vue'
import LoopSetVariableForm from './forms/LoopSetVariableForm.vue'
import BreakLoopForm from './forms/BreakLoopForm.vue'
import ContinueLoopForm from './forms/ContinueLoopForm.vue'
import CodeForm from './forms/CodeForm.vue'
import JsonSerializeForm from './forms/JsonSerializeForm.vue'
import JsonDeserializeForm from './forms/JsonDeserializeForm.vue'

defineOptions({ name: 'NodeConfigPanel' })

const props = defineProps({
  node: { type: Object, default: null },
  variableTree: { type: Array, default: () => [] },
  canvasNodes: { type: Array, default: () => [] },
  lastRunStep: { type: Object, default: null },
  traceSteps: { type: Array, default: () => [] },
  fieldErrors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:node', 'delete-node'])

const activeTab = ref('settings')
const localData = reactive({})

const FORM_MAP = {
  start: StartForm,
  end: OutputForm,
  answer: OutputForm,
  llm: LlmForm,
  'knowledge-retrieval': KnowledgeForm,
  'if-else': IfElseForm,
  'template-transform': TemplateForm,
  'text-process': TextProcessForm,
  'variable-assign': VariableAssignForm,
  'variable-aggregator': VariableAggregatorForm,
  'http-request': HttpRequestForm,
  code: CodeForm,
  'json-serialize': JsonSerializeForm,
  'json-deserialize': JsonDeserializeForm,
  'question-classifier': QuestionClassifierForm,
  'parameter-extractor': ParameterExtractorForm,
  'list-operator': ListOperatorForm,
  loop: LoopForm,
  batch: BatchForm,
  'loop-set-variable': LoopSetVariableForm,
  'break-loop': BreakLoopForm,
  'continue-loop': ContinueLoopForm
}

const nodeLabel = computed(() => getNodeLabel(props.node?.data?.wfType))
const formComponent = computed(() => FORM_MAP[props.node?.data?.wfType] || null)
const deletable = computed(() => !isFixedWorkflowNode(props.node))

const loopIterationTraces = computed(() => {
  if (props.node?.data?.wfType !== 'loop') return []
  const outputs = props.lastRunStep?.nodeId === props.node?.id ? props.lastRunStep?.outputs : null
  const traces = outputs?.iterationTraces
  return Array.isArray(traces) ? traces : []
})

const loopBodySteps = computed(() => {
  const nodeId = props.node?.id
  if (!nodeId || !props.node?.parentNode) return []
  const bodyParent = props.canvasNodes?.find((n) => n.id === props.node?.parentNode)
  if (bodyParent?.data?.wfType !== 'loop-body') return []
  const related = (props.traceSteps || []).filter((s) => s.nodeId === nodeId && s.inputs?._loop)
  const map = new Map()
  related.forEach((s) => {
    const it = Number(s.inputs._loop.iteration)
    if (!map.has(it)) map.set(it, [])
    map.get(it).push(s)
  })
  return [...map.entries()]
    .sort((a, b) => a[0] - b[0])
    .map(([iteration, steps]) => ({ iteration, steps }))
})

watch(
  () => props.node?.id,
  (id) => {
    activeTab.value = 'settings'
    Object.keys(localData).forEach((k) => delete localData[k])
    if (id && props.node?.data) {
      Object.assign(localData, JSON.parse(JSON.stringify(props.node.data)))
    }
  },
  { immediate: true }
)

function emitData() {
  if (!props.node) return
  emit('update:node', { ...props.node, data: { ...localData } })
}

function onFormUpdate(data) {
  Object.assign(localData, data)
  emitData()
}

function formatIo(val) {
  if (val == null) return '—'
  try {
    return JSON.stringify(val, null, 2)
  } catch {
    return String(val)
  }
}
</script>

<style scoped lang="scss">
.wf-config {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 0;
  overflow: hidden;
  background: #fff;
  border-left: 1px solid #ebeef5;
}

.wf-config__sticky {
  flex-shrink: 0;
  padding: 12px 12px 0;
  border-bottom: 1px solid #f0f2f5;
  background: #fff;
}

.wf-config__scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 12px 16px;
}

.wf-config__tab-body {
  padding-top: 4px;
}

.wf-config__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}

.wf-config__header-main {
  min-width: 0;
  flex: 1;
}

.wf-config__title {
  font-size: 15px;
  font-weight: 600;
  color: #0a2463;
}

.wf-config__id {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  word-break: break-all;
}

.wf-config__label-field {
  margin-bottom: 8px;
}

.wf-config__tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__content) {
    display: none;
  }
}

.wf-config__run-io {
  font-size: 12px;
}

.wf-config__io-block {
  margin-bottom: 12px;
}

.wf-config__io-title {
  font-weight: 600;
  color: #0a2463;
  margin-bottom: 4px;
}

.wf-config__io-pre {
  margin: 0;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 11px;
  overflow: auto;
  max-height: 240px;
  white-space: pre-wrap;
  word-break: break-word;
  user-select: text;
}

.wf-config__dur {
  color: #909399;
  font-size: 12px;
}

.wf-config__empty {
  padding: 40px 12px 16px;
  overflow-y: auto;
}

.wf-config__hint {
  text-align: center;
  font-size: 12px;
  color: #909399;
  padding: 0 16px;
}

.wf-config__loop-body-hint {
  margin: 8px 0 12px;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
  background: #ecf5ff;
  border-radius: 6px;
  border: 1px solid #d9ecff;
}

.wf-config__loop-trace {
  margin-bottom: 12px;
}

.wf-config__loop-step {
  margin-bottom: 10px;
}
</style>
