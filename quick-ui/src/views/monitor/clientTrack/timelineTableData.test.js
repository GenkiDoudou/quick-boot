import { describe, it, expect } from 'vitest'
import {
  buildFlowEdgeTableRows,
  buildPageNavTableRows,
  buildDetailEventTableRows
} from './timelineTableData'

describe('timelineTableData', () => {
  const sessionModel = {
    graph: {
      nodes: [
        { id: 'p:1', name: '用户管理', pagePath: '/system/user' },
        { id: 'p:2', name: '前端监控', pagePath: '/monitor/clientTrack' }
      ],
      links: [{ source: 'p:1', target: 'p:2', value: '系统管理 / 前端监控' }]
    },
    pageIndex: [
      { id: 'p:1', step: 1, label: '用户管理', pagePath: '/system/user', actionCount: 1, eventCount: 3 },
      { id: 'p:2', step: 2, label: '前端监控', pagePath: '/monitor/clientTrack', actionCount: 0, eventCount: 1 }
    ]
  }

  it('buildFlowEdgeTableRows 含跳转边', () => {
    const rows = buildFlowEdgeTableRows(sessionModel)
    expect(rows).toHaveLength(1)
    expect(rows[0].fromLabel).toBe('用户管理')
    expect(rows[0].targetPageId).toBe('p:2')
  })

  it('buildPageNavTableRows', () => {
    expect(buildPageNavTableRows(sessionModel.pageIndex)).toHaveLength(2)
  })

  it('buildDetailEventTableRows', () => {
    const rows = buildDetailEventTableRows({
      batchGroups: [
        {
          label: '[访问] 用户管理',
          isVisit: true,
          events: [{ nodeId: 'e1', name: '[进入] /system/user', eventType: 'route_enter' }]
        }
      ]
    })
    expect(rows).toHaveLength(1)
    expect(rows[0].nodeId).toBe('e1')
  })
})
