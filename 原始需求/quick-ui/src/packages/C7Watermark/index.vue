<template>
  <!-- fullscreen 模式：固定定位覆盖全屏 -->
  <div v-if="fullscreen" class="c7-watermark c7-watermark--fullscreen">
    <!-- 水印层 -->
    <div v-if="!disabled" ref="watermarkRef" class="c7-watermark__layer" />
  </div>

  <!-- 普通容器模式 -->
  <div v-else class="c7-watermark c7-watermark--container">
    <div v-if="!disabled" ref="watermarkRef" class="c7-watermark__layer" />
    <slot />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, getCurrentInstance } from 'vue'

defineOptions({ name: 'C7Watermark' })

const props = defineProps({
  /** 文本水印，字符串或字符串数组（多行） */
  text: {
    type: [String, Array],
    default: ''
  },
  /** 图片水印 URL，优先于文本 */
  image: {
    type: String,
    default: ''
  },
  /** 字体大小（px），默认 14 */
  fontSize: {
    type: Number,
    default: 14
  },
  /** 字体颜色，默认半透明黑色 */
  fontColor: {
    type: String,
    default: 'rgba(0, 0, 0, 0.12)'
  },
  /** 字体，默认跟随系统 */
  fontFamily: {
    type: String,
    default: 'sans-serif'
  },
  /** 透明度 0~1，默认 1（颜色本身控制透明度） */
  opacity: {
    type: Number,
    default: 1
  },
  /** 旋转角度（度），默认 -25 */
  rotate: {
    type: Number,
    default: -25
  },
  /** 水印单元水平间距（px），默认 100 */
  gapX: {
    type: Number,
    default: 100
  },
  /** 水印单元垂直间距（px），默认 100 */
  gapY: {
    type: Number,
    default: 100
  },
  /** 水印单元宽度（px），默认 160 */
  width: {
    type: Number,
    default: 160
  },
  /** 水印单元高度（px），默认 80 */
  height: {
    type: Number,
    default: 80
  },
  /** 水平偏移（px），默认 0 */
  offsetX: {
    type: Number,
    default: 0
  },
  /** 垂直偏移（px），默认 0 */
  offsetY: {
    type: Number,
    default: 0
  },
  /**
   * false 时通过 MutationObserver 防止水印被删除，默认 false
   */
  editable: {
    type: Boolean,
    default: false
  },
  /** 全屏水印模式，默认 false */
  fullscreen: {
    type: Boolean,
    default: false
  },
  /** 水印层 z-index，默认 9 */
  zIndex: {
    type: Number,
    default: 9
  },
  /** 禁用水印，默认 false */
  disabled: {
    type: Boolean,
    default: false
  }
})

// 使用组件 uid 稳定化水印类名
const uid = getCurrentInstance()?.uid
const watermarkClass = `c7-watermark-${uid}`

const watermarkRef = ref(null)
let observer = null

// ── 创建文本水印 Canvas，返回 dataURL ──
function createTextWatermark() {
  const canvas = document.createElement('canvas')
  const totalWidth = props.width + props.gapX
  const totalHeight = props.height + props.gapY
  canvas.width = totalWidth
  canvas.height = totalHeight

  const ctx = canvas.getContext('2d')
  ctx.globalAlpha = props.opacity
  ctx.translate(totalWidth / 2 + props.offsetX, totalHeight / 2 + props.offsetY)
  ctx.rotate((Math.PI / 180) * props.rotate)

  ctx.font = `${props.fontSize}px ${props.fontFamily}`
  ctx.fillStyle = props.fontColor
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'

  const lines = Array.isArray(props.text) ? props.text : [props.text]
  const lineHeight = props.fontSize * 1.5
  const startY = -((lines.length - 1) * lineHeight) / 2
  lines.forEach((line, i) => {
    ctx.fillText(line, 0, startY + i * lineHeight)
  })

  return canvas.toDataURL()
}

// ── 创建图片水印 Canvas，返回 Promise<dataURL> ──
function createImageWatermark() {
  return new Promise((resolve) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const totalWidth = props.width + props.gapX
      const totalHeight = props.height + props.gapY
      canvas.width = totalWidth
      canvas.height = totalHeight
      const ctx = canvas.getContext('2d')
      ctx.globalAlpha = props.opacity
      ctx.translate(totalWidth / 2 + props.offsetX, totalHeight / 2 + props.offsetY)
      ctx.rotate((Math.PI / 180) * props.rotate)
      ctx.drawImage(img, -props.width / 2, -props.height / 2, props.width, props.height)
      resolve(canvas.toDataURL())
    }
    img.onerror = () => resolve(createTextWatermark())
    img.src = props.image
  })
}

// ── 应用水印到 DOM ──
async function applyWatermark() {
  if (props.disabled || !watermarkRef.value) return

  // 断开旧观察，防重复
  if (observer) {
    observer.disconnect()
    observer = null
  }

  // 获取背景 dataURL
  const dataUrl = props.image
    ? await createImageWatermark()
    : createTextWatermark()

  if (!watermarkRef.value) return

  // 应用样式
  const el = watermarkRef.value
  el.className = `c7-watermark__layer ${watermarkClass}`
  Object.assign(el.style, {
    position: 'absolute',
    inset: '0',
    width: '100%',
    height: '100%',
    backgroundImage: `url(${dataUrl})`,
    backgroundRepeat: 'repeat',
    backgroundSize: `${props.width + props.gapX}px ${props.height + props.gapY}px`,
    pointerEvents: 'none',
    zIndex: props.zIndex
  })

  // 防删除：MutationObserver 监听父容器
  if (!props.editable && el.parentElement) {
    observer = new MutationObserver(() => {
      const existing = el.parentElement?.querySelector(`.${watermarkClass}`)
      if (!existing) {
        // 水印被删除，重新插入
        el.parentElement?.appendChild(el)
        applyWatermark()
      }
    })
    observer.observe(el.parentElement, {
      childList: true,
      subtree: false,
      attributes: true,
      attributeFilter: ['style', 'class']
    })
  }
}

// ── 监听所有相关 props 变化，自动重绘 ──
watch(
  () => [
    props.text, props.image, props.fontSize, props.fontColor,
    props.fontFamily, props.opacity, props.rotate,
    props.gapX, props.gapY, props.width, props.height,
    props.offsetX, props.offsetY, props.zIndex, props.disabled
  ],
  () => applyWatermark(),
  { deep: true }
)

onMounted(() => {
  applyWatermark()
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style scoped>
.c7-watermark {
  /* 普通容器模式 */
  &--container {
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
  }

  /* 全屏模式 */
  &--fullscreen {
    position: fixed;
    inset: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    overflow: hidden;
  }

  /* 水印层 */
  &__layer {
    position: absolute;
    inset: 0;
    pointer-events: none;
  }
}
</style>
