import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  openBatch,
  openPageVisit,
  touchBatchPassive,
  flushBatchSync,
  getOperationId,
  getPageVisitId,
  getBatchKind,
  getLastTrigger,
  registerBatchFlushHook,
  configureBatchSession,
  resetBatchSessionForTest,
  setOverlayBlockingOverrideForTest
} from '@/monitor/batchSession'

describe('batchSession', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    resetBatchSessionForTest()
    configureBatchSession({ idleMs: 2000 })
  })

  afterEach(() => {
    vi.useRealTimers()
    resetBatchSessionForTest()
  })

  it('openBatch 设置 operationId 与 trigger', () => {
    openBatch('删除')
    expect(getOperationId()).toMatch(/^[0-9a-f-]{36}$/i)
    expect(getLastTrigger()).toBe('删除')
  })

  it('idle 后触发 flush hook', () => {
    const hook = vi.fn()
    registerBatchFlushHook(hook)
    const id = openBatch('删除')
    vi.advanceTimersByTime(2000)
    expect(hook).toHaveBeenCalledWith('idle', id)
    expect(getOperationId()).toBeNull()
  })

  it('overlay 打开时推迟 idle flush', () => {
    const hook = vi.fn()
    registerBatchFlushHook(hook)
    let blocked = true
    setOverlayBlockingOverrideForTest(() => blocked)
    openBatch('删除')
    vi.advanceTimersByTime(2000)
    expect(hook).not.toHaveBeenCalled()
    blocked = false
    vi.advanceTimersByTime(400)
    expect(hook).toHaveBeenCalledWith('idle', expect.any(String))
  })

  it('touchBatchPassive 不替换主 trigger', () => {
    openBatch('删除')
    touchBatchPassive()
    expect(getLastTrigger()).toBe('删除')
  })

  it('flushBatchSync 同步上报 route_leave', () => {
    const hook = vi.fn()
    registerBatchFlushHook(hook)
    const id = openBatch('修改')
    flushBatchSync('route_leave', id)
    expect(hook).toHaveBeenCalledWith('route_leave', id)
    expect(getOperationId()).toBeNull()
  })

  it('连续 openBatch 会先 flush 上一操作批', () => {
    const hook = vi.fn()
    registerBatchFlushHook(hook)
    openPageVisit('用户管理', '/system/user')
    const id1 = openBatch('修改')
    openBatch('删除')
    expect(hook).toHaveBeenCalledWith('page_action', expect.any(String))
    expect(hook).toHaveBeenCalledWith('idle', id1)
  })

  it('openPageVisit 创建 pageVisitId 与页面 trigger', () => {
    const id = openPageVisit('用户管理', '/system/user')
    expect(getPageVisitId()).toBe(id)
    expect(getOperationId()).toBe(id)
    expect(getBatchKind()).toBe('page_visit')
    expect(getLastTrigger()).toBe('访问:用户管理')
  })
})
