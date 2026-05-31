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

const GRAPH_COMPACT_THRESHOLD = 30

/**
 * @param {Record<string, unknown>} node
 * @param {number} index
 * @param {number} count
 * @param {string|undefined} selectedPageId
 * @param {boolean} compact
 */
function buildGraphNode(node, index, count, selectedPageId, compact) {
  const selected = selectedPageId && node.id === selectedPageId
  const step = index + 1
  const span = count <= 1 ? 0 : Math.min(220, Math.max(100, 680 / Math.max(count - 1, 1)))
  return {
    ...node,
    x: count <= 1 ? 400 : 60 + index * span,
    y: 160,
    fixed: true,
    symbol: 'roundRect',
    symbolSize: compact
      ? [Math.min(120, Math.max(80, String(node.name || '').length * 10 + 32)), 48]
      : [Math.min(160, Math.max(100, String(node.name || '').length * 12 + 40)), 56],
    itemStyle: {
      color: selected ? COLORS.pageSelected : COLORS.page,
      borderColor: selected ? '#F56C6C' : COLORS.pageBorder,
      borderWidth: selected ? 3 : 1.5,
      shadowBlur: compact ? 0 : selected ? 12 : 4,
      shadowColor: compact ? 'transparent' : selected ? 'rgba(230, 162, 60, 0.45)' : 'rgba(64, 158, 255, 0.25)'
    },
    label: {
      show: true,
      formatter: compact ? `{step|${step}}` : `{step|${step}}\n{name|${node.name || '页面'}}`,
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
          padding: [4, 0, 0, 0]
        }
      }
    }
  }
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
  const compact = count > GRAPH_COMPACT_THRESHOLD

  const nodes = rawNodes.map((node, index) => buildGraphNode(node, index, count, selectedPageId, compact))

  const links = (model.graph?.links || []).map((link, index) => ({
    ...link,
    label: compact
      ? { show: false }
      : {
          show: true,
          formatter: link.value ? `跳转\n${link.value}` : `第 ${index + 1} 步`,
          fontSize: 11,
          color: COLORS.edge
        },
    lineStyle: {
      color: COLORS.edgeActive,
      width: compact ? 1.5 : 2.5,
      curveness: 0.08
    }
  }))

  return {
    animation: false,
    title: {
      text: '页面跳转路径（按时间从左到右）',
      subtext: compact ? '节点较多已精简标签；点击节点查看该页操作明细' : '点击节点查看该页操作明细',
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
        return [d.name, d.pagePath].filter(Boolean).join('<br/>')
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
        emphasis: { focus: 'adjacency', scale: !compact },
        label: { show: true }
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
  const count = rawNodes.length
  const compact = count > GRAPH_COMPACT_THRESHOLD
  const nodes = rawNodes.map((node, index) =>
    buildGraphNode(node, index, count, selectedPageId || undefined, compact)
  )
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
 * @param {{ tree?: Record<string, unknown>, pageLabel?: string }} pageDetailModel
 * @param {{ expandDepth?: number }} [options]
 * @returns {Record<string, unknown>}
 */
export function createTreeOption(pageDetailModel, options = {}) {
  const { expandDepth = 4 } = options
  const root = pageDetailModel?.tree || { name: '行为明细', children: [] }

  /**
   * @param {Record<string, unknown>} node
   * @returns {Record<string, unknown>}
   */
  function decorate(node) {
    const children = Array.isArray(node.children) ? node.children.map(decorate) : undefined
    const role = node.nodeRole
    const isLeaf = node.isEventLeaf
    const symbolSize = isLeaf ? 10 : role === 'page' ? 16 : 12
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
        fontSize: isLeaf ? 11 : role === 'page' ? 13 : 12,
        fontWeight: role === 'page' || role === 'action' ? 600 : 400,
        color: isLeaf ? '#606266' : '#303133',
        backgroundColor: isLeaf ? 'rgba(255,255,255,0.85)' : 'transparent',
        padding: isLeaf ? [2, 6, 2, 6] : 0,
        borderRadius: 4
      },
      lineStyle: role === 'page' ? { color: COLORS.page, width: 2 } : undefined,
      children
    }
  }

  const treeData = decorate(root)
  const pageLabel = pageDetailModel?.pageLabel || root.name || '当前页面'

  return {
    animation: false,
    title: {
      text: `${pageLabel} · 行为树`,
      subtext: '展开到操作层；点击 [API]/[点击] 叶子查看明细',
      left: 'center',
      top: 4,
      textStyle: { fontSize: 15, fontWeight: 600 },
      subtextStyle: { fontSize: 12, color: '#909399' }
    },
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const d = params.data || {}
        return d.name || ''
      }
    },
    series: [
      {
        type: 'tree',
        data: [treeData],
        top: 56,
        bottom: 20,
        left: 40,
        right: 40,
        orient: 'TB',
        expandAndCollapse: true,
        initialTreeDepth: expandDepth,
        edgeShape: 'polyline',
        edgeForkPosition: '50%',
        label: { position: 'top', verticalAlign: 'middle', align: 'center', distance: 8 },
        leaves: { label: { position: 'bottom', align: 'center', distance: 6 } },
        emphasis: { focus: 'descendant' },
        animationDuration: 0,
        animationDurationUpdate: 0
      }
    ]
  }
}
