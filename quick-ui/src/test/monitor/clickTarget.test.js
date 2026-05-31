import { describe, it, expect } from 'vitest'
import { readClickTarget, isOperationTriggerElement } from '@/monitor/clickTarget'

describe('clickTarget', () => {
  it('isOperationTriggerElement 识别 el-button', () => {
    const node = {
      tagName: 'BUTTON',
      classList: { contains: (c) => c === 'el-button' },
      getAttribute: () => null
    }
    expect(isOperationTriggerElement(node)).toBe(true)
  })

  it('readClickTarget 无 data-track 时解析 innerText', () => {
    const btn = {
      tagName: 'BUTTON',
      classList: { contains: (c) => c === 'el-button' },
      innerText: '查看',
      dataset: {},
      getAttribute: () => null,
      parentElement: null,
      childNodes: []
    }
    const span = {
      tagName: 'SPAN',
      parentElement: btn
    }
    const { label, isAction } = readClickTarget(span)
    expect(isAction).toBe(true)
    expect(label).toBe('查看')
  })

  it('readClickTarget 无 data-track 时解析图标按钮文本', () => {
    const btn = {
      tagName: 'BUTTON',
      classList: { contains: (c) => c === 'el-button' },
      innerText: '',
      dataset: {},
      getAttribute: () => null,
      parentElement: null,
      childNodes: [
        { nodeType: 1, tagName: 'I', classList: { contains: (c) => c === 'el-icon' }, childNodes: [] },
        { nodeType: 3, textContent: '修改' }
      ]
    }
    const icon = {
      tagName: 'I',
      parentElement: btn
    }
    const { label, isAction } = readClickTarget(icon)
    expect(isAction).toBe(true)
    expect(label).toBe('修改')
  })

  it('readClickTarget 有 data-track 时优先使用 data-track', () => {
    const btn = {
      tagName: 'BUTTON',
      classList: { contains: (c) => c === 'el-button' },
      innerText: '修改',
      dataset: { track: 'user-edit' },
      getAttribute: () => null,
      parentElement: null,
      childNodes: []
    }
    const span = {
      tagName: 'SPAN',
      parentElement: btn
    }
    const { label, isAction } = readClickTarget(span)
    expect(isAction).toBe(true)
    expect(label).toBe('user-edit')
  })
})
