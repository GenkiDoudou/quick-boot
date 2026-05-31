import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import {
  getOrCreateSessionId,
  resetSessionId,
  clearSessionId,
  onSessionContextChange,
  resetSessionContextForTest
} from '@/monitor/sessionContext'

describe('sessionContext', () => {
  beforeEach(() => {
    resetSessionContextForTest()
  })

  afterEach(() => {
    resetSessionContextForTest()
  })

  it('getOrCreateSessionId 返回稳定 ID', () => {
    const a = getOrCreateSessionId()
    const b = getOrCreateSessionId()
    expect(a).toBe(b)
    expect(a.length).toBeGreaterThan(10)
  })

  it('resetSessionId 轮换会话', () => {
    const a = getOrCreateSessionId()
    const b = resetSessionId()
    expect(b).not.toBe(a)
    expect(getOrCreateSessionId()).toBe(b)
  })

  it('clearSessionId 后重新创建', () => {
    const a = getOrCreateSessionId()
    clearSessionId()
    const b = getOrCreateSessionId()
    expect(b).not.toBe(a)
  })

  it('onSessionContextChange 在 clear 时触发', () => {
    getOrCreateSessionId()
    let called = 0
    const off = onSessionContextChange(() => {
      called += 1
    })
    clearSessionId()
    expect(called).toBe(1)
    off()
  })
})
