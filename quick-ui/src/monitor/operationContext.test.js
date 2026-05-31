import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  beginOperation,
  endOperation,
  ensureOperation,
  suppressEndOperation,
  resumeEndOperation,
  registerOperationEndHook
} from '@/monitor/operationContext'
describe('operationContext', () => {
  beforeEach(() => {
    while (true) {
      try {
        endOperation()
        break
      } catch {
        break
      }
    }
    resumeEndOperation()
    registerOperationEndHook(() => {})
    resumeEndOperation()
  })

  it('endOperation 不抛错且 suppress 期间不触发 hook', () => {
    const hook = vi.fn()
    registerOperationEndHook(hook)
    beginOperation('修改')
    suppressEndOperation()
    endOperation()
    expect(hook).not.toHaveBeenCalled()
    resumeEndOperation()
    endOperation()
    expect(hook).toHaveBeenCalledTimes(1)
    expect(hook.mock.calls[0][0]).toMatch(/^[0-9a-f-]{36}$/i)
  })

  it('ensureOperation 不覆盖已有 operationId', () => {
    const id1 = beginOperation('修改')
    const id2 = ensureOperation('修改部门')
    expect(id2).toBe(id1)
  })

  it('beginOperation 会先结束上一段 operation 并触发 hook', () => {
    const hook = vi.fn()
    registerOperationEndHook(hook)
    beginOperation('修改')
    beginOperation('新增')
    expect(hook).toHaveBeenCalledTimes(1)
  })
})