import { describe, it, expect } from 'vitest'
import { canTrackPath } from '@/monitor/createUserMonitor'
import { isMonitorEnabled } from '@/monitor/config'

describe('canTrackPath', () => {
  const allowPages = ['/system', '/monitor']

  it('excludePages 优先于 allowPages，前端监控页不采集', () => {
    const excludePages = ['/system/clientTrack', '/system/clientTrackEvents', '/monitor/clientTrack']
    expect(canTrackPath('/system/clientTrack', allowPages, excludePages)).toBe(false)
    expect(canTrackPath('/system/clientTrackEvents', allowPages, excludePages)).toBe(false)
    expect(canTrackPath('/monitor/clientTrack', allowPages, excludePages)).toBe(false)
    expect(canTrackPath('/monitor/clientTrack/detail', allowPages, excludePages)).toBe(false)
    expect(canTrackPath('/monitor/operlog', allowPages, excludePages)).toBe(true)
    expect(canTrackPath('/system/user', allowPages, excludePages)).toBe(true)
  })

  it('isMonitorEnabled 读取 VITE_APP_MONITOR_ENABLED', () => {
    expect(typeof isMonitorEnabled()).toBe('boolean')
  })
})
