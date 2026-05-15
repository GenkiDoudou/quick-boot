<template>
  <el-card
    v-bind="cardAttrs"
    class="c7-card"
    :class="{ 'is-collapsed': !innerExpanded }"
  >
    <!-- header slot 完全自定义时直接渲染 -->
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>

    <!-- 内置 header -->
    <template v-else #header>
      <div class="c7-card__header">
        <!-- 左侧：色块 + 标题 -->
        <div class="c7-card__title">
          <span v-if="showColorBlock || isShowColorBlock" class="c7-card__color-block" :style="{ background: colorBlockColor }" />
          <span :class="[`is-${textSize}`, { 'is-bold': isBold }]" class="c7-card__label">{{ label }}</span>
        </div>

        <!-- 右侧：extra slot + 折叠控制 -->
        <div class="c7-card__actions">
          <!-- extra 操作区 -->
          <slot name="extra" />

          <!-- 折叠触发器 -->
          <template v-if="collapsible">
            <!-- 自定义 toggle slot -->
            <slot v-if="$slots.toggle" name="toggle" :expanded="innerExpanded" :toggle="toggle" />
            <!-- 默认折叠按钮 -->
            <span v-else class="c7-card__toggle" @click="toggle">
              <el-icon class="c7-card__toggle-icon" :class="{ 'is-expanded': innerExpanded }">
                <ArrowDown />
              </el-icon>
              <span class="c7-card__toggle-text">{{ innerExpanded ? collapseText : expandText }}</span>
            </span>
          </template>
        </div>
      </div>
    </template>

    <!-- 内容区：fade 过渡动画 -->
    <transition name="c7-card-fade">
      <div v-show="innerExpanded" class="c7-card__body">
        <slot />
      </div>
    </transition>
  </el-card>
</template>

<script setup>
import { ref, watch, computed, useAttrs } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'

defineOptions({ name: 'C7Card', inheritAttrs: false })

const props = defineProps({
  // ── 标题 ──
  label: {
    type: String,
    default: ''
  },
  textSize: {
    type: String,
    default: 'h2',
    validator: (v) => ['h1', 'h2', 'h3', 'h4', 'h5'].includes(v)
  },
  isBold: {
    type: Boolean,
    default: true
  },
  // ── 色块 ──
  showColorBlock: {
    type: Boolean,
    default: false
  },
  /** @deprecated 请使用 showColorBlock */
  isShowColorBlock: {
    type: Boolean,
    default: false
  },
  colorBlockColor: {
    type: String,
    default: '#409eff'
  },
  // ── 折叠 ──
  collapsible: {
    type: Boolean,
    default: true
  },
  defaultExpanded: {
    type: Boolean,
    default: true
  },
  /** v-model:expanded 外部双向绑定 */
  modelValue: {
    type: Boolean,
    default: undefined
  },
  expandText: {
    type: String,
    default: '展开'
  },
  collapseText: {
    type: String,
    default: '收起'
  },
  // ── el-card 透传 ──
  shadow: {
    type: String,
    default: 'never',
    validator: (v) => ['always', 'hover', 'never'].includes(v)
  }
})

const emit = defineEmits([
  'update:modelValue',
  'change'
])

const attrs = useAttrs()

// el-card 透传 attrs（过滤 class/style 外的内容）
const cardAttrs = computed(() => ({
  shadow: props.shadow,
  ...attrs
}))

// ── 展开状态：优先使用外部 v-model，否则用内部状态 ──
const _inner = ref(props.modelValue !== undefined ? props.modelValue : props.defaultExpanded)

const innerExpanded = computed({
  get() {
    return props.modelValue !== undefined ? props.modelValue : _inner.value
  },
  set(val) {
    _inner.value = val
    emit('update:modelValue', val)
    emit('change', val)
  }
})

// 同步外部 v-model 变更
watch(
  () => props.modelValue,
  (val) => {
    if (val !== undefined) {
      _inner.value = val
    }
  }
)

// ── 暴露方法 ──
function toggle() {
  innerExpanded.value = !innerExpanded.value
}
function expand() {
  innerExpanded.value = true
}
function collapse() {
  innerExpanded.value = false
}

defineExpose({ expanded: innerExpanded, toggle, expand, collapse })
</script>

<style scoped lang="scss">
// ── 字号映射 ──
$text-sizes: (
  h1: 22px,
  h2: 18px,
  h3: 16px,
  h4: 14px,
  h5: 12px,
);

.c7-card {
  // ── header ──
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 32px;
  }

  &__title {
    display: flex;
    align-items: center;
    gap: 0;
  }

  // ── 色块：相对单位，跟随字号 ──
  &__color-block {
    display: inline-block;
    width: 4px;
    height: 1em;
    border-radius: 2px;
    margin-right: 8px;
    vertical-align: middle;
    flex-shrink: 0;
  }

  // ── 标题字号 ──
  &__label {
    vertical-align: middle;
    line-height: 1.4;
    color: #303133;

    @each $size, $px in $text-sizes {
      &.is-#{$size} {
        font-size: $px;
      }
    }

    &.is-bold {
      font-weight: 600;
    }
  }

  // ── 右侧操作区 ──
  &__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  // ── 折叠按钮 ──
  &__toggle {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    color: #409eff;
    font-size: 13px;
    user-select: none;
    transition: color 0.2s;

    &:hover {
      color: #66b1ff;
    }
  }

  &__toggle-icon {
    transition: transform 0.3s ease;
    transform: rotate(-90deg);

    &.is-expanded {
      transform: rotate(0deg);
    }
  }

  &__toggle-text {
    font-size: 13px;
  }

  // ── 内容区 ──
  &__body {
    overflow: hidden;
  }
}

// ── fade 过渡动画 ──
.c7-card-fade-enter-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.c7-card-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.c7-card-fade-enter-from {
  opacity: 0;
  transform: translateY(-6px);
}
.c7-card-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
