import { describe, it, expect } from 'vitest'
import { buildTimelineModel, pageDisplayLabel, pageNodeId } from './buildTimelineModel'

/** 2 页 + 3 操作批 mock，验证 graph 节点/边与 tree 层级 */
const mockTimelineVo = {
  browserVisitId: 'bv-1',
  sessionId: 'sess-1',
  userName: 'admin',
  totalBatches: 5,
  truncated: false,
  pages: [
    {
      pageVisitId: 'pv-1',
      pagePath: '/system/user',
      menuName: '用户管理',
      menuBreadcrumb: '系统管理 / 用户管理',
      firstTime: '2026-05-31T10:00:00',
      pageVisitBatch: {
        batchId: 1,
        triggerAction: '访问:用户管理',
        reason: 'page_action',
        pageVisitBatch: true,
        events: [{ type: 'route_enter', label: '进入 /system/user', rawJson: '{"type":"route_enter","path":"/system/user"}' }]
      },
      actions: [
        {
          batchId: 2,
          triggerAction: '修改',
          reason: 'page_action',
          events: [
            { type: 'click', label: '点击 修改', rawJson: '{"type":"click","target":"修改"}' },
            { type: 'api_call', label: 'GET /system/user/1', url: '/system/user/1', method: 'get', rawJson: '{"type":"api_call","url":"/system/user/1","method":"get"}' }
          ]
        }
      ]
    },
    {
      pageVisitId: 'pv-2',
      pagePath: '/monitor/clientTrack',
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
        },
        {
          batchId: 5,
          triggerAction: '详情',
          events: [{ type: 'api_call', label: 'GET /monitor/clientTrack/list', rawJson: '{"type":"api_call"}' }]
        }
      ]
    }
  ],
  pageFlowEdges: [
    {
      fromPageVisitId: 'pv-1',
      toPageVisitId: 'pv-2',
      fromPagePath: '/system/user',
      toPagePath: '/monitor/clientTrack',
      fromMenuLabel: '系统管理 / 用户管理',
      toMenuLabel: '系统管理 / 前端监控',
      atTime: '2026-05-31T10:05:00'
    }
  ]
}

describe('buildTimelineModel', () => {
  it('pageNodeId 与 pageDisplayLabel', () => {
    expect(pageNodeId({ pageVisitId: 'pv-1', pagePath: '/a' })).toBe('p:pv-1')
    expect(pageDisplayLabel('A / B', null, '/x')).toBe('A / B')
    expect(pageDisplayLabel(null, '菜单', '/x')).toBe('菜单')
  })

  it('2 页 1 边 graph；tree 含访问批、操作批与 API 叶子', () => {
    const model = buildTimelineModel(mockTimelineVo)

    expect(model.summary.totalBatches).toBe(5)
    expect(model.graph.nodes).toHaveLength(2)
    expect(model.graph.links).toHaveLength(1)
    expect(model.graph.links[0].source).toBe('p:pv-1')
    expect(model.graph.links[0].target).toBe('p:pv-2')

    expect(model.tree.children).toHaveLength(2)
    const page1 = model.tree.children[0]
    expect(page1.name).toContain('[1]')
    expect(page1.name).toContain('用户管理')
    expect(page1.children).toHaveLength(2)
    expect(page1.children[0].name).toBe('[访问] 用户管理')
    expect(page1.children[1].name).toBe('[操作] 修改')
    expect(page1.children[1].children).toHaveLength(2)

    expect(model.flowSteps).toHaveLength(2)
    expect(model.flowSteps[0].title).toBe('第 1 步')

    const leafId = page1.children[1].children[1].nodeId
    expect(model.eventMap[leafId]).toMatchObject({ type: 'api_call' })
  })
})
