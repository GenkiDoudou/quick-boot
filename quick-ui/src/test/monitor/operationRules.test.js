import { describe, it, expect } from 'vitest'
import {
  isPrimaryAction,
  isPassiveAction,
  isQueryOnlyAction,
  shouldAutoBeginOperation,
  shouldRecordPendingTrigger
} from '@/monitor/operationRules'

describe('operationRules', () => {
  it('主操作 click 应 openBatch', () => {
    expect(isPrimaryAction('修改')).toBe(true)
    expect(isPrimaryAction('新增')).toBe(true)
    expect(isPrimaryAction('删除')).toBe(true)
    expect(isPrimaryAction('user-edit')).toBe(true)
    expect(shouldAutoBeginOperation('修改')).toBe(true)
  })

  it('查询/工具类不建批次', () => {
    expect(isQueryOnlyAction('搜索')).toBe(true)
    expect(isQueryOnlyAction('重置')).toBe(true)
    expect(isPrimaryAction('搜索')).toBe(false)
    expect(isPrimaryAction('全部删除')).toBe(false)
    expect(shouldRecordPendingTrigger('搜索')).toBe(true)
  })

  it('确定/取消为被动 click', () => {
    expect(isPassiveAction('确定')).toBe(true)
    expect(isPassiveAction('取消')).toBe(true)
    expect(isPrimaryAction('确定')).toBe(false)
  })
})
