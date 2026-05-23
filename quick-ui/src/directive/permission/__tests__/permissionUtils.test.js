import { describe, expect, it, vi, beforeEach } from 'vitest'

const mockStore = vi.hoisted(() => ({
  permissions: ['system:user:list'],
}))

vi.mock('@/store/modules/user', () => ({
  default: () => mockStore,
}))

import { checkPermission } from '../permissionUtils'

describe('checkPermission', () => {
  beforeEach(() => {
    mockStore.permissions = ['system:user:list']
  })

  it('allows super admin wildcard', () => {
    mockStore.permissions = ['*:*:*']
    expect(checkPermission(['any:perm:here'])).toBe(true)
  })

  it('allows when permission matches', () => {
    expect(checkPermission(['system:user:list'])).toBe(true)
  })

  it('denies missing permission', () => {
    expect(checkPermission(['system:user:remove'])).toBe(false)
  })
})
