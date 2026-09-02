import { describe, expect, it } from 'vitest'
import { dialogCloseEmits, resolveDialogOpen } from '../support/c7DialogSupport.js'

describe('c7DialogSupport', () => {
  it('resolveDialogOpen 优先 modelValue', () => {
    expect(resolveDialogOpen(true, false)).toBe(true)
    expect(resolveDialogOpen(false, true)).toBe(false)
  })

  it('resolveDialogOpen 回退 visible', () => {
    expect(resolveDialogOpen(undefined, true)).toBe(true)
    expect(resolveDialogOpen(undefined, false)).toBe(false)
  })

  it('resolveDialogOpen 双缺省为 false', () => {
    expect(resolveDialogOpen(undefined, undefined)).toBe(false)
  })

  it('dialogCloseEmits 同时关闭双 v-model', () => {
    expect(dialogCloseEmits()).toEqual({ modelValue: false, visible: false })
  })
})
