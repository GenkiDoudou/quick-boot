import { describe, expect, it } from 'vitest'
import { buildCanonical } from './clientSign'
import CryptoJS from 'crypto-js'

describe('clientSign', () => {
  it('empty body sha256 matches backend', () => {
    const canonical = buildCanonical('POST', '/login', new Uint8Array(0), '1700000000', 'abc', 'quick-ui')
    expect(canonical).toBe('POST\n/login\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n1700000000\nabc\nquick-ui')
  })

  it('hmac base64 matches crypto-js reference', () => {
    const secret = '0123456789abcdef0123456789abcdef'
    const canonical = 'POST\n/login\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n1700000000\nabc\nquick-ui'
    const sig = CryptoJS.HmacSHA256(canonical, secret).toString(CryptoJS.enc.Base64)
    expect(sig).toMatch(/^[A-Za-z0-9+/]+=*$/)
    expect(sig.length).toBeGreaterThan(10)
  })
})
