<template>
  <!-- 按钮模式 -->
  <el-button
    v-if="effectiveMode === 'button'"
    v-bind="$attrs"
    :type="buttonType"
    :size="buttonSize"
    :plain="buttonPlain"
    :link="buttonLink"
    :circle="buttonCircle"
    :disabled="disabled"
    @click="handleCopy"
  >
    <el-icon v-if="!buttonCircle"><DocumentCopy /></el-icon>
    <span v-if="!buttonCircle">{{ buttonText }}</span>
    <el-icon v-else><DocumentCopy /></el-icon>
  </el-button>

  <!-- 图标模式 -->
  <span
    v-else-if="effectiveMode === 'icon'"
    v-bind="$attrs"
    class="c7-copy__icon"
    :class="{ 'c7-copy--disabled': disabled, 'c7-copy--inline': inline }"
    :style="iconStyle"
    :title="displayText || safeCopyText"
    @click="handleCopy"
  >
    <el-icon :size="iconSize" :color="iconColor"><DocumentCopy /></el-icon>
    <span v-if="showText" class="c7-copy__text">{{ displayText || safeCopyText }}</span>
  </span>

  <!-- 文本模式 -->
  <span
    v-else-if="effectiveMode === 'text'"
    v-bind="$attrs"
    class="c7-copy__text-mode"
    :class="{ 'c7-copy--disabled': disabled, 'c7-copy--inline': inline }"
    @click="handleCopy"
  >
    {{ displayText || safeCopyText }}
  </span>

  <!-- 可点击插槽模式（clickable / none） -->
  <span
    v-else
    v-bind="$attrs"
    class="c7-copy__clickable"
    :class="{ 'c7-copy--disabled': disabled, 'c7-copy--inline': inline }"
    @click="handleCopy"
  >
    <slot>{{ displayText || safeCopyText }}</slot>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'

defineOptions({ name: 'C7Copy', inheritAttrs: false })

const props = defineProps({
  // ── 核心 ──
  /** 要复制的内容，支持 null/undefined（空值安全） */
  text: {
    type: [String, Number],
    default: ''
  },
  /**
   * 动态计算实际复制内容
   * (text: string) => string | Promise<string>
   */
  getCopyText: {
    type: Function,
    default: null
  },
  // ── 展示 ──
  /**
   * 展示模式
   * 'button'    → el-button
   * 'icon'      → 图标（默认）
   * 'text'      → 文字链
   * 'clickable' → 自定义插槽可点击区域
   * 'none'      → 同 clickable（向后兼容）
   */
  mode: {
    type: String,
    default: 'icon',
    validator: (v) => ['button', 'icon', 'text', 'clickable', 'none'].includes(v)
  },
  /** 是否行内显示 */
  inline: {
    type: Boolean,
    default: true
  },
  /** 图标模式下是否展示文字 */
  showText: {
    type: Boolean,
    default: false
  },
  /** 展示用文本（不影响实际复制内容），不传则显示 text 本身 */
  displayText: {
    type: String,
    default: ''
  },
  // ── 按钮模式专用 ──
  buttonType: {
    type: String,
    default: 'primary'
  },
  buttonSize: {
    type: String,
    default: 'default',
    validator: (v) => ['large', 'default', 'small'].includes(v)
  },
  buttonText: {
    type: String,
    default: '复制'
  },
  buttonPlain: {
    type: Boolean,
    default: true
  },
  buttonLink: {
    type: Boolean,
    default: false
  },
  buttonCircle: {
    type: Boolean,
    default: false
  },
  // ── 图标模式专用 ──
  iconSize: {
    type: [Number, String],
    default: 14
  },
  iconColor: {
    type: String,
    default: '#409eff'
  },
  // ── 状态 ──
  disabled: {
    type: Boolean,
    default: false
  },
  // ── 提示 ──
  /** 是否显示复制结果提示 */
  showMessage: {
    type: Boolean,
    default: true
  },
  successMessage: {
    type: String,
    default: '复制成功'
  },
  errorMessage: {
    type: String,
    default: '复制失败'
  },
  /**
   * 自定义通知方式，替代内置 ElMessage
   * (type: 'success' | 'error', message: string) => void
   */
  notify: {
    type: Function,
    default: null
  },
  // ── 钩子 ──
  /** 复制前钩子，返回 false 或 Promise<false> 可阻止复制 */
  beforeCopy: {
    type: Function,
    default: null
  },
  /** 复制成功后钩子 */
  afterCopy: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['copy', 'success', 'error'])

// ── 空值安全处理：null/undefined 转空字符串 ──
const safeCopyText = computed(() => {
  if (props.text === null || props.text === undefined) return ''
  return String(props.text)
})

// ── mode 兼容：'none' 等同于 'clickable' ──
const effectiveMode = computed(() => {
  return props.mode === 'none' ? 'clickable' : props.mode
})

// ── 图标模式样式 ──
const iconStyle = computed(() => ({
  cursor: props.disabled ? 'not-allowed' : 'pointer',
  opacity: props.disabled ? 0.5 : 1,
  display: props.inline ? 'inline-flex' : 'flex',
  alignItems: 'center',
  gap: '4px'
}))

// ── 触发通知 ──
function triggerNotify(type, message) {
  if (!props.showMessage) return
  if (props.notify) {
    props.notify(type, message)
    return
  }
  // 默认使用 ElMessage
  if (type === 'success') {
    ElMessage.success(message)
  } else {
    ElMessage.error(message)
  }
}

// ── 复制到剪贴板（Clipboard API + execCommand 降级）──
async function copyToClipboard(text) {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    await navigator.clipboard.writeText(text)
    return
  }
  // 降级方案：document.execCommand
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  textarea.style.pointerEvents = 'none'
  document.body.appendChild(textarea)
  textarea.select()
  const success = document.execCommand('copy')
  document.body.removeChild(textarea)
  if (!success) throw new Error('execCommand copy failed')
}

// ── 核心复制流程 ──
async function handleCopy() {
  if (props.disabled) return

  // 执行 beforeCopy 钩子，返回 false 则阻止
  if (props.beforeCopy) {
    const allow = await props.beforeCopy()
    if (allow === false) return
  }

  // 计算实际复制内容
  let copyText = safeCopyText.value
  if (props.getCopyText) {
    try {
      copyText = await props.getCopyText(safeCopyText.value)
    } catch (err) {
      emit('error', err)
      triggerNotify('error', props.errorMessage)
      return
    }
  }

  // 执行复制
  try {
    await copyToClipboard(copyText)
    emit('copy', copyText)
    emit('success', copyText)
    triggerNotify('success', props.successMessage)
    if (props.afterCopy) {
      props.afterCopy(copyText)
    }
  } catch (err) {
    emit('error', err)
    triggerNotify('error', props.errorMessage)
  }
}
</script>

<style scoped>
.c7-copy__icon {
  cursor: pointer;
  vertical-align: middle;
  user-select: none;
}

.c7-copy__text-mode {
  cursor: pointer;
  color: #409eff;
  user-select: none;
  transition: opacity 0.2s;
}

.c7-copy__text-mode:hover {
  opacity: 0.75;
}

.c7-copy__clickable {
  cursor: pointer;
  user-select: none;
  display: inline-block;
}

.c7-copy__text {
  font-size: 12px;
  color: #409eff;
}

.c7-copy--disabled {
  cursor: not-allowed !important;
  opacity: 0.5;
  pointer-events: none;
}

.c7-copy--inline {
  display: inline-flex;
}
</style>
