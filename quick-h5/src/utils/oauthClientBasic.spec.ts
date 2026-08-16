/** oauthClientBasic 单元测试：校验与 quick-ui 一致的混淆结果及缺参边界 */
import { describe, expect, it } from 'vitest'
import { buildObfuscatedBasicAuthorization } from './oauthClientBasic'

describe('oauthClientBasic', () => {
  it('matches quick-ui obfuscation for quick-h5 credentials', () => {
    expect(buildObfuscatedBasicAuthorization('quick-h5', 'quick-h5-secret')).toBe(
      'Basic IAAAAABvB1pOPjQcFwMcOUBEEA4hHQoA',
    )
  })

  it('returns null when missing id or secret', () => {
    expect(buildObfuscatedBasicAuthorization('', 'x')).toBeNull()
    expect(buildObfuscatedBasicAuthorization('x', '')).toBeNull()
  })
})
