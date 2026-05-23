import { describe, expect, it } from 'vitest'

/**
 * 与 request.js 内 parseJsonResponseBody 逻辑一致的单测副本。
 */
function parseJsonResponseBody(data) {
  if (data == null) {
    return null
  }
  if (typeof data === 'object' && !Array.isArray(data)) {
    return data
  }
  if (typeof data === 'string') {
    try {
      const o = JSON.parse(data)
      return typeof o === 'object' && o !== null && !Array.isArray(o) ? o : null
    } catch {
      return null
    }
  }
  return null
}

describe('parseJsonResponseBody', () => {
  it('returns object as-is', () => {
    const body = { code: 200, msg: 'ok' }
    expect(parseJsonResponseBody(body)).toEqual(body)
  })

  it('parses json string', () => {
    expect(parseJsonResponseBody('{"code":401}')).toEqual({ code: 401 })
  })

  it('returns null for invalid json', () => {
    expect(parseJsonResponseBody('not-json')).toBeNull()
  })
})
