import { describe, it, expect } from 'vitest'
import { shouldAutoBeginOperation, shouldRecordPendingTrigger } from '@/monitor/operationRules'

describe('operationRules', () => {
  it('CRUD 按钮自动开 operation', () => {
    expect(shouldAutoBeginOperation('修改')).toBe(true)
    expect(shouldAutoBeginOperation('新增')).toBe(true)
    expect(shouldAutoBeginOperation('user-edit')).toBe(true)
  })

  it('搜索/重置不自动开 operation', () => {
    expect(shouldAutoBeginOperation('搜索')).toBe(false)
    expect(shouldAutoBeginOperation('重置')).toBe(false)
    expect(shouldAutoBeginOperation('保存排序')).toBe(false)
    expect(shouldAutoBeginOperation('全部删除')).toBe(false)
    expect(shouldAutoBeginOperation('清空')).toBe(false)
    expect(shouldRecordPendingTrigger('搜索')).toBe(true)
  })
})
