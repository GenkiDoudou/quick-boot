import { describe, expect, it } from 'vitest'
import { DATE_PATTERN, DATETIME_PATTERN, formatTime } from '@/utils/formatTime'

describe('formatTime', () => {
  it('formats ISO date string', () => {
    expect(formatTime('2021-01-01 12:30:45')).toBe('2021-01-01 12:30:45')
  })

  it('supports legacy ruoyi placeholder pattern', () => {
    expect(formatTime('2021-01-01 12:30:45', '{y}-{m}-{d} {h}:{i}:{s}')).toBe('2021-01-01 12:30:45')
  })

  it('supports dayjs pattern constants', () => {
    expect(formatTime('2021-01-01 12:30:45', DATE_PATTERN)).toBe('2021-01-01')
    expect(formatTime('2021-01-01 12:30:45', DATETIME_PATTERN)).toBe('2021-01-01 12:30:45')
  })

  it('returns null for empty input', () => {
    expect(formatTime('')).toBeNull()
    expect(formatTime(null)).toBeNull()
  })
})
