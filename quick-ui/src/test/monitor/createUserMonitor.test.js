import { describe, it, expect } from 'vitest'
import { canTrackPath } from '@/monitor/createUserMonitor'
import { isMonitorEnabled, loadMonitorConfig } from '@/monitor/config'

describe('canTrackPath', () => {
  const excludePages = [
    '/redirect',
    '/system/clientTrack',
    '/system/clientTrackEvents',
    '/system/clientTrackTimeline',
    '/monitor/clientTrack'
  ]

  it('allowPages 为空时全站采集（除 excludePages）', () => {
    expect(canTrackPath('/system/user', [], excludePages)).toBe(true)
    expect(canTrackPath('/demo/c7-button-e2e', [], excludePages)).toBe(true)
    expect(canTrackPath('/visual/jimu-report', [], excludePages)).toBe(true)
    expect(canTrackPath('/tool/gen/edit', [], excludePages)).toBe(true)
    expect(canTrackPath('/oauth/authorize', [], excludePages)).toBe(true)
    expect(canTrackPath('/login', [], excludePages)).toBe(true)
    expect(canTrackPath('/index', [], excludePages)).toBe(true)
  })

  it('excludePages 优先，监控管理页与 redirect 不采集', () => {
    expect(canTrackPath('/system/clientTrack', [], excludePages)).toBe(false)
    expect(canTrackPath('/system/clientTrackEvents', [], excludePages)).toBe(false)
    expect(canTrackPath('/system/clientTrackTimeline', [], excludePages)).toBe(false)
    expect(canTrackPath('/monitor/clientTrack', [], excludePages)).toBe(false)
    expect(canTrackPath('/monitor/clientTrack/detail', [], excludePages)).toBe(false)
    expect(canTrackPath('/redirect/index', [], excludePages)).toBe(false)
    expect(canTrackPath('/monitor/operlog', [], excludePages)).toBe(true)
  })

  it('非空 allowPages 仍可按前缀限制', () => {
    const allowPages = ['/system', '/monitor']
    expect(canTrackPath('/demo/c7-button-e2e', allowPages, excludePages)).toBe(false)
    expect(canTrackPath('/system/user', allowPages, excludePages)).toBe(true)
  })

  it('loadMonitorConfig 默认 allowPages 为空（全站）', () => {
    const cfg = loadMonitorConfig()
    expect(cfg.allowPages).toEqual([])
    expect(cfg.excludePages).toContain('/system/clientTrackTimeline')
  })

  it('isMonitorEnabled 读取 VITE_APP_MONITOR_ENABLED', () => {
    expect(typeof isMonitorEnabled()).toBe('boolean')
  })
})
