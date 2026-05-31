import { describe, it, expect } from 'vitest'
import { graphShortLabel } from '@/views/monitor/clientTrack/buildTimelineModel'
import { computeGraphChartSize, computeTreeChartSize, createTreeOption } from '@/views/monitor/clientTrack/timelineChart'

describe('graphShortLabel', () => {
  it('优先 menuName', () => {
    expect(graphShortLabel('系统管理 / 日志管理 / 前端监控', '前端监控', '/system/log/clientTrack')).toBe('前端监控')
  })

  it('无 menuName 时取面包屑最后一段', () => {
    expect(graphShortLabel('系统管理 / 日志管理 / 行为轨迹', '', '/system/log/clientTrackTimeline')).toBe('行为轨迹')
  })
})

describe('computeGraphChartSize', () => {
  it('节点较多时宽度随数量增长', () => {
    const nodes = Array.from({ length: 13 }, (_, i) => ({
      id: `p-${i}`,
      name: `系统管理 / 日志管理 / 页面${i}`,
      shortName: `页面${i}`
    }))
    const size = computeGraphChartSize(nodes)
    expect(size.chartWidth).toBeGreaterThan(900)
    expect(size.chartHeight).toBeGreaterThan(300)
  })

  it('超过每行上限时增加高度（换行）', () => {
    const nodes = Array.from({ length: 13 }, (_, i) => ({
      id: `p-${i}`,
      shortName: `页${i}`
    }))
    const size = computeGraphChartSize(nodes)
    expect(size.chartHeight).toBeGreaterThan(380)
  })
})

describe('computeTreeChartSize', () => {
  it('事件较多时高度随数量增长', () => {
    const detail = {
      eventCount: 12,
      tree: {
        name: '登录日志',
        children: Array.from({ length: 12 }, (_, i) => ({ name: `事件${i}` }))
      }
    }
    const size = computeTreeChartSize(detail)
    expect(parseInt(size.height, 10)).toBeGreaterThan(360)
    expect(parseInt(size.height, 10)).toBeLessThanOrEqual(760)
  })
})

describe('createTreeOption', () => {
  it('根节点隐藏 label，标题用短标签', () => {
    const option = createTreeOption({
      pageLabel: '系统管理 / 日志管理 / 登录日志',
      pageShortLabel: '登录日志',
      tree: {
        name: '登录日志',
        nodeRole: 'page',
        children: [
          {
            name: '[访问] 登录日志',
            nodeRole: 'action',
            children: [{ name: '[点击] 查询', nodeRole: 'event', isEventLeaf: true, eventType: 'click' }]
          }
        ]
      }
    })
    expect(option.title.text).toBe('登录日志 · 行为树')
    expect(option.title.subtext).toContain('系统管理 / 日志管理 / 登录日志')
    const root = option.series[0].data[0]
    expect(root.label.show).toBe(false)
    expect(root.children[0].label.show).toBe(true)
  })
})
