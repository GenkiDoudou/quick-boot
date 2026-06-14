import { describe, expect, it } from 'vitest'
import { deepParseJsonStringValues } from '@/views/workflow/design/components/forms/startFieldTypes'
import { formatRunDisplayOutputs } from '@/views/workflow/design/utils/runOutputUtils'

describe('deepParseJsonStringValues', () => {
  it('数字与布尔值原样返回，不触发无限递归', () => {
    expect(deepParseJsonStringValues(3)).toBe(3)
    expect(deepParseJsonStringValues(true)).toBe(true)
    expect(deepParseJsonStringValues({ count: 3, index: 0 })).toEqual({ count: 3, index: 0 })
  })

  it('JSON 字符串仍可解析为嵌套结构', () => {
    expect(deepParseJsonStringValues('{"a":1}')).toEqual({ a: 1 })
  })
})

describe('formatRunDisplayOutputs', () => {
  it('循环模板 outputs 含 count 等数字字段时可正常格式化', () => {
    const text = formatRunDisplayOutputs({
      results: ['[0] Loop', '[1] Loop', '[2] Loop'],
      count: 3,
      lastSnap: 'snap'
    })
    expect(text).toContain('"count": 3')
    expect(text).toContain('"results"')
  })

  it('空字符串字段仍保留在最终输出（与步骤 Trace 一致）', () => {
    const text = formatRunDisplayOutputs({
      results: ['a'],
      count: 3,
      lastSnap: ''
    })
    expect(text).toContain('"lastSnap": ""')
  })
})
