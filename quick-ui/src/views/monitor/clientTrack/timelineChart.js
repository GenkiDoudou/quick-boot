import * as echarts from 'echarts/core'
import { GraphChart, TreeChart } from 'echarts/charts'
import { TooltipComponent, TitleComponent, GraphicComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([GraphChart, TreeChart, TooltipComponent, TitleComponent, GraphicComponent, CanvasRenderer])

export { echarts }

const COLORS = {
  page: '#409EFF',
  pageSelected: '#E6A23C',
  pageBorder: '#337ecc',
  edge: '#606266',
  edgeActive: '#409EFF',
  visit: '#79bbff',
  action: '#E6A23C',
  click: '#409EFF',
  api: '#67C23A',
  route: '#909399',
  error: '#F56C6C'
}

/** 超过此数量隐藏边上的跳转文字（节点仍显示短菜单名） */
const GRAPH_EDGE_LABEL_THRESHOLD = 24
/** 超过此数量自动换行展示 */
const GRAPH_NODES_PER_ROW = 8
const GRAPH_NODE_GAP = 40
const GRAPH_PADDING_X = 72
const GRAPH_ROW_HEIGHT = 132
const GRAPH_BASE_HEIGHT = 300

/**
 * 节点展示名：优先 shortName，否则从完整面包屑取最后一段。
 * @param {Record<string, unknown>} node
 * @returns {string}
 */
function resolveGraphNodeLabel(node) {
  if (node.shortName != null && String(node.shortName).trim()) {
    return String(node.shortName).trim()
  }
  const name = node.name != null ? String(node.name).trim() : ''
  if (name.includes('/')) {
    const parts = name.split('/').map((s) => s.trim()).filter(Boolean)
    if (parts.length) {
      return parts[parts.length - 1]
    }
  }
  return name || '页面'
}

/**
 * @param {string|undefined} label
 * @returns {number}
 */
function estimateNodeWidth(label) {
  const len = String(label || '').length
  return Math.min(148, Math.max(92, len * 11 + 36))
}

/**
 * 计算单行节点中心 x 坐标与行宽。
 * @param {Record<string, unknown>[]} rowNodes
 * @param {boolean} compact
 * @returns {{ centers: number[], rowWidth: number }}
 */
function layoutGraphRow(rowNodes) {
  const widths = rowNodes.map((node) => estimateNodeWidth(resolveGraphNodeLabel(node)))
  if (!widths.length) {
    return { centers: [], rowWidth: 720 }
  }
  const centers = []
  let cx = GRAPH_PADDING_X + widths[0] / 2
  centers.push(cx)
  for (let i = 1; i < widths.length; i += 1) {
    cx += widths[i - 1] / 2 + GRAPH_NODE_GAP + widths[i] / 2
    centers.push(cx)
  }
  const rowWidth = cx + widths[widths.length - 1] / 2 + GRAPH_PADDING_X
  return { centers, rowWidth, widths }
}

/**
 * 计算跳转路径图布局尺寸（供容器 minWidth / height 与 ECharts 对齐）。
 * @param {Record<string, unknown>[]} rawNodes
 * @returns {{ minWidth: string, height?: string, chartWidth: number, chartHeight: number }}
 */
export function computeGraphChartSize(rawNodes) {
  const count = rawNodes?.length || 0
  if (count <= 1) {
    return { minWidth: '100%', chartWidth: 720, chartHeight: GRAPH_BASE_HEIGHT }
  }
  const rowCount = Math.ceil(count / GRAPH_NODES_PER_ROW)
  let chartWidth = 720
  for (let row = 0; row < rowCount; row += 1) {
    const slice = rawNodes.slice(row * GRAPH_NODES_PER_ROW, (row + 1) * GRAPH_NODES_PER_ROW)
    const { rowWidth } = layoutGraphRow(slice)
    chartWidth = Math.max(chartWidth, rowWidth)
  }
  const chartHeight = GRAPH_BASE_HEIGHT + Math.max(0, rowCount - 1) * GRAPH_ROW_HEIGHT
  return {
    minWidth: `${Math.ceil(chartWidth)}px`,
    height: `${Math.ceil(chartHeight)}px`,
    chartWidth: Math.ceil(chartWidth),
    chartHeight: Math.ceil(chartHeight)
  }
}

/**
 * @param {Record<string, unknown>} node
 * @param {number} index
 * @param {number} count
 * @param {string|undefined} selectedPageId
 * @param {{ x: number, y: number, width: number }} layout
 */
function buildGraphNode(node, index, selectedPageId, layout) {
  const selected = selectedPageId && node.id === selectedPageId
  const step = index + 1
  const displayName = resolveGraphNodeLabel(node)
  const nodeWidth = layout.width
  const nodeHeight = 64
  return {
    ...node,
    x: layout.x,
    y: layout.y,
    fixed: true,
    symbol: 'roundRect',
    symbolSize: [nodeWidth, nodeHeight],
    itemStyle: {
      color: selected ? COLORS.pageSelected : COLORS.page,
      borderColor: selected ? '#F56C6C' : COLORS.pageBorder,
      borderWidth: selected ? 3 : 1.5,
      shadowBlur: selected ? 12 : 4,
      shadowColor: selected ? 'rgba(230, 162, 60, 0.45)' : 'rgba(64, 158, 255, 0.25)'
    },
    label: {
      show: true,
      formatter: `{step|${step}}\n{name|${displayName}}`,
      rich: {
        step: {
          color: '#fff',
          backgroundColor: selected ? '#F56C6C' : '#337ecc',
          borderRadius: 10,
          padding: [2, 8, 2, 8],
          fontSize: 11,
          fontWeight: 700,
          lineHeight: 18
        },
        name: {
          color: '#303133',
          fontSize: 12,
          fontWeight: 600,
          lineHeight: 18,
          padding: [4, 0, 0, 0],
          width: nodeWidth - 12,
          overflow: 'truncate'
        }
      }
    }
  }
}

/**
 * @param {Record<string, unknown>[]} rawNodes
 * @param {string|undefined} selectedPageId
 * @returns {Record<string, unknown>[]}
 */
function buildGraphNodes(rawNodes, selectedPageId) {
  const count = rawNodes.length
  const rowCount = Math.ceil(count / GRAPH_NODES_PER_ROW)
  /** @type {Record<string, unknown>[]} */
  const nodes = []

  for (let row = 0; row < rowCount; row += 1) {
    const start = row * GRAPH_NODES_PER_ROW
    const slice = rawNodes.slice(start, start + GRAPH_NODES_PER_ROW)
    const { centers, widths } = layoutGraphRow(slice)
    const y = 128 + row * GRAPH_ROW_HEIGHT
    slice.forEach((node, colIndex) => {
      const globalIndex = start + colIndex
      nodes.push(
        buildGraphNode(node, globalIndex, selectedPageId, {
          x: centers[colIndex],
          y,
          width: widths[colIndex]
        })
      )
    })
  }
  return nodes
}

/**
 * @param {{ graph?: { nodes?: Record<string, unknown>[], links?: Record<string, unknown>[] } }} model
 * @param {{ selectedPageId?: string }} [options]
 * @returns {Record<string, unknown>}
 */
export function createGraphOption(model, options = {}) {
  const { selectedPageId } = options
  const rawNodes = model.graph?.nodes || []
  const count = rawNodes.length
  const hideEdgeLabels = count > GRAPH_EDGE_LABEL_THRESHOLD
  const size = computeGraphChartSize(rawNodes)

  const nodes = buildGraphNodes(rawNodes, selectedPageId)

  const links = (model.graph?.links || []).map((link, index) => ({
    ...link,
    label: hideEdgeLabels
      ? { show: false }
      : {
          show: true,
          formatter: link.value ? `跳转\n${link.value}` : `第 ${index + 1} 步`,
          fontSize: 11,
          color: COLORS.edge
        },
    lineStyle: {
      color: COLORS.edgeActive,
      width: hideEdgeLabels ? 1.5 : 2.5,
      curveness: count > GRAPH_NODES_PER_ROW ? 0.22 : 0.08
    }
  }))

  return {
    animation: false,
    title: {
      text: '页面跳转路径（按时间从左到右）',
      subtext:
        count > GRAPH_NODES_PER_ROW
          ? '已自动换行；悬停查看完整路径，点击节点查看该页操作明细'
          : '悬停查看完整路径，点击节点查看该页操作明细',
      left: 'center',
      top: 4,
      textStyle: { fontSize: 15, fontWeight: 600 },
      subtextStyle: { fontSize: 12, color: '#909399' }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'edge') {
          const d = params.data || {}
          return [`${d.source} → ${d.target}`, d.value].filter(Boolean).join('<br/>')
        }
        const d = params.data || {}
        const lines = []
        if (d.name) {
          lines.push(String(d.name))
        }
        if (d.pagePath) {
          lines.push(String(d.pagePath))
        }
        return lines.join('<br/>') || ''
      }
    },
    series: [
      {
        type: 'graph',
        layout: 'none',
        roam: true,
        draggable: true,
        data: nodes,
        links,
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [6, 12],
        emphasis: { focus: 'adjacency', scale: true },
        label: { show: true },
        top: 48,
        bottom: 16,
        left: 16,
        right: 16,
        height: size.chartHeight - 64
      }
    ]
  }
}

/**
 * 仅更新 graph 节点选中样式，避免全量重建 option。
 * @param {import('echarts/core').EChartsType} chart
 * @param {{ graph?: { nodes?: Record<string, unknown>[] } }} model
 * @param {string} selectedPageId
 */
export function patchGraphSelection(chart, model, selectedPageId) {
  if (!chart || !model?.graph?.nodes) return
  const rawNodes = model.graph.nodes
  const nodes = buildGraphNodes(rawNodes, selectedPageId || undefined)
  chart.setOption({ series: [{ data: nodes }] })
}

/**
 * @param {string|undefined} role
 * @param {string|undefined} eventType
 */
function nodeStyle(role, eventType) {
  if (role === 'page') {
    return { color: COLORS.page, borderColor: COLORS.pageBorder, borderWidth: 1 }
  }
  if (role === 'visit') {
    return { color: COLORS.visit, borderColor: '#337ecc' }
  }
  if (role === 'action') {
    return { color: '#fdf6ec', borderColor: COLORS.action, borderWidth: 2 }
  }
  if (eventType === 'click') {
    return { color: '#ecf5ff', borderColor: COLORS.click }
  }
  if (eventType === 'api_call' || eventType === 'api_slow') {
    return { color: '#f0f9eb', borderColor: COLORS.api }
  }
  if (eventType === 'api_error' || eventType === 'js_error' || eventType === 'promise_error') {
    return { color: '#fef0f0', borderColor: COLORS.error }
  }
  if (eventType === 'route_enter' || eventType === 'route_leave') {
    return { color: '#f4f4f5', borderColor: COLORS.route }
  }
  return { color: '#fff', borderColor: '#dcdfe6' }
}

/**
 * 统计行为树节点数（含根），用于估算图表高度。
 * @param {Record<string, unknown>|undefined|null} node
 * @returns {number}
 */
function countTreeNodes(node) {
  if (!node) return 0
  let total = 1
  const children = node.children
  if (Array.isArray(children)) {
    for (const child of children) {
      total += countTreeNodes(child)
    }
  }
  return total
}

/**
 * 行为树容器高度（随事件数量增高，减轻标签重叠）。
 * @param {{ tree?: Record<string, unknown>, eventCount?: number }} pageDetailModel
 * @returns {{ height: string, minHeight: string }}
 */
export function computeTreeChartSize(pageDetailModel) {
  const tree = pageDetailModel?.tree
  const nodeCount = countTreeNodes(tree)
  const eventCount = pageDetailModel?.eventCount ?? Math.max(0, nodeCount - 1)
  const height = Math.max(360, Math.min(760, 300 + eventCount * 34 + Math.ceil(nodeCount / 4) * 20))
  return {
    height: `${height}px`,
    minHeight: '360px'
  }
}

/**
 * @param {{ tree?: Record<string, unknown>, pageLabel?: string, pageShortLabel?: string }} pageDetailModel
 * @param {{ expandDepth?: number }} [options]
 * @returns {Record<string, unknown>}
 */
export function createTreeOption(pageDetailModel, options = {}) {
  const { expandDepth = 4 } = options
  const root = pageDetailModel?.tree || { name: '行为明细', children: [] }
  const pageLabel = pageDetailModel?.pageLabel || root.name || '当前页面'
  const pageShortLabel = pageDetailModel?.pageShortLabel || pageLabel
  const size = computeTreeChartSize(pageDetailModel)

  /**
   * @param {Record<string, unknown>} node
   * @returns {Record<string, unknown>}
   */
  function decorate(node) {
    const children = Array.isArray(node.children) ? node.children.map(decorate) : undefined
    const role = node.nodeRole
    const isLeaf = node.isEventLeaf
    const symbolSize = isLeaf ? 10 : role === 'page' ? 14 : 12
    const labelWidth = isLeaf ? 168 : role === 'action' ? 148 : 120
    return {
      name: node.name,
      nodeId: node.nodeId,
      pageNodeId: node.pageNodeId,
      nodeRole: role,
      isEventLeaf: node.isEventLeaf,
      eventType: node.eventType,
      symbol: isLeaf ? 'circle' : 'roundRect',
      symbolSize,
      itemStyle: nodeStyle(role, node.eventType),
      label: {
        show: role !== 'page',
        fontSize: isLeaf ? 11 : role === 'action' ? 12 : 12,
        fontWeight: role === 'action' ? 600 : 400,
        color: isLeaf ? '#606266' : '#303133',
        backgroundColor: isLeaf ? 'rgba(255, 255, 255, 0.92)' : 'rgba(255, 255, 255, 0.85)',
        padding: isLeaf ? [2, 6, 2, 6] : [2, 8, 2, 8],
        borderRadius: 4,
        width: labelWidth,
        overflow: 'truncate'
      },
      lineStyle: role === 'page' ? { color: COLORS.page, width: 2 } : undefined,
      children
    }
  }

  const treeData = decorate(root)
  const subtext =
    pageLabel !== pageShortLabel
      ? `${pageLabel} · 点击 [API]/[点击] 叶子查看明细`
      : '展开到操作层；点击 [API]/[点击] 叶子查看明细'

  return {
    animation: false,
    title: {
      text: `${pageShortLabel} · 行为树`,
      subtext,
      left: 'center',
      top: 4,
      textStyle: { fontSize: 15, fontWeight: 600 },
      subtextStyle: { fontSize: 12, color: '#909399', width: 560, overflow: 'break' }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const d = params.data || {}
        if (d.nodeRole === 'page' && pageLabel && pageLabel !== d.name) {
          return pageLabel
        }
        return d.name || ''
      }
    },
    series: [
      {
        type: 'tree',
        data: [treeData],
        top: pageLabel !== pageShortLabel ? 72 : 64,
        bottom: 24,
        left: 24,
        right: 24,
        orient: 'TB',
        expandAndCollapse: true,
        initialTreeDepth: expandDepth,
        edgeShape: 'polyline',
        edgeForkPosition: '50%',
        layerGap: 56,
        nodeGap: 28,
        label: { position: 'top', verticalAlign: 'bottom', align: 'center', distance: 6 },
        leaves: { label: { position: 'bottom', verticalAlign: 'top', align: 'center', distance: 8 } },
        emphasis: { focus: 'descendant' },
        animationDuration: 0,
        animationDurationUpdate: 0
      }
    ],
    _chartHeight: size.height
  }
}
