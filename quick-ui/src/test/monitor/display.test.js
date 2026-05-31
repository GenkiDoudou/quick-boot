import { describe, it, expect } from 'vitest'
import {
  formatTrackLabel,
  resolveBatchTriggerAction,
  resolveDisplayTrigger,
  isHumanReadableLabel,
  extractVisibleActionLabel
} from '@/monitor/display'

describe('monitor/display', () => {
  it('中文按钮文案原样展示', () => {
    expect(formatTrackLabel('查看')).toBe('查看')
    expect(formatTrackLabel('修改')).toBe('修改')
    expect(isHumanReadableLabel('查看')).toBe(true)
  })

  it('技术 slug 仍可启发式转换', () => {
    expect(formatTrackLabel('user-edit:42')).toBe('修改')
    expect(formatTrackLabel('c7-json-table-delete')).toBe('删除')
  })

  it('resolveDisplayTrigger 优先采用 pending（含 data-track slug）', () => {
    expect(resolveDisplayTrigger('user-edit', 'user-edit:9')).toBe('修改')
    expect(resolveDisplayTrigger('查看', undefined)).toBe('查看')
  })

  it('resolveBatchTriggerAction 优先 click 可见文案', () => {
    const { raw, label } = resolveBatchTriggerAction([
      { type: 'click', target: '查看' },
      { type: 'api_call', trigger: 'user-edit:9', url: '/system/dept/1' }
    ])
    expect(raw).toBe('查看')
    expect(label).toBe('查看')
  })

  it('resolveBatchTriggerAction 跳过确定/取消，优先新增/修改类触发', () => {
    const { raw, label } = resolveBatchTriggerAction([
      { type: 'click', target: '新增', source: 'operation_begin' },
      { type: 'click', target: '确定' },
      { type: 'api_call', trigger: '新增', url: '/system/user/create' }
    ])
    expect(raw).toBe('新增')
    expect(label).toBe('新增')
  })

  it('resolveBatchTriggerAction 从 route_enter 推断访问页面', () => {
    const { raw, label } = resolveBatchTriggerAction([
      { type: 'route_enter', path: '/system/user', title: '用户管理' },
      { type: 'api_call', url: '/system/user/list' }
    ])
    expect(raw).toBe('访问:用户管理')
    expect(label).toBe('访问:用户管理')
  })

  it('extractVisibleActionLabel 读取 innerText / aria-label', () => {
    expect(extractVisibleActionLabel({ innerText: '查看', getAttribute: () => null })).toBe('查看')
    expect(
      extractVisibleActionLabel({
        innerText: '',
        getAttribute: (name) => (name === 'aria-label' ? '删除' : null)
      })
    ).toBe('删除')
  })
})
