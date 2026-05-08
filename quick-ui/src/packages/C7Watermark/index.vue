<template>
  <div ref="rootRef" class="c7-watermark" v-bind="$attrs">
    <slot/>
    <div
        v-if="!disabled && layerVisible"
        :key="layerKey"
        ref="layerRef"
        class="c7-watermark__layer"
        :style="layerStyle"
    />
  </div>
</template>

<script setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch
} from 'vue'
import { buildWatermarkPattern } from './buildWatermarkPattern.js'

defineOptions({
  name: 'C7Watermark',
  inheritAttrs: false
})

/**
 * 页面/全屏水印：Canvas 生成平铺图，`image` 优先，失败回落 `text`。
 *
 * **防删**：`tamperResistant === true` 开启 MutationObserver；若仅传 `editable=false` 亦开启。
 * 若 **`tamperResistant` 显式传入**（含 `false`），**以其为准**，覆盖 `editable`。
 *
 * **`fullscreenScope`**：仅当 `fullscreen=true` 时生效；`false` 时忽略（见 props 说明）。
 */
const props = defineProps({
  /** 文本或字符串数组（多行）；图片失败时作为回落 */
  text: { type: [String, Array], default: '' },
  /** 图片 URL；优先于 `text`，加载或绘制失败回落文本 */
  image: { type: String, default: '' },
  /** 全屏固定定位；`false` 为容器模式（水印层 `absolute` 铺满根节点） */
  fullscreen: { type: Boolean, default: false },
  /**
   * 全屏覆盖范围：`viewport` 仅视口；`document` 随 `document.documentElement.scrollHeight` 延伸。
   * 仅 **`fullscreen=true`** 时生效。
   */
  fullscreenScope: { type: String, default: 'viewport' },
  /** 为 `true` 时不渲染水印、不挂载 MutationObserver / 文档尺寸监听 */
  disabled: { type: Boolean, default: false },
  /**
   * 显式防删开关。传入任意布尔时 **优先于 `editable`**。
   * 未传入时：仅当 **`editable === false`** 时启用防删。
   */
  tamperResistant: { type: Boolean, default: undefined },
  /**
   * 兼容原始需求命名：`false` 表示开启防删（与 `tamperResistant: true` 同效），
   * 缺省或 `true` 表示不按该字段启用防删（仍可由 `tamperResistant` 显式控制）。
   */
  editable: { type: Boolean, default: undefined },
  /** 叠放层级（全屏必选；容器模式亦应用于水印层） */
  zIndex: { type: Number, default: 4100 },
  /** 传给 `Image.crossOrigin`，如 `'anonymous'`、`''`；`undefined` 不传 */
  crossOrigin: { type: String, default: undefined },
  fontSize: { type: Number, default: 16 },
  fontColor: { type: String, default: 'rgba(0, 0, 0, 0.15)' },
  fontFamily: { type: String, default: 'PingFang SC, Microsoft YaHei, sans-serif' },
  /** 全局透明度 0~1 */
  opacity: { type: Number, default: 1 },
  /** 旋转角（度） */
  rotate: { type: Number, default: -22 },
  gapX: { type: Number, default: 100 },
  gapY: { type: Number, default: 100 },
  /** 单格内容区宽度（px） */
  width: { type: Number, default: 160 },
  /** 单格内容区高度（px） */
  height: { type: Number, default: 80 },
  offsetX: { type: Number, default: 0 },
  offsetY: { type: Number, default: 0 }
})

const rootRef = ref(null)
const layerRef = ref(null)
/** 水印层被手动移除时递增，强制 Vue 重建节点以便恢复 */
const layerKey = ref(0)

/** @type {{ dataUrl: string, tileW: number, tileH: number } | null} */
const pattern = ref(null)

let imageLoadToken = 0
let paintRaf = 0
let resizeTimer = 0
/** @type {MutationObserver | null} */
let tamperObserver = null
/** @type {ResizeObserver | null} */
let docResizeObserver = null
let tamperRestoreTimer = 0

const documentCoverHeight = ref(
  typeof document !== 'undefined' ? document.documentElement.scrollHeight : 0
)

const effectiveTamperResistant = computed(() => {
  if (props.tamperResistant !== undefined) {
    return props.tamperResistant
  }
  if (props.editable === false) {
    return true
  }
  return false
})

const layerVisible = computed(() => Boolean(pattern.value?.dataUrl))

const layerStyle = computed(() => {
  const p = pattern.value
  if (!p?.dataUrl) {
    return { display: 'none' }
  }
  const base = {
    backgroundImage: `url(${p.dataUrl})`,
    backgroundRepeat: 'repeat',
    backgroundSize: `${p.tileW}px ${p.tileH}px`,
    pointerEvents: 'none',
    userSelect: 'none',
    zIndex: props.zIndex
  }
  if (props.fullscreen) {
    if (props.fullscreenScope === 'document') {
      return {
        ...base,
        position: 'fixed',
        top: '0',
        left: '0',
        width: '100%',
        height: `${documentCoverHeight.value}px`
      }
    }
    return {
      ...base,
      position: 'fixed',
      inset: '0',
      width: '100%',
      height: '100%'
    }
  }
  return {
    ...base,
    position: 'absolute',
    inset: '0',
    width: '100%',
    height: '100%'
  }
})

/**
 * @param {string} url
 * @param {string|undefined} crossOrigin
 * @returns {Promise<HTMLImageElement>}
 */
function loadImageElement(url, crossOrigin) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    if (crossOrigin !== undefined) {
      img.crossOrigin = crossOrigin
    }
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error('image load error'))
    img.src = url
  })
}

function patternOptionsFromProps() {
  return {
    text: props.text,
    fontSize: props.fontSize,
    fontColor: props.fontColor,
    fontFamily: props.fontFamily,
    opacity: props.opacity,
    rotate: props.rotate,
    gapX: props.gapX,
    gapY: props.gapY,
    width: props.width,
    height: props.height,
    offsetX: props.offsetX,
    offsetY: props.offsetY
  }
}

function updateDocumentCoverHeight() {
  if (typeof document === 'undefined') {
    return
  }
  documentCoverHeight.value = document.documentElement.scrollHeight
}

async function applyPattern() {
  if (props.disabled) {
    pattern.value = null
    return
  }

  const token = ++imageLoadToken
  const baseOpts = patternOptionsFromProps()

  /** @type {{ dataUrl: string, tileW: number, tileH: number } | null} */
  let next = null

  const setFromBuild = (built) => {
    if (!built) {
      return
    }
    next = {
      dataUrl: built.dataUrl,
      tileW: built.tileWidth,
      tileH: built.tileHeight
    }
  }

  if (props.image) {
    try {
      const img = await loadImageElement(props.image, props.crossOrigin)
      if (token !== imageLoadToken) {
        return
      }
      let built = buildWatermarkPattern({ ...baseOpts, image: img })
      if (!built) {
        built = buildWatermarkPattern({ ...baseOpts, image: null })
      }
      setFromBuild(built)
    } catch {
      if (token !== imageLoadToken) {
        return
      }
      const built = buildWatermarkPattern({ ...baseOpts, image: null })
      setFromBuild(built)
    }
  } else {
    const built = buildWatermarkPattern({ ...baseOpts, image: null })
    setFromBuild(built)
  }

  if (token !== imageLoadToken) {
    return
  }
  pattern.value = next
}

function scheduleApplyPattern() {
  cancelAnimationFrame(paintRaf)
  paintRaf = requestAnimationFrame(() => {
    paintRaf = 0
    void applyPattern()
  })
}

function scheduleApplyPatternDebouncedResize() {
  if (resizeTimer) {
    clearTimeout(resizeTimer)
  }
  resizeTimer = window.setTimeout(() => {
    resizeTimer = 0
    updateDocumentCoverHeight()
    scheduleApplyPattern()
  }, 120)
}

/**
 * MutationObserver：监视根节点子树；`.c7-watermark__layer` 被移除时递增 `layerKey` 强制重建。
 */
function attachTamperObserver() {
  detachTamperObserver()
  const root = rootRef.value
  if (!root || !effectiveTamperResistant.value || props.disabled) {
    return
  }
  tamperObserver = new MutationObserver(() => {
    if (props.disabled || !effectiveTamperResistant.value) {
      return
    }
    if (tamperRestoreTimer) {
      clearTimeout(tamperRestoreTimer)
    }
    tamperRestoreTimer = window.setTimeout(() => {
      tamperRestoreTimer = 0
      const el = rootRef.value
      if (!el || props.disabled) {
        return
      }
      const layer = el.querySelector('.c7-watermark__layer')
      if (!layer && pattern.value?.dataUrl) {
        layerKey.value += 1
        nextTick(() => scheduleApplyPattern())
      }
    }, 40)
  })
  tamperObserver.observe(root, { childList: true, subtree: true })
}

function detachTamperObserver() {
  if (tamperObserver) {
    tamperObserver.disconnect()
    tamperObserver = null
  }
}

function attachDocumentResizeWatchers() {
  detachDocumentResizeWatchers()
  if (!props.fullscreen || props.fullscreenScope !== 'document' || props.disabled) {
    return
  }
  updateDocumentCoverHeight()
  window.addEventListener('resize', scheduleApplyPatternDebouncedResize)
  if (typeof ResizeObserver !== 'undefined') {
    docResizeObserver = new ResizeObserver(() => scheduleApplyPatternDebouncedResize())
    docResizeObserver.observe(document.documentElement)
  }
}

function detachDocumentResizeWatchers() {
  window.removeEventListener('resize', scheduleApplyPatternDebouncedResize)
  if (docResizeObserver) {
    docResizeObserver.disconnect()
    docResizeObserver = null
  }
}

watch(
  () => [
    props.disabled,
    props.text,
    props.image,
    props.crossOrigin,
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
    if (props.disabled) {
      pattern.value = null
      detachTamperObserver()
      detachDocumentResizeWatchers()
      return
    }
    scheduleApplyPattern()
  },
  { deep: true }
)

watch(
  () => [props.fullscreen, props.fullscreenScope, props.disabled],
  () => {
    detachDocumentResizeWatchers()
    if (!props.disabled && props.fullscreen && props.fullscreenScope === 'document') {
      attachDocumentResizeWatchers()
    }
    scheduleApplyPattern()
  }
)

watch(
  () => [effectiveTamperResistant.value, props.disabled],
  () => {
    detachTamperObserver()
    if (effectiveTamperResistant.value && !props.disabled) {
      nextTick(() => attachTamperObserver())
    }
  }
)

onMounted(() => {
  scheduleApplyPattern()
  nextTick(() => {
    if (effectiveTamperResistant.value && !props.disabled) {
      attachTamperObserver()
    }
    if (!props.disabled && props.fullscreen && props.fullscreenScope === 'document') {
      attachDocumentResizeWatchers()
    }
  })
})

onBeforeUnmount(() => {
  imageLoadToken += 1
  cancelAnimationFrame(paintRaf)
  if (resizeTimer) {
    clearTimeout(resizeTimer)
    resizeTimer = 0
  }
  if (tamperRestoreTimer) {
    clearTimeout(tamperRestoreTimer)
    tamperRestoreTimer = 0
  }
  detachTamperObserver()
  detachDocumentResizeWatchers()
})
</script>

<style scoped>
.c7-watermark {
  position: relative;
  display: block;
  width: 100%;
  min-height: 0;
}

.c7-watermark__layer {
  box-sizing: border-box;
}
</style>
