<template>
  <el-card
      class="c7-card"
      v-bind="cardAttrs"
      :class="attrs.class"
      :style="attrs.style"
      :shadow="shadow"
  >
    <template v-if="hasHeaderSlot" #header>
      <slot name="header"/>
    </template>
    <template v-else #header>
      <div class="c7-card__head">
        <span
            v-if="colorBlockVisible"
            class="c7-card__color-block"
            :style="{ backgroundColor: colorBlockColor }"
            aria-hidden="true"
        />
        <el-text
            :tag="titleTag"
            class="c7-card__title"
            :class="{ 'c7-card__title--bold': isBold }"
        >
          {{ label }}
        </el-text>
        <div class="c7-card__head-right">
          <slot name="extra"/>
          <template v-if="collapsible">
            <slot
                name="toggle"
                :expanded="effectiveExpanded"
                :toggle="onToggleClick"
                :content-id="contentId"
            >
              <button
                  type="button"
                  class="c7-card__fold-btn"
                  :aria-expanded="effectiveExpanded"
                  :aria-controls="contentId"
                  @click="onToggleClick"
              >
                {{ effectiveExpanded ? collapseText : expandText }}
              </button>
            </slot>
          </template>
        </div>
      </div>
    </template>

    <transition name="c7-card-fade">
      <div
          v-show="bodyVisible"
          :id="contentId"
          class="c7-card__body"
          role="region"
          :aria-hidden="collapsible && !effectiveExpanded ? true : undefined"
      >
        <slot/>
      </div>
    </transition>
  </el-card>
</template>

<script setup>
import {computed, ref, useAttrs, useId, useSlots} from 'vue'

defineOptions({name: 'C7Card', inheritAttrs: false})

/**
 * C7 业务卡片：基于 **`ElCard`** 统一 **标题栏**（可选色块 + **`ElText` 语义标签 h1~h5** + 加粗）、**`extra` / 折叠** 与 **内容区 fade**。
 *
 * **`#header`**：若提供，则 **整块 `ElCard` 头部** 仅渲染该插槽，**不**再出现默认色块、标题、`extra`、内置折叠按钮（折叠需在插槽内自行处理）。
 *
 * **受控 / 非受控**：传入 **`modelValue`**（`undefined` 以外）时为受控，展开态以 **`modelValue`** 为准；否则以内部状态为准，初值来自 **`defaultExpanded`（默认 `true`）**。
 * **`toggle` / `expand` / `collapse`**：在受控模式下通过 **`emit('update:modelValue', …)`** 请求父组件更新；并始终 **`emit('change', expanded)`**。
 *
 * **色块显隐**：若 props 中 **传入** **`showColorBlock`**（含 **`false`**），**仅认 `showColorBlock`**；**未传 `showColorBlock`** 时回退 **`isShowColorBlock`**（兼容别名）。
 *
 * @emits update:modelValue
 * @emits change(expanded:boolean)
 */
const props = defineProps({
  /** 标题文案（默认头部） */
  label: {type: String, default: ''},
  /** 标题层级，对应 **`ElText` 的 `tag`**，取 `h1`~`h5` */
  textSize: {
    type: String,
    default: 'h4',
    validator: (v) => ['h1', 'h2', 'h3', 'h4', 'h5'].includes(v),
  },
  /** 标题是否加粗 */
  isBold: {type: Boolean, default: false},
  /**
   * 是否显示左侧色块（主开关）。
   * 若显式传入（含 `false`），**仅**以此为准。
   */
  showColorBlock: {type: Boolean, default: undefined},
  /** 兼容别名：仅在 **未传入 `showColorBlock`** 时生效 */
  isShowColorBlock: {type: Boolean, default: false},
  /** 色块背景色，默认 Element 主色变量 */
  colorBlockColor: {type: String, default: 'var(--el-color-primary)'},
  /** 是否可折叠内容区（总开关）；为 `false` 时不渲染 `#toggle` 与内置折叠按钮 */
  collapsible: {type: Boolean, default: false},
  /** 非受控初始是否展开 */
  defaultExpanded: {type: Boolean, default: true},
  /** 受控展开态；`undefined` 表示非受控 */
  modelValue: {type: Boolean, default: undefined},
  /** 折叠态下默认按钮文案 */
  expandText: {type: String, default: '展开'},
  /** 展开态下默认按钮文案 */
  collapseText: {type: String, default: '收起'},
  /** 透传 `ElCard` 的 `shadow` */
  shadow: {type: String, default: undefined},
})

const emit = defineEmits(['update:modelValue', 'change'])

const attrs = useAttrs()
const slots = useSlots()

const hasHeaderSlot = computed(() => !!slots.header)

/** 供 **`aria-controls`** 与 **`#toggle` 插槽的 `contentId`** 使用（`useId` 保证同实例稳定） */
const contentId = useId()

const cardAttrs = computed(() => {
  const {class: _c, style: _s, ...rest} = attrs
  return rest
})

const titleTag = computed(() => {
  const valid = ['h1', 'h2', 'h3', 'h4', 'h5']
  return valid.includes(props.textSize) ? props.textSize : 'h4'
})

const colorBlockVisible = computed(() => {
  if (props.showColorBlock !== undefined) {
    return props.showColorBlock
  }
  return props.isShowColorBlock === true
})

const isControlled = computed(() => props.modelValue !== undefined)

const innerExpanded = ref(props.defaultExpanded !== false)

const effectiveExpanded = computed(() =>
    isControlled.value ? !!props.modelValue : innerExpanded.value,
)

const bodyVisible = computed(() => !props.collapsible || effectiveExpanded.value)

function setExpanded(next) {
  if (!props.collapsible) {
    return
  }
  emit('change', next)
  emit('update:modelValue', next)
  if (!isControlled.value) {
    innerExpanded.value = next
  }
}

function onToggleClick() {
  if (!props.collapsible) {
    return
  }
  setExpanded(!effectiveExpanded.value)
}

function expand() {
  setExpanded(true)
}

function collapse() {
  setExpanded(false)
}

function toggle() {
  if (!props.collapsible) {
    return
  }
  setExpanded(!effectiveExpanded.value)
}

defineExpose({
  toggle,
  expand,
  collapse,
})
</script>

<style scoped lang="scss">
.c7-card {
  :deep(.el-card__header) {
    padding: 12px 16px;
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

.c7-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 24px;
}

.c7-card__color-block {
  flex-shrink: 0;
  width: 4px;
  min-height: 1em;
  border-radius: 2px;
}

.c7-card__title {
  flex: 1;
  min-width: 0;
  margin: 0;
  color: var(--el-text-color-primary);
}

.c7-card__title--bold {
  font-weight: var(--el-font-weight-primary, 600);
}

.c7-card__head-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: auto;
}

.c7-card__fold-btn {
  padding: 0 4px;
  border: none;
  background: transparent;
  color: var(--el-color-primary);
  font: inherit;
  cursor: pointer;
  line-height: 1.5;
}

.c7-card__fold-btn:hover {
  opacity: 0.85;
}

.c7-card__body {
  padding: 16px;
}

.c7-card-fade-enter-active,
.c7-card-fade-leave-active {
  transition: opacity 0.2s ease;
}

.c7-card-fade-enter-from,
.c7-card-fade-leave-to {
  opacity: 0;
}
</style>
