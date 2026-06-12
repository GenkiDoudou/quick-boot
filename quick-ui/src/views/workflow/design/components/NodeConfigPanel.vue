<template>
  <aside class="wf-config">
    <template v-if="node">
      <div class="wf-config__header">
        <div class="wf-config__header-main">
          <div class="wf-config__title">{{ nodeLabel }}</div>
          <div class="wf-config__id">{{ node.id }}</div>
        </div>
        <el-tooltip v-if="!deletable" content="输入节点不可删除" placement="top">
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
        <el-tab-pane label="设置" name="settings">
          <component
            :is="formComponent"
            v-if="formComponent"
            :model-value="localData"
            :node-id="node?.id"
            :variable-tree="variableTree"
            :errors="fieldErrors"
            @update:model-value="onFormUpdate"
          />
        </el-tab-pane>
        <el-tab-pane label="上次运行" name="lastRun">
          <div v-if="lastRunStep && lastRunStep.nodeId === node.id" class="wf-config__run-io">
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
        </el-tab-pane>
      </el-tabs>
    </template>
    <div v-else class="wf-config__empty">
      <el-empty description="选择画布节点进行配置" :image-size="80" />
      <p class="wf-config__hint">从左侧面板拖拽节点到画布，连线后即可配置属性。</p>
    </div>
  </aside>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { getNodeLabel } from '../nodeMeta'
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
import ListOperatorForm from './forms/ListOperatorForm.vue'

defineOptions({ name: 'NodeConfigPanel' })

const props = defineProps({
  node: { type: Object, default: null },
  variableTree: { type: Array, default: () => [] },
  lastRunStep: { type: Object, default: null },
  fieldErrors: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:node', 'delete-node'])

const activeTab = ref('settings')
const localData = reactive({})

const FORM_MAP = {
  start: StartForm,
  answer: OutputForm,
  llm: LlmForm,
  'knowledge-retrieval': KnowledgeForm,
  'if-else': IfElseForm,
  'template-transform': TemplateForm,
  'variable-assign': VariableAssignForm,
  'variable-aggregator': VariableAggregatorForm,
  'http-request': HttpRequestForm,
  'question-classifier': QuestionClassifierForm,
  'parameter-extractor': ParameterExtractorForm,
  'list-operator': ListOperatorForm
}

const nodeLabel = computed(() => getNodeLabel(props.node?.data?.wfType))
const formComponent = computed(() => FORM_MAP[props.node?.data?.wfType] || null)
const deletable = computed(() => props.node?.data?.wfType !== 'start')

watch(
  () => props.node,
  (n) => {
    activeTab.value = 'settings'
    Object.keys(localData).forEach((k) => delete localData[k])
    if (n?.data) Object.assign(localData, JSON.parse(JSON.stringify(n.data)))
  },
  { immediate: true, deep: true }
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
  padding: 12px;
  overflow-y: auto;
  background: #fff;
  border-left: 1px solid #ebeef5;
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
}

.wf-config__label-field {
  margin-bottom: 8px;
}

.wf-config__tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 12px;
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
  max-height: 160px;
}

.wf-config__dur {
  color: #909399;
  font-size: 12px;
}

.wf-config__empty {
  padding-top: 40px;
}

.wf-config__hint {
  text-align: center;
  font-size: 12px;
  color: #909399;
  padding: 0 16px;
}
</style>
