import { describe, expect, it } from 'vitest'
import { applyTimeFormatters, splitDateRangeParam, toLegacyPageQuery } from '@/views/_schemas/tier-a/_shared'

describe('tier-a schema shared helpers', () => {
  it('applyTimeFormatters injects formatter for loginTime', () => {
    const cols = applyTimeFormatters([{ prop: 'loginTime', label: '时间' }])
    expect(typeof cols[0].formatter).toBe('function')
    expect(cols[0].formatter({}, {}, '2024-01-02 03:04:05')).toContain('2024')
  })

  it('splitDateRangeParam maps createTimeRange to begin/end', () => {
    const out = splitDateRangeParam(
      { createTimeRange: ['2024-01-01', '2024-01-02'], foo: '1' },
      'createTimeRange'
    )
    expect(out.beginTime).toBe('2024-01-01')
    expect(out.endTime).toBe('2024-01-02')
    expect(out.createTimeRange).toBeUndefined()
  })

  it('toLegacyPageQuery flattens C7 page request', () => {
    const q = toLegacyPageQuery({ current: 2, size: 20, param: { userName: 'admin' } })
    expect(q.pageNum).toBe(2)
    expect(q.pageSize).toBe(20)
    expect(q.userName).toBe('admin')
  })
})
