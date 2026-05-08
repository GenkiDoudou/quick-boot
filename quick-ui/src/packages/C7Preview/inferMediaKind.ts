export type C7PreviewMediaKind = 'image' | 'video' | 'file'

const IMAGE_EXT = new Set([
  'jpg',
  'jpeg',
  'png',
  'gif',
  'webp',
  'bmp',
  'svg',
])

const VIDEO_EXT = new Set(['mp4', 'webm', 'ogg', 'mov', 'avi'])

/**
 * 按 URL 的 **pathname** 扩展名推断预览类型（忽略 **hash**；**query 不参与扩展名截取**）。
 *
 * @param url - 完整或相对 URL
 * @returns **`image` | `video` | `file`**
 */
export function inferMediaKind(url: string): C7PreviewMediaKind {
  let pathPart = url
  const hashIdx = pathPart.indexOf('#')
  if (hashIdx !== -1) {
    pathPart = pathPart.slice(0, hashIdx)
  }
  const qIdx = pathPart.indexOf('?')
  if (qIdx !== -1) {
    pathPart = pathPart.slice(0, qIdx)
  }
  const slash = pathPart.lastIndexOf('/')
  const last = slash === -1 ? pathPart : pathPart.slice(slash + 1)
  const dot = last.lastIndexOf('.')
  if (dot === -1 || dot === last.length - 1) {
    return 'file'
  }
  const ext = last.slice(dot + 1).toLowerCase()
  if (IMAGE_EXT.has(ext)) {
    return 'image'
  }
  if (VIDEO_EXT.has(ext)) {
    return 'video'
  }
  return 'file'
}
