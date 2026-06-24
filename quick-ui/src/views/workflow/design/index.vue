<template>
  <div class="wf-design">
    <DesignToolbar
      :name="workflowName"
      :save-status="saveStatus"
      :validating="validating"
      :running="running"
      :publishing="publishing"
      :template-mode="isTemplateMode"
      :back-label="isTemplateMode ? '返回模板列表' : '返回工作流列表'"
      :show-minimap="showMinimap"
      @back="goBack"
      @validate="handleValidate"
      @test-run="openRunPanel"
      @publish="handlePublish"
      @optimize-layout="handleOptimizeLayout"
      @export-template="openExportTemplate"
      @toggle-minimap="showMinimap = !showMinimap"
    />

    <div class="wf-design__body" v-loading="loading">
      <main ref="canvasWrapRef" class="wf-design__canvas-wrap" @drop="onDrop" @dragover.prevent>
        <VueFlow
          v-model:nodes="nodes"
          v-model:edges="edges"
          :node-types="flowNodeTypes"
          :edge-types="flowEdgeTypes"
          :default-edge-options="defaultEdgeOptions"
          :delete-key-code="null"
          :nodes-deletable="false"
          :edges-deletable="false"
          :edges-selectable="true"
          fit-view-on-init
          :elevate-nodes-on-select="false"
          @node-drag-stop="onNodeDragStop"
          @node-click="onNodeClick"
          @node-context-menu="onNodeContextMenu"
          @edge-click="onEdgeClick"
          @edge-context-menu="onEdgeContextMenu"
          @pane-click="onPaneClick"
          @pane-context-menu="closeContextMenu"
        >
          <Background pattern-color="#dfe3ea" :gap="20" />
          <Controls />
          <MiniMap v-if="showMinimap" pannable zoomable />
        </VueFlow>
        <CanvasNodeAddBar :container-kind="containerKind" @add-node="addNodeByType" />
      </main>

      <template v-if="isTemplateMode">
        <div
          class="wf-design__resize-handle"
          :class="{ 'wf-design__resize-handle--active': configResizing }"
          title="拖动调整配置面板宽度"
          @mousedown="startConfigResize"
        />
        <NodeConfigPanel
          class="wf-design__config"
          :style="{ width: `${configPanelWidth}px` }"
          :node="selectedNode"
          :variable-tree="variableTree"
          :canvas-nodes="canvasNodeSummaries"
          :last-run-step="lastRunStep"
          :trace-steps="traceSteps"
          :field-errors="fieldErrors"
          @update:node="onNodeUpdate"
          @delete-node="handleDeleteSelectedNode"
        />
      </template>

      <template v-else>
        <div
          class="wf-design__resize-handle"
          :class="{ 'wf-design__resize-handle--active': configResizing }"
          :title="runPanelVisible ? '拖动调整试运行面板宽度' : '拖动调整配置面板宽度'"
          @mousedown="startConfigResize"
        />

        <RunPanel
          v-if="runPanelVisible"
          v-model:visible="runPanelVisible"
          v-model:stream-enabled="streamEnabled"
          class="wf-design__config"
          :style="{ width: `${configPanelWidth}px` }"
          :start-inputs="startInputs"
          :running="running"
          :trace-steps="traceSteps"
          :stream-text="streamText"
          :run-info="runInfo"
          :last-run-inputs="lastRunInputs"
          @run="handleRunTest"
        />

        <NodeConfigPanel
          v-else
          class="wf-design__config"
          :style="{ width: `${configPanelWidth}px` }"
          :node="selectedNode"
          :variable-tree="variableTree"
          :canvas-nodes="canvasNodeSummaries"
          :last-run-step="lastRunStep"
          :trace-steps="traceSteps"
          :field-errors="fieldErrors"
          @update:node="onNodeUpdate"
          @delete-node="handleDeleteSelectedNode"
        />
      </template>
    </div>

      <NodeContextMenu
        :visible="contextMenu.visible"
        :x="contextMenu.x"
        :y="contextMenu.y"
        :deletable="contextMenuDeletable"
        :copyable="contextMenuCopyable"
        :fixed-tip="contextMenuFixedTip"
        @close="closeContextMenu"
      @rename="handleContextMenuRename"
      @copy="handleContextMenuCopy"
      @delete="handleContextMenuDelete"
    />

    <EdgeContextMenu
      :visible="edgeContextMenu.visible"
      :x="edgeContextMenu.x"
      :y="edgeContextMenu.y"
      @close="closeEdgeContextMenu"
      @delete="handleEdgeContextMenuDelete"
    />

    <c7-dialog v-model="exportVisible" title="导出为模板" :on-confirm="submitExportTemplate" width="520px">
      <el-form ref="exportFormRef" :model="exportForm" :rules="exportRules" label-width="100px">
        <el-form-item label="模板编码" prop="code">
          <el-input v-model="exportForm.code" maxlength="64" placeholder="小写字母、数字、连字符" />
        </el-form-item>
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="exportForm.name" maxlength="128" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="exportForm.description" type="textarea" :rows="2" maxlength="512" />
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, markRaw, nextTick, onBeforeUnmount, onMounted, provide, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'

import DesignToolbar from './components/DesignToolbar.vue'
import CanvasNodeAddBar from './components/CanvasNodeAddBar.vue'
import NodeConfigPanel from './components/NodeConfigPanel.vue'
import RunPanel from './components/RunPanel.vue'
import NodeContextMenu from './components/NodeContextMenu.vue'
import EdgeContextMenu from './components/EdgeContextMenu.vue'
import WorkflowNodeCard from './nodes/WorkflowNodeCard.vue'
import WorkflowEdge from './components/edges/WorkflowEdge.vue'
import { createVueFlowNode, cloneVueFlowNode, graphToVueFlow, vueFlowToGraph } from './graphConverter'
import {
  createLoopBodyChildNode,
  createLoopNodePair,
  ensureLoopGraphStructure,
  isLoopBodyAnchorType,
  isLoopBodyOnlyNodeType,
  loopBodyAnchorGuardMessage
} from './utils/loopUtils'
import {
  createBatchBodyChildNode,
  createBatchNodePair,
  ensureBatchGraphStructure,
  isForbiddenInBatchBody
} from './utils/batchUtils'
import {
  expandContainerDeletionIds,
  isContainerInternalCanvasNode,
  resolveActiveContainerBody,
  validateContainerConnection
} from './utils/containerUtils'
import {
  fixedNodeGuardMessage,
  isFixedWorkflowNode,
  isFixedWorkflowNodeType
} from './utils/workflowNodePolicy'
import { useAutoSave } from './composables/useAutoSave'
import { usePanelResize } from './composables/usePanelResize'
import { useWorkflowRun } from './composables/useWorkflowRun'
import { useUpstreamVariables, collectUpstreamNodeIds } from './composables/useUpstreamVariables'
import { getWorkflow, publishWorkflow, saveGraph, validateGraph } from '@/api/workflow'
import {
  addWorkflowTemplate,
  getWorkflowTemplateInfo,
  saveWorkflowTemplateGraph,
  validateWorkflowTemplateGraph
} from '@/api/workflow/template'
import { optimizeWorkflowLayout } from './utils/workflowLayout'
import { buildCanvasNodeSummaries, buildGraphStructureFingerprint } from './utils/graphPerfUtils'

defineOptions({ name: 'WfWorkflowDesign' })

const route = useRoute()
const router = useRouter()
const { project, addEdges, onConnect, setCenter, fitView } = useVueFlow()

/** 模板设计模式（路由 meta.templateMode 或 name=WfTemplateDesign） */
const isTemplateMode = computed(() => route.meta?.templateMode === true || route.name === 'WfTemplateDesign')
/** 工作流 ID 必须为字符串，避免雪花 ID 精度丢失 */
const workflowId = computed(() => (isTemplateMode.value ? '' : String(route.params.id || '')))
const templateId = computed(() => (isTemplateMode.value ? String(route.params.templateId || '') : ''))
const saveTargetId = computed(() => (isTemplateMode.value ? templateId.value : workflowId.value))
const graphReady = ref(false)
const loading = ref(false)
const validating = ref(false)
const publishing = ref(false)
const workflowName = ref('')
const nodes = ref([])
const edges = ref([])
const selectedNodeId = ref(null)
const selectedEdgeId = ref(null)
const fieldErrors = ref({})
const contextMenu = ref({ visible: false, x: 0, y: 0, nodeId: null })
const edgeContextMenu = ref({ visible: false, x: 0, y: 0, edgeId: null })
const canvasWrapRef = ref(null)
const showMinimap = ref(false)
const exportVisible = ref(false)
const exportFormRef = ref(null)
const exportForm = ref({ code: '', name: '', description: '' })
const exportRules = {
  code: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }]
}

const {
  width: configPanelWidth,
  resizing: configResizing,
  startResize: startConfigResize
} = usePanelResize({ initial: 320, min: 260, max: 560, direction: 'left' })

const flowNodeTypes = { workflow: markRaw(WorkflowNodeCard) }
const flowEdgeTypes = { workflow: markRaw(WorkflowEdge) }
const defaultEdgeOptions = { type: 'workflow', animated: false, style: { stroke: '#94b8ff' } }

onConnect((connection) => {
  const err = validateContainerConnection(connection, nodes.value)
  if (err) {
    ElMessage.warning(err)
    return
  }
  addEdges([
    {
      ...connection,
      id: `e_${connection.source}_${connection.target}_${Date.now()}`,
      type: 'workflow',
      animated: false,
      style: { stroke: '#94b8ff' }
    }
  ])
})

const selectedNode = computed(() => nodes.value.find((n) => n.id === selectedNodeId.value) || null)

const containerKind = computed(() => resolveActiveContainerBody(selectedNode.value, nodes.value)?.kind ?? null)

const contextMenuNode = computed(() =>
  nodes.value.find((n) => n.id === contextMenu.value.nodeId) || null
)

const contextMenuDeletable = computed(() => isNodeDeletable(contextMenuNode.value))
const contextMenuCopyable = computed(() => isNodeCopyable(contextMenuNode.value))
const contextMenuFixedTip = computed(() => {
  const wfType = contextMenuNode.value?.data?.wfType
  const anchorMsg = loopBodyAnchorGuardMessage(wfType)
  if (anchorMsg) return anchorMsg
  if (!isFixedWorkflowNodeType(wfType)) return ''
  return fixedNodeGuardMessage(wfType)
})

const { variableTree } = useUpstreamVariables(selectedNodeId, nodes, edges)

const graphStructureFingerprint = computed(() =>
  buildGraphStructureFingerprint(nodes.value, edges.value)
)

const canvasNodeSummaries = shallowRef([])
watch(
  graphStructureFingerprint,
  () => {
    canvasNodeSummaries.value = buildCanvasNodeSummaries(nodes.value)
  },
  { immediate: true }
)

const startInputs = computed(() => {
  const start = nodes.value.find((n) => n.data?.wfType === 'start')
  return Array.isArray(start?.data?.inputs) ? start.data.inputs : []
})

function currentGraph() {
  const loopSynced = ensureLoopGraphStructure(nodes.value, edges.value)
  const synced = ensureBatchGraphStructure(loopSynced.nodes, loopSynced.edges)
  edges.value = synced.edges
  return vueFlowToGraph(synced.nodes, synced.edges)
}

const { saveStatus, markDirty } = useAutoSave({
  targetId: saveTargetId,
  getGraph: currentGraph,
  structureFingerprint: graphStructureFingerprint,
  enabled: graphReady,
  saveFn: (id, graph) => {
    if (isTemplateMode.value) {
      return saveWorkflowTemplateGraph({ templateId: id, graph })
    }
    return saveGraph({ workflowId: id, graph })
  }
})

function onNodeDragStop() {
  markDirty()
}

const {
  running,
  runPanelVisible,
  traceSteps,
  streamText,
  lastRunStep,
  lastRunInputs,
  streamEnabled,
  runInfo,
  runTest,
  focusTraceStep,
  toggleNodeTraceExpanded,
  clearAllRunStatus
} = useWorkflowRun({
  workflowId,
  nodes,
  focusNode,
  getGraph: currentGraph
})

provide('wfToggleNodeTrace', toggleNodeTraceExpanded)
provide(
  'wfRunDebugActive',
  computed(() => runPanelVisible.value)
)

watch(runPanelVisible, (visible) => {
  if (!visible) {
    clearAllRunStatus()
  }
})

function loadWorkflow() {
  loading.value = true
  graphReady.value = false
  getWorkflow(workflowId.value)
    .then((res) => {
      const data = res.data || {}
      workflowName.value = data.name || ''
      const converted = graphToVueFlow(data.draftGraph)
      nodes.value = converted.nodes
      edges.value = converted.edges
    })
    .finally(() => {
      loading.value = false
      graphReady.value = true
    })
}

function loadTemplate() {
  loading.value = true
  graphReady.value = false
  getWorkflowTemplateInfo(templateId.value)
    .then((res) => {
      const data = res.data || {}
      workflowName.value = data.name ? `模板：${data.name}` : '模板设计'
      const converted = graphToVueFlow(data.graph)
      nodes.value = converted.nodes
      edges.value = converted.edges
    })
    .finally(() => {
      loading.value = false
      graphReady.value = true
    })
}

function clearValidationErrors() {
  nodes.value = nodes.value.map((n) => ({
    ...n,
    data: { ...n.data, validationError: null }
  }))
  fieldErrors.value = {}
}

function applyValidationError(message) {
  clearValidationErrors()
  const msg = message || '校验失败'
  const match = msg.match(/节点\s+([a-zA-Z0-9_-]+)/)
  if (match) {
    const nodeId = match[1]
    nodes.value = nodes.value.map((n) =>
      n.id === nodeId ? { ...n, data: { ...n.data, validationError: msg } } : n
    )
    selectedNodeId.value = nodeId
    focusNode(nodeId)
    fieldErrors.value = { _global: msg }
  }
}

function handleValidate() {
  validating.value = true
  clearValidationErrors()
  const graph = currentGraph()
  const request = isTemplateMode.value
    ? validateWorkflowTemplateGraph({ graph })
    : validateGraph({ graph })
  request
    .then(() => {
      ElMessage.success('校验通过')
      clearValidationErrors()
    })
    .catch((err) => {
      const msg = err?.message || err?.msg || String(err)
      applyValidationError(msg)
      ElMessage.error(msg)
    })
    .finally(() => {
      validating.value = false
    })
}

function handlePublish() {
  ElMessageBox.confirm('确认发布当前工作流？', '发布确认', { type: 'warning' })
    .then(() => {
      publishing.value = true
      return publishWorkflow({ workflowId: workflowId.value })
    })
    .then(() => ElMessage.success('发布成功'))
    .finally(() => {
      publishing.value = false
    })
}

function openRunPanel() {
  runPanelVisible.value = true
}

function hasValidOutputConfig(node) {
  const mode = node.data?.outputMode === 'text' ? 'text' : 'variables'
  const vars = node.data?.outputVariables
  const hasValidVar = Array.isArray(vars) && vars.some((v) => v?.key && v?.value)
  if (mode === 'text') {
    return !!(node.data?.output || '').trim() || hasValidVar
  }
  return hasValidVar
}

function handleRunTest(inputs) {
  const endNode = nodes.value.find((n) => n.data?.wfType === 'end')
  const start = nodes.value.find((n) => n.data?.wfType === 'start')
  if (!start) {
    ElMessage.warning('画布缺少开始节点')
    return
  }
  if (!endNode) {
    ElMessage.warning('画布缺少结束节点')
    return
  }
  const upstreamEnd = collectUpstreamNodeIds(endNode.id, edges.value)
  if (!upstreamEnd.has(start.id)) {
    ElMessage.warning('结束节点须与开始节点处于同一连通路径上')
    return
  }
  if (!hasValidOutputConfig(endNode)) {
    ElMessage.warning('请先在结束节点配置「返回变量」或「返回文本」')
    return
  }
  runTest(inputs, streamEnabled.value)
}

function onFocusTraceStep(step) {
  focusTraceStep(step)
}

function focusNode(nodeId) {
  const node = nodes.value.find((n) => n.id === nodeId)
  if (!node) return
  selectedNodeId.value = nodeId
  const x = (node.position?.x ?? 0) + 120
  const y = (node.position?.y ?? 0) + 40
  setCenter(x, y, { zoom: 1.1, duration: 300 })
}

/**
 * 自动优化画布节点排布（dagre LR，参考 @vue-flow/core layout 示例）。
 */
function handleOptimizeLayout() {
  if (!nodes.value.length) {
    ElMessage.warning('画布暂无节点')
    return
  }
  const { nodes: laidOut } = optimizeWorkflowLayout(nodes.value, edges.value)
  nodes.value = laidOut
  nextTick(() => {
    fitView({ padding: 0.18, duration: 320 })
  })
  ElMessage.success('布局已优化')
}

function onNodeUpdate(updated) {
  nodes.value = nodes.value.map((n) =>
    n.id === updated.id ? { ...updated, data: { ...updated.data } } : n
  )
}

function onDrop(event) {
  const type = event.dataTransfer.getData('application/vueflow')
  if (!type) return
  if (isFixedWorkflowNodeType(type)) {
    ElMessage.warning(fixedNodeGuardMessage(type))
    return
  }
  const bounds = event.currentTarget.getBoundingClientRect()
  const position = project({
    x: event.clientX - bounds.left,
    y: event.clientY - bounds.top
  })
  addNodeAtPosition(type, position)
}

/**
 * 在画布可视区域中心添加节点（底部「添加节点」按钮）。
 * @param {string} type
 */
function addNodeByType(type) {
  if (isFixedWorkflowNodeType(type)) {
    ElMessage.warning(fixedNodeGuardMessage(type))
    return
  }
  const wrap = canvasWrapRef.value
  if (!wrap) return
  const rect = wrap.getBoundingClientRect()
  const position = project({
    x: rect.width / 2,
    y: rect.height / 2
  })
  addNodeAtPosition(type, position)
}

/**
 * @param {string} type
 * @param {{ x: number, y: number }} position
 */
function addNodeAtPosition(type, position) {
  if (type === 'loop') {
    const { nodes: pairNodes, edges: pairEdges } = createLoopNodePair(position, nodes.value)
    nodes.value = [...nodes.value, ...pairNodes]
    edges.value = [...edges.value, ...pairEdges]
    selectedNodeId.value = pairNodes[0].id
    focusNode(pairNodes[0].id)
    return
  }
  if (type === 'batch') {
    const { nodes: pairNodes, edges: pairEdges } = createBatchNodePair(position, nodes.value)
    nodes.value = [...nodes.value, ...pairNodes]
    edges.value = [...edges.value, ...pairEdges]
    selectedNodeId.value = pairNodes[0].id
    focusNode(pairNodes[0].id)
    return
  }

  const container = resolveActiveContainerBody(selectedNode.value, nodes.value)
  if (isLoopBodyOnlyNodeType(type) && container?.kind !== 'loop') {
    ElMessage.warning('终止循环、继续循环、设置变量仅能在循环体内添加')
    return
  }
  if (container?.kind === 'loop') {
    if (type === 'loop' || type === 'batch') {
      ElMessage.warning('循环体内不可嵌套循环或批处理')
      return
    }
    const bodyId = container.bodyId
    const childPos = {
      x: Math.max(24, position.x - (nodes.value.find((n) => n.id === bodyId)?.position?.x ?? 0)),
      y: Math.max(24, position.y - (nodes.value.find((n) => n.id === bodyId)?.position?.y ?? 0))
    }
    const node = createLoopBodyChildNode(type, childPos, bodyId, nodes.value)
    nodes.value = [...nodes.value, node]
    selectedNodeId.value = node.id
    focusNode(node.id)
    return
  }
  if (container?.kind === 'batch') {
    if (type === 'batch' || type === 'loop') {
      ElMessage.warning('批处理体内不可嵌套批处理或循环')
      return
    }
    if (isForbiddenInBatchBody(type)) {
      ElMessage.warning('批处理体内不可添加该节点类型')
      return
    }
    const bodyId = container.bodyId
    const childPos = {
      x: Math.max(24, position.x - (nodes.value.find((n) => n.id === bodyId)?.position?.x ?? 0)),
      y: Math.max(24, position.y - (nodes.value.find((n) => n.id === bodyId)?.position?.y ?? 0))
    }
    const node = createBatchBodyChildNode(type, childPos, bodyId, nodes.value)
    nodes.value = [...nodes.value, node]
    selectedNodeId.value = node.id
    focusNode(node.id)
    return
  }

  const node = createVueFlowNode(type, position, nodes.value)
  nodes.value = [...nodes.value, node]
  selectedNodeId.value = node.id
  focusNode(node.id)
}

function onNodeClick({ node }) {
  closeContextMenu()
  closeEdgeContextMenu()
  selectedEdgeId.value = null
  edges.value = edges.value.map((e) => ({ ...e, selected: false }))
  selectedNodeId.value = node.id
}

function onEdgeClick({ edge }) {
  closeContextMenu()
  closeEdgeContextMenu()
  selectedNodeId.value = null
  selectedEdgeId.value = edge.id
  edges.value = edges.value.map((e) => ({ ...e, selected: e.id === edge.id }))
}

function onEdgeContextMenu({ event, edge }) {
  event.preventDefault()
  closeContextMenu()
  selectedNodeId.value = null
  selectedEdgeId.value = edge.id
  edges.value = edges.value.map((e) => ({ ...e, selected: e.id === edge.id }))
  edgeContextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    edgeId: edge.id
  }
}

function closeEdgeContextMenu() {
  edgeContextMenu.value.visible = false
}

/**
 * 删除画布连线。
 * @param {string} edgeId
 * @param {{ silent?: boolean }} [options]
 */
function deleteEdge(edgeId, options = {}) {
  if (!edgeId) return false
  const exists = edges.value.some((e) => e.id === edgeId)
  if (!exists) return false
  edges.value = edges.value.filter((e) => e.id !== edgeId)
  if (selectedEdgeId.value === edgeId) {
    selectedEdgeId.value = null
  }
  if (!options.silent) {
    ElMessage.success('已删除连线')
  }
  return true
}

function handleEdgeContextMenuDelete() {
  const edgeId = edgeContextMenu.value.edgeId
  closeEdgeContextMenu()
  if (edgeId) {
    deleteEdge(edgeId)
  }
}

provide('deleteWorkflowEdge', (edgeId) => deleteEdge(edgeId))

function onNodeContextMenu({ event, node }) {
  event.preventDefault()
  closeEdgeContextMenu()
  selectedEdgeId.value = null
  edges.value = edges.value.map((e) => ({ ...e, selected: false }))
  selectedNodeId.value = node.id
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    nodeId: node.id
  }
}

function closeContextMenu() {
  contextMenu.value.visible = false
}

function handleContextMenuRename() {
  const node = contextMenuNode.value
  closeContextMenu()
  if (node) renameNode(node.id)
}

function handleContextMenuCopy() {
  const node = contextMenuNode.value
  closeContextMenu()
  if (node) duplicateNode(node.id)
}

function handleContextMenuDelete() {
  const node = contextMenuNode.value
  closeContextMenu()
  if (!node) return
  if (!isNodeDeletable(node)) {
    ElMessage.warning(fixedNodeGuardMessage(node.data?.wfType))
    return
  }
  const label = node.data?.label || node.id
  ElMessageBox.confirm(`确认删除节点「${label}」？相关连线将一并移除。`, '删除节点', {
    type: 'warning',
    confirmButtonText: '删除',
    confirmButtonClass: 'el-button--danger'
  })
    .then(() => deleteNodes([node.id]))
    .catch(() => {})
}

function onPaneClick() {
  closeContextMenu()
  closeEdgeContextMenu()
  selectedNodeId.value = null
  selectedEdgeId.value = null
  edges.value = edges.value.map((e) => ({ ...e, selected: false }))
}

/** 开始/结束为内置节点；loop-body / batch-body 须随父节点一并删除；循环体锚点不可单独删除 */
function isNodeDeletable(node) {
  if (isFixedWorkflowNode(node)) return false
  if (isLoopBodyAnchorType(node?.data?.wfType)) return false
  if (node?.data?.wfType === 'loop-body' || node?.data?.wfType === 'batch-body') return true
  return true
}

function isNodeCopyable(node) {
  if (isFixedWorkflowNode(node)) return false
  if (isLoopBodyAnchorType(node?.data?.wfType)) return false
  if (isContainerInternalCanvasNode(node)) return false
  return true
}

/**
 * 重命名节点显示名。
 * @param {string} nodeId
 */
function renameNode(nodeId) {
  const node = nodes.value.find((n) => n.id === nodeId)
  if (!node) return
  const current = node.data?.label || ''
  ElMessageBox.prompt('请输入节点显示名称', '重命名', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: current,
    inputValidator: (value) => {
      if (!value?.trim()) return '名称不能为空'
      return true
    }
  })
    .then(({ value }) => {
      const label = value.trim()
      nodes.value = nodes.value.map((n) =>
        n.id === nodeId ? { ...n, data: { ...n.data, label } } : n
      )
      ElMessage.success('重命名成功')
    })
    .catch(() => {})
}

/**
 * 复制节点（新 ID，位置略偏移，不复制连线）。
 * @param {string} nodeId
 */
function duplicateNode(nodeId) {
  const node = nodes.value.find((n) => n.id === nodeId)
  if (!node) return
  if (!isNodeCopyable(node)) {
    const msg = loopBodyAnchorGuardMessage(node.data?.wfType) || fixedNodeGuardMessage(node.data?.wfType)
    ElMessage.warning(msg || '该节点不可复制')
    return
  }
  const cloned = cloneVueFlowNode(node, nodes.value)
  if (!cloned) return
  nodes.value = [...nodes.value, cloned]
  selectedNodeId.value = cloned.id
  focusNode(cloned.id)
  ElMessage.success('已复制节点')
}

/**
 * 删除画布节点并移除关联连线。
 * @param {string[]} nodeIds 待删除节点 ID 列表
 * @param {{ silent?: boolean }} [options]
 */
function deleteNodes(nodeIds, options = {}) {
  const expanded = new Set()
  ;(nodeIds || []).forEach((id) => {
    expandContainerDeletionIds(id, nodes.value).forEach((x) => expanded.add(x))
  })
  const ids = expanded
  if (!ids.size) return false

  const targets = nodes.value.filter((n) => ids.has(n.id))
  if (targets.some((n) => !isNodeDeletable(n))) {
    const anchor = targets.find((n) => isLoopBodyAnchorType(n.data?.wfType))
    ElMessage.warning(anchor ? loopBodyAnchorGuardMessage(anchor.data?.wfType) : '开始/结束节点不可删除')
    return false
  }

  nodes.value = nodes.value.filter((n) => !ids.has(n.id))
  edges.value = edges.value.filter((e) => !ids.has(e.source) && !ids.has(e.target))
  if (selectedNodeId.value && ids.has(selectedNodeId.value)) {
    selectedNodeId.value = null
  }
  if (!options.silent) {
    ElMessage.success('已删除节点')
  }
  return true
}

function handleDeleteSelectedNode() {
  const node = selectedNode.value
  if (!node) return
  if (!isNodeDeletable(node)) {
    ElMessage.warning(fixedNodeGuardMessage(node.data?.wfType))
    return
  }
  const label = node.data?.label || node.id
  ElMessageBox.confirm(`确认删除节点「${label}」？相关连线将一并移除。`, '删除节点', {
    type: 'warning',
    confirmButtonText: '删除',
    confirmButtonClass: 'el-button--danger'
  })
    .then(() => deleteNodes([node.id]))
    .catch(() => {})
}

function isEditableTarget(target) {
  if (!target || !(target instanceof HTMLElement)) return false
  const tag = target.tagName.toLowerCase()
  if (tag === 'input' || tag === 'textarea' || tag === 'select') return true
  return target.isContentEditable
}

function onCanvasKeyDown(event) {
  if (isEditableTarget(event.target)) return

  const modKey = event.ctrlKey || event.metaKey

  if (event.key === 'F2' && selectedNodeId.value) {
    event.preventDefault()
    renameNode(selectedNodeId.value)
    return
  }

  if (modKey && event.key.toLowerCase() === 'd' && selectedNodeId.value) {
    event.preventDefault()
    duplicateNode(selectedNodeId.value)
    return
  }

  if (event.key !== 'Delete' && event.key !== 'Backspace') return
  if (contextMenu.value.visible) {
    closeContextMenu()
    return
  }
  if (edgeContextMenu.value.visible) {
    closeEdgeContextMenu()
    return
  }
  if (selectedEdgeId.value) {
    event.preventDefault()
    deleteEdge(selectedEdgeId.value)
    return
  }
  if (!selectedNodeId.value) return
  event.preventDefault()
  deleteNodes([selectedNodeId.value])
}

function goBack() {
  router.push({ path: isTemplateMode.value ? '/workflow/template' : '/workflow/list' })
}

function openExportTemplate() {
  exportForm.value = {
    code: '',
    name: workflowName.value ? `${workflowName.value}-template` : '',
    description: ''
  }
  exportVisible.value = true
}

async function submitExportTemplate() {
  await exportFormRef.value?.validate?.()
  await addWorkflowTemplate({
    code: exportForm.value.code,
    name: exportForm.value.name,
    description: exportForm.value.description,
    graph: currentGraph()
  })
  ElMessage.success('已导出为模板')
  exportVisible.value = false
}

onMounted(() => {
  if (isTemplateMode.value) {
    if (!templateId.value) {
      ElMessage.error('缺少模板 ID')
      goBack()
      return
    }
    loadTemplate()
  } else {
    if (!workflowId.value) {
      ElMessage.error('缺少工作流 ID')
      goBack()
      return
    }
    loadWorkflow()
  }
  window.addEventListener('keydown', onCanvasKeyDown)
})

onBeforeUnmount(() => {
  graphReady.value = false
  window.removeEventListener('keydown', onCanvasKeyDown)
})
</script>

<style scoped lang="scss">
.wf-design {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f2f4f7;
  overflow: hidden;
}

.wf-design__body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.wf-design__canvas-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  min-height: 0;
  background: #f2f4f7;
}

.wf-design__resize-handle {
  flex-shrink: 0;
  width: 5px;
  cursor: col-resize;
  background: transparent;
  transition: background 0.15s ease;
  position: relative;
  z-index: 2;

  &::after {
    content: '';
    position: absolute;
    left: 2px;
    top: 0;
    bottom: 0;
    width: 1px;
    background: #ebeef5;
  }

  &:hover,
  &--active {
    background: rgba(64, 158, 255, 0.08);

    &::after {
      background: #409eff;
    }
  }
}

.wf-design__config {
  flex-shrink: 0;
  min-height: 0;
  overflow: hidden;
}
</style>

<style>
.wf-design__canvas-wrap .vue-flow {
  width: 100%;
  height: 100%;
}
</style>
