/**
 * 离屏 Canvas 生成水印平铺图案（文本或图片），供 `C7Watermark` 作为 `background-image` 使用。
 *
 * 说明：
 * - 图片分支依赖「已加载且可绘制」的 `HTMLImageElement`；跨域未正确配置时 `toDataURL` 可能抛错，调用方应捕获并回落文本。
 * - 返回的 `tileWidth`/`tileHeight` 应与 CSS `background-size` 一致，以保证重复间距与画布一致。
 */

/**
 * @typedef {object} WatermarkPatternOptions
 * @property {string|string[]} [text] 文本或多行；图片模式失败时由调用方传入。
 * @property {HTMLImageElement|null} [image] 已解码图片；为 `null` 时仅绘制文本。
 * @property {number} [fontSize=16] 字号（px）。
 * @property {string} [fontColor='rgba(0,0,0,0.15)'] 文本颜色。
 * @property {string} [fontFamily] 字体栈。
 * @property {number} [opacity=1] 全局透明度（0~1）。
 * @property {number} [rotate=-22] 旋转角（度，顺时针为正，与 Canvas `rotate` 一致）。
 * @property {number} [gapX=100] 单元水平间距（含内容区外的留白）。
 * @property {number} [gapY=100] 单元垂直间距。
 * @property {number} [width=160] 单格内容区宽度（文本/图片绘制参考框）。
 * @property {number} [height=80] 单格内容区高度。
 * @property {number} [offsetX=0] 在单元内额外平移（px，于旋转前施加在内容中心）。
 * @property {number} [offsetY=0] 在单元内额外平移（px）。
 */

/**
 * @param {WatermarkPatternOptions} options
 * @returns {{ dataUrl: string, tileWidth: number, tileHeight: number } | null}
 *          不可绘制（如画布被污染且无法导出）时返回 `null`。
 */
export function buildWatermarkPattern(options) {
  const {
    text,
    image = null,
    fontSize = 16,
    fontColor = 'rgba(0, 0, 0, 0.15)',
    fontFamily = 'PingFang SC, Microsoft YaHei, sans-serif',
    opacity = 1,
    rotate = -22,
    gapX = 100,
    gapY = 100,
    width = 160,
    height = 80,
    offsetX = 0,
    offsetY = 0
  } = options

  const tileWidth = Math.max(1, width + gapX)
  const tileHeight = Math.max(1, height + gapY)

  /** @type {HTMLCanvasElement} */
  const canvas = document.createElement('canvas')
  canvas.width = tileWidth
  canvas.height = tileHeight
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    return null
  }

  const cx = tileWidth / 2 + offsetX
  const cy = tileHeight / 2 + offsetY

  ctx.clearRect(0, 0, tileWidth, tileHeight)
  ctx.save()
  ctx.globalAlpha = Math.min(1, Math.max(0, opacity))
  ctx.translate(cx, cy)
  ctx.rotate((rotate * Math.PI) / 180)

  if (image && image.naturalWidth > 0 && image.naturalHeight > 0) {
    try {
      const scale = Math.min(width / image.naturalWidth, height / image.naturalHeight)
      const dw = image.naturalWidth * scale
      const dh = image.naturalHeight * scale
      ctx.drawImage(image, -dw / 2, -dh / 2, dw, dh)
    } catch {
      ctx.restore()
      return buildTextPatternInternal(ctx, canvas, {
        text,
        fontSize,
        fontColor,
        fontFamily,
        opacity,
        rotate,
        gapX,
        gapY,
        width,
        height,
        offsetX,
        offsetY
      })
    }
  } else {
    drawTextLines(ctx, text, fontSize, fontColor, fontFamily, width)
  }

  ctx.restore()

  try {
    const dataUrl = canvas.toDataURL('image/png')
    return { dataUrl, tileWidth, tileHeight }
  } catch {
    if (image) {
      return buildTextPatternInternal(null, canvas, {
        text,
        fontSize,
        fontColor,
        fontFamily,
        opacity,
        rotate,
        gapX,
        gapY,
        width,
        height,
        offsetX,
        offsetY
      })
    }
    return null
  }
}

/**
 * 图片绘制失败后改绘文本并导出。
 *
 * @param {CanvasRenderingContext2D | null} _discardCtx
 * @param {HTMLCanvasElement} canvas
 * @param {WatermarkPatternOptions} o
 * @returns {{ dataUrl: string, tileWidth: number, tileHeight: number } | null}
 */
function buildTextPatternInternal(_discardCtx, canvas, o) {
  const width = o.width ?? 160
  const height = o.height ?? 80
  const gapX = o.gapX ?? 100
  const gapY = o.gapY ?? 100
  const tileWidth = Math.max(1, width + gapX)
  const tileHeight = Math.max(1, height + gapY)
  canvas.width = tileWidth
  canvas.height = tileHeight
  const ctx = canvas.getContext('2d')
  if (!ctx) {
    return null
  }
  const cx = tileWidth / 2 + (o.offsetX ?? 0)
  const cy = tileHeight / 2 + (o.offsetY ?? 0)
  ctx.clearRect(0, 0, tileWidth, tileHeight)
  ctx.save()
  ctx.globalAlpha = Math.min(1, Math.max(0, o.opacity ?? 1))
  ctx.translate(cx, cy)
  ctx.rotate(((o.rotate ?? -22) * Math.PI) / 180)
  drawTextLines(
    ctx,
    o.text,
    o.fontSize ?? 16,
    o.fontColor ?? 'rgba(0, 0, 0, 0.15)',
    o.fontFamily ?? 'PingFang SC, Microsoft YaHei, sans-serif',
    width
  )
  ctx.restore()
  try {
    return { dataUrl: canvas.toDataURL('image/png'), tileWidth, tileHeight }
  } catch {
    return null
  }
}

/**
 * @param {CanvasRenderingContext2D} ctx
 * @param {string|string[]|undefined|null} text
 * @param {number} fontSize
 * @param {string} fontColor
 * @param {string} fontFamily
 * @param {number} width 单行最大宽度（`fillText` 第四参）
 */
function drawTextLines(ctx, text, fontSize, fontColor, fontFamily, width) {
  const lines =
    text == null || text === ''
      ? ['']
      : Array.isArray(text)
        ? text.map((l) => String(l))
        : [String(text)]

  ctx.font = `${fontSize}px ${fontFamily}`
  ctx.fillStyle = fontColor
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  const lineHeight = fontSize * 1.25
  const totalH = (lines.length - 1) * lineHeight
  let y = -totalH / 2
  for (const line of lines) {
    ctx.fillText(line, 0, y, width)
    y += lineHeight
  }
}
