import { describe, expect, it } from 'vitest'
import { inferMediaKind } from './inferMediaKind'

describe('inferMediaKind', () => {
  it('does not take extension from query only', () => {
    expect(inferMediaKind('https://cdn/x?fmt=.png')).toBe('file')
  })

  it('is case-insensitive on extension', () => {
    expect(inferMediaKind('https://a/B.C.MP4')).toBe('video')
    expect(inferMediaKind('/static/X.JPG')).toBe('image')
  })

  it('classifies known lists', () => {
    expect(inferMediaKind('a.png')).toBe('image')
    expect(inferMediaKind('b.webm')).toBe('video')
    expect(inferMediaKind('c.pdf')).toBe('file')
  })
})
