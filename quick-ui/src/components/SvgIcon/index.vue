<template>
  <svg :class="svgClass" aria-hidden="true">
    <use :xlink:href="iconName" />
  </svg>
</template>

<script setup>
/**
 * SVG 雪碧图图标：通过 symbol id（icon-[dir]-[name]）引用 vite-plugin-svg-icons 生成的 sprite。
 *
 * @prop {string} iconClass 图标名（对应 assets/icons/svg 下文件名，不含扩展名）
 * @prop {string} [className] 附加 CSS 类名
 */
import { computed } from 'vue'

const props = defineProps({
  iconClass: {
    type: String,
    required: true
  },
  className: {
    type: String,
    default: ''
  }
})

const iconName = computed(() => `#icon-${props.iconClass}`)
const svgClass = computed(() => {
  if (props.className) {
    return 'svg-icon ' + props.className
  } else {
    return 'svg-icon'
  }
})
</script>

<style scoped>
.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
</style>
