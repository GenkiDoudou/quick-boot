<template>
  <div class="wf-design">
    <DesignToolbar
      :name="workflowName"
      :save-status="saveStatus"
      :validating="validating"
      :running="running"
      :publishing="publishing"
      @back="goBack"
      @validate="handleValidate"
      @test-run="openRunPanel"
      @publish="handlePublish"
    />

    <div class="wf-design__body" v-loading="loading">
      <NodePalette class="wf-design__palette" />

      <main class="wf-design__canvas-wrap" @drop="onDrop" @dragover.prevent>
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
          @node-click="onNodeClick"
          @node-context-menu="onNodeContextMenu"
          @edge-click="onEdgeClick"
          @edge-context-menu="onEdgeContextMenu"
          @pane-click="onPaneClick"
          @pane-context-menu="closeContextMenu"
        >
          <Background pattern-color="#dfe3ea" :gap="20" />
          <Controls />
          <MiniMap pannable zoomable />
        </VueFlow>
      </main>

      <NodeContextMenu
        :visible="contextMenu.visible"
        :x="contextMenu.x"
        :y="contextMenu.y"
        :deletable="contextMenuDeletable"
        :copyable="contextMenuCopyable"
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

      <NodeConfigPanel
        class="wf-design__config"
        :node="selectedNode"
        :variable-tree="variableTree"
        :last-run-step="lastRunStep"
        :field-errors="fieldErrors"
        @update:node="onNodeUpdate"
        @delete-node="handleDeleteSelectedNode"
      />
    </div>

    <RunPanel
      v-model:visible="runPanelVisible"
      v-model:stream-enabled="streamEnabled"
      :start-inputs="startInputs"
      :running="running"
      :trace-steps="traceSteps"
      :stream-text="streamText"
      @run="handleRunTest"
      @focus-step="onFocusTraceStep"
    />
  </div>
</template>

<script setup>
import { computed, markRaw, onBeforeUnmount, onMounted, provide, ref } from 'vue'
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
import NodePalette from './components/NodePalette.vue'
import NodeConfigPanel from './components/NodeConfigPanel.vue'
import RunPanel from './components/RunPanel.vue'
import NodeContextMenu from './components/NodeContextMenu.vue'
import EdgeContextMenu from './components/EdgeContextMenu.vue'
import WorkflowNodeCard from './nodes/WorkflowNodeCard.vue'
import WorkflowEdge from './components/edges/WorkflowEdge.vue'
import { createVueFlowNode, cloneVueFlowNode, graphToVueFlow, vueFlowToGraph } from './graphConverter'
import { useAutoSave } from './composables/useAutoSave'
import { useWorkflowRun } from './composables/useWorkflowRun'
import { useUpstreamVariables, collectUpstreamNodeIds } from './composables/useUpstreamVariables'
import { getWorkflow, publishWorkflow, validateGraph } from '@/api/workflow'

defineOptions({ name: 'WfWorkflowDesign' })

const route = useRoute()
const router = useRouter()
const { project, addEdges, onConnect, setCenter } = useVueFlow()

/** 工作流 ID 必须为字符串，避免雪花 ID 精度丢失 */
const workflowId = computed(() => String(route.params.id || ''))
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

const flowNodeTypes = { workflow: markRaw(WorkflowNodeCard) }
const flowEdgeTypes = { workflow: markRaw(WorkflowEdge) }
const defaultEdgeOptions = { type: 'workflow', animated: true, style: { stroke: '#94b8ff' } }

onConnect((connection) => {
  addEdges([
    {
      ...connection,
      id: `e_${connection.source}_${connection.target}_${Date.now()}`,
      type: 'workflow',
      animated: true,
      style: { stroke: '#94b8ff' }
    }
  ])
})

const selectedNode = computed(() => nodes.value.find((n) => n.id === selectedNodeId.value) || null)

const contextMenuNode = computed(() =>
  nodes.value.find((n) => n.id === contextMenu.value.nodeId) || null
)

const contextMenuDeletable = computed(() => isNodeDeletable(contextMenuNode.value))
const contextMenuCopyable = computed(() => isNodeCopyable(contextMenuNode.value))

const { variableTree } = useUpstreamVariables(selectedNodeId, nodes, edges)

const startInputs = computed(() => {
  const start = nodes.value.find((n) => n.data?.wfType === 'start')
  return Array.isArray(start?.data?.inputs) ? start.data.inputs : []
})

function currentGraph() {
  return vueFlowToGraph(nodes.value, edges.value)
}

const { saveStatus } = useAutoSave({
  workflowId,
  nodes,
  edges,
  getGraph: currentGraph,
  enabled: graphReady
})

const {
  running,
  runPanelVisible,
  traceSteps,
  streamText,
  lastRunStep,
  streamEnabled,
  runTest,
  focusTraceStep
} = useWorkflowRun({
  workflowId,
  nodes,
  focusNode,
  getGraph: currentGraph
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
  validateGraph({ graph: currentGraph() })
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

function handleRunTest(inputs) {
  const answer = nodes.value.find((n) => n.data?.wfType === 'answer')
  const start = nodes.value.find((n) => n.data?.wfType === 'start')
  if (!start) {
    ElMessage.warning('画布缺少输入节点')
    return
  }
  if (!answer) {
    ElMessage.warning('画布缺少输出节点')
    return
  }
  const upstream = collectUpstreamNodeIds(answer.id, edges.value)
  if (!upstream.has(start.id)) {
    ElMessage.warning('输出节点未与输入节点连线，无法执行输出')
    return
  }
  const mode = answer.data?.outputMode === 'text' ? 'text' : 'variables'
  if (mode === 'text') {
    if (!(answer.data?.output || '').trim()) {
      ElMessage.warning('请先在输出节点配置「回答内容」')
      return
    }
  } else {
    const vars = answer.data?.outputVariables
    const hasValidVar = Array.isArray(vars) && vars.some((v) => v?.key && v?.value)
    if (!hasValidVar) {
      ElMessage.warning('请先在输出节点配置「输出变量」')
      return
    }
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

function onNodeUpdate(updated) {
  const idx = nodes.value.findIndex((n) => n.id === updated.id)
  if (idx >= 0) {
    nodes.value[idx] = updated
    nodes.value = [...nodes.value]
  }
}

function onDrop(event) {
  const type = event.dataTransfer.getData('application/vueflow')
  if (!type) return
  const bounds = event.currentTarget.getBoundingClientRect()
  const position = project({
    x: event.clientX - bounds.left,
    y: event.clientY - bounds.top
  })
  const node = createVueFlowNode(type, position, nodes.value)
  nodes.value = [...nodes.value, node]
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
    ElMessage.warning('输入节点不可删除')
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

/** 输入节点为工作流入口，禁止删除 */
function isNodeDeletable(node) {
  return node?.data?.wfType !== 'start'
}

/** 输入节点不可复制，避免多个入口 */
function isNodeCopyable(node) {
  return !!node && node.data?.wfType !== 'start'
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
    ElMessage.warning('输入节点不可复制')
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
  const ids = new Set((nodeIds || []).filter(Boolean))
  if (!ids.size) return false

  const targets = nodes.value.filter((n) => ids.has(n.id))
  if (targets.some((n) => !isNodeDeletable(n))) {
    ElMessage.warning('输入节点不可删除')
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
    ElMessage.warning('输入节点不可删除')
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
  if (!selectedNodeId.value) return
  event.preventDefault()
  deleteNodes([selectedNodeId.value])
}

function goBack() {
  router.push({ path: '/workflow/list' })
}

onMounted(() => {
  if (!workflowId.value) {
    ElMessage.error('缺少工作流 ID')
    goBack()
    return
  }
  loadWorkflow()
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
  display: grid;
  grid-template-columns: 240px 1fr 320px;
  gap: 0;
  min-height: 0;
}

.wf-design__palette {
  min-height: 0;
  overflow: hidden;
}

.wf-design__config {
  min-height: 0;
  overflow: hidden;
}

.wf-design__canvas-wrap {
  position: relative;
  min-height: 0;
  background: #f2f4f7;
}
</style>

<style>
.wf-design__canvas-wrap .vue-flow {
  width: 100%;
  height: 100%;
}
</style>
