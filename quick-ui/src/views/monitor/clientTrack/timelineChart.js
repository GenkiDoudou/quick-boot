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

/**
 * @param {ReturnType<import('./buildTimelineModel.js').buildTimelineModel>} model
 * @param {{ selectedPageId?: string }} [options]
 * @returns {Record<string, unknown>}
 */
export function createGraphOption(model, options = {}) {
  const { selectedPageId } = options
  const rawNodes = model.graph?.nodes || []
  const count = rawNodes.length
  const span = count <= 1 ? 0 : Math.min(220, Math.max(140, 680 / Math.max(count - 1, 1)))

  const nodes = rawNodes.map((node, index) => {
    const selected = selectedPageId && node.id === selectedPageId
    const step = index + 1
    return {
      ...node,
      x: count <= 1 ? 400 : 80 + index * span,
      y: 160,
      fixed: true,
      symbol: 'roundRect',
      symbolSize: [Math.min(160, Math.max(100, String(node.name || '').length * 12 + 40)), 56],
      itemStyle: {
        color: selected ? COLORS.pageSelected : COLORS.page,
        borderColor: selected ? '#F56C6C' : COLORS.pageBorder,
        borderWidth: selected ? 3 : 1.5,
        shadowBlur: selected ? 12 : 4,
        shadowColor: selected ? 'rgba(230, 162, 60, 0.45)' : 'rgba(64, 158, 255, 0.25)'
      },
      label: {
        show: true,
        formatter: `{step|${step}}\n{name|${node.name || '页面'}}`,
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
  })

  const links = (model.graph?.links || []).map((link, index) => ({
    ...link,
    label: {
      show: true,
      formatter: link.value ? `跳转\n${link.value}` : `第 ${index + 1} 步`,
      fontSize: 11,
      color: COLORS.edge
    },
    lineStyle: {
      color: COLORS.edgeActive,
      width: 2.5,
      curveness: 0.08
    }
  }))

  return {
    title: {
      text: '页面跳转路径（按时间从左到右）',
      subtext: '点击节点可联动下方行为树',
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
        emphasis: { focus: 'adjacency', scale: true },
        label: { show: true }
      }
    ]
  }
}

/**
 * @param {string|undefined} role
 * @param {boolean} selected
 * @param {string|undefined} eventType
 */
function nodeStyle(role, selected, eventType) {
  if (role === 'page') {
    return {
      color: selected ? COLORS.pageSelected : COLORS.page,
      borderColor: selected ? '#F56C6C' : COLORS.pageBorder,
      borderWidth: selected ? 2 : 1
    }
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
 * @param {ReturnType<import('./buildTimelineModel.js').buildTimelineModel>} model
 * @param {{ selectedPageId?: string, expandDepth?: number }} [options]
 * @returns {Record<string, unknown>}
 */
export function createTreeOption(model, options = {}) {
  const { selectedPageId, expandDepth = 3 } = options

  let root = model.tree || { name: '行为轨迹', children: [] }
  if (selectedPageId && Array.isArray(root.children)) {
    const hit = root.children.find((c) => c.pageNodeId === selectedPageId)
    if (hit) {
      root = { ...root, name: `${root.name} · 当前页`, children: [hit] }
    }
  }

  /**
   * @param {Record<string, unknown>} node
   * @returns {Record<string, unknown>}
   */
  function decorate(node) {
    const children = Array.isArray(node.children) ? node.children.map(decorate) : undefined
    const role = node.nodeRole
    const isPage = role === 'page'
    const selected = isPage && selectedPageId && node.pageNodeId === selectedPageId
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
      itemStyle: nodeStyle(role, selected, node.eventType),
      label: {
        fontSize: isLeaf ? 11 : role === 'page' ? 13 : 12,
        fontWeight: isPage || role === 'action' ? 600 : 400,
        color: selected ? COLORS.pageSelected : isLeaf ? '#606266' : '#303133',
        backgroundColor: isLeaf ? 'rgba(255,255,255,0.85)' : 'transparent',
        padding: isLeaf ? [2, 6, 2, 6] : 0,
        borderRadius: 4
      },
      lineStyle: role === 'page' ? { color: COLORS.page, width: 2 } : undefined,
      children
    }
  }

  const treeData = decorate(root)

  return {
    title: {
      text: selectedPageId ? '当前页面行为树' : '全链路行为树',
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
        animationDurationUpdate: 250
      }
    ]
  }
}
