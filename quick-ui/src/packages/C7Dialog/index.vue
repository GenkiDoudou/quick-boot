<template>
  <el-dialog
      v-if="mode === 'dialog'"
      class="c7-dialog"
      :class="attrs.class"
      :style="attrs.style"
      :model-value="isOpen"
      v-bind="dialogShellAttrs"
      v-on="forwardedShellListeners"
      @update:model-value="onShellModelUpdate"
  >
    <template v-if="slots.title" #header>
      <slot name="title"/>
    </template>
    <slot/>
    <template v-if="footerSectionVisible" #footer>
      <slot v-if="slots.footer" name="footer"/>
      <div v-else class="c7-dialog__footer-default">
        <div class="c7-dialog__footer-extra">
          <slot name="extra"/>
        </div>
        <div class="c7-dialog__footer-actions">
          <el-button @click="onCancelClick">{{ cancelText }}</el-button>
          <el-button
              type="primary"
              :loading="effectiveConfirmLoading"
              @click="onConfirmBtnClick"
          >
            {{ confirmText }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
  <el-drawer
      v-else
      class="c7-dialog c7-dialog--drawer"
      :class="attrs.class"
      :style="attrs.style"
      :model-value="isOpen"
      v-bind="drawerShellAttrs"
      v-on="forwardedShellListeners"
      @update:model-value="onShellModelUpdate"
  >
    <template v-if="slots.title" #header>
      <slot name="title"/>
    </template>
    <slot/>
    <template v-if="footerSectionVisible" #footer>
      <slot v-if="slots.footer" name="footer"/>
      <div v-else class="c7-dialog__footer-default">
        <div class="c7-dialog__footer-extra">
          <slot name="extra"/>
        </div>
        <div class="c7-dialog__footer-actions">
          <el-button @click="onCancelClick">{{ cancelText }}</el-button>
          <el-button
              type="primary"
              :loading="effectiveConfirmLoading"
              @click="onConfirmBtnClick"
          >
            {{ confirmText }}
          </el-button>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import {computed, onBeforeUnmount, ref, useAttrs, useSlots, watch} from 'vue'
import {resolveDialogOpen} from '../support/c7DialogSupport.js'

defineOptions({name: 'C7Dialog', inheritAttrs: false})

/**
 * C7 弹窗 / 抽屉：在 **`ElDialog` / `ElDrawer`** 上统一 **footer、异步确定、双 v-model** 与 **`modalProps` 透传**。
 *
 * **显隐**：以 **`modelValue`** 为主；未传 **`modelValue`** 时回退 **`visible`**。关闭时 **同时** **`emit('update:modelValue', false)`** 与 **`emit('update:visible', false)`**。
 * 若二者 **同时显式传入** 且 **布尔不一致**，开发环境 **至多每个打开周期** **`console.warn` 一次**（以 **`modelValue`** 为准）。
 *
 * **确定**：传入 **`onConfirm`** 时点击确定会 **await** 其结果；**resolve** 后自动关闭，**reject/throw** 不关闭、**不**内置 **`ElMessage`**。
 * 确定钮 **`loading`**：**`confirmLoading !== undefined`** 时 **以外部为准**（可覆盖内部 **`onConfirm` pending**）；否则使用内部 loading。
 * 未传 **`onConfirm`** 时点击确定 **依次** **`emit('confirm')`、`emit('submit')`**，**不**自动关闭（由父级处理）。
 *
 * **自定义标题**：具名插槽 **`#title`** 映射至 EP 的 **`#header`**（Dialog / Drawer 一致）。
 *
 * **透传**：**`v-bind` 到根（除 `class`/`style` 外）** 与 **`modalProps`** 合并进壳层；与内部默认 **同名键** 时 **以后合并者为准**（顺序：**内部默认 → `modalProps` → 根上非监听属性**）。
 *
 * **不建议** 在根上覆盖 **`model-value` / `v-model`**，否则可能与双 **`emit`** 行为冲突。
 *
 * @emits update:modelValue
 * @emits update:visible
 * @emits open
 * @emits opened
 * @emits close
 * @emits closed
 * @emits cancel
 * @emits confirm
 * @emits submit
 */
const props = defineProps({
  /** 显隐主绑定；未传时回退 `visible` */
  modelValue: {type: Boolean, default: undefined},
  /** 兼容 `v-model:visible` */
  visible: {type: Boolean, default: undefined},
  /** `dialog`：ElDialog；`drawer`：ElDrawer */
  mode: {
    type: String,
    default: 'dialog',
    validator: (v) => ['dialog', 'drawer'].includes(v),
  },
  title: {type: String, default: ''},
  /** 为 `true` 且无 `#footer` 时渲染默认「取消 / 确定」；有 `#footer` 时始终展示插槽内容 */
  footer: {type: Boolean, default: true},
  cancelText: {type: String, default: '取消'},
  confirmText: {type: String, default: '确定'},
  /**
   * 若不为 `undefined`，确定钮 **`:loading`** 仅认该值（覆盖内部 **`onConfirm` pending**）。
   */
  confirmLoading: {type: Boolean, default: undefined},
  /** 异步确定；成功 resolve 后自动关闭 */
  onConfirm: {type: Function, default: null},
  /** Dialog 宽度（Drawer 请用 `size` 或 `modalProps`） */
  width: {type: [String, Number], default: '520px'},
  /** Drawer 尺寸，对齐 `ElDrawer.size` */
  size: {type: [String, Number], default: undefined},
  /** 合并进壳层；键冲突时优先于内部默认、弱于根组件上的非监听透传 */
  modalProps: {type: Object, default: () => ({})},
})

const emit = defineEmits([
  'update:modelValue',
  'update:visible',
  'open',
  'opened',
  'close',
  'closed',
  'cancel',
  'confirm',
  'submit',
])

const attrs = useAttrs()
const slots = useSlots()

const internalConfirmLoading = ref(false)
/** 每个「打开周期」至多提示一次双源不一致 */
const mismatchWarnedForCycle = ref(false)

const isOpen = computed(() => resolveDialogOpen(props.modelValue, props.visible))

const footerSectionVisible = computed(() => Boolean(slots.footer) || props.footer)

watch(
    () => [props.modelValue, props.visible, isOpen.value],
    () => {
      if (!isOpen.value) {
        mismatchWarnedForCycle.value = false
        return
      }
      if (
          props.modelValue !== undefined &&
          props.visible !== undefined &&
          Boolean(props.modelValue) !== Boolean(props.visible)
      ) {
        if (import.meta.env.DEV && !mismatchWarnedForCycle.value) {
          mismatchWarnedForCycle.value = true
          console.warn(
              '[C7Dialog] modelValue 与 visible 同时为显式传入且不一致，以 modelValue 为准驱动显隐。',
          )
        }
      }
    },
    {immediate: true},
)

/** 根上非 class/style、非监听的透传（监听器单独合并） */
const inheritedBindOnly = computed(() => {
  const out = {}
  for (const key of Object.keys(attrs)) {
    if (key === 'class' || key === 'style') {
      continue
    }
    if (key.startsWith('on')) {
      continue
    }
    out[key] = attrs[key]
  }
  return out
})

const forwardedShellListeners = computed(() => {
  const out = {}
  for (const key of Object.keys(attrs)) {
    if (!key.startsWith('on')) {
      continue
    }
    if (key === 'onUpdate:modelValue' || key === 'onUpdate:visible') {
      continue
    }
    out[key] = attrs[key]
  }
  return out
})

const dialogShellAttrs = computed(() => {
  const base = {
    title: slots.title ? undefined : props.title,
    width: props.width,
    appendToBody: true,
    ...props.modalProps,
    ...inheritedBindOnly.value,
  }
  return base
})

const drawerShellAttrs = computed(() => {
  const base = {
    /** 有 `#title` 插槽时交给插槽，否则走 `ElDrawer` 的 `title` 文案 */
    title: slots.title ? undefined : props.title,
    size: props.size,
    ...props.modalProps,
    ...inheritedBindOnly.value,
  }
  return base
})

const effectiveConfirmLoading = computed(() =>
  props.confirmLoading !== undefined
      ? props.confirmLoading
      : internalConfirmLoading.value,
)

function emitClose() {
  emit('update:modelValue', false)
  emit('update:visible', false)
}

function onShellModelUpdate(v) {
  if (!v) {
    emitClose()
  }
}

function onCancelClick() {
  emit('cancel')
  emitClose()
}

async function onConfirmBtnClick() {
  if (typeof props.onConfirm === 'function') {
    internalConfirmLoading.value = true
    try {
      await props.onConfirm()
      emitClose()
    } catch {
      // 失败不关窗；错误提示由业务处理
    } finally {
      internalConfirmLoading.value = false
    }
    return
  }
  emit('confirm')
  emit('submit')
}

onBeforeUnmount(() => {
  if (isOpen.value) {
    emitClose()
  }
})
</script>

<style scoped lang="scss">
.c7-dialog__footer-default {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.c7-dialog__footer-extra {
  flex: 1;
  min-width: 0;
}

.c7-dialog__footer-actions {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
}

</style>
