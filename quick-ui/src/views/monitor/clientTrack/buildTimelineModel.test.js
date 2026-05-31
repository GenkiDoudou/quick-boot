import { describe, it, expect } from 'vitest'
import {
  buildTimelineOverview,
  buildPageDetailModel,
  buildTimelineModel,
  pageDisplayLabel,
  pageNodeId,
  sessionTabLabel,
  PAGE_DETAIL_TREE_THRESHOLD
} from './buildTimelineModel'

const mockPage = {
  pageVisitId: 'pv-1',
  pagePath: '/system/user',
  sessionId: 'sess-1',
  menuName: '用户管理',
  menuBreadcrumb: '系统管理 / 用户管理',
  firstTime: '2026-05-31T10:00:00',
  actionCount: 1,
  eventCount: 3,
  pageVisitBatch: {
    batchId: 1,
    triggerAction: '访问:用户管理',
    events: [{ type: 'route_enter', label: '进入 /system/user', rawJson: '{"type":"route_enter","path":"/system/user"}' }]
  },
  actions: [
    {
      batchId: 2,
      triggerAction: '修改',
      events: [
        { type: 'click', label: '点击 修改', rawJson: '{"type":"click","target":"修改"}' },
        { type: 'api_call', label: 'GET /system/user/1', rawJson: '{"type":"api_call","url":"/system/user/1","method":"get"}' }
      ]
    }
  ]
}

const mockTimelineVo = {
  browserVisitId: 'bv-1',
  sessionId: 'sess-1',
  userName: 'admin',
  totalBatches: 5,
  truncated: false,
  sessions: [
    {
      sessionId: 'sess-1',
      browserVisitId: 'bv-1',
      firstTime: '2026-05-31T10:00:00',
      pageCount: 2,
      pages: [
        mockPage,
        {
          pageVisitId: 'pv-2',
          pagePath: '/monitor/clientTrack',
          sessionId: 'sess-1',
          menuName: '前端监控',
          menuBreadcrumb: '系统管理 / 前端监控',
          firstTime: '2026-05-31T10:05:00',
          pageVisitBatch: {
            batchId: 3,
            triggerAction: '访问:前端监控',
            events: [{ type: 'route_enter', label: '进入 /monitor/clientTrack', rawJson: '{"type":"route_enter"}' }]
          },
          actions: [
            {
              batchId: 4,
              triggerAction: '查询',
              events: [{ type: 'click', label: '点击 查询', rawJson: '{"type":"click"}' }]
            }
          ]
        }
      ],
      pageFlowEdges: [
        {
          fromPageVisitId: 'pv-1',
          toPageVisitId: 'pv-2',
          toMenuLabel: '系统管理 / 前端监控'
        }
      ]
    }
  ]
}

const mockMultiSessionVo = {
  userName: 'admin',
  totalBatches: 4,
  truncated: false,
  sessions: [
    {
      sessionId: 'sess-new',
      firstTime: '2026-05-31T14:00:00',
      pageCount: 1,
      pages: [
        {
          pageVisitId: 'pv-a',
          sessionId: 'sess-new',
          pagePath: '/system/config',
          menuName: '参数设置',
          firstTime: '2026-05-31T14:00:00',
          pageVisitBatch: { batchId: 10, triggerAction: '访问:参数设置', events: [] },
          actions: []
        }
      ],
      pageFlowEdges: []
    },
    {
      sessionId: 'sess-old',
      firstTime: '2026-05-31T09:00:00',
      pageCount: 1,
      pages: [
        {
          pageVisitId: 'pv-a',
          sessionId: 'sess-old',
          pagePath: '/system/user',
          menuName: '用户管理',
          firstTime: '2026-05-31T09:00:00',
          pageVisitBatch: { batchId: 20, triggerAction: '访问:用户管理', events: [] },
          actions: []
        }
      ],
      pageFlowEdges: []
    }
  ]
}

describe('buildTimelineOverview', () => {
  it('pageNodeId 与 pageDisplayLabel', () => {
    expect(pageNodeId({ pageVisitId: 'pv-1', pagePath: '/a' })).toBe('p:pv-1')
    expect(pageNodeId({ pageVisitId: 'pv-1' }, 0, 'sess-1')).toBe('p:sess-1:pv-1')
    expect(pageDisplayLabel('A / B', null, '/x')).toBe('A / B')
  })

  it('overview 含 graph/pageIndex，不含 tree 与 eventMap', () => {
    const overview = buildTimelineOverview(mockTimelineVo)

    expect(overview.summary.totalBatches).toBe(5)
    expect(overview.summary.totalPages).toBe(2)
    expect(overview.multiSession).toBe(false)
    expect(overview.sessions[0].graph.nodes).toHaveLength(2)
    expect(overview.sessions[0].graph.links).toHaveLength(1)
    expect(overview.sessions[0].pageIndex).toHaveLength(2)
    expect(overview.sessions[0].tree).toBeUndefined()
    expect(overview.eventMap).toBeUndefined()
  })

  it('多 session 节点 ID 不冲突', () => {
    const overview = buildTimelineOverview(mockMultiSessionVo)
    expect(overview.multiSession).toBe(true)
    const [sessNew, sessOld] = overview.sessions
    expect(sessNew.graph.nodes[0].id).toBe('p:sess-new:pv-a')
    expect(sessOld.graph.nodes[0].id).toBe('p:sess-old:pv-a')
    expect(sessionTabLabel(sessNew, 0, 2)).toContain('第 2 次登录')
  })
})

describe('buildPageDetailModel', () => {
  it('仅构建单页 tree 与浅 eventMap', () => {
    const detail = buildPageDetailModel(mockPage, 'sess-1', 0)

    expect(detail.pageNodeId).toBe('p:sess-1:pv-1')
    expect(detail.eventCount).toBe(3)
    expect(detail.useListFallback).toBe(false)
    expect(detail.tree.children).toHaveLength(2)
    expect(Object.keys(detail.eventMap)).toHaveLength(3)
    expect(detail.eventMap[Object.keys(detail.eventMap)[0]].rawJson).toBeDefined()
    expect(detail.eventMap[Object.keys(detail.eventMap)[0]].type).toBeDefined()
  })

  it('事件超过阈值时启用列表降级', () => {
    const heavyPage = {
      ...mockPage,
      eventCount: PAGE_DETAIL_TREE_THRESHOLD + 1,
      pageVisitBatch: {
        batchId: 1,
        triggerAction: '访问:用户管理',
        events: Array.from({ length: PAGE_DETAIL_TREE_THRESHOLD + 1 }, (_, i) => ({
          type: 'api_call',
          label: `API ${i}`,
          rawJson: `{}`
        }))
      },
      actions: []
    }
    const detail = buildPageDetailModel(heavyPage, 'sess-1', 0)
    expect(detail.useListFallback).toBe(true)
    expect(detail.batchGroups.length).toBeGreaterThan(0)
  })
})

describe('buildTimelineModel 兼容', () => {
  it('返回 overview 结构且 eventMap 为空', () => {
    const model = buildTimelineModel(mockTimelineVo)
    expect(model.graph.nodes).toHaveLength(2)
    expect(model.eventMap).toEqual({})
  })
})
