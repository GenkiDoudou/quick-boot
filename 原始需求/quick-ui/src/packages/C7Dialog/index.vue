<template>
  <!-- dialog 模式 -->
  <el-dialog
    v-if="mode === 'dialog'"
    v-bind="mergedDialogProps"
    :model-value="innerVisible"
    :title="title"
    :width="width"
    append-to-body
    destroy-on-close
    @update:model-value="handleVisibleChange"
    @open="emit('open')"
    @close="handleClose"
  >
    <!-- 自定义标题 -->
    <template v-if="$slots.title" #header>
      <slot name="title" />
    </template>

    <!-- 内容区 -->
    <slot />

    <!-- footer 区 -->
    <template v-if="footer" #footer>
      <slot name="footer">
        <div class="c7-dialog__footer">
          <!-- 左侧 extra -->
          <div class="c7-dialog__footer-extra">
            <slot name="extra" />
          </div>
          <!-- 右侧按钮 -->
          <div class="c7-dialog__footer-actions">
            <el-button @click="handleCancel">{{ cancelText }}</el-button>
            <el-button
              type="primary"
              :loading="confirmLoading || internalLoading"
              @click="handleConfirm"
            >
              {{ confirmText }}
            </el-button>
          </div>
        </div>
      </slot>
    </template>
  </el-dialog>

  <!-- drawer 模式 -->
  <el-drawer
    v-else
    v-bind="mergedDrawerProps"
    :model-value="innerVisible"
    :title="title"
    :size="size"
    direction="rtl"
    append-to-body
    destroy-on-close
    @update:model-value="handleVisibleChange"
    @open="emit('open')"
    @close="handleClose"
  >
    <!-- 自定义标题 -->
    <template v-if="$slots.title" #header>
      <slot name="title" />
    </template>

    <!-- 内容区 -->
    <slot />

    <!-- footer 区 -->
    <template v-if="footer" #footer>
      <slot name="footer">
        <div class="c7-dialog__footer">
          <div class="c7-dialog__footer-extra">
            <slot name="extra" />
          </div>
          <div class="c7-dialog__footer-actions">
            <el-button @click="handleCancel">{{ cancelText }}</el-button>
            <el-button
              type="primary"
              :loading="confirmLoading || internalLoading"
              @click="handleConfirm"
            >
              {{ confirmText }}
            </el-button>
          </div>
        </div>
      </slot>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'

defineOptions({ name: 'C7Dialog' })

const props = defineProps({
  // ── 显隐控制 ──
  /** v-model 绑定（推荐） */
  modelValue: {
    type: Boolean,
    default: undefined
  },
  /** 向后兼容：v-model:visible */
  visible: {
    type: Boolean,
    default: undefined
  },
  // ── 内容 ──
  /** 弹窗/抽屉标题 */
  title: {
    type: String,
    default: ''
  },
  /** 底层组件模式：dialog | drawer */
  mode: {
    type: String,
    default: 'dialog',
    validator: (v) => ['dialog', 'drawer'].includes(v)
  },
  /** 是否显示默认 footer */
  footer: {
    type: Boolean,
    default: true
  },
  // ── 按钮文字 ──
  confirmText: {
    type: String,
    default: '确 定'
  },
  cancelText: {
    type: String,
    default: '取 消'
  },
  // ── loading ──
  /** 外部控制确认按钮 loading */
  confirmLoading: {
    type: Boolean,
    default: false
  },
  /**
   * 异步确认函数，传入后点击「确定」自动管理 loading 并在成功后关闭弹窗
   * () => Promise<any> | any
   */
  onConfirm: {
    type: Function,
    default: null
  },
  // ── 尺寸（直接 prop，优先级高于 modalProps） ──
  /** dialog 模式宽度，默认 50% */
  width: {
    type: [String, Number],
    default: '50%'
  },
  /** drawer 模式尺寸，默认 500px */
  size: {
    type: [String, Number],
    default: '500px'
  },
  /** 透传给底层 el-dialog / el-drawer 的额外属性 */
  modalProps: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits([
  'update:modelValue',
  'update:visible',
  'open',
  'close',
  'confirm',
  'cancel',
  'submit'   // 向后兼容
])

// ── 内部 loading（onConfirm 异步自动管理）──
const internalLoading = ref(false)

// ── 内部显隐状态：优先 modelValue，其次 visible ──
const innerVisible = computed(() => {
  if (props.modelValue !== undefined) return props.modelValue
  if (props.visible !== undefined) return props.visible
  return false
})

// ── 合并 modalProps（用户透传属性优先级最高）──
const mergedDialogProps = computed(() => ({
  ...props.modalProps
}))

const mergedDrawerProps = computed(() => ({
  ...props.modalProps
}))

// ── 关闭弹窗（同时更新两种 v-model）──
function closeDialog() {
  emit('update:modelValue', false)
  emit('update:visible', false)
}

// ── 底层组件 update:modelValue 回调 ──
function handleVisibleChange(val) {
  if (!val) closeDialog()
}

// ── 关闭事件 ──
function handleClose() {
  closeDialog()
  emit('close')
}

// ── 取消按钮 ──
function handleCancel() {
  closeDialog()
  emit('cancel')
}

// ── 确认按钮 ──
async function handleConfirm() {
  // 有 onConfirm 异步函数：自动管理 loading，成功后关闭
  if (props.onConfirm) {
    internalLoading.value = true
    try {
      await props.onConfirm()
      closeDialog()
    } catch {
      // 失败不关闭，由业务自行处理错误提示
    } finally {
      internalLoading.value = false
    }
    return
  }
  // 无 onConfirm：仅触发事件，由父组件控制关闭时机
  emit('submit')   // 向后兼容
  emit('confirm')
}

// ── 路由切换时自动关闭，防止弹窗残留白屏 ──
onUnmounted(() => {
  if (innerVisible.value) {
    closeDialog()
  }
})
</script>

<style scoped>
.c7-dialog__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.c7-dialog__footer-extra {
  display: flex;
  align-items: center;
  gap: 8px;
}

.c7-dialog__footer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
