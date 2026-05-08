import { describe, expect, it } from 'vitest'
import { parseUrls } from './parseUrls'

describe('parseUrls', () => {
  it('splits trims and drops empties', () => {
    expect(parseUrls(' a.png , b.png , ')).toEqual(['a.png', 'b.png'])
    expect(parseUrls('x,,y')).toEqual(['x', 'y'])
  })

  it('treats nullish as empty', () => {
    expect(parseUrls(undefined)).toEqual([])
    expect(parseUrls(null)).toEqual([])
    expect(parseUrls('')).toEqual([])
    expect(parseUrls(', ,')).toEqual([])
  })
})
