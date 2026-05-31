import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  openBatch,
  endOperation,
  ensureOperation,
  getOperationId,
  registerBatchFlushHook,
  resetBatchSessionForTest
} from '@/monitor/operationContext'

describe('operationContext', () => {
  beforeEach(() => {
    resetBatchSessionForTest()
    registerBatchFlushHook(() => {})
  })

  it('openBatch / getOperationId 返回批次 ID', () => {
    const id = openBatch('修改')
    expect(getOperationId()).toBe(id)
  })

  it('ensureOperation 不覆盖已有 operationId', () => {
    const id1 = openBatch('修改')
    const id2 = ensureOperation('修改部门')
    expect(id2).toBe(id1)
  })

  it('连续 openBatch 会先 flush 上一段', () => {
    const hook = vi.fn()
    registerBatchFlushHook(hook)
    const id1 = openBatch('修改')
    openBatch('新增')
    expect(hook).toHaveBeenCalledWith('idle', id1)
  })

  it('endOperation 触发 flush hook', () => {
    const hook = vi.fn()
    registerBatchFlushHook(hook)
    const id = openBatch('删除')
    endOperation()
    expect(hook).toHaveBeenCalledWith('operation_end', id)
  })
})
