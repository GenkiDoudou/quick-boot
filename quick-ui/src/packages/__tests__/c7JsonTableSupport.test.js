import { describe, expect, it } from 'vitest'
import { buildListRequest, resolveEffectiveTableColumns, sortSearchColumns } from '../support/c7JsonTableSupport.js'

describe('c7JsonTableSupport', () => {
  it('sortSearchColumns 按 order 升序并过滤未知 type', () => {
    const cols = [
      { prop: 'b', type: 'select', order: 2 },
      { prop: 'a', type: 'input', order: 1 },
      { prop: 'x', type: 'unknown' },
    ]
    expect(sortSearchColumns(cols).map((c) => c.prop)).toEqual(['a', 'b'])
  })

  it('buildListRequest 组装分页与搜索 param', () => {
    expect(
      buildListRequest({
        current: 2,
        size: 20,
        searchParam: { kw: 'abc' },
      }),
    ).toEqual({
      current: 2,
      size: 20,
      param: { kw: 'abc' },
    })
  })

  it('buildListRequest 附带排序字段', () => {
    expect(
      buildListRequest({
        current: 1,
        size: 10,
        searchParam: {},
        orderByColumn: 'name',
        isAsc: 'asc',
      }).param,
    ).toEqual({ orderByColumn: 'name', isAsc: 'asc' })
  })

  it('resolveEffectiveTableColumns 按 columnCheck 过滤隐藏列', () => {
    const cols = [
      { prop: 'name', label: '名称' },
      { prop: 'status', label: '状态', visible: true },
    ]
    const effective = resolveEffectiveTableColumns(cols, { status: false })
    expect(effective.map((c) => c.prop)).toEqual(['name'])
  })
})
