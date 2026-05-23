<template>
  <div
    ref="watermarkRef"
    class="c7-watermark"
    :style="containerStyle"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, defineOptions, nextTick } from 'vue'

defineOptions({
  name: 'C7Watermark'
})

/**
 * 水印配置接口
 */
interface WatermarkConfig {
  // 水印文本
  text?: string
  // 水印图片 URL
  image?: string
  // 字体大小
  fontSize?: number
  // 字体颜色
  fontColor?: string
  // 字体家族
  fontFamily?: string
  // 透明度
  opacity?: number
  // 旋转角度
  rotate?: number
  // 水印间距（水平）
  gapX?: number
  // 水印间距（垂直）
  gapY?: number
  // 水印宽度
  width?: number
  // 水印高度
  height?: number
  // 水印偏移量（水平）
  offsetX?: number
  // 水印偏移量（垂直）
  offsetY?: number
  // 是否可编辑（防止删除）
  editable?: boolean
}

/**
 * 组件属性接口
 */
interface Props {
  // 水印文本
  text?: string | string[]
  // 水印图片 URL
  image?: string
  // 字体大小
  fontSize?: number
  // 字体颜色
  fontColor?: string
  // 字体家族
  fontFamily?: string
  // 透明度
  opacity?: number
  // 旋转角度
  rotate?: number
  // 水印间距（水平）
  gapX?: number
  // 水印间距（垂直）
  gapY?: number
  // 水印宽度
  width?: number
  // 水印高度
  height?: number
  // 水印偏移量（水平）
  offsetX?: number
  // 水印偏移量（垂直）
  offsetY?: number
  // 是否可编辑（防止删除）
  editable?: boolean
  // 是否全屏水印
  fullscreen?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  text: '',
  image: undefined,
  fontSize: 16,
  fontColor: 'rgba(0, 0, 0, 0.15)',
  fontFamily: 'sans-serif',
  opacity: 0.15,
  rotate: -22,
  gapX: 100,
  gapY: 100,
  width: 120,
  height: 64,
  offsetX: 0,
  offsetY: 0,
  editable: false,
  fullscreen: false
})

const watermarkRef = ref<HTMLElement>()
const watermarkId = `c7-watermark-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
let observer: MutationObserver | null = null

/**
 * 计算属性：容器样式
 */
const containerStyle = computed(() => {
  if (props.fullscreen) {
    return {
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      pointerEvents: 'none',
      zIndex: 9999
    }
  }
  return {
    position: 'relative',
    width: '100%',
    height: '100%'
  }
})

/**
 * 创建水印 Canvas
 */
const createWatermarkCanvas = (): HTMLCanvasElement => {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  
  if (!ctx) {
    throw new Error('无法创建 Canvas 上下文')
  }
  
  const ratio = window.devicePixelRatio || 1
  const width = props.width * ratio
  const height = props.height * ratio
  
  canvas.width = width
  canvas.height = height
  canvas.style.width = `${props.width}px`
  canvas.style.height = `${props.height}px`
  
  ctx.scale(ratio, ratio)
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.font = `${props.fontSize}px ${props.fontFamily}`
  ctx.fillStyle = props.fontColor
  ctx.globalAlpha = props.opacity
  
  // 旋转
  ctx.translate(props.width / 2, props.height / 2)
  ctx.rotate((props.rotate * Math.PI) / 180)
  ctx.translate(-props.width / 2, -props.height / 2)
  
  // 如果有图片，绘制图片
  if (props.image) {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.src = props.image
    img.onload = () => {
      ctx.drawImage(img, 0, 0, props.width, props.height)
    }
  } else {
    // 绘制文本
    const texts = Array.isArray(props.text) ? props.text : [props.text]
    const lineHeight = props.fontSize + 4
    const startY = (props.height - (texts.length - 1) * lineHeight) / 2
    
    texts.forEach((text, index) => {
      ctx.fillText(text, props.width / 2, startY + index * lineHeight)
    })
  }
  
  return canvas
}

/**
 * 创建水印背景
 */
const createWatermarkBackground = (): string => {
  const canvas = createWatermarkCanvas()
  return canvas.toDataURL()
}

/**
 * 应用水印
 */
const applyWatermark = () => {
  if (!watermarkRef.value) return
  
  // 移除旧的水印
  const oldWatermark = watermarkRef.value.querySelector(`.${watermarkId}`)
  if (oldWatermark) {
    oldWatermark.remove()
  }
  
  // 创建水印元素
  const watermark = document.createElement('div')
  watermark.className = watermarkId
  watermark.style.position = 'absolute'
  watermark.style.top = '0'
  watermark.style.left = '0'
  watermark.style.width = '100%'
  watermark.style.height = '100%'
  watermark.style.pointerEvents = 'none'
  watermark.style.backgroundImage = `url(${createWatermarkBackground()})`
  watermark.style.backgroundRepeat = 'repeat'
  watermark.style.backgroundPosition = `${props.offsetX}px ${props.offsetY}px`
  watermark.style.backgroundSize = `${props.width + props.gapX}px ${props.height + props.gapY}px`
  watermark.style.zIndex = '1'
  
  watermarkRef.value.appendChild(watermark)
  
  // 监听 DOM 变化（防止删除水印）
  if (!props.editable && !observer) {
    observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'childList') {
          const removedNodes = Array.from(mutation.removedNodes)
          const watermarkElement = watermarkRef.value?.querySelector(`.${watermarkId}`)
          
          if (!watermarkElement && removedNodes.some(node => node === watermark)) {
            // 水印被删除，重新应用
            nextTick(() => {
              applyWatermark()
            })
          }
        } else if (mutation.type === 'attributes') {
          const watermarkElement = watermarkRef.value?.querySelector(`.${watermarkId}`)
          if (watermarkElement && mutation.target === watermarkElement) {
            // 水印属性被修改，重新应用
            nextTick(() => {
              applyWatermark()
            })
          }
        }
      })
    })
    
    observer.observe(watermarkRef.value, {
      childList: true,
      attributes: true,
      attributeFilter: ['style', 'class']
    })
  }
}

/**
 * 监听属性变化
 */
watch(
  () => [
    props.text,
    props.image,
    props.fontSize,
    props.fontColor,
    props.fontFamily,
    props.opacity,
    props.rotate,
    props.gapX,
    props.gapY,
    props.width,
    props.height,
    props.offsetX,
    props.offsetY
  ],
  () => {
    applyWatermark()
  },
  { deep: true }
)

onMounted(() => {
  nextTick(() => {
    applyWatermark()
  })
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
  position: relative;
  overflow: hidden;
}
</style>

