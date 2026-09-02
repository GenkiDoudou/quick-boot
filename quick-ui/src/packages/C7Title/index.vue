<template>
  <div
      class="c7-title"
      :class="{ 'c7-title--bordered': showBorder }"
      :style="rootInlineStyle"
  >
    <div class="c7-title__row">
      <span v-if="iconAreaVisible" class="c7-title__icon-wrap" aria-hidden="true">
        <slot name="icon">
          <el-icon v-if="resolvedIconComponent" :size="18">
            <component :is="resolvedIconComponent"/>
          </el-icon>
        </slot>
      </span>
      <el-text
          :tag="resolvedTag"
          class="c7-title__text"
          :class="textPresetClass"
          :style="textCustomStyle"
      >
        <slot name="title">{{ effectiveText }}</slot>
      </el-text>
      <div class="c7-title__actions">
        <slot/>
      </div>
    </div>
  </div>
</template>

<!-- 与 script setup 合并为同一模块作用域；供 defineProps validator 引用（避免 SFC 提升报错） -->
<script>
const HEADING_LEVELS = ['h1', 'h2', 'h3', 'h4', 'h5', 'h6']
const VALID_TAGS = ['h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'div', 'p']
const CUSTOM_SIZE_RE = /^\s*\d+(\.\d+)?(px|rem|em)\s*$/i
</script>

<script setup>
import {computed, useSlots, watchEffect} from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

defineOptions({name: 'C7Title', inheritAttrs: true})

/**
 * @deprecated 新页面优先使用 {@code ElText} + 布局类；本组件保留兼容旧区块标题样式。
 * C7 区块标题：统一字号/加粗、底部分割线、左侧图标（EP 图标名或 `#icon`）、右侧默认插槽操作区。
 *
 * **`resolvedTag`（语义标签）**
 * - **`tag` prop 为 `undefined`**（未传）：若 **`labelSize`** 为 **`h1`~`h6`**，则语义标签与 **`labelSize`** 同级；否则为 **`h4`**。
 * - **`tag` 已传**：始终使用该标签（含显式 **`h4`**），**不**被 h 级 **`labelSize`** 改写语义层级。
 *
 * **字号**
 * - **`labelSize`** 为 **`h1`~`h6`**：按上表预设；与 **`tag`** 解耦（例如 **`labelSize=h2`** + **`tag=div`** 时仍为 h2 档字号）。
 * - **`labelSize`** 为带 **`px`/`rem`/`em`** 的数值串：仅作用于 **`font-size`**（及约定行高），**不**改 **`resolvedTag`**。
 * - 非法 **`labelSize`**：开发环境 **`console.warn`**，字号回退为当前 **`resolvedTag`** 对应预设（非标题标签时回退 **`h4`**）。
 *
 * **颜色**：根节点 CSS 变量 **`--c7-title-decoration-color`**，取 **`decorationColor` ?? `labelColor` ?? Element 主色**。
 *
 * @slot icon 若提供则 **完全覆盖** **`icon` 字符串** 解析结果（自行放置 **`el-icon`** 等）。
 * @slot title 若提供则 **覆盖** **`label`/`title`** 字符串。
 * @slot default 右侧 **actions** 区。
 */
const props = defineProps({
  /**
   * 语义标签；**默认 `undefined`**（与「显式传 `h4`」区分，见组件说明）。
   * @type {'h1'|'h2'|'h3'|'h4'|'h5'|'h6'|'div'|'p'|undefined}
   */
  tag: {
    type: String,
    default: undefined,
    validator: (v) => v === undefined || VALID_TAGS.includes(v),
  },
  /** 主标题文案（优先于 **`title`**） */
  label: {type: String, default: ''},
  /** **`label` 的兼容别名** */
  title: {type: String, default: ''},
  /**
   * 预设层级 **`h1`~`h6`** 或自定义字号（如 **`20px`**、**`1.2rem`**）。
   * @default 'h4'
   */
  labelSize: {type: String, default: 'h4'},
  /** 装饰线/底边颜色（优先于 **`labelColor`**） */
  decorationColor: {type: String, default: undefined},
  /** **`decorationColor` 的兼容别名** */
  labelColor: {type: String, default: undefined},
  /** 是否显示底部线（与「装饰线」同一元素） */
  showBorder: {type: Boolean, default: true},
  /** `@element-plus/icons-vue` 导出组件名（PascalCase，如 **`Setting`**） */
  icon: {type: String, default: ''},
})

const slots = useSlots()

function isHeadingSize(v) {
  const t = typeof v === 'string' ? v.trim() : ''
  return HEADING_LEVELS.includes(t)
}

function isCustomSize(v) {
  return typeof v === 'string' && CUSTOM_SIZE_RE.test(v.trim())
}

const resolvedTag = computed(() => {
  if (props.tag !== undefined && props.tag !== null) {
    const t = props.tag
    if (VALID_TAGS.includes(t)) {
      return t
    }
    if (import.meta.env.DEV) {
      console.warn(`[C7Title] 非法 tag="${t}"，已回退为 h4`)
    }
    return 'h4'
  }
  if (isHeadingSize(props.labelSize)) {
    return props.labelSize.trim()
  }
  return 'h4'
})

/** 用于非法 `labelSize` 时回退字号的层级键 */
const presetKeyForFallback = computed(() => {
  const t = resolvedTag.value
  return isHeadingSize(t) ? t : 'h4'
})

const textPresetClass = computed(() => {
  if (isCustomSize(props.labelSize)) {
    return ''
  }
  if (isHeadingSize(props.labelSize)) {
    return `c7-title__text--sz-${props.labelSize.trim()}`
  }
  return `c7-title__text--sz-${presetKeyForFallback.value}`
})

watchEffect(() => {
  if (!import.meta.env.DEV) {
    return
  }
  const raw = props.labelSize?.trim() ?? ''
  if (!raw) {
    return
  }
  if (!isHeadingSize(raw) && !isCustomSize(raw)) {
    console.warn(`[C7Title] 无法解析 labelSize="${raw}"，已按 ${presetKeyForFallback.value} 预设回退字号`)
  }
})

const textCustomStyle = computed(() => {
  if (!isCustomSize(props.labelSize)) {
    return undefined
  }
  return {
    fontSize: props.labelSize.trim(),
    lineHeight: 1.4,
  }
})

const effectiveDecorationColor = computed(
    () => props.decorationColor ?? props.labelColor ?? undefined,
)

const rootInlineStyle = computed(() => {
  const c = effectiveDecorationColor.value
  return c
      ? {'--c7-title-decoration-color': c}
      : {'--c7-title-decoration-color': 'var(--el-color-primary)'}
})

const effectiveText = computed(() => props.label || props.title)

watchEffect(() => {
  if (import.meta.env.DEV && props.label && props.title) {
    console.warn('[C7Title] 同时传入 label 与 title，仅以 label 为准')
  }
})

const resolvedIconComponent = computed(() => {
  if (slots.icon) {
    return null
  }
  const name = props.icon?.trim()
  if (!name) {
    return null
  }
  const comp = ElementPlusIconsVue[name]
  if (!comp && import.meta.env.DEV) {
    console.warn(`[C7Title] 未知 icon="${name}"（应为 @element-plus/icons-vue 的 PascalCase 导出名）`)
  }
  return comp || null
})

const iconAreaVisible = computed(() => Boolean(slots.icon) || Boolean(resolvedIconComponent.value))
</script>

<style scoped lang="scss">
.c7-title {
  width: 100%;
  box-sizing: border-box;
}

.c7-title__row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 28px;
}

.c7-title--bordered .c7-title__row {
  padding-bottom: 8px;
  border-bottom: 2px solid var(--c7-title-decoration-color, var(--el-color-primary));
}

.c7-title__icon-wrap {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  color: var(--el-text-color-regular);
}

.c7-title__text {
  flex: 1;
  min-width: 0;
  margin: 0 !important;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.c7-title__text--sz-h1 {
  font-size: 2rem;
  line-height: 1.25;
}

.c7-title__text--sz-h2 {
  font-size: 1.5rem;
  line-height: 1.35;
}

.c7-title__text--sz-h3 {
  font-size: 1.25rem;
  line-height: 1.4;
}

.c7-title__text--sz-h4 {
  font-size: 1.125rem;
  line-height: 1.45;
}

.c7-title__text--sz-h5 {
  font-size: 1rem;
  line-height: 1.5;
}

.c7-title__text--sz-h6 {
  font-size: 0.875rem;
  line-height: 1.55;
}

.c7-title__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}
</style>
