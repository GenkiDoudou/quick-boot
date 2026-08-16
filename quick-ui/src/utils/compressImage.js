/**
 * 浏览器端图片压缩：超过阈值才压；失败返回原 File。
 */

const IMAGE_EXT = new Set(['jpg', 'jpeg', 'png', 'bmp', 'webp'])

/**
 * @param {string} name
 * @returns {string}
 */
export function fileExtLower(name) {
  const n = String(name || '')
  const i = n.lastIndexOf('.')
  if (i < 0 || i === n.length - 1) return ''
  return n.substring(i + 1).toLowerCase()
}

/**
 * @param {File} file
 * @param {{ compressEnabled?: string|boolean, compressMinSizeKb?: number, compressQuality?: number, compressMaxEdge?: number }} rule
 * @returns {Promise<File>}
 */
export async function maybeCompressImageFile(file, rule) {
  if (!file || !rule) return file
  const enabled = rule.compressEnabled === '1' || rule.compressEnabled === true || rule.compressEnabled === 1
  if (!enabled) return file

  const ext = fileExtLower(file.name)
  if (!IMAGE_EXT.has(ext)) return file

  const minKb = Number(rule.compressMinSizeKb)
  const minBytes = (Number.isFinite(minKb) && minKb > 0 ? minKb : 200) * 1024
  if (file.size < minBytes) return file

  const qualityRaw = Number(rule.compressQuality)
  const quality = Number.isFinite(qualityRaw) ? Math.min(1, Math.max(0.1, qualityRaw)) : 0.85
  const maxEdgeRaw = Number(rule.compressMaxEdge)
  const maxEdge = Number.isFinite(maxEdgeRaw) && maxEdgeRaw > 0 ? maxEdgeRaw : 1920

  try {
    const bitmap = await loadImage(file)
    const { width, height } = scaleSize(bitmap.width, bitmap.height, maxEdge)
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const ctx = canvas.getContext('2d')
    if (!ctx) return file
    ctx.drawImage(bitmap, 0, 0, width, height)
    if (typeof bitmap.close === 'function') bitmap.close()

    const mime = ext === 'png' ? 'image/png' : 'image/jpeg'
    const blob = await canvasToBlob(canvas, mime, mime === 'image/png' ? undefined : quality)
    if (!blob || blob.size <= 0 || blob.size >= file.size) return file

    const outName = mime === 'image/jpeg' && ext === 'png' ? file.name.replace(/\.png$/i, '.jpg') : file.name
    return new File([blob], outName, { type: mime, lastModified: Date.now() })
  } catch {
    return file
  }
}

/**
 * @param {File} file
 * @returns {Promise<CanvasImageSource & { width: number, height: number, close?: () => void }>}
 */
function loadImage(file) {
  if (typeof createImageBitmap === 'function') {
    return createImageBitmap(file)
  }
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve(img)
    }
    img.onerror = (e) => {
      URL.revokeObjectURL(url)
      reject(e)
    }
    img.src = url
  })
}

/**
 * @param {number} w
 * @param {number} h
 * @param {number} maxEdge
 */
function scaleSize(w, h, maxEdge) {
  const edge = Math.max(w, h)
  if (!maxEdge || edge <= maxEdge) return { width: w, height: h }
  const ratio = maxEdge / edge
  return {
    width: Math.max(1, Math.round(w * ratio)),
    height: Math.max(1, Math.round(h * ratio))
  }
}

/**
 * @param {HTMLCanvasElement} canvas
 * @param {string} mime
 * @param {number|undefined} quality
 * @returns {Promise<Blob|null>}
 */
function canvasToBlob(canvas, mime, quality) {
  return new Promise((resolve) => {
    canvas.toBlob((blob) => resolve(blob), mime, quality)
  })
}
