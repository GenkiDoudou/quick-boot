import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import {
  getOrCreateBrowserVisitId,
  resetBrowserVisitContextForTest,
  seedBrowserVisitForTest,
  stopBrowserVisitHeartbeat
} from '@/monitor/browserVisitContext'

describe('browserVisitContext', () => {
  beforeEach(() => {
    resetBrowserVisitContextForTest()
  })

  afterEach(() => {
    stopBrowserVisitHeartbeat()
    resetBrowserVisitContextForTest()
  })

  it('getOrCreateBrowserVisitId 同 tab 返回稳定 ID', () => {
    const a = getOrCreateBrowserVisitId()
    const b = getOrCreateBrowserVisitId()
    expect(a).toBe(b)
    expect(a.length).toBeGreaterThan(10)
  })

  it('心跳未过期时复用已有 browserVisitId', () => {
    const a = getOrCreateBrowserVisitId()
    resetBrowserVisitContextForTest()
    seedBrowserVisitForTest(a, Date.now())
    const b = getOrCreateBrowserVisitId()
    expect(b).toBe(a)
  })
})
