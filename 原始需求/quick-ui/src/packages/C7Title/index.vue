<template>
  <component :is="tag" class="c7-title" :class="{ 'has-border': showBorder }">
    <!-- 底部装饰线通过 CSS 变量控制 -->
    <div
      class="c7-title__inner"
      :style="innerStyle"
    >
      <!-- 前置图标 -->
      <slot name="icon">
        <el-icon v-if="icon" class="c7-title__icon">
          <component :is="resolvedIcon" />
        </el-icon>
      </slot>

      <!-- 标题文字 -->
      <span class="c7-title__label" :class="{ 'is-bold': bold }" :style="labelStyle">
        <slot name="title">{{ label }}</slot>
      </span>

      <!-- 右侧操作区 -->
      <div class="c7-title__actions">
        <slot />
      </div>
    </div>
  </component>
</template>

<script setup>
import { computed } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

defineOptions({ name: 'C7Title', inheritAttrs: false })

// h1~h6 预设字号映射
const sizeMap = {
  h1: '26px',
  h2: '20px',
  h3: '18px',
  h4: '16px',
  h5: '14px',
  h6: '12px',
}

// 支持 px / em / rem 单位
const sizeRegex = /^(\d+(\.\d+)?)(px|em|rem)$/

const props = defineProps({
  /** 标题文本 */
  label: {
    type: String,
    default: '默认标题'
  },
  /**
   * 尺寸：h1~h6 预设，或自定义如 '20px'、'1.5rem'、'1.2em'
   * 默认: 'h2'
   */
  labelSize: {
    type: String,
    default: 'h2'
  },
  /** 底部装饰线颜色（原 labelColor） */
  decorationColor: {
    type: String,
    default: ''
  },
  /** @deprecated 请使用 decorationColor，保留为兼容别名 */
  labelColor: {
    type: String,
    default: ''
  },
  /** 渲染的 HTML 标签，默认 'div' */
  tag: {
    type: String,
    default: 'div',
    validator: (v) => ['h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'div', 'p'].includes(v)
  },
  /** 标题前置图标名（Element Plus 图标） */
  icon: {
    type: String,
    default: ''
  },
  /** 是否加粗，默认 true */
  bold: {
    type: Boolean,
    default: true
  },
  /** 是否显示底部边框线，默认 true */
  showBorder: {
    type: Boolean,
    default: true
  }
})

// ── 解析字号 ──
const resolvedFontSize = computed(() => {
  const s = props.labelSize
  if (sizeMap[s]) return sizeMap[s]
  if (sizeRegex.test(s)) return s
  return sizeMap['h2']
})

// ── 解析装饰线颜色（decorationColor 优先，兼容旧 labelColor）──
const resolvedDecorationColor = computed(() => {
  return props.decorationColor || props.labelColor || ''
})

// ── inner 行内样式：通过 CSS 变量传递装饰线颜色 ──
const innerStyle = computed(() => {
  const style = {}
  if (resolvedDecorationColor.value) {
    style['--c7-title-decoration-color'] = resolvedDecorationColor.value
  }
  return style
})

// ── 标题字号样式 ──
const labelStyle = computed(() => ({
  fontSize: resolvedFontSize.value
}))

// ── 解析图标组件 ──
const resolvedIcon = computed(() => {
  if (!props.icon) return null
  return ElementPlusIconsVue[props.icon] || null
})
</script>

<style scoped lang="scss">
.c7-title {
  display: block;
  margin: 0;
  padding: 0;

  &.has-border {
    border-bottom: 3px solid var(--c7-title-border-color, var(--el-border-color, #dcdfe6));
    padding-bottom: 10px;
    margin-bottom: 16px;
  }

  &__inner {
    display: flex;
    align-items: center;
    gap: 6px;
    position: relative;

    // 底部装饰线伪元素（通过 CSS 变量控制显隐）
    &::before {
      content: '';
      display: block;
      position: absolute;
      bottom: -13px;
      left: 0;
      width: 40px;
      height: 3px;
      background: var(--c7-title-decoration-color, transparent);
      border-radius: 2px;
    }
  }

  &__icon {
    font-size: 1em;
    color: var(--c7-title-color, var(--el-text-color-primary, #303133));
    flex-shrink: 0;
  }

  &__label {
    color: var(--c7-title-color, var(--el-text-color-primary, #303133));
    font-family: var(--c7-title-font-family, var(--el-font-family, inherit));
    font-weight: 400;
    line-height: 1.4;
    flex: 1;

    &.is-bold {
      font-weight: 600;
    }
  }

  // 右侧操作区
  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: auto;

    // 如果操作区为空则不占位
    &:empty {
      display: none;
    }
  }
}
</style>
